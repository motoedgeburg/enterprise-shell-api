CREATE TABLE certifications (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id      BIGINT NOT NULL,
    name           VARCHAR(255) NOT NULL,
    issuing_body   VARCHAR(255),
    issue_date     DATE,
    expiry_date    DATE,
    credential_id  VARCHAR(255),

    CONSTRAINT fk_certifications_record
        FOREIGN KEY (record_id) REFERENCES records(id) ON DELETE CASCADE
);
