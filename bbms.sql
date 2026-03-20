CREATE DATABASE IF NOT EXISTS bbms;
USE bbms;

CREATE TABLE IF NOT EXISTS donors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nm VARCHAR(100),
    age INT,
    grp VARCHAR(5),
    ph VARCHAR(20),
    city VARCHAR(50),
    dt DATE,
    units INT
);

CREATE TABLE IF NOT EXISTS stok (
    grp VARCHAR(5) PRIMARY KEY,
    units INT
);

CREATE TABLE IF NOT EXISTS reqs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient VARCHAR(100),
    hospital VARCHAR(100),
    grp VARCHAR(5),
    units INT,
    priority VARCHAR(20),
    dt DATE,
    status VARCHAR(20) DEFAULT 'Pending'
);

CREATE TABLE IF NOT EXISTS histry (
    id INT AUTO_INCREMENT PRIMARY KEY,
    dt DATE,
    typ VARCHAR(30),
    nm VARCHAR(100),
    grp VARCHAR(5),
    units INT,
    det VARCHAR(200)
);

INSERT IGNORE INTO stok VALUES
('A+', 20), ('A-', 8), ('B+', 15), ('B-', 5),
('AB+', 10), ('AB-', 3), ('O+', 25), ('O-', 7);
