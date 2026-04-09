CREATE TABLE records (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                  VARCHAR(255) NOT NULL,
    email                 VARCHAR(255) NOT NULL,
    phone                 VARCHAR(50),
    address               VARCHAR(500),
    date_of_birth         DATE,
    ssn                   VARCHAR(20),
    bio                   TEXT,
    department            VARCHAR(100),
    job_title             VARCHAR(200),
    employment_type       VARCHAR(50),
    start_date            DATE,
    manager               VARCHAR(255),
    status                VARCHAR(50) DEFAULT 'active',
    remote_eligible       BOOLEAN DEFAULT FALSE,
    notifications_enabled BOOLEAN DEFAULT TRUE,
    notification_channels VARCHAR(500),
    access_level          VARCHAR(50) DEFAULT 'standard',
    notes                 TEXT,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_records_email (email)
);
