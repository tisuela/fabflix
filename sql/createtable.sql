-- For the movies DBÎ

DROP TABLE IF EXISTS movies, stars, stars_in_movies, genres, genres_in_movies, customers, sales, creditcards, ratings;

CREATE TABLE movies(
	id varchar(10) PRIMARY KEY,
	title varchar(100) NOT NULL,
	year integer NOT NULL,
	director varchar(100) NOT NULL
);


CREATE TABLE stars(
	id varchar(10) primary key,
	name varchar(100) NOT NULL,
	birthYear integer 
);


CREATE TABLE stars_in_movies(
	starId varchar(10) NOT NULL,
	movieId varchar(10) NOT NULL,

	FOREIGN KEY (starID) REFERENCES stars(id),
	FOREIGN KEY (movieID) REFERENCES movies(id)
);



CREATE TABLE  genres(
	id integer AUTO_INCREMENT PRIMARY KEY,
	name varchar(32) NOT NULL
);


CREATE TABLE genres_in_movies(
	genreId integer NOT NULL,
	movieId varchar(10) NOT NULL,

	FOREIGN KEY (genreId) REFERENCES genres(id),
	FOREIGN KEY (movieId) REFERENCES movies(id)
);


CREATE TABLE creditcards(
	id VARCHAR(20),
	firstName VARCHAR(50) NOT NULL,
	lastName VARCHAR(50) NOT NULL,
	expiration DATE NOT NULL,
	PRIMARY KEY (id)
);


CREATE TABLE customers(
	id INT NOT NULL AUTO_INCREMENT,
	firstName VARCHAR(50) NOT NULL,
	lastName VARCHAR(50) NOT NULL,
	ccId VARCHAR(20) NOT NULL,
	address VARCHAR(200) NOT NULL,
	email VARCHAR(50) NOT NULL,
	password VARCHAR(20) NOT NULL,
	PRIMARY KEY (id)	
);


CREATE TABLE sales(
	id INT AUTO_INCREMENT PRIMARY KEY,
	customerId INT NOT NULL,
	movieId VARCHAR(10) NOT NULL,
	saleDate DATE NOT NULL,

	FOREIGN KEY (customerId) REFERENCES customers(id),
	FOREIGN KEY (movieId) REFERENCES movies(id)
);


CREATE TABLE ratings(
	movieId VARCHAR(10),
	rating FLOAT,
	numVotes INT,

	FOREIGN KEY (movieId) REFERENCES movies(id)
);

-- views

DROP VIEW IF EXISTS movies_with_rating;
CREATE VIEW movies_with_rating AS(
	SELECT *
	FROM movies LEFT JOIN ratings ON movies.id = ratings.movieId
	ORDER BY ratings.rating DESC	
);







