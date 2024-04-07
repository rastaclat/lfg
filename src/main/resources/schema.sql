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
    state VARCHAR(255),
    zip_code VARCHAR(10),
    email VARCHAR(255),
    password VARCHAR(255),
    success_num INT,
    fail_num INT
);

CREATE TABLE IF NOT EXISTS task_info (
    id INT AUTO_INCREMENT PRIMARY KEY,
    task_id VARCHAR(255),
    task_url VARCHAR(1000),
    task_msg VARCHAR(255),
    page_info VARCHAR(255),
    real_task_url VARCHAR(1000),
    open_time VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS ip_proxy_info (
    id INT PRIMARY KEY,
    proxy_type VARCHAR(50),
    host VARCHAR(100),
    port INT,
    user_name VARCHAR(255),
    password VARCHAR(255),
    refresh_link VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS config_info (
    id INT AUTO_INCREMENT PRIMARY KEY,
    browser_type VARCHAR(50),
    os_type VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS browser_info (
    browser_id VARCHAR(255) PRIMARY KEY,
    finger_print_id VARCHAR(255),
    browser_type VARCHAR(50),
    refresh_proxy_url VARCHAR(255),
    created_time TIMESTAMP,
    update_time TIMESTAMP,
    oper_time TIMESTAMP,
    close_time TIMESTAMP
);
