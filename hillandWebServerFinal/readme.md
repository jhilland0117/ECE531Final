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

## Paths
- /report - gets reported temperatures that have been written to db
- /state - gets whether heater is on or off
- /temps - displays what the three temp settings are set to

# Change temp settings
- POST urlFromAbove:port -d id,temp1,temp2
- please keep id between 1-3, server not built for more than MORNING, AFTERNOON and EVENING values.
- temp1 and temp2 are a temperature range, so temp1 < temp2, guess i should have check for that bug in my code. oh well!

## Running the code
`mvn compile`
`mvn exec:java -Dexec.mainClass="com.hilland.HillandCurlServer"`
