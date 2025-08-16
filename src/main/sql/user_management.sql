CREATE DATABASE milou_db;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE emails (
    code VARCHAR(6) NOT NULL,
    sender_id INT NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body VARCHAR(1000) NOT NULL,
    isReply BOOLEAN NOT NULL DEFAULT FALSE,
    isForward BOOLEAN NOT NULL DEFAULT FALSE,
    original_email_code VARCHAR(6),
    sent_time DATETIME NOT NULL,
    PRIMARY KEY (code),
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (original_email_code) REFERENCES emails(code)
);

CREATE TABLE deliveries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email_id VARCHAR(6) NOT NULL,
    recipient_id INT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (email_id) REFERENCES emails(code),
    FOREIGN KEY (recipient_id) REFERENCES users(id)
);