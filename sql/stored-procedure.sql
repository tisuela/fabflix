-- BEFORE RUNNING DO THIS: set global log_bin_trust_function_creators=1;
set global log_bin_trust_function_creators=1;
DROP PROCEDURE IF EXISTS add_movie;
DROP PROCEDURE IF EXISTS add_star;
DROP PROCEDURE IF EXISTS add_movie_from_XML;
DROP PROCEDURE IF EXISTS add_genre_from_XML;
DROP PROCEDURE IF EXISTS add_genre_from_XML_by_movie_xmlId;
DROP PROCEDURE IF EXISTS naive_add_movie_from_XML;
DROP PROCEDURE IF EXISTS add_transaction;

DROP FUNCTION IF EXISTS generate_movie_id;
DROP FUNCTION IF EXISTS generate_star_id;

SET GLOBAL sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));
DELIMITER $$

-- Helper functions for creating IDs --

CREATE FUNCTION generate_movie_id()
RETURNS VARCHAR(10)
BEGIN
    DECLARE new_id, new_id_sub_int, max_id, max_id_sub_int VARCHAR(10);
    DECLARE max_id_sub_char VARCHAR(2);
    DECLARE max_id_int MEDIUMINT ZEROFILL;

    -- split the maximum ID into two substrings
    -- 1st substring max_id_sub_char is the first two char "tt"
    -- 2nd substring max_id_sub_int is the rest, which are integers, to be converted to an INT
    SELECT max(id) INTO max_id FROM movies;
    SELECT SUBSTRING(max_id, 1, 2) INTO max_id_sub_char;
    SELECT SUBSTRING(max_id, 3) INTO max_id_sub_int;

    -- increment maximum ID int
    SET max_id_int = CAST(max_id_sub_int AS UNSIGNED);
    SET max_id_int = max_id_int + 1;

    -- cast back to String
    SET new_id_sub_int = CAST(max_id_int AS CHAR(8));

    -- remove leading zeros
    SELECT SUBSTRING(new_id_sub_int, 2) INTO new_id_sub_int;

    -- put it back together
    SET new_id = CONCAT(max_id_sub_char, new_id_sub_int);

    RETURN new_id;

END $$


-- basically a copy generate_movie_id, except for the stars table
CREATE FUNCTION generate_star_id()
    RETURNS VARCHAR(10)
BEGIN
    DECLARE new_id, new_id_sub_int, max_id, max_id_sub_int VARCHAR(10);
    DECLARE max_id_sub_char VARCHAR(2);
    DECLARE max_id_int MEDIUMINT ZEROFILL;

    -- split the maximum ID into two substrings
    -- 1st substring max_id_sub_char is the first two char "nm"
    -- 2nd substring max_id_sub_int is the rest, which are integers, to be converted to an INT
    SELECT max(id) INTO max_id FROM stars;
    SELECT SUBSTRING(max_id, 1, 2) INTO max_id_sub_char;
    SELECT SUBSTRING(max_id, 3) INTO max_id_sub_int;

    -- increment maximum ID int
    SET max_id_int = CAST(max_id_sub_int AS UNSIGNED);
    SET max_id_int = max_id_int + 1;

    -- cast back to String
    SET new_id_sub_int = CAST(max_id_int AS CHAR(8));

    -- remove leading zeros
    SELECT SUBSTRING(new_id_sub_int, 2) INTO new_id_sub_int;

    -- put it back together
    SET new_id = CONCAT(max_id_sub_char, new_id_sub_int);

    RETURN new_id;

END $$


-- adding star and movie procedures --

-- for the dashboard
CREATE PROCEDURE add_star(IN name VARCHAR(100), IN birth_year INT)
BEGIN
    DECLARE id VARCHAR(10);
    SELECT generate_star_id() INTO id;
    INSERT INTO stars (stars.id, stars.name, stars.birthYear) VALUES (id, name, birth_year);
END $$


-- for the dashboard
CREATE PROCEDURE add_movie(IN title VARCHAR(100), IN year INT, IN director VARCHAR(100), IN star_name VARCHAR(100), IN genre_name VARCHAR(32))
BEGIN
    DECLARE movie_id, star_id  VARCHAR(10);
    DECLARE genre_id INT;

    IF NOT EXISTS (SELECT * FROM movies WHERE movies.title = title AND movies.year = year AND movies.director = director) THEN
        SELECT generate_movie_id() INTO movie_id;
        INSERT INTO movies VALUES (movie_id, title, year, director);

        -- create new star if it doesn't exist
        IF NOT EXISTS (SELECT * FROM stars WHERE stars.name = star_name) THEN
            CALL add_star(star_name, NULL);
        END IF;

        -- create new genre if it doesn't exist
        IF NOT EXISTS (SELECT * FROM genres WHERE genres.name = genre_name) THEN
            INSERT INTO genres (genres.name) VALUES (GENRE_name);
        END IF;

        -- Get affiliated star and genre IDs
        SELECT DISTINCT stars.id INTO star_id FROM stars WHERE stars.name = star_name;
        SELECT DISTINCT genres.id INTO genre_id FROM genres WHERE genres.name = genre_name;

        -- Insert movie ID, star ID, and genre ID into their respective tables
        INSERT INTO stars_in_movies VALUES (star_id, movie_id);
        INSERT INTO genres_in_movies VALUES (genre_id, movie_id);

    END IF;

END $$


-- for XML parsing
CREATE PROCEDURE add_movie_from_XML(IN title VARCHAR(100), IN year INT, IN director VARCHAR(100), IN xml_id VARCHAR(10))
BEGIN
    DECLARE movie_id  VARCHAR(10);

    -- add movie if it doesn't already exist
    IF NOT EXISTS (SELECT * FROM movies WHERE movies.title = title AND movies.year = year AND movies.director = director) THEN
        SELECT generate_movie_id() INTO movie_id;
        INSERT INTO movies VALUES (movie_id, title, year, director);
        -- INSERT INTO movies_in_xml (movieId, xmlId) VALUES (movie_id, xml_id);
    END IF;


    -- add movie to XML table, and assert no duplicates with xml ID
    IF NOT EXISTS (SELECT * FROM movies_in_xml WHERE xmlId = xml_id) THEN
        -- get movie ID again, in case this movie already exists
        SELECT movies.id INTO movie_id FROM movies WHERE movies.title = title AND movies.year = year AND movies.director = director;
        INSERT INTO movies_in_xml (movieId, xmlId) VALUES (movie_id, xml_id);
    END IF;





END $$



CREATE PROCEDURE add_genre_from_XML(IN title VARCHAR(100), IN year INT, IN director VARCHAR(100), IN genre_name VARCHAR(32))
BEGIN
    DECLARE genre_id INT;
    DECLARE movie_id VARCHAR(10);

    -- create new genre if it doesn't exist
    IF NOT EXISTS (SELECT * FROM genres WHERE genres.name = genre_name) THEN
        INSERT INTO genres (genres.name) VALUES (GENRE_name);
    END IF;

    -- get genre ID
    SELECT DISTINCT genres.id INTO genre_id FROM genres WHERE genres.name = genre_name;

    -- check if movie exists and get the movie ID
    IF EXISTS (SELECT * FROM movies WHERE movies.title = title AND movies.year = year AND movies.director = director) THEN
        SELECT movies.id INTO movie_id FROM movies WHERE movies.title = title AND movies.year = year AND movies.director = director;
        -- Check for duplicates, then insert movie ID and genre ID to genres_in_movies
        IF NOT EXISTS(SELECT * FROM genres_in_movies WHERE genreId = genre_id AND movieId = movie_id) THEN
            INSERT INTO genres_in_movies VALUES (genre_id, movie_id);
        END IF;
    END IF;
END $$


CREATE PROCEDURE add_genre_from_XML_by_movie_xmlId(IN movie_xmlId VARCHAR(10), IN genre_name VARCHAR(32))
BEGIN
    DECLARE genre_id INT;
    DECLARE movie_id VARCHAR(10);

    -- create new genre if it doesn't exist
    IF NOT EXISTS (SELECT * FROM genres WHERE genres.name = genre_name) THEN
        INSERT INTO genres (genres.name) VALUES (GENRE_name);
    END IF;

    -- get genre ID
    SELECT DISTINCT genres.id INTO genre_id FROM genres WHERE genres.name = genre_name;

    IF EXISTS (SELECT movies.id FROM movies_in_xml JOIN movies ON movies.id = movies_in_xml.movieId WHERE xmlId = movie_xmlId) THEN
        -- get movie ID
        SELECT DISTINCT movies.id INTO movie_id FROM movies_in_xml JOIN movies ON movies.id = movies_in_xml.movieId WHERE xmlId = movie_xmlId;

        -- Check for duplicates, then insert movie ID and genre ID to genres_in_movies
        IF NOT EXISTS(SELECT * FROM genres_in_movies WHERE genreId = genre_id AND movieId = movie_id) THEN
            INSERT INTO genres_in_movies (genreId, movieId) VALUES (genre_id, movie_id);
        END IF;
    END IF;

END $$




-- for naive implementation of XML parsing
CREATE PROCEDURE naive_add_movie_from_XML(IN title VARCHAR(100), IN year INT, IN director VARCHAR(100))
BEGIN
    DECLARE movie_id, star_id  VARCHAR(10);

    -- add movie if it doesn't already exist
    IF NOT EXISTS (SELECT * FROM movies WHERE movies.title = title AND movies.year = year AND movies.director = director) THEN
        SELECT generate_movie_id() INTO movie_id;
        INSERT INTO movies VALUES (movie_id, title, year, director);
    END IF;
END $$


-- payment / sales / transaction --

-- for making a payment (sale and transaction)
CREATE PROCEDURE add_transaction(IN customerId INT, IN movieId VARCHAR(10), IN saleDate DATE, IN quantity INT, IN transactionId INT)
BEGIN
    DECLARE saleId INT;
    INSERT INTO sales (sales.customerId, sales.movieId, sales.saleDate) VALUES (customerId, movieId, saleDate);
    SELECT max(sales.id) INTO saleId FROM sales;

    INSERT INTO transactions (transactions.transactionId, transactions.saleId) VALUES  (transactionId, saleId);

END $$

DELIMITER ;

