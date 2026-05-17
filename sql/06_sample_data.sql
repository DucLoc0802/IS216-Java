-- Sample data demo cho chi nhánh, nhân viên, khách hàng, thú cưng, booking và ghi nhận sức khỏe
-- Dựa trên schema hiện tại: sql/02_tables.sql

-- Xóa dữ liệu cũ nếu đã tồn tại
DELETE FROM pet_health_record WHERE health_record_id IN ('HRD001','HRD002','HRD003') OR pet_id IN ('PET001','PET002','PET003','PET004','PET005','PET006','PET007','PET008','PET009','PET010','PET011','PET012');
DELETE FROM order_details WHERE order_id IN (SELECT order_id FROM orders WHERE booking_id IN (SELECT booking_id FROM booking WHERE booking_id IN ('BKD001','BKD002','BKD003','BKD004','BKD005','BKD006','BKD007','BKD008') OR customer_id IN ('CUS001','CUS002','CUS003','CUS004','CUS005','CUS006','CUS007','CUS008'))) OR booking_room_id IN (SELECT booking_room_id FROM booking_room WHERE booking_id IN (SELECT booking_id FROM booking WHERE booking_id IN ('BKD001','BKD002','BKD003','BKD004','BKD005','BKD006','BKD007','BKD008') OR customer_id IN ('CUS001','CUS002','CUS003','CUS004','CUS005','CUS006','CUS007','CUS008'))) OR booking_service_id IN (SELECT booking_service_id FROM booking_services WHERE booking_id IN (SELECT booking_id FROM booking WHERE booking_id IN ('BKD001','BKD002','BKD003','BKD004','BKD005','BKD006','BKD007','BKD008') OR customer_id IN ('CUS001','CUS002','CUS003','CUS004','CUS005','CUS006','CUS007','CUS008')));
DELETE FROM payments WHERE order_id IN (SELECT order_id FROM orders WHERE booking_id IN (SELECT booking_id FROM booking WHERE booking_id IN ('BKD001','BKD002','BKD003','BKD004','BKD005','BKD006','BKD007','BKD008') OR customer_id IN ('CUS001','CUS002','CUS003','CUS004','CUS005','CUS006','CUS007','CUS008')));
DELETE FROM orders WHERE booking_id IN (SELECT booking_id FROM booking WHERE booking_id IN ('BKD001','BKD002','BKD003','BKD004','BKD005','BKD006','BKD007','BKD008') OR customer_id IN ('CUS001','CUS002','CUS003','CUS004','CUS005','CUS006','CUS007','CUS008'));
DELETE FROM booking_room_pet WHERE pet_id IN ('PET001','PET002','PET003','PET004','PET005','PET006','PET007','PET008','PET009','PET010','PET011','PET012') OR booking_room_id IN (SELECT booking_room_id FROM booking_room WHERE booking_id IN (SELECT booking_id FROM booking WHERE booking_id IN ('BKD001','BKD002','BKD003','BKD004','BKD005','BKD006','BKD007','BKD008') OR customer_id IN ('CUS001','CUS002','CUS003','CUS004','CUS005','CUS006','CUS007','CUS008')));
DELETE FROM booking_services WHERE booking_id IN (SELECT booking_id FROM booking WHERE booking_id IN ('BKD001','BKD002','BKD003','BKD004','BKD005','BKD006','BKD007','BKD008') OR customer_id IN ('CUS001','CUS002','CUS003','CUS004','CUS005','CUS006','CUS007','CUS008')) OR pet_id IN ('PET001','PET002','PET003','PET004','PET005','PET006','PET007','PET008','PET009','PET010','PET011','PET012') OR employee_id IN ('EMP_LT01','EMP_LT02','EMP_LT03','EMP_CS01','EMP_CS02','EMP_QL01','EMP_QL02');
DELETE FROM booking_room WHERE booking_id IN (SELECT booking_id FROM booking WHERE booking_id IN ('BKD001','BKD002','BKD003','BKD004','BKD005','BKD006','BKD007','BKD008') OR customer_id IN ('CUS001','CUS002','CUS003','CUS004','CUS005','CUS006','CUS007','CUS008'));
DELETE FROM booking WHERE booking_id IN ('BKD001','BKD002','BKD003','BKD004','BKD005','BKD006','BKD007','BKD008') OR customer_id IN ('CUS001','CUS002','CUS003','CUS004','CUS005','CUS006','CUS007','CUS008');
DELETE FROM pet WHERE pet_id IN ('PET001','PET002','PET003','PET004','PET005','PET006','PET007','PET008','PET009','PET010','PET011','PET012');
DELETE FROM customer WHERE customer_id IN ('CUS001','CUS002','CUS003','CUS004','CUS005','CUS006','CUS007','CUS008');
DELETE FROM app_user WHERE employee_id IN ('EMP_LT01','EMP_LT02','EMP_LT03','EMP_CS01','EMP_CS02','EMP_QL01','EMP_QL02') OR user_name IN ('letan01','letan02','letan03','chamsoc01','chamsoc02','quanly01','quanly02');
DELETE FROM employee WHERE employee_id IN ('EMP_LT01','EMP_LT02','EMP_LT03','EMP_CS01','EMP_CS02','EMP_QL01','EMP_QL02');
COMMIT;

-- Thêm/cập nhật chi nhánh demo BR001
MERGE INTO branch b USING (SELECT 'BR001' AS branch_id FROM dual) src ON (b.branch_id = src.branch_id) WHEN MATCHED THEN UPDATE SET b.branch_name = N'Chi nhánh Pet Hotel 01', b.phone = '02873000001', b.email = 'br001.demo@pethotel.vn', b.address = N'Thủ Đức, TP.HCM', b.is_active = 1 WHEN NOT MATCHED THEN INSERT (branch_id, branch_name, phone, email, address, is_active) VALUES ('BR001', N'Chi nhánh Pet Hotel 01', '02873000001', 'br001.demo@pethotel.vn', N'Thủ Đức, TP.HCM', 1);

-- Thêm nhân viên lễ tân demo
INSERT INTO employee (employee_id, branch_id, full_name, salary, email, phone, hire_date, status_code, note) VALUES ('EMP_LT01', 'BR001', N'Lễ Tân Demo 01', 8000000, 'letan01@pethotel.vn', '0911000001', SYSTIMESTAMP, 'WORKING', N'Tài khoản demo lễ tân 01');
INSERT INTO employee (employee_id, branch_id, full_name, salary, email, phone, hire_date, status_code, note) VALUES ('EMP_LT02', 'BR001', N'Lễ Tân Demo 02', 8000000, 'letan02@pethotel.vn', '0911000002', SYSTIMESTAMP, 'WORKING', N'Tài khoản demo lễ tân 02');
INSERT INTO employee (employee_id, branch_id, full_name, salary, email, phone, hire_date, status_code, note) VALUES ('EMP_LT03', 'BR001', N'Lễ Tân Demo 03', 8000000, 'letan03@pethotel.vn', '0911000003', SYSTIMESTAMP, 'WORKING', N'Tài khoản demo lễ tân 03');

-- Thêm nhân viên chăm sóc demo
INSERT INTO employee (employee_id, branch_id, full_name, salary, email, phone, hire_date, status_code, note) VALUES ('EMP_CS01', 'BR001', N'Chăm Sóc Thú Cưng 01', 9000000, 'chamsoc01@pethotel.vn', '0912000001', SYSTIMESTAMP, 'WORKING', N'Tài khoản demo nhân viên chăm sóc 01');
INSERT INTO employee (employee_id, branch_id, full_name, salary, email, phone, hire_date, status_code, note) VALUES ('EMP_CS02', 'BR001', N'Chăm Sóc Thú Cưng 02', 9000000, 'chamsoc02@pethotel.vn', '0912000002', SYSTIMESTAMP, 'WORKING', N'Tài khoản demo nhân viên chăm sóc 02');

-- Thêm nhân viên quản lý demo
INSERT INTO employee (employee_id, branch_id, full_name, salary, email, phone, hire_date, status_code, note) VALUES ('EMP_QL01', 'BR001', N'Quản Lý Chi Nhánh 01', 12000000, 'quanly01@pethotel.vn', '0913000001', SYSTIMESTAMP, 'WORKING', N'Tài khoản demo quản lý chi nhánh 01');
INSERT INTO employee (employee_id, branch_id, full_name, salary, email, phone, hire_date, status_code, note) VALUES ('EMP_QL02', 'BR001', N'Quản Lý Chi Nhánh 02', 12000000, 'quanly02@pethotel.vn', '0913000002', SYSTIMESTAMP, 'WORKING', N'Tài khoản demo quản lý chi nhánh 02');

-- Thêm tài khoản đăng nhập demo
-- role_emp = 1: Lễ tân, role_emp = 2: Nhân viên chăm sóc, role_emp = 3: Quản lý chi nhánh
INSERT INTO app_user (employee_id, password_hash, role_emp, user_name, is_active, last_login) VALUES ('EMP_LT01', '123456', 1, 'letan01', 1, NULL);
INSERT INTO app_user (employee_id, password_hash, role_emp, user_name, is_active, last_login) VALUES ('EMP_LT02', '123456', 1, 'letan02', 1, NULL);
INSERT INTO app_user (employee_id, password_hash, role_emp, user_name, is_active, last_login) VALUES ('EMP_LT03', '123456', 1, 'letan03', 1, NULL);
INSERT INTO app_user (employee_id, password_hash, role_emp, user_name, is_active, last_login) VALUES ('EMP_CS01', '123456', 2, 'chamsoc01', 1, NULL);
INSERT INTO app_user (employee_id, password_hash, role_emp, user_name, is_active, last_login) VALUES ('EMP_CS02', '123456', 2, 'chamsoc02', 1, NULL);
INSERT INTO app_user (employee_id, password_hash, role_emp, user_name, is_active, last_login) VALUES ('EMP_QL01', '123456', 3, 'quanly01', 1, NULL);
INSERT INTO app_user (employee_id, password_hash, role_emp, user_name, is_active, last_login) VALUES ('EMP_QL02', '123456', 3, 'quanly02', 1, NULL);

-- Thêm khách hàng demo
INSERT INTO customer (customer_id, full_name, email, cccd, phone, address, note, created_at, updated_at) VALUES ('CUS001', N'Nguyễn Minh Anh', 'minhanh.demo01@pethotel.vn', '079201000001', '0901000001', N'Quận 1, TP.HCM', N'Khách hàng demo có 2 thú cưng', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO customer (customer_id, full_name, email, cccd, phone, address, note, created_at, updated_at) VALUES ('CUS002', N'Trần Quốc Bảo', 'quocbao.demo02@pethotel.vn', '079201000002', '0901000002', N'Quận 3, TP.HCM', N'Khách hàng demo có 1 thú cưng', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO customer (customer_id, full_name, email, cccd, phone, address, note, created_at, updated_at) VALUES ('CUS003', N'Lê Hoàng Chi', 'hoangchi.demo03@pethotel.vn', '079201000003', '0901000003', N'Thủ Đức, TP.HCM', N'Khách hàng demo có 3 thú cưng', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO customer (customer_id, full_name, email, cccd, phone, address, note, created_at, updated_at) VALUES ('CUS004', N'Phạm Gia Hân', 'giahan.demo04@pethotel.vn', '079201000004', '0901000004', N'Bình Thạnh, TP.HCM', N'Khách hàng demo', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO customer (customer_id, full_name, email, cccd, phone, address, note, created_at, updated_at) VALUES ('CUS005', N'Võ Tuấn Kiệt', 'tuankiet.demo05@pethotel.vn', '079201000005', '0901000005', N'Gò Vấp, TP.HCM', N'Khách hàng demo', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO customer (customer_id, full_name, email, cccd, phone, address, note, created_at, updated_at) VALUES ('CUS006', N'Đặng Ngọc Linh', 'ngoclinh.demo06@pethotel.vn', '079201000006', '0901000006', N'Quận 7, TP.HCM', N'Khách hàng demo có 2 thú cưng', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO customer (customer_id, full_name, email, cccd, phone, address, note, created_at, updated_at) VALUES ('CUS007', N'Bùi Thanh Nam', 'thanhnam.demo07@pethotel.vn', '079201000007', '0901000007', N'Phú Nhuận, TP.HCM', N'Khách hàng demo', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO customer (customer_id, full_name, email, cccd, phone, address, note, created_at, updated_at) VALUES ('CUS008', N'Huỳnh Mai Phương', 'maiphuong.demo08@pethotel.vn', '079201000008', '0901000008', N'Tân Bình, TP.HCM', N'Khách hàng demo', SYSTIMESTAMP, SYSTIMESTAMP);

-- Thêm thú cưng demo
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, sex, weight_kg, special_note, created_at, updated_at) VALUES ('PET001', 'CUS001', N'Milo', N'Chó', N'Poodle', 'Male', 5.40, N'Sức khỏe ổn định, ăn uống tốt', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, sex, weight_kg, special_note, created_at, updated_at) VALUES ('PET002', 'CUS001', N'Miu', N'Mèo', N'Anh lông ngắn', 'Female', 4.10, N'Cần theo dõi ăn uống trong ngày đầu', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, sex, weight_kg, special_note, created_at, updated_at) VALUES ('PET003', 'CUS002', N'Lucky', N'Chó', N'Corgi', 'Male', 10.20, N'Hơi nhạy cảm với thức ăn lạ', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, sex, weight_kg, special_note, created_at, updated_at) VALUES ('PET004', 'CUS003', N'Bông', N'Thỏ', N'Holland Lop', 'Female', 1.80, N'Cần chuồng yên tĩnh', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, sex, weight_kg, special_note, created_at, updated_at) VALUES ('PET005', 'CUS003', N'Kem', N'Mèo', N'Ba Tư', 'Female', 3.90, N'Lông dài, cần chải lông mỗi ngày', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, sex, weight_kg, special_note, created_at, updated_at) VALUES ('PET006', 'CUS003', N'Đốm', N'Chó', N'Beagle', 'Male', 9.30, N'Năng động, cần vận động ngoài trời', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, sex, weight_kg, special_note, created_at, updated_at) VALUES ('PET007', 'CUS004', N'Nâu', N'Chó', N'Pug', 'Male', 7.60, N'Dễ thở gấp khi trời nóng', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, sex, weight_kg, special_note, created_at, updated_at) VALUES ('PET008', 'CUS005', N'Sushi', N'Mèo', N'Scottish Fold', 'Female', 3.70, N'Tính hiền, thích khu vực ít tiếng ồn', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, sex, weight_kg, special_note, created_at, updated_at) VALUES ('PET009', 'CUS006', N'Cà Rốt', N'Thỏ', N'Mini Rex', 'Male', 1.60, N'Ưu tiên thức ăn khô và rau sạch', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, sex, weight_kg, special_note, created_at, updated_at) VALUES ('PET010', 'CUS006', N'Max', N'Chó', N'Golden Retriever', 'Male', 24.50, N'Thân thiện, cần phòng rộng', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, sex, weight_kg, special_note, created_at, updated_at) VALUES ('PET011', 'CUS007', N'Simba', N'Mèo', N'Bengal', 'Male', 5.20, N'Rất hiếu động, cần đồ chơi', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, sex, weight_kg, special_note, created_at, updated_at) VALUES ('PET012', 'CUS008', N'Luna', N'Chó', N'Samoyed', 'Female', 18.30, N'Lông dày, cần phòng mát', SYSTIMESTAMP, SYSTIMESTAMP);

-- Thêm booking demo để gắn dữ liệu khách/thú cưng với chi nhánh BR001 và làm FK cho pet_health_record
INSERT INTO booking (booking_id, customer_id, branch_id, checkin_expected_at, checkout_expected_at, status, deposit_amount, special_note, created_at, updated_at) VALUES ('BKD001', 'CUS001', 'BR001', SYSTIMESTAMP - INTERVAL '1' DAY, SYSTIMESTAMP + INTERVAL '2' DAY, 'CHECKED_IN', 300000, N'Booking demo cho CUS001', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO booking (booking_id, customer_id, branch_id, checkin_expected_at, checkout_expected_at, status, deposit_amount, special_note, created_at, updated_at) VALUES ('BKD002', 'CUS002', 'BR001', SYSTIMESTAMP, SYSTIMESTAMP + INTERVAL '2' DAY, 'CONFIRMED', 200000, N'Booking demo cho CUS002', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO booking (booking_id, customer_id, branch_id, checkin_expected_at, checkout_expected_at, status, deposit_amount, special_note, created_at, updated_at) VALUES ('BKD003', 'CUS003', 'BR001', SYSTIMESTAMP - INTERVAL '2' DAY, SYSTIMESTAMP + INTERVAL '3' DAY, 'CHECKED_IN', 500000, N'Booking demo cho CUS003', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO booking (booking_id, customer_id, branch_id, checkin_expected_at, checkout_expected_at, status, deposit_amount, special_note, created_at, updated_at) VALUES ('BKD004', 'CUS004', 'BR001', SYSTIMESTAMP + INTERVAL '1' DAY, SYSTIMESTAMP + INTERVAL '4' DAY, 'PENDING', 0, N'Booking demo cho CUS004', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO booking (booking_id, customer_id, branch_id, checkin_expected_at, checkout_expected_at, status, deposit_amount, special_note, created_at, updated_at) VALUES ('BKD005', 'CUS005', 'BR001', SYSTIMESTAMP, SYSTIMESTAMP + INTERVAL '1' DAY, 'CONFIRMED', 150000, N'Booking demo cho CUS005', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO booking (booking_id, customer_id, branch_id, checkin_expected_at, checkout_expected_at, status, deposit_amount, special_note, created_at, updated_at) VALUES ('BKD006', 'CUS006', 'BR001', SYSTIMESTAMP - INTERVAL '1' DAY, SYSTIMESTAMP + INTERVAL '5' DAY, 'CHECKED_IN', 450000, N'Booking demo cho CUS006', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO booking (booking_id, customer_id, branch_id, checkin_expected_at, checkout_expected_at, status, deposit_amount, special_note, created_at, updated_at) VALUES ('BKD007', 'CUS007', 'BR001', SYSTIMESTAMP - INTERVAL '5' DAY, SYSTIMESTAMP - INTERVAL '1' DAY, 'CHECKED_OUT', 250000, N'Booking demo cho CUS007', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO booking (booking_id, customer_id, branch_id, checkin_expected_at, checkout_expected_at, status, deposit_amount, special_note, created_at, updated_at) VALUES ('BKD008', 'CUS008', 'BR001', SYSTIMESTAMP + INTERVAL '2' DAY, SYSTIMESTAMP + INTERVAL '6' DAY, 'CONFIRMED', 350000, N'Booking demo cho CUS008', SYSTIMESTAMP, SYSTIMESTAMP);

-- Thêm ghi nhận sức khỏe demo
-- Bảng pet_health_record hiện không có cột employee_id/recorded_by, nên không lưu người ghi nhận trong script này
INSERT INTO pet_health_record (health_record_id, pet_id, booking_id, recorded_at, note, status) VALUES ('HRD001', 'PET001', 'BKD001', SYSTIMESTAMP, N'Bình thường: ăn uống tốt, không có triệu chứng bất thường', 1);
INSERT INTO pet_health_record (health_record_id, pet_id, booking_id, recorded_at, note, status) VALUES ('HRD002', 'PET002', 'BKD001', SYSTIMESTAMP, N'Cần theo dõi: ăn ít, hơi mệt trong buổi sáng', 0);
INSERT INTO pet_health_record (health_record_id, pet_id, booking_id, recorded_at, note, status) VALUES ('HRD003', 'PET003', 'BKD002', SYSTIMESTAMP, N'Bất thường nhẹ: ho nhẹ, cần theo dõi thêm', 0);
COMMIT;

-- Kiểm tra dữ liệu khách hàng demo
SELECT customer_id, full_name, phone, email FROM customer WHERE customer_id IN ('CUS001','CUS002','CUS003','CUS004','CUS005','CUS006','CUS007','CUS008') ORDER BY customer_id;

-- Kiểm tra dữ liệu thú cưng demo
SELECT pet_id, pet_name, customer_id FROM pet WHERE pet_id IN ('PET001','PET002','PET003','PET004','PET005','PET006','PET007','PET008','PET009','PET010','PET011','PET012') ORDER BY pet_id;

-- Kiểm tra số thú cưng theo khách hàng
SELECT c.customer_id, c.full_name, COUNT(p.pet_id) AS pet_count FROM customer c LEFT JOIN pet p ON p.customer_id = c.customer_id WHERE c.customer_id IN ('CUS001','CUS002','CUS003','CUS004','CUS005','CUS006','CUS007','CUS008') GROUP BY c.customer_id, c.full_name ORDER BY c.customer_id;

-- Kiểm tra tài khoản demo
SELECT au.employee_id, au.user_name, au.role_emp, e.full_name FROM app_user au JOIN employee e ON au.employee_id = e.employee_id WHERE au.user_name IN ('letan01','letan02','letan03','chamsoc01','chamsoc02','quanly01','quanly02') ORDER BY au.user_name;
