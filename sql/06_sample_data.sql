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
    '0', 
    'admin', 
    1, 
    NULL
);

INSERT INTO EMPLOYEE (
    employee_id, branch_id, full_name, email, phone, hire_date, status_code, note
) VALUES (
    'EMP001',
    'BR001',
    N'Nguyễn Văn A',
    'nguyen.van.a@pethotel.vn',
    '0123456789',
    SYSTIMESTAMP,
    'WORKING',
    N'Nhân viên lễ tân'
);

INSERT INTO EMPLOYEE (
    employee_id, branch_id, full_name, email, phone, hire_date, status_code, note
) VALUES (
    'EMP002',
    'BR001',
    N'Lê Thị B',
    'le.thi.b@pethotel.vn',
    '0987654321',
    SYSTIMESTAMP,
    'WORKING',
    N'Nhân viên chăm sóc thú cưng'
);

INSERT INTO EMPLOYEE (
    employee_id, branch_id, full_name, email, phone, hire_date, status_code, note
) VALUES (
    'EMP003',
    'BR001',
    N'Phạm Văn C',
    'pham.van.c@pethotel.vn',
    '0912345678',
    SYSTIMESTAMP,
    'WORKING',
    N'Quản lý chi nhánh'
);

insert into employee (
    employee_id, branch_id, full_name, email, phone, hire_date, status_code, note
) values (
    'EMP004', 
    'BR001', 
    N'Nguyễn Thị D', 
    'nguyen.thi.d@pethotel.vn',
    '0901234567',
    SYSTIMESTAMP,
    'WORKING',
    N'CEO'
);

insert into app_user (
    employee_id, password_hash, role_emp, user_name, is_active, last_login
) values (
    'EMP004', 
    'ceo', 
    '4', 
    'ceo', 
    1, 
    NULL
);

insert into app_user (
    employee_id, password_hash, role_emp, user_name, is_active, last_login
) values (
    'EMP001', 
    'letan', 
    '1', 
    'letan', 
    1, 
    NULL
);

insert into app_user (
    employee_id, password_hash, role_emp, user_name, is_active, last_login
) values (
    'EMP002', 
    'chamsoc', 
    '1', 
    'chamsoc', 
    1, 
    NULL
);

insert into app_user (
    employee_id, password_hash, role_emp, user_name, is_active, last_login
) values (
    'EMP003', 
    'quanly', 
    '1', 
    'quanly', 
    1, 
    NULL
);

select * from app_user;

COMMIT;