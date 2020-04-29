## CS 122B Project 2

By:
Nathaniel Louis Crisostomo Tisuela 
Garfield Tsui

## Video URLs:

project 1 demo: https://www.youtube.com/watch?v=5nVmqSMmUgQ

project 2 demo: https://youtu.be/iyrqFT7_ZKA

## Deployment

If you have not done so, populate your 'moviedb' database with the movie-data.sql file.

If on development machine, simply run the program on intellij with the Tomcat Configuartion.

If on production machine, use  ``mvn package`` command where the pom.xml file is located to compile the java code. 
Move the .war file to the the tomcat/webapps directory. 

No technologies were used beyond the ones supported by the class
(CSS, JS, Java, HTML)

## Substring Matching
Pattern 1 for anything containing "term"


LIKE %term%

	java code = "%s LIKE \"%%%s%%\""

Pattern 2 for exact match on string


string = "string"

	java code = "%s = \"%s\""

Pattern 3 for exact match on int


int = int

	java code = "%s = %s"

Pattern 4 for begins with "term"


LIKE term%

	java code = "%s LIKE \"%s%%\""

Pattern 5 for anything that begins with a non-alphanumeric character


REGEXP "^[^a-z0-9]+"

Pattern 1 was used for matching actors, titles, and directors in search


Pattern 2 was used for genre browsing


Pattern 3 was used for years in search

Pattern 4 was used for title browsing except for the "\*" option


Pattern 5 was used for the "\*" option in title browsing


All these patterns can be found in the "src/BuildQuery.java" of our project on lines 52-55.          
Pattern 5 can be found on line 83 instead.                        

## Contributions
Nathaniel:

	Login Page and Filter


	Backend queries for searching and the search page


	correctly displaying the main page, single star page, and single movie page all the content inside these pages (eg correct ordering of movies/stars)


	All of shopping cart, payment, and place order along with check out and the add to cart on movie pages

Garfield:

	Modified backend queries to correctly show options for "\*"


	Jump functionality (eg saving state when going from the main movies page then to a star and back)


	All of browsing


	All of pagination including the prev/next button


	Changing the number of results per page


	All of sorting
