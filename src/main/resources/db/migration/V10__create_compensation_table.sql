CREATE TABLE compensation (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id         BIGINT NOT NULL,
    base_salary       DECIMAL(12,2) NOT NULL,
    pay_frequency     VARCHAR(20) NOT NULL,
    bonus_target      DECIMAL(5,2),
    stock_options     INT,
    effective_date    DATE NOT NULL,
    overtime_eligible BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_compensation_record
        FOREIGN KEY (record_id) REFERENCES records(id) ON DELETE CASCADE,
    CONSTRAINT uq_compensation_record
        UNIQUE (record_id)
);

CREATE TABLE pay_frequencies (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO pay_frequencies (name) VALUES
    ('annual'), ('monthly'), ('bi-weekly'), ('weekly');
