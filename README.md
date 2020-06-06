- # General
    - #### Team#: 99
    
    - #### Names:
            Garfield Tsui
    
            Nathaniel Louis Crisostomo Tisuela
    
    - #### Project 5 Video Demo Link:

    - #### Instruction of deployment:
    If you have not done so, populate your 'moviedb' database with the movie-data.sql file.
    
    If on development machine, simply run the program on intellij with the Tomcat Configuartion.
    
    If on production machine, use mvn package command where the pom.xml file is located to compile the java code. Move the .war file to the the tomcat/webapps directory.
    
    Run the password encryption script UpdateSecurePassword. In order to do so, you need to set iWantToEncrypt to true as well as database to either customers or employees depending on the database you wish to encrypt.
    
    No technologies were used beyond the ones supported by the class (CSS, JS, Java, HTML)

    - #### Collaborations and Work Distribution:
            Nathantiel:
                Connection pooling
                MySql Routing
            
            Garfield:
                Jmeter tests
                log_processing.py
                
            Group effort:
                Master-slave replication
                Load balancing configuration


- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
            
            Web/src/
                AutocompleteServlet.java
                CartServlet.java
                ConfirmationServlet.java 
                DashboardServlet.java
                GenreServlet.java
                MoviesServlet.java
                ParseAll.java
                PaymentServlet.java
                SingleMovieServlet.java
                SingleStarServlet.java
                UpdateSecurePassword.java
            
    - #### Explain how Connection Pooling is utilized in the Fabflix code.
            
            asd
    - #### Explain how Connection Pooling works with two backend SQL.
    
            asd
    

- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.

    - #### How read/write requests were routed to Master/Slave SQL?
    

- # JMeter TS/TJ Time Logs
    - #### Instructions of how to use the `log_processing.*` script to process the JMeter logs.
    
            The log_processing.py script found at the root of our project is used as a command line script.
            eg python log_processing.py <pathtofile>
            The file it processes is a csv file where the first column of each entry is for TS values 
            and the second column of each entry is for TJ values.


- # JMeter TS/TJ Time Measurement Report

| **Single-instance Version Test Plan**          | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![](img/single-pooling-1thread.png)   | ??                         | ??                                  | ??                        |              |
| Case 2: HTTP/10 threads                        | ![](img/single-pooling-10threads-http.png)   | ??                         | ??                                  | ??                        | ??           |
| Case 3: HTTPS/10 threads                       | ![](img/single-pooling-10threads-https.png)   | ??                         | ??                                  | ??                        | ??           |
| Case 4: HTTP/10 threads/No connection pooling  | ![](img/single-noPool-10thread.png)   | ??                         | ??                                  | ??                        | ??           |

| **Scaled Version Test Plan**                   | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![](img/scaled-pooling-1thread.png)   | ??                         | ??                                  | ??                        | ??           |
| Case 2: HTTP/10 threads                        | ![](img/scaled-pooling-10thread.png)   | ??                         | ??                                  | ??                        | ??           |
| Case 3: HTTP/10 threads/No connection pooling  | ![](img/scaled-noPool-10thread.png)   | ??                         | ??                                  | ??                        | ??           |


