//
// Sample JDBC input configurations
//
'hsqldb-test' {
  database = 'test'
  jdbc { 
    url = 'jdbc:hsqldb:file:test'
    driverClassName = 'org.hsqldb.jdbcDriver'
    username = 'sa'
    password = ''
  }
}

'postgresql-test' {
  database = 'test'
  jdbc { 
    url = 'jdbc:postgresql://localhost:5432/test'
    driverClassName = 'org.postgresql.Driver'
    username = 'test'
    password = ''
  }
}

'mysql-test' {
  database = 'test'
  jdbc { 
    url = 'jdbc:mysql://localhost:3306/test'
    driverClassName = 'com.mysql.jdbc.Driver'
    username = 'test'
    password = 'test'
  }
}

'sqlfire-test' {
  database = 'test'
  jdbc { 
    url = 'jdbc:sqlfire://localhost:1527/'
    driverClassName = 'com.vmware.sqlfire.jdbc.ClientDriver'
    username = 'test'
    password = 'test'
  }
}

//
// SQL output configurations for dialects supported by DDLUtils
// see http://db.apache.org/ddlutils/ for the full list of supported databases and dialects
//
db2 {
  dialect = 'DB2'
  sqlFile = 'schema-db2.sql'	
}

db2v8 {
  dialect = 'DB2v8'
  sqlFile = 'schema-db2.sql'	
}

derby {
  dialect = 'Derby'
  sqlFile = 'schema-derby.sql'	
}

hsqldb {
  dialect = 'HsqlDb'
  sqlFile = 'schema-hsqldb.sql'	
}

mssql {
  dialect = 'MsSql'
  sqlFile = 'schema-mssql.sql'	
}

mysql {
  dialect = 'MySQL'
  sqlFile = 'schema-mysql.sql'	
}

mysql5 {
  dialect = 'MySQL5'
  sqlFile = 'schema-mysql.sql'	
}

oracle {
  dialect = 'Oracle'
  sqlFile = 'schema-oracle.sql'	
}

oracle9 {
  dialect = 'Oracle9'
  sqlFile = 'schema-oracle.sql'	
}

oracle10 {
  dialect = 'Oracle10'
  sqlFile = 'schema-oracle.sql'	
}

postgresql {
  dialect = 'PostgreSql'
  sqlFile = 'schema-postgres.sql'	
}

sqlfire {
  dialect = 'Derby'
  sqlFile = 'schema-sqlfire.sql'	
}

//
// XML output configurations
// see http://db.apache.org/ddlutils/schema/ for details of the XML output format 
//
xml {
  xmlFile = 'schema.xml'	
}
