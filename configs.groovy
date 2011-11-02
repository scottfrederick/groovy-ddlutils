'sqlfire-shipping' {
  database = 'shipping'
  jdbc { 
    url = 'jdbc:sqlfire://localhost:1527/'
    driverClassName = 'com.vmware.sqlfire.jdbc.ClientDriver'
    username = 'shipping'
    password = 'shipping'
  }
}

pgsql {
  dialect = 'postgresql'
  sqlFile = 'schema-postgres.sql'	
}

mysql {
  dialect = 'mysql'
  sqlFile = 'schema-mysql.sql'	
}

sqlf {
  dialect = 'derby'
  sqlFile = 'schema-sqlfire.sql'	
}

xml {
  xmlFile = 'schema.xml'	
}