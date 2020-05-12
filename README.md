## CS 122B Project 2

By:
Nathaniel Louis Crisostomo Tisuela 
Garfield Tsui

## Video URLs:

project 1 demo: https://www.youtube.com/watch?v=5nVmqSMmUgQ

project 2 demo: https://youtu.be/iyrqFT7_ZKA

## CS 122B Project 2

By:
Nathaniel Louis Crisostomo Tisuela 
Garfield Tsui

## Video URLs:

project 1 demo: https://www.youtube.com/watch?v=5nVmqSMmUgQ

project 2 demo: https://youtu.be/iyrqFT7_ZKA

project 3 demo: 

## Deployment

If you have not done so, populate your 'moviedb' database with the movie-data.sql file.

If on development machine, simply run the program on intellij with the Tomcat Configuartion.

If on production machine, use  ``mvn package`` command where the pom.xml file is located to compile the java code. 
Move the .war file to the the tomcat/webapps directory. 

Run the password encryption script ``UpdateSecurePassword``.
In order to do so, you need to set ``iWantToEncrypt`` to ``true`` 
as well as ``database`` to either ``customers`` or ``employees`` depending on
the database you wish to encrypt.

No technologies were used beyond the ones supported by the class
(CSS, JS, Java, HTML)

## PreparedStatements

## XML Optimization Strategies

## Inconsistency Report

## Contributions
Nathaniel:

    prepared statements
    stored procedures
    xml parsing
    HTTPS
    recaptcha

Garfield:

    dashboard metadata tables and styling
    employee login page
    access to dashboard for employees only
    
    password encryption and verifcation for both customers and employees database

