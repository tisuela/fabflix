-- BEFORE RUNNING DO THIS: set global log_bin_trust_function_creators=1;





DROP PROCEDURE IF EXISTS add_movie;
DROP PROCEDURE IF EXISTS add_star;
DROP PROCEDURE IF EXISTS add_movie_from_XML;
DROP FUNCTION IF EXISTS generate_movie_id;
DROP FUNCTION IF EXISTS generate_star_id;

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
    INSERT INTO stars VALUES (id, name, birth_year);
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
CREATE PROCEDURE add_movie_from_XML(IN title VARCHAR(100), IN year INT, IN director VARCHAR(100),  IN genre_name VARCHAR(32))
BEGIN
    DECLARE movie_id, star_id  VARCHAR(10);
    DECLARE genre_id INT;

    -- add movie if it doesn't already exist
    IF NOT EXISTS (SELECT * FROM movies WHERE movies.title = title AND movies.year = year AND movies.director = director) THEN
        SELECT generate_movie_id() INTO movie_id;
        INSERT INTO movies VALUES (movie_id, title, year, director);
    END IF;

    -- get movie ID
    SELECT movies.id into movie_id FROM movies WHERE movies.title = title AND movies.year = year AND movies.director = director;

    -- create new genre if it doesn't exist
    IF NOT EXISTS (SELECT * FROM genres WHERE genres.name = genre_name) THEN
        INSERT INTO genres (genres.name) VALUES (GENRE_name);
    END IF;

    -- get genre ID
    SELECT DISTINCT genres.id INTO genre_id FROM genres WHERE genres.name = genre_name;

    -- Check for duplicates, then insert movie ID and genre ID to genres_in_movies
    IF NOT EXISTS(SELECT * FROM genres_in_movies WHERE genreId = genre_id AND movieId = movie_id) THEN
        INSERT INTO genres_in_movies VALUES (genre_id, movie_id);
    END IF;



END $$

DELIMITER ;

