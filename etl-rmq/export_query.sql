-- 1. XUẤT BẢNG NHÂN VIÊN
SELECT 'Mã NV,Họ Tên,Phòng Ban,Lương CB,Phụ Cấp'; -- Dòng tiêu đề (Header)
SELECT CONCAT_WS(',', emp_id, name, department, base_salary, allowance)
FROM employees;

-- (Dấu phân cách giữa 2 file để script tách ra nếu cần, hoặc để trống)