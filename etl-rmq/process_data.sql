-- 1. LOC SACH
TRUNCATE dedup_unique;
INSERT INTO dedup_unique(business_key, record_type, representative_source, payload_json)
SELECT business_key, record_type, JSON_EXTRACT(sources, '$[0]'), sample_payload
FROM (
    SELECT record_type, business_key, COUNT(*) AS cnt, JSON_ARRAYAGG(source_name) AS sources, ANY_VALUE(payload_json) AS sample_payload
    FROM staging_records GROUP BY record_type, business_key
) AS grouped WHERE cnt = 1;

-- 2. LOC TRUNG
TRUNCATE dedup_duplicate;
INSERT INTO dedup_duplicate(business_key, record_type, conflict_count, sources, sample_payload)
SELECT business_key, record_type, cnt, sources, sample_payload
FROM (
    SELECT record_type, business_key, COUNT(*) AS cnt, JSON_ARRAYAGG(source_name) AS sources, ANY_VALUE(payload_json) AS sample_payload
    FROM staging_records GROUP BY record_type, business_key
) AS grouped WHERE cnt > 1;

-- 3. NAP BANG DICH
TRUNCATE employees;
INSERT INTO employees (emp_id, name, department, base_salary, allowance)
SELECT JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.emp_id')), JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.name')), JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.department')), JSON_EXTRACT(payload_json, '$.base_salary'), JSON_EXTRACT(payload_json, '$.allowance')
FROM dedup_unique WHERE record_type = 'employees';

TRUNCATE attendance;
INSERT INTO attendance (emp_id, work_date, check_in, check_out, ot_hours)
SELECT JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.emp_id')), JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.work_date')), JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.check_in')), JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.check_out')), JSON_EXTRACT(payload_json, '$.ot_hours')
FROM dedup_unique WHERE record_type = 'attendance';