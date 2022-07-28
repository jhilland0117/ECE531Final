# Java curl receiving application running on AWS EC2 instance.

# to use the script
to be able to execute the script
`chmod +x compileAndRun.sh`
run script in background "daemonized"
`nohup bash compileAndRun.sh &`

## Required packages
you will need to have the following installed
java openjdk 11+ (comes default with 17 on AWS EC2), 
maven 3.6+, 
git,
mysql

## Intro
You can perform curl actions against the application by doing the below commands.
This application is meant to satisfy base requirements for ECE 531 Final project.

All required applications dependencies are listed in the pom file.

## Database
Database has one table, thermostat.
Database has two tables:
1. temps, which includes id (long), temp (int), time (int) based on 24 hour clock
2. state, which includes id (long), state(char(0)) which is '' for on and NULL for off, time (date)

## AWS EC2
Currently only allow port 8080 traffic, port ssh to my IP. 
Public IP for this instance with httpdnano running is
http://52.8.135.131:8080

## Commands
needs to be updated


## Running the code
`mvn compile`
`mvn exec:java -Dexec.mainClass="com.hilland.HillandCurlServer"`
