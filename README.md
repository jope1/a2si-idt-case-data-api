# a2si-idt-case-data-api
This application acts as a REST API allowing clients to access aggregated case data

It is a Spring Boot application that bundles Tomcat as a runtime container into a single jar 
(often known as the Uber jar, holding everything required to run the application)

## Getting started
First, download the code from GitHub. This can be done using the desktop git tool, an IDE which supports git or by downloading the code as a zip file which you can then extract.

Next, install the dev tools and dependencies....

##Installation of Development Tools and Dependencies
Install Git for Windows:
Install official git release: https://git-scm.com/download/win

Or install GitHub Desktop which also includes a GUI interface for git management: https://desktop.github.com/

###Install Java Development Kit 8:
http://www.oracle.com/technetwork/java/javase/downloads/

###Install Maven 3:
https://maven.apache.org/download.cgi

###Environment Variables
Ensure that the system environment variables for Java and Maven are set correctly, as described below...

M2_HOME should point to the install directory of your local Maven install folder, e.g.
M2_HOME C:\Maven\apache-maven-3.3.9

JAVA_HOME should point to the install directory of your local Java JDK install folder, e.g.

JAVA_HOME C:\Program Files\Java\jdk1.8.0_121

PATH should contain the bin directory of both M2_HOME and JAVA_HOME, e.g.

```
...;%JAVA_HOME%\bin;%M2_HOME%\bin;...
```

## Use maven to build the project:

cd {projectRoot}
mvn clean install

the Maven "install" goal stores the built artifact into the local Maven repository, 
making it accessible to other projects using this repository.

The application is going to be deployed in AWS using Elastic Beanstalk, using Docker as a container. Elastic Beanstalk
allows Spring Boot applications to be packaged along with a DockerFile in a single zip file. This zip file is all
that is required to deploy into AWS Elastic Beanstalk. Environment variables may be required to define the 
Spring Profiles Active variable.

## Running the Application
The Spring Boot application uses port 7070 as it's default port (all A2SI Spring Boot applications use different
ports so they can all be run one a single developer machine).
The application exposes a REST controller that is used by the A2SI Graph Web Application (an HTML and JavaScript).
This application provides the data for the web application to draw the graph.

## Maintaining the Application
Any changes to user requirements might change the data this application accesses. This application uses data extracted
from the NHS Digital's IDT system.

N.B. The work to define and implement APIs for IDT systems to pass the latest service and case data has not been
formally commissioned. There have been discussions about a potential API and an initial implementation has been done
for the purposes of discussion and testing. 
A Rest API with separate endpoints for service data and case data has been developed. 
The expectation mode of operation would be to take regular updates of new information from IDT, e.g. every fifteen
minutes.
It is better to take new information as a "little and often" feed rather than taking larger, less frequent updates.
Short intervals between getting updates makes the data more up to date and reduces processing load.

The database queries required to create the data grouped into time periods, disposition groups etc. is complicated
and it is very, very difficult to split data like this using a single database query.

For these reasons, the application uses two stored procedures that generate MySql temporary tables, which are tables
that exist only as long as the current database session that created them exists.

When getting the data requested, two stored procedures are called that use temporary tables.

The first simply retrieves all the disposition broad group that cases have fallen into within the date range.

The second is a complicated stored procedure that was taken from an open source project (see the stored procedure for 
the URLs of the site and Git Repo) and generates temporary tables with clearly defined time periods. 

The data generated from these tables allows a query to get ALL disposition broad groups and ALL time periods for the 
request, this is very important because the graph will not display correctly if data or disposition broad groups are missing.
e.g. if we have data for all time periods for one disposition broad group but some time periods for another disposition 
broad group doesn't have data for all time periods (because there were zero cases in some time periods), the graph will 
just show the second disposition broad group without any gaps, i.e. it will ignore the time periods the data applies to.
The data for all time periods and disposition broad groups is used with a UNION that gets the number of cases, creating
a final result set that includes the time periods where zero cases were handled.



## Application Configuration
Following a best practice approach that comes from Spring, configuration files hold data specific to 
environments whilst wiring of class dependencies is done via Java Configuration classes. These classes
use the configuration files to set simple properties and are created when the application starts.

