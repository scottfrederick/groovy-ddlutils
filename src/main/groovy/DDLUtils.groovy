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

def defaultInputConfig = { configName ->
  if (configName == 'xml') {
    [ xmlFile: 'schema.xml' ]
  }
}

def defaultOutputConfig = { configName ->
  if (configName == 'xml') {
    [ xmlFile: 'schema.xml' ]
  }
  else if (PlatformFactory.isPlatformSupported(configName)) {
    [ dialect: configName, sqlFile: 'schema.sql' ]
  }
}

def (cli, options) = parseOptions(args)

def config = parseConfig options
displayUsageIfNecessary cli, options, config

def inputConfig = getConfig config, options.i, defaultInputConfig
def outputConfig = getConfig config, options.o, defaultOutputConfig

def inputModel = getInputModel inputConfig
showInputModelDetails(inputModel)

def outputPlatform = getOutputPlatform outputConfig

writeOutput inputModel, outputPlatform, outputConfig
println "Done."

/*
 * Parse command line arguments and return a map
 */
private def parseOptions(args) {
	def cli = new CliBuilder(usage: 'ddlutil -i inputConfig -o outputConfig [-c configFile] [-h]')
	cli.with {
  	i longOpt:'input', argName:"inputConfig", args:1, 'Input database configuration (required)'
  	o longOpt:'output', argName:"outputConfig", args:1, 'Output database configuration (required)'
  	c longOpt:'config', argName:"configFile", args:1, 'Configuration file (defaults to "configs.groovy")'
  	h longOpt:'help', argName:"help", args:0, 'Display help message and quit'
	}

	def options = cli.parse(args)
	if (options == null) {
	  quit()
	}
	[cli, options]
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

private void displayUsageIfNecessary(cli, options, config) {
  if (!options.i || !options.o || options.h) {
    cli.usage()
    println getValidConfigsMessage(config)
    quit()
  }
}

/*
 * Get a named configuration from the config script
 */
private def getConfig(config, configName, defaultConfig) {
	def targetConfig = config[configName]

	if (!targetConfig) {
	  targetConfig = defaultConfig.call configName
	}
	
	if (!targetConfig) {
	  quit "Configuration name '${configName}' is not valid" + getValidConfigsMessage(config)
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
  println "Reading from JDBC data source..."
	def dataSource = createDataSource(jdbc)
	def inputPlatform = createPlatform dataSource, inputConfig
	inputPlatform.readModelFromDatabase(inputConfig.database)	
}

/*
 * Read a database model from an XML file
 */
private def readXmlInput(xmlFileName) {
  println "Reading from XML file $xmlFileName..."
	def xmlFile = new File(xmlFileName)
	if (!xmlFile.canRead()) {
		quit "Input file ${xmlFileName} was not found or cannot be read"
	}
	new DatabaseIO().read(xmlFileName)	
}

/*
 * Show the tables found in the input model
 */
private def showInputModelDetails(inputModel) {
  if (inputModel.tableCount == 0) {
    quit "No tables found in input source"
  }
  println "Found ${inputModel.tableCount} tables: "
  inputModel.tables.each { println "  ${it.name}" }
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
 * Write the output schema to an SQL file, an XML file, or a live database 
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
 * Write the output schema to an SQL file
 */
private void writeSqlOutput(inputModel, outputPlatform, fileName) {
  println "Writing SQL output to $fileName..."
	def sqlBuilder = outputPlatform.sqlBuilder
	def writer = new FileWriter(fileName)
	sqlBuilder.writer = writer
	
	sqlBuilder.createTables(inputModel)
	
	writer.close()
}

/*
 * Write the output schema to an XML file
 */
private void writeXmlOutput(inputModel, fileName) {
  println "Writing XML output to $fileName..."
	new DatabaseIO().write(inputModel, fileName);
}

/*
 * Write the output schema to a live database
 * *** This option is a work-in-progress ***
 */
private void writeJdbcOutput(inputModel, outputPlatform) {
  println "Writing output to JDBC data source..."
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
 * Build a message containing all valid configurations and supported platforms
 */
private def getValidConfigsMessage(config) { """

Valid named configurations: ${config.keySet()}

Supported SQL schema dialects: ${PlatformFactory.supportedPlatforms.sort()}
"""
} 
 
/*
 * Print an error message and exit the script
 */
private def quit(def message="") {
	if (message) println "ERROR: $message"
	System.exit(1)
}