# oradiff
> Command line Java migration tool to generate Oracle database patches

This tool is able to compare between two Oracle databases and generate a set of SQL files that describe the
operations to migrate the schema. The tool can either work online (source and destination are Oracle connections) or
offline (working from backup files).

## Prerequisites

You will need:
- Java 1.8 (however you can probably use a lower version by changing the gradle build file) 
- the JDBC driver for Oracle (ojdbc14.jar). You can easily download it from http://www.oracle.com/technetwork/apps-tech/jdbc-10201-088211.html
- git

### Building

```shell
git clone https://github.com/khodev/oradiff.git
cd oradiff
```
Copy ojdbc14.jar in the subfolder libs and run:


```shell
gradlew build
cp build/libs/oradiff.jar where/you/want
```

### Using

```shell
java -jar oradiff.jar -h


usage: java -jar oradiff.jar -<command> [...]
 -d,--diff <from> <to> <destdir>                      generate a diff
                                                      between 2 schemas
 -h,--help                                            show this help
 -on,--old-new                                        generate old and new
                                                      files
 -rf,--rename-folder                                  automatically change
                                                      the destination
                                                      folder to a new one
                                                      if already exists
 -s,--save <host:port:sid:user:pass> <filename.xml>   save a schema to a
                                                      file
 -sm,--save-map                                       save new
                                                      substitutions into a
                                                      file
<from> and <to> can be either a database connection host:port:sid:user:pass or a file.xml
Examples
java -jar oradiff.jar --save localhost:1521:MYDB:USER:PASS backup.xml
java -jar oradiff.jar --diff localhost:1521:DB1:USER:PASS localhost:1521:DB2:USER:PASS path/to/results
java -jar oradiff.jar --diff backup1.xml backup2.xml path/to/results
java -jar oradiff.jar --diff backup1.xml localhost:1521:MYDB:USER:PASS backup.xml path/to/results
```
If you get a memory error during the diff, try:
```shell
jar -Xms512m -jar oradiff.jar -d db1.xml db2.xml
```

## Features

This tool can:
* Backup the structure of a database into an xml file 
* Compare a database to another, and generate the required migration queries `ALTER TABLE ...`. The tool can compare databases by connecting directly or using a backup xml file.
* The tool will be able to migrate the following objects:
    - columns
    - tables
    - views
    - procedures
    - functions
    - triggers
    - packages
    - sequences
    - jobs

Some use cases:
* I need to apply changes made on a development database to UAT (ALTER something, CREATE PACKAGE, etc) but I don't have any log of what has been done
* I want to know what is done on the database: you can easily setup a night job to keep track of the modifications done during the day  
* I need to revert some modifications and have a backup or another database  containing the initial structure

Additional notes:
- the tool will never alter a connected database. It will only create the SQL files in
 the specified directory, you can then review and run the SQL scripts on the target environment.
- if you have two database schemas, e.g. OLD.xml and NEW.xml, you would 
run `java -jar oradiff.jar OLD.xml NEW.xml results`: the tool will create the directory
 results and put the SQL files there. Then, you will run the scripts on OLD database
  to migrate it to the NEW structure, and not the other way around.

## Configuration

#### tablespaces.txt

You might want to apply your SQL on a database that has different tablespaces. 
Create a text file next to the jar and add a couple of `key=value`, for example if your
tablespaces.txt file contains:
```
TBS_UAT_BLOB=TBS_PROD_BLOB
```

Then oradiff will generate patch files by replacing the tablespace TBS_UAT_BLOB with TBS_PROD_BLOB.

### usersroles.txt ###

This mapping allows you to replace users (e.g. grantees) when generating a patch:
```
ALICE=BOB
```

### equivalences.txt ###

You can use this mapping to tell the tool that a table has been renamed.
For example if you know that your table ADMINS has been renamed to ADMIN, then write:
```
ADMINS=ADMIN
```
Instead of creating a `DROP TABLE ADMINS` and `CREATE TABLE ADMIN`, the tool will create `ALTER TABLE ADMINS` and `RENAME ADMIN` scripts.

## Contributing

I welcome any suggestions and comments.  

## Licensing

The code in this project is licensed under [MIT license](./LICENSE).
