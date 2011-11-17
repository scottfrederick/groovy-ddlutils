# Groovy DDLUtils

Groovy-DDLUtils is a Groovy script that uses the [Apache DDLUtils][] library to provide a simple way to transform database schema 
information from one database dialect to another. 

The Groovy-DDLUtils script can read database schema information from a live database (using the appropriate JDBC drivers) or from 
an XML file in the [Turbine XML format][]. The schema can then be written to a Turbine XML file or to an SQL script file in a 
specified SQL dialect.  

## Running Groovy-DDLUtils

### Running as a Java jar file

The simplest way to use Groovy-DDLUtils is to download the compiled executable jar from the [files download page][] on GitHub. 
With Java 1.6 or greater installed, you can run the utility using the `java` executable, like this:

    > java -jar groovy-ddlutils-<version>.jar <command line arguments>

In order to read or write from a live database, the appropriate JDBC drivers are also required. Groovy-DDLUtils does not include
any JDBC drivers. JDBC drivers can be used with the compiled executable by adding the `-classpath` argument to the Java command 
line.

### Running as a Groovy script

The Groovy-DDLUtils script can also be run using the Groovy interpreter directly. This might be a good option if you want to hack
on the script. Clone the project or download the [raw script][] from GitHub and run it with Groovy like this: 

    > groovy DDLUtils.groovy <command line arguments>

The script has been tested with Groovy 1.8.4, but should work with older versions of the interpreter. 

When running the script with the interpreter, the DDLUtils library and any JDBC drivers must be added to the classpath. This can be 
done by adding the `-classpath` argument to the groovy command line or by copying all the necessary jars to the `~/.groovy/lib` 
directory.

### Command line arguments

The format of the Groovy-DDLUtils command line is: 

    > groovy DDLUtils.groovy -i <input config name> -o <output config name> [-c <configs file name>]

where: 

* `-i <input database>` (required)

Names the input or source database. The value of the argument must be the name of a configuration in the configuration file (see
below), or `'xml'`. If the value is a named configuration, then that configuration will be used to read the input database schema. 
If the value is `'xml'`, the script will look for a file named `schema.xml` in the current working directory. 

* `-o <output database>` (required)

Names an output or target database. The value of the argument must be the name of a configuration in the configuration file (see 
below), the name of a database dialect supported by DDLUtils, or `'xml'`. If the value is a named configuration, then that 
configuration will be used to write the output database schema. If the value is the name of a DDLUtils supported dialect, a file 
named `schema.sql` in the selected SQL dialect will be written to the current working directory. If the value is `'xml'`, a file 
named `schema.xml` will be written to the current working directory.

* `-c <configs file path>` (optional)

Names the file containing the input and output configurations. If not specified, the default file path is `configs.groovy`
in the current working directory.

#### Examples:

    # read from the live database config 'mysql-test', write to 'schema.xml'
    > groovy DDLUtils.groovy -i mysql-test -o xml
     
    # read from the live database config 'mysql-test', write to PostgreSQL SQL file 'schema.sql'
    > groovy DDLUtils.groovy -i mysql-test -o postgresql 
    
    # read from 'schema.xml', write to PostgreSQL SQL file 'schema.sql'
    > groovy DDLUtils.groovy -i xml -o postgresql 
    
    # read configs from the named file
    > groovy DDLUtils.groovy -i mysql-test -o xml -c myConfig.groovy 

### Configurations

The configurations file contains named configurations that can be used to specify JDBC database connection parameters, XML input 
and output file names, and SQL dialects and output file names. The file is in the format of a Groovy configuration file. The
default `configs.groovy` configurations file contains several example configurations, and can be modified as necessary.

The outermost scope in the configuration file is the name of the configuration. The name can be any valid Groovy symbol. If the name
contains any Groovy reserved words, or can be evaluated as Groovy expression, then the name must be surrounded by single or double
quotes.

JDBC database configurations contain the parameters necessary to create a connection to a live database. Here is an example of a JDBC 
connection configuration:

    'mysql-test' {
      database = 'test'
      jdbc { 
        url = 'jdbc:mysql://localhost:3306/test'
        driverClassName = 'com.mysql.jdbc.Driver'
        username = 'test'
        password = 'test'
      }
    }

XML input/output configurations contain the name of an input and/or output file. Here is an example of an XML configuration:

    xml {
      xmlFile = '/path/to/schema.xml'	
    }

SQL output configurations contain the name of an SQL dialect supported by DDLUtils and the name of an output file. Here is an
example of an SQL output configuration:

    mysql {
      dialect = 'MySQL'
      sqlFile = '/path/to/schema-mysql.sql'	
    }

## Building and customizing

### Building the jar file

The Groovy-DDLUtils project contains the build script necessary to build the self-contained executable jar file. To build the 
project, first clone the repository from GitHub, then run the following command from the root of the project structure: 

    > ./gradlew assemble" (Linux/Unix/OSX)
    > gradle.bat assemble" (Windows)

This command should generate the file `groovy-ddlutils-<version>.jar` in the directory `build/libs` under the project root.

### Customizing the jar file

A self-contained executable jar file can be created that contains the Groovy-DDLUtils script, the DDLUtils library, and any
JDBC driver jar files necessary to read from or write to a live database. To build the project with additional jar files, simply
clone the repository, copy the additional jar files into the `lib` directory under the project root, and run the build script as 
described above. 


[Apache DDLUtils]:      http://db.apache.org/ddlutils/
[Turbine XML format]:   http://db.apache.org/ddlutils/schema/
[files download page]:  https://github.com/scottfrederick/groovy-ddlutils/downloads
[raw script]:           https://raw.github.com/scottfrederick/groovy-ddlutils/master/src/main/groovy/DDLUtils.groovy
