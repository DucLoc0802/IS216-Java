INSERT INTO branch (
    branch_id, branch_name, phone, email, address, is_active
) VALUES (
    'BR001', 
    N'Chi nhánh 1', 
    '0123456789', 
    'hq@pethotel.vn', 
    N'Thủ Đức, TP.HCM', 
    1
);

INSERT INTO employee (
    employee_id, branch_id, full_name, email, phone, hire_date, status_code, note
) VALUES (
    'EMP_ADMIN', 
    'BR001', 
    N'Quản Trị Viên Hệ Thống', 
    'admin@pethotel.vn', 
    '09999999999', 
    SYSTIMESTAMP, 
    'WORKING', 
    N'Tài khoản quản trị viên cấp cao nhất (CEO)'
);

INSERT INTO app_user (
    employee_id, password_hash, role_emp, user_name, is_active, last_login
) VALUES (
    'EMP_ADMIN', 
    'admin', 
    '5', 
    'admin', 
    1, 
    NULL
);

COMMIT;