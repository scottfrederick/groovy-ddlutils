/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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


/*
 * Parse command line arguments and return a map
 */
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

/*
 * Parse the config script
 */
private def parseConfig(options) {
	def configFileName = options.c ?: 'configs.groovy'
	def configFile = new File(configFileName)
	if (!configFile.canRead()) {
		quit "Configuration file ${configFileName} was not found or cannot be read"
	}
	new ConfigSlurper().parse(configFile.toURL())
}

/*
 * Get a named configuration from the config script
 */
private def getConfig(config, configName) {
	def targetConfig = config[configName]

	if (!targetConfig) {
		quit "Configuration name ${configName} is not valid"
	}
	
	targetConfig
}

/*
 * Create a DDLUtils model structure from a live database or XML file 
 */
private def getInputModel(inputConfig) {
	inputConfig.with {
	  if (jdbc) {
		  return readJdbcInput(jdbc, inputConfig)
	  }
	  else if (xmlFile) {
			return readXmlInput(xmlFile)
	  }
	  else {
			quit "Input configuration does not contain a 'jdbc' or 'xmlFile' specification"
		}
	}
}

/*
 * Read a database model from a live database
 */
private def readJdbcInput(jdbc, inputConfig) {
	def dataSource = createDataSource(jdbc)
	def inputPlatform = createPlatform dataSource, inputConfig
	inputPlatform.readModelFromDatabase(inputConfig.database)	
}

/*
 * Read a database model from an XML file
 */
private def readXmlInput(xmlFileName) {
	def xmlFile = new File(xmlFileName)
	if (!xmlFile.canRead()) {
		quit "Input file ${xmlFileName} was not found or cannot be read"
	}
	new DatabaseIO().read(xmlFileName)	
}

/*
 * Create a DDLUtils platform structure from for a live database or SQL dialect
 */
private def getOutputPlatform(outputConfig) {
	outputConfig.with {
	  if (jdbc) {
			createPlatform createDataSource(jdbc), outputConfig
	  }
		else if (dialect) {
			createPlatform dialect, outputConfig
		}
	}
}

/*
 * Write output schema to a live database, SQL file, or XML file
 */
private void writeOutput(inputModel, outputPlatform, outputConfig) {
	if (outputConfig.sqlFile) {
		writeSqlOutput inputModel, outputPlatform, outputConfig.sqlFile 
	}
	else if (outputConfig.xmlFile) {
		writeXmlOutput inputModel, outputConfig.xmlFile
	}
}

/*
 * Write output schema to a SQL file
 */
private void writeSqlOutput(inputModel, outputPlatform, fileName) {
	def sqlBuilder = outputPlatform.sqlBuilder
	def writer = new FileWriter(fileName)
	sqlBuilder.writer = writer
	
	sqlBuilder.createTables(inputModel)
	
	writer.close()
}

/*
 * Write output schema to an XML file
 */
private void writeXmlOutput(inputModel, fileName) {
	new DatabaseIO().write(inputModel, fileName);
}

/*
 * Write output schema to a live database
 */
private void writeJdbcOutput(inputModel, outputPlatform) {
	def params = [:]
	def dropTablesFirst = true
	outputPlatform.createTables(inputModel, params, dropTablesFirst, false)
}

/*
 * Create a JDBC DataSource 
 */
private def createDataSource(jdbc) {
	return new BasicDataSource(url: jdbc.url, driverClassName: jdbc.driverClassName, username: jdbc.username, password: jdbc.password)
}

/*
 * Create a DDLUtils platform data structure
 */
private def createPlatform(database, config) {
	try {
		def outputPlatform = PlatformFactory.createNewPlatformInstance(database)	
		if (!outputPlatform) {
			quit "Could not create create database model for configuration $config"
		}
		outputPlatform
	}
	catch (Exception e) {
		quit e.message
	}
}

/*
 * Print an error message and exit the script
 */
private def quit(def message="") {
	if (message) println "ERROR: $message"
	System.exit(1)
}