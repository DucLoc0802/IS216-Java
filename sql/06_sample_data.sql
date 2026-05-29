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
    employee_id, branch_id, full_name, salary, email, phone, hire_date, status_code, note
) VALUES (
    'EMP_ADMIN', 
    'BR001', 
    N'Quản Trị Viên Hệ Thống', 
    30000000,
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
    employee_id, branch_id, full_name, salary, email, phone, hire_date, status_code, note
) VALUES (
    'EMP001',
    'BR001',
    N'Nguyễn Văn A',
    12000000,
    'nguyen.van.a@pethotel.vn',
    '0123456789',
    SYSTIMESTAMP,
    'WORKING',
    N'Nhân viên lễ tân'
);

INSERT INTO EMPLOYEE (
    employee_id, branch_id, full_name, salary, email, phone, hire_date, status_code, note
) VALUES (
    'EMP002',
    'BR001',
    N'Lê Thị B',
    11000000,
    'le.thi.b@pethotel.vn',
    '0987654321',
    SYSTIMESTAMP,
    'WORKING',
    N'Nhân viên chăm sóc thú cưng'
);

INSERT INTO EMPLOYEE (
    employee_id, branch_id, full_name, salary, email, phone, hire_date, status_code, note
) VALUES (
    'EMP003',
    'BR001',
    N'Phạm Văn C',
    18000000,
    'pham.van.c@pethotel.vn',
    '0912345678',
    SYSTIMESTAMP,
    'WORKING',
    N'Quản lý chi nhánh'
);

insert into employee (
    employee_id, branch_id, full_name, salary, email, phone, hire_date, status_code, note
) values (
    'EMP004', 
    'BR001', 
    N'Nguyễn Thị D', 
    40000000,
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

-- =========================================================
-- TYPE_ROOM (Loại phòng & Giá)
-- =========================================================
INSERT INTO type_room (type_room_id, type_name, note, max_pets, max_weight_kg, base_price_per_day, is_active)
VALUES ('TYPE01', 'STANDARD', N'Phòng tiêu chuẩn', 1, 15, 200000, 1);
INSERT INTO type_room (type_room_id, type_name, note, max_pets, max_weight_kg, base_price_per_day, is_active)
VALUES ('TYPE02', 'PREMIUM', N'Phòng VIP đầy đủ tiện nghi', 2, 30, 350000, 1);
INSERT INTO type_room (type_room_id, type_name, note, max_pets, max_weight_kg, base_price_per_day, is_active)
VALUES ('TYPE03', 'SUITE', N'Phòng Suite cao cấp', 3, 50, 500000, 1);

-- =========================================================
-- ROOM (Phòng)
-- =========================================================
INSERT INTO room (room_id, branch_id, type_room_id, room_number, status)
VALUES ('R001', 'BR001', 'TYPE01', '101', 'AVAILABLE');
INSERT INTO room (room_id, branch_id, type_room_id, room_number, status)
VALUES ('R002', 'BR001', 'TYPE01', '102', 'AVAILABLE');
INSERT INTO room (room_id, branch_id, type_room_id, room_number, status)
VALUES ('R003', 'BR001', 'TYPE02', '201', 'AVAILABLE');
INSERT INTO room (room_id, branch_id, type_room_id, room_number, status)
VALUES ('R004', 'BR001', 'TYPE02', '202', 'AVAILABLE');
INSERT INTO room (room_id, branch_id, type_room_id, room_number, status)
VALUES ('R005', 'BR001', 'TYPE03', '301', 'AVAILABLE');

COMMIT;