CREATE TABLE IF NOT EXISTS credit_card_info (
    card_number VARCHAR(255) PRIMARY KEY,
    month_num VARCHAR(2) NOT NULL,
    year_num VARCHAR(4) NOT NULL,
    security_code VARCHAR(3) NOT NULL,
    full_name VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    address VARCHAR(255),
    city VARCHAR(255),
    phone_number VARCHAR(255),
    phone_number_country INT,
    state VARCHAR(255),
    zip_code VARCHAR(10),
    email VARCHAR(255),
    password VARCHAR(255),
    success_num INT,
    fail_num INT,
    is_active VARCHAR(255)
);

