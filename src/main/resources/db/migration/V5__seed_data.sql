-- Lookup data
INSERT INTO departments (name) VALUES
    ('Engineering'), ('Human Resources'), ('Marketing'), ('Sales'),
    ('Finance'), ('Operations'), ('Legal'), ('Product');

INSERT INTO statuses (name) VALUES
    ('active'), ('inactive'), ('on-leave'), ('terminated');

INSERT INTO employment_types (name) VALUES
    ('full-time'), ('part-time'), ('contract'), ('intern');

INSERT INTO notification_channels (name) VALUES
    ('email'), ('slack'), ('sms'), ('push');

INSERT INTO access_levels (name) VALUES
    ('standard'), ('elevated'), ('admin'), ('restricted');

-- Seed records
INSERT INTO records (name, email, phone, address, date_of_birth, ssn, bio, department, job_title, employment_type, start_date, manager, status, remote_eligible, notifications_enabled, notification_channels, access_level, notes, created_at)
VALUES
    ('Alice Johnson', 'alice.johnson@company.com', '(215) 555-0101', '123 Market St, Philadelphia, PA 19103', '1990-03-15', '123-45-6789', 'Senior software engineer with 10 years of experience in distributed systems and cloud architecture.', 'Engineering', 'Senior Software Engineer', 'full-time', '2019-06-01', 'Jane Smith', 'active', TRUE, TRUE, 'email,slack', 'standard', 'Team lead for the infrastructure squad.', '2024-01-15 10:30:00'),
    ('Bob Martinez', 'bob.martinez@company.com', '(215) 555-0102', '456 Broad St, Philadelphia, PA 19102', '1985-07-22', '234-56-7890', 'HR director specializing in talent acquisition and employee development programs.', 'Human Resources', 'HR Director', 'full-time', '2018-03-15', 'Carol White', 'active', FALSE, TRUE, 'email', 'elevated', 'Oversees all recruitment processes.', '2024-01-15 11:00:00'),
    ('Carol White', 'carol.white@company.com', '(215) 555-0103', '789 Walnut St, Philadelphia, PA 19106', '1978-11-30', '345-67-8901', 'VP of Operations with expertise in process optimization and cross-functional team leadership.', 'Operations', 'VP of Operations', 'full-time', '2015-01-10', 'David Chen', 'active', TRUE, TRUE, 'email,slack,sms', 'admin', 'Executive committee member.', '2024-02-01 09:00:00'),
    ('David Chen', 'david.chen@company.com', '(215) 555-0104', '321 Pine St, Philadelphia, PA 19107', '1982-05-18', '456-78-9012', 'Chief Technology Officer driving digital transformation and engineering excellence.', 'Engineering', 'CTO', 'full-time', '2014-06-01', NULL, 'active', TRUE, TRUE, 'email,slack', 'admin', 'Reports directly to CEO.', '2024-02-01 09:30:00'),
    ('Eva Rodriguez', 'eva.rodriguez@company.com', '(215) 555-0105', '654 Chestnut St, Philadelphia, PA 19103', '1995-09-08', '567-89-0123', 'Marketing specialist focused on digital campaigns and brand strategy.', 'Marketing', 'Marketing Specialist', 'full-time', '2021-09-01', 'Frank Kim', 'active', TRUE, TRUE, 'email,slack', 'standard', 'Leading the Q4 product launch campaign.', '2024-02-15 10:00:00'),
    ('Frank Kim', 'frank.kim@company.com', '(215) 555-0106', '987 Spruce St, Philadelphia, PA 19107', '1980-12-03', '678-90-1234', 'Marketing director with 15 years of experience in B2B SaaS marketing.', 'Marketing', 'Marketing Director', 'full-time', '2017-04-15', 'Carol White', 'active', FALSE, TRUE, 'email', 'elevated', 'Budget owner for marketing department.', '2024-03-01 08:30:00'),
    ('Grace Liu', 'grace.liu@company.com', '(215) 555-0107', '147 Vine St, Philadelphia, PA 19102', '1992-06-25', '789-01-2345', 'Financial analyst specializing in revenue forecasting and budget planning.', 'Finance', 'Senior Financial Analyst', 'full-time', '2020-02-01', 'Bob Martinez', 'on-leave', TRUE, FALSE, '', 'standard', 'Currently on parental leave, returning March 2025.', '2024-03-01 09:00:00'),
    ('Henry Park', 'henry.park@company.com', '(215) 555-0108', '258 Race St, Philadelphia, PA 19106', '1998-01-14', '890-12-3456', 'Junior developer in the engineering team, focusing on frontend development with React.', 'Engineering', 'Junior Developer', 'contract', '2024-01-15', 'Alice Johnson', 'active', TRUE, TRUE, 'email,slack', 'restricted', 'Contract through end of 2025, potential full-time conversion.', '2024-03-15 14:00:00');

-- Emergency contacts
INSERT INTO emergency_contacts (record_id, name, relationship, phone, email, is_primary) VALUES
    (1, 'Michael Johnson', 'Spouse', '(215) 555-0199', 'michael.j@personal.com', TRUE),
    (2, 'Maria Martinez', 'Spouse', '(215) 555-0299', 'maria.m@personal.com', TRUE),
    (2, 'Carlos Martinez', 'Brother', '(215) 555-0298', 'carlos.m@personal.com', FALSE),
    (3, 'Robert White', 'Spouse', '(215) 555-0399', 'robert.w@personal.com', TRUE),
    (4, 'Lisa Chen', 'Spouse', '(215) 555-0499', 'lisa.c@personal.com', TRUE),
    (4, 'Wei Chen', 'Parent', '(215) 555-0498', 'wei.c@personal.com', FALSE),
    (5, 'Ana Rodriguez', 'Mother', '(215) 555-0599', 'ana.r@personal.com', TRUE),
    (6, 'Sarah Kim', 'Spouse', '(215) 555-0699', 'sarah.k@personal.com', TRUE),
    (7, 'James Liu', 'Spouse', '(215) 555-0799', 'james.l@personal.com', TRUE),
    (8, 'Min Park', 'Parent', '(215) 555-0899', 'min.p@personal.com', TRUE);

-- Certifications
INSERT INTO certifications (record_id, name, issuing_body, issue_date, expiry_date, credential_id) VALUES
    (1, 'AWS Solutions Architect', 'Amazon Web Services', '2022-03-15', '2025-03-15', 'AWS-SAP-001234'),
    (1, 'Certified Kubernetes Administrator', 'CNCF', '2023-01-10', '2026-01-10', 'CKA-005678'),
    (2, 'SHRM-SCP', 'SHRM', '2020-06-01', '2026-06-01', 'SHRM-SCP-112233'),
    (3, 'PMP', 'PMI', '2018-09-15', '2027-09-15', 'PMP-445566'),
    (3, 'Six Sigma Black Belt', 'ASQ', '2019-03-01', '2025-03-01', 'SSBB-778899'),
    (4, 'AWS Solutions Architect Professional', 'Amazon Web Services', '2021-11-01', '2024-11-01', 'AWS-SAP-009876'),
    (5, 'Google Analytics Certified', 'Google', '2023-06-15', '2025-06-15', 'GA-334455'),
    (6, 'HubSpot Inbound Marketing', 'HubSpot', '2022-08-01', '2025-08-01', 'HS-667788'),
    (7, 'CFA Level II', 'CFA Institute', '2022-12-01', NULL, 'CFA-990011'),
    (8, 'Meta Front-End Developer', 'Meta', '2024-01-20', '2027-01-20', 'META-FE-223344');
