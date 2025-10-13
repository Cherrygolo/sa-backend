CREATE DATABASE sa;

# Use the sa database to insert tables 

USE sa;

# Create a table for customers to give their opinions, with automatically generated IDs and unique email addresses

CREATE TABLE customer (
    id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
    email VARCHAR(50) UNIQUE,
    phone VARCHAR(15)
);

# Create a review table to contain the review (text, type) of the user (customer_id)

CREATE TABLE review (
    id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
    text VARCHAR(50),
    type VARCHAR(10),
    customer_id INTEGER,
    CONSTRAINT fk_review_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
);

