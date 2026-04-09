CREATE TABLE emergency_contacts (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id    BIGINT NOT NULL,
    name         VARCHAR(255) NOT NULL,
    relationship VARCHAR(100),
    phone        VARCHAR(50),
    email        VARCHAR(255),
    is_primary   BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_emergency_contacts_record
        FOREIGN KEY (record_id) REFERENCES records(id) ON DELETE CASCADE
);
