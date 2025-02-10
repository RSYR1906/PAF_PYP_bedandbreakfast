-- Write your Task 1 answers in this file
CREATE DATABASE bedandbreakfast;

USE bedandbreakfast;

CREATE TABLE users (
	email varchar(128) NOT NULL,
	name varchar(128) NOT NULL,
	
	CONSTRAINT pk_email PRIMARY KEY (email)
)

CREATE TABLE bookings (
	booking_id char(8) NOT NULL,
	listing_id varchar(20),
	duration int,
	email varchar(128),
	
	CONSTRAINT pk_booking_id PRIMARY KEY (booking_id),
	CONSTRAINT fk_email FOREIGN KEY (email) REFERENCES users(email)
)

CREATE TABLE reviews (
	id int AUTO_INCREMENT NOT NULL,
	date timestamp,
	listing_id varchar(20),
	reviewer_name varchar(64),
	comments text,
	
	CONSTRAINT pk_id PRIMARY KEY (id)
	
)

INSERT INTO users 
VALUES ("fred@gmail.com", "Fred Flintstone"),("barney@gmail.com","Barney Rubble"),
	("fry@planetexpress.com","Philip J Fry"),("hlmer@gmail.com","Homer Simpson")
