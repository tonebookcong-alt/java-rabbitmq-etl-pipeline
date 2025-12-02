-- 0. (Tùy chọn) Đưa các dòng LỖI ĐỊNH DẠNG sang bảng báo cáo riêng
-- (Chỉ có tác dụng nếu code Java đã update và gửi status='INVALID')
TRUNCATE invalid_format_records;
INSERT INTO invalid_format_records(source_name, raw_data, error_message)
SELECT source_name, payload_json, error_msg
FROM staging_records
WHERE status = 'INVALID';

-- 1. LỌC SẠCH (Tách bản ghi duy nhất vào dedup_unique)
TRUNCATE dedup_unique;
INSERT INTO dedup_unique(business_key, record_type, representative_source, payload_json)
SELECT 
    business_key, 
    record_type, 
    JSON_EXTRACT(sources, '$[0]'), 
    sample_payload
FROM (
    SELECT 
        record_type, 
        business_key, 
        COUNT(*) AS cnt,
        JSON_ARRAYAGG(source_name) AS sources, 
        ANY_VALUE(payload_json) AS sample_payload
    FROM staging_records 
    -- Chỉ xét các bản ghi HỢP LỆ (đã qua vòng Validate của Java)
    WHERE status = 'VALID'
    GROUP BY record_type, business_key
) AS grouped
-- Chỉ lấy những bản ghi xuất hiện DUY NHẤT 1 lần
WHERE cnt = 1;

-- 2. LỌC TRÙNG (Tách bản ghi trùng vào dedup_duplicate)
TRUNCATE dedup_duplicate;
INSERT INTO dedup_duplicate(business_key, record_type, conflict_count, sources, sample_payload)
SELECT 
    business_key, 
    record_type, 
    cnt, 
    sources, 
    sample_payload
FROM (
    SELECT 
        record_type, 
        business_key, 
        COUNT(*) AS cnt, 
        JSON_ARRAYAGG(source_name) AS sources, 
        ANY_VALUE(payload_json) AS sample_payload
    FROM staging_records 
    WHERE status = 'VALID'
    GROUP BY record_type, business_key
) AS grouped 
-- Lấy những bản ghi xuất hiện TỪ 2 LẦN TRỞ LÊN
WHERE cnt > 1;

-- 3. NẠP BẢNG ĐÍCH (Đưa dữ liệu sạch về đúng nhà)
TRUNCATE employees;
INSERT INTO employees (emp_id, name, department, base_salary, allowance)
SELECT 
    JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.emp_id')), 
    JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.name')), 
    JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.department')), 
    JSON_EXTRACT(payload_json, '$.base_salary'), 
    JSON_EXTRACT(payload_json, '$.allowance')
FROM dedup_unique 
WHERE record_type = 'employees'
-- Thêm kiểm tra nhỏ để đảm bảo JSON có đủ trường quan trọng
AND JSON_CONTAINS_PATH(payload_json, 'one', '$.emp_id');

TRUNCATE attendance;
INSERT INTO attendance (emp_id, work_date, check_in, check_out, ot_hours)
SELECT 
    JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.emp_id')), 
    JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.work_date')), 
    JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.check_in')), 
    JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.check_out')), 
    JSON_EXTRACT(payload_json, '$.ot_hours')
FROM dedup_unique 
WHERE record_type = 'attendance';