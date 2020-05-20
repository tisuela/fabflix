SET GLOBAL local_infile = 0;

LOAD DATA LOCAL INFILE 'movies.txt' INTO TABLE movies FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n'
    (@column1, @column2, @column3) SET id = @column1, year = @column2, director = @column3
;