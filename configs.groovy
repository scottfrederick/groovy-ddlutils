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
// Sample output configurations for SQL dialects
// see http://db.apache.org/ddlutils/ for the full list of supported databases and dialects
//
// mysql {
//   dialect = 'MySQL'
//   sqlFile = 'schema-mysql.sql'	
// }

//
// Sample XML output configurations
// see http://db.apache.org/ddlutils/schema/ for details of the XML output format 
//
// xml {
//  xmlFile = 'schema.xml'	
// }
