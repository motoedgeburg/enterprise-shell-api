-- Seed compensation data for all 60 records
-- Salaries vary by role: executives > directors > seniors > mid-level > junior/interns
INSERT INTO compensation (record_id, base_salary, pay_frequency, bonus_target, stock_options, effective_date, overtime_eligible)
SELECT id, base_salary, pay_frequency, bonus_target, stock_options, effective_date, overtime_eligible
FROM (
    SELECT r.id,
        CASE
            WHEN r.job_title LIKE '%CTO%' OR r.job_title LIKE '%VP%' OR r.job_title LIKE '%General Counsel%' THEN 220000.00
            WHEN r.job_title LIKE '%Director%' THEN 165000.00
            WHEN r.job_title LIKE '%Manager%' OR r.job_title LIKE '%Lead%' THEN 140000.00
            WHEN r.job_title LIKE '%Senior%' OR r.job_title LIKE '%Staff%' THEN 130000.00
            WHEN r.job_title LIKE '%Specialist%' OR r.job_title LIKE '%Analyst%' OR r.job_title LIKE '%Designer%' OR r.job_title LIKE '%Counsel%' THEN 105000.00
            WHEN r.job_title LIKE '%Junior%' OR r.job_title LIKE '%Intern%' THEN 70000.00
            ELSE 95000.00
        END AS base_salary,
        'annual' AS pay_frequency,
        CASE
            WHEN r.job_title LIKE '%CTO%' OR r.job_title LIKE '%VP%' THEN 25.00
            WHEN r.job_title LIKE '%Director%' THEN 20.00
            WHEN r.job_title LIKE '%Manager%' OR r.job_title LIKE '%Lead%' THEN 15.00
            WHEN r.job_title LIKE '%Senior%' OR r.job_title LIKE '%Staff%' THEN 12.00
            WHEN r.job_title LIKE '%Junior%' OR r.job_title LIKE '%Intern%' THEN NULL
            ELSE 10.00
        END AS bonus_target,
        CASE
            WHEN r.job_title LIKE '%CTO%' OR r.job_title LIKE '%VP%' THEN 10000
            WHEN r.job_title LIKE '%Director%' THEN 5000
            WHEN r.job_title LIKE '%Manager%' OR r.job_title LIKE '%Senior%' OR r.job_title LIKE '%Staff%' THEN 2000
            WHEN r.job_title LIKE '%Junior%' OR r.job_title LIKE '%Intern%' THEN NULL
            ELSE 1000
        END AS stock_options,
        COALESCE(r.start_date, CURRENT_DATE) AS effective_date,
        CASE
            WHEN r.employment_type IN ('contract', 'intern') THEN 1
            WHEN r.job_title LIKE '%Junior%' THEN 1
            ELSE 0
        END AS overtime_eligible
    FROM records r
) AS comp;
