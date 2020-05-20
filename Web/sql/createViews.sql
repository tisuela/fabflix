-- views

DROP VIEW IF EXISTS movies_with_rating;
CREATE VIEW movies_with_rating AS(
	SELECT *
	FROM movies LEFT JOIN ratings ON movies.id = ratings.movieId
	ORDER BY ratings.rating DESC	
);

