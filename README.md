## CS 122B Project 2

By:
Nathaniel Louis Crisostomo Tisuela 
Garfield Tsui

## CS 122B Project 2

By:
Nathaniel Louis Crisostomo Tisuela 
Garfield Tsui

## Video URLs:

project 1 demo: https://www.youtube.com/watch?v=5nVmqSMmUgQ

project 2 demo: https://youtu.be/iyrqFT7_ZKA

project 3 demo: https://www.youtube.com/watch?v=rOGeSoHD6IM&feature=youtu.be

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

    Used in:
    MyQuery (module by us) - https://github.com/UCI-Chenli-teaching/cs122b-spring20-team-99/blob/master/src/MyQuery.java
    SingleMovieServlet     - https://github.com/UCI-Chenli-teaching/cs122b-spring20-team-99/blob/master/src/SingleMovieServlet.java
    SingleStarServlet      - https://github.com/UCI-Chenli-teaching/cs122b-spring20-team-99/blob/master/src/SingleStarServlet.java
    

## XML Optimization Strategies
    
    Naive implementations took over 30 minutes in totality
    Files are: NaiveSAXParser....java 
    
    I implemented batch inserts using stored procedures (in stored-procedure.sql) for ParseMoviesBatch.java. For that single file, it took 5 minutes. The naive implementation (NaiveSaxParserMovies.java) took about ten minutes.
    
    Lastly I implemented LOAD DATA statements (ParseActors.java, ParseMovies.java, ParseAll.java). Exact performance (nanoseconds) is in the video (when I display the inconsistency report). This improvement cut it down to about 5 seconds. 


## Inconsistency Report
The inconsistency report was printed out on the video.

Timestamp:  11:23 https://youtu.be/rOGeSoHD6IM?t=683

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

