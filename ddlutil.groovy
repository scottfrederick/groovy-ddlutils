import org.apache.ddlutils.model.Database
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.PlatformFactory
import org.apache.commons.dbcp.BasicDataSource

def options = parseOptions args

def config = parseConfig options
def inputConfig = getConfig config, options.i
def outputConfig = getConfig config, options.o

def inputModel = getInputModel inputConfig
inputModel.tables.each { println "found table ${it.name}" }

def outputPlatform = getOutputPlatform outputConfig

writeOutput inputModel, outputPlatform, outputConfig

private def parseOptions(args) {
	def cli = new CliBuilder(usage: 'ddlutil -i inputConfig -o outputConfig [-c configFile]')
	cli.i(argName:"inputConfig", args:1, required:true, 'Input database configuration')
	cli.o(argName:"outputConfig", args:1, required:true, 'Output database configuration')
	cli.c(argName:"configFile", args:1, required:false, 'Configuration file (defaults to "configs.groovy")')

	def options = cli.parse(args)
	if (options == null) {
	  quit()
	}
	options
}

private def parseConfig(options) {
	def configFileName = options.c ?: 'configs.groovy'
	def configFile = new File(configFileName)
	if (!configFile.canRead()) {
		quit "Configuration file ${configFileName} was not found or cannot be read"
	}
	new ConfigSlurper().parse(configFile.toURL())
}

private def getConfig(config, configName) {
	def targetConfig = config[configName]

	if (!targetConfig) {
		quit "Configuration name ${configName} is not valid"
	}
	
	targetConfig
}

private def getInputModel(inputConfig) {
	def inputDatabase = getInputDatabase inputConfig
	def inputPlatform = createPlatform inputDatabase, inputConfig
	return inputPlatform.readModelFromDatabase(inputConfig.database)	
}

private def getInputDatabase(inputConfig) {
	inputConfig.with {
	  if (jdbc) {
			return createDataSource(jdbc)
	  }
	  else if (xmlFile) {
			return new DatabaseIO().read(xmlFile);
	  }
	  else {
			quit "Input configuration does not contain a 'jdbc' or 'xmlFile' specification"
		}
	}
}

private def getOutputPlatform(outputConfig) {
	def outputDatabase = getOutputDatabase outputConfig
	createPlatform(outputDatabase, outputConfig)
}

private def getOutputDatabase(outputConfig) {
	outputConfig.with {
	  if (jdbc) {
			return createDataSource(jdbc)
	  }
		else if (dialect) {
			return dialect
		}
	}
}

private def createDataSource(jdbc) {
	return new BasicDataSource(url: jdbc.url, driverClassName: jdbc.driverClassName, username: jdbc.username, password: jdbc.password)
}

private def createPlatform(database, config) {
	def outputPlatform = PlatformFactory.createNewPlatformInstance(database)	
	if (!outputPlatform) {
		quit "Could not create create database model for configuration $config"
	}
	outputPlatform
}

private def writeOutput(inputModel, outputPlatform, outputConfig) {
	if (outputConfig.sqlFile) {
		writeSqlOutput inputModel, outputPlatform, outputConfig.sqlFile 
	}
	else if (outputConfig.xmlFile) {
		writeXmlOutput inputModel, outputConfig.xmlFile
	}
}

private def writeSqlOutput(inputModel, outputPlatform, fileName) {
	def sqlBuilder = outputPlatform.sqlBuilder
	def writer = new FileWriter(fileName)
	sqlBuilder.writer = writer
	
	sqlBuilder.createTables(inputModel)
	
	writer.close()
}

private def writeXmlOutput(inputModel, fileName) {
	new DatabaseIO().write(inputModel, fileName);
}

private def quit(def message="") {
	if (message) println "ERROR: $message"
	System.exit(1)
}