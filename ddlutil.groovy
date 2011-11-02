import org.apache.ddlutils.model.Database
import org.apache.ddlutils.PlatformFactory
import org.apache.commons.dbcp.BasicDataSource

def cli = new CliBuilder(usage: 'groovy schemagen -d database -o outputFile [-u user] [-p password] [-url jdbc:db://host:port] [-driver com.company.database.jdbc.Driver]')
cli.d(argName:"database", args:1, required:true, 'Database name')
cli.o(argName:"outputFile", args:1, required:true, 'Output file')
cli.u(argName:"user", args:1, required:false, 'Database user (defaults to database name)')
cli.p(argName:"password", args:1, required:false, 'Database password (defaults to database name)')
cli.url(argName:"url", args:1, required:false, 'JDBC URL')
cli.driver(argName:"driver class", args:1, required:false, 'JDBC driver class')

def options = cli.parse(args)
if (options == null) {
	System.exit(1)
}

def jdbcUrl = options.url ?: 'jdbc:sqlfire://localhost:1527/'
def jdbcDriverClass = options.driver ?: 'com.vmware.sqlfire.jdbc.ClientDriver'
def user = options.u ?: options.d
def password = options.p ?: options.d

def inputDataSource = new BasicDataSource(url: jdbcUrl, 
                                          driverClassName: jdbcDriverClass,
                                          username: user,
                                          password: password)

def outputDialect = 'postgresql'

def inputPlatform = PlatformFactory.createNewPlatformInstance(inputDataSource)
def outputPlatform = PlatformFactory.createNewPlatformInstance(outputDialect)

def inputDatabase = inputPlatform.readModelFromDatabase(options.d)

inputDatabase.tables.each { println "found table ${it.name}" }

def sqlBuilder = outputPlatform.sqlBuilder

def writer = new FileWriter(options.o)
sqlBuilder.writer = writer
sqlBuilder.createTables(inputDatabase)
writer.close()
