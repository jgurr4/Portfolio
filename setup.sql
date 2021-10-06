CREATE DATABASE IF NOT EXISTS lawn;
USE lawn;
CREATE TABLE IF NOT EXISTS contact
(
    name            VARCHAR(40)  NOT NULL,
    email           VARCHAR(100) NOT NULL,
    phone           VARCHAR(40)  NOT NULL,
    service         VARCHAR(100) NOT NULL,
    address         VARCHAR(255) NOT NULL,
    intervals       VARCHAR(40)  NOT NULL,
    message         TEXT         NOT NULL,
    submission_date DATE DEFAULT CURDATE(),
    PRIMARY KEY (name)
) ENGINE ARIA;

CREATE DATABASE IF NOT EXISTS portfolio;
USE portfolio;
CREATE TABLE IF NOT EXISTS contact
(
  name VARCHAR(45), 
  business VARCHAR(100), 
  position VARCHAR(100), 
  callback VARCHAR(100), 
  phone VARCHAR(18),  
  interview_date VARCHAR(200)
) ENGINE ARIA;

CREATE USER 'portfolio'@'%' IDENTIFIED BY 'somePassword';
GRANT INSERT ON portfolio.contact TO 'portfolio'@'%';
FLUSH PRIVILEGES;
