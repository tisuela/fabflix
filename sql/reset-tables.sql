set foreign_key_checks = 0;
delete from genres_in_movies where movieid > 'tt0499469';
delete from stars_in_movies where starid > 'nm9423080' OR movieid > 'tt0499469';
delete from stars where id > 'nm9423080';
delete from movies where id > 'tt0499469';
delete from genres where id > 23;
delete from movies_in_xml where 1 = 1;
set foreign_key_checks = 1;