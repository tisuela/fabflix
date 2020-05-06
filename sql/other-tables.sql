DROP TABLE IF EXISTS employees;

CREATE TABLE employees(
    fullName VARCHAR(100) NOT NULL,
    email VARCHAR(50) NOT NULL,
    password VARCHAR(20) NOT NULL,
    PRIMARY KEY (email)
);