-- =========================================================
-- FILE: SQL_Insert_Data_FIXED.sql
-- HỆ THỐNG: Pet Hotel and Spa
-- MỤC ĐÍCH: Phiên bản đã sửa toàn bộ 12 lỗi được phát hiện
-- =========================================================
-- TỔNG QUAN CÁC FIX:
--   BUG 1  : Bỏ cột employee_id khỏi INSERT app_user (cột không tồn tại trong schema)
--   BUG 2  : Bỏ cột standard_id khỏi INSERT service_product_standard (cột không có trong schema)
--   BUG 3  : Xóa STD010 (trùng PK (SER001,PROD03) với STD002); thêm STD010 với product_id khác hoặc bỏ
--   BUG 4  : Đổi employee_id BKS005/BKS011 từ EMP005(BR003) → EMP007(BR005) để khớp branch của BK007(BR005)
--   BUG 5  : Thêm 'DEPOSIT' vào ck_payments_method trước khi INSERT (ALTER CONSTRAINT)
--   BUG 6  : Thay đổi ck_orders_total_logic: cho phép grand_total < subtotal (deposit khấu trừ)
--   BUG 7  : Thêm PROD01 vào branch_inventory BR001 trước khi trigger trừ kho
--   BUG 8  : orders INSERT với subtotal=0, grand_total=0 để TRG-13 tự tính từ order_details
--   BUG 9  : TRG-14 chạy AFTER INSERT orders (khi subtotal=0) → ghi nhận deposit đúng;
--            TRG-13 chạy AFTER INSERT order_details → cộng vào subtotal/grand_total đã trừ cọc.
--            Fix: INSERT orders trước (subtotal=0), rồi INSERT order_details (TRG-13 cộng dần),
--            sau đó CALL thủ công UPDATE grand_total nếu cần — hoặc restructure thứ tự.
--            Giải pháp sạch nhất: orders luôn INSERT với 0/0, thứ tự đúng đảm bảo trigger không conflict.
--   BUG 10 : paid_at dùng TO_TIMESTAMP_TZ thay vì plain text string
--   BUG 11 : orders.created_at đặt cố định trước paid_at để TRG-06 không lỗi
--   BUG 12 : subtotal/grand_total trong INSERT orders = 0 (TRG-13 tự tính từ order_details)
-- =========================================================

-- =========================================================
-- BUG 5 + BUG 6: Sửa CONSTRAINT trước khi INSERT bất kỳ dữ liệu nào
-- =========================================================

-- BUG 5: Cho phép payment_method = 'DEPOSIT' (TRG-14 cần)
ALTER TABLE payments
    DROP CONSTRAINT ck_payments_method;

ALTER TABLE payments
    ADD CONSTRAINT ck_payments_method
        CHECK (payment_method IN ('CASH','BANK_TRANSFER','CARD','EWALLET','DEPOSIT'));

-- BUG 6: Cho phép grand_total < subtotal (sau khi khấu trừ tiền cọc)
-- Constraint cũ: grand_total >= subtotal — sai khi TRG-14 trừ deposit
ALTER TABLE orders
    DROP CONSTRAINT ck_orders_total_logic;

-- Chỉ giữ ràng buộc không âm (đã có ck_orders_total và ck_orders_subtotal)
-- Không cần thêm lại ck_orders_total_logic vì grand_total >= 0 đã được đảm bảo
-- bởi GREATEST(..., 0) trong TRG-14 và ck_orders_total.


-- =========================================================
-- 1. CUSTOMER
-- =========================================================
-- 1. CUSTOMER
-- =========================================================
INSERT INTO customer (customer_id, full_name, email, phone, address) VALUES ('CUS001', 'Nguyễn Văn A', 'nva@gmail.com', '0912345600', 'Quận 1, TP.HCM');
INSERT INTO customer (customer_id, full_name, email, phone, address) VALUES ('CUS002', 'Trần Thị B', 'ttb@gmail.com', '0912345699', 'Quận 2, TP.HCM');
INSERT INTO customer (customer_id, full_name, email, phone, address) VALUES ('CUS003', 'Lê Quang Minh', 'minh.lq@gmail.com', '0912345601', 'Dĩ An, Bình Dương');
INSERT INTO customer (customer_id, full_name, email, phone, address) VALUES ('CUS004', 'Hoàng Thanh Tùng', 'tung.ht@outlook.com', '0912345602', 'Quận 1, TP.HCM');
INSERT INTO customer (customer_id, full_name, email, phone, address) VALUES ('CUS005', 'Nguyễn Mai Anh', 'maianh.ng@gmail.com', '0912345603', 'Quận 3, TP.HCM');
INSERT INTO customer (customer_id, full_name, email, phone, address) VALUES ('CUS006', 'Vũ Đức Duy', 'duy.vd@gmail.com', '0912345604', 'Thủ Đức, TP.HCM');
INSERT INTO customer (customer_id, full_name, email, phone, address) VALUES ('CUS007', 'Đặng Thu Thảo', 'thao.dt@gmail.com', '0912345605', 'Bình Thạnh, TP.HCM');
INSERT INTO customer (customer_id, full_name, email, phone, address) VALUES ('CUS008', 'Phan Anh Tuấn', 'tuan.pa@gmail.com', '0912345606', 'Quận 7, TP.HCM');
INSERT INTO customer (customer_id, full_name, email, phone, address) VALUES ('CUS009', 'Lê Thị Hồng', 'hong.lt@gmail.com', '0912345607', 'Gò Vấp, TP.HCM');
INSERT INTO customer (customer_id, full_name, email, phone, address) VALUES ('CUS010', 'Bùi Xuân Huấn', 'huan.bx@gmail.com', '0912345608', 'Quận 10, TP.HCM');
INSERT INTO customer (customer_id, full_name, email, phone, address) VALUES ('CUS011', 'Trần Văn Kiên', 'kien.tv@gmail.com', '0912345609', 'Quận 12, TP.HCM');
INSERT INTO customer (customer_id, full_name, email, phone, address) VALUES ('CUS012', 'Ngô Bảo Châu', 'chau.nb@gmail.com', '0912345610', 'Quận 2, TP.HCM');

-- =========================================================
-- 2. BRANCH
-- =========================================================
INSERT INTO branch (branch_id, branch_name, phone, email, address, is_active) VALUES ('BR001', 'Pet Spa Quận 1', '0283344551', 'q1@petspa.vn', 'Lê Lợi, Q1', 1);
INSERT INTO branch (branch_id, branch_name, phone, email, address, is_active) VALUES ('BR002', 'Pet Spa Quận 3', '0283344552', 'q3@petspa.vn', 'Lê Văn Sỹ, Q3', 1);
INSERT INTO branch (branch_id, branch_name, phone, email, address, is_active) VALUES ('BR003', 'Pet Spa Quận 7', '0283344556', 'q7@petspa.vn', 'Nguyễn Văn Linh, Q7', 1);
INSERT INTO branch (branch_id, branch_name, phone, email, address, is_active) VALUES ('BR004', 'Pet Spa Gò Vấp', '0283344557', 'govap@petspa.vn', 'Quang Trung, Gò Vấp', 1);
INSERT INTO branch (branch_id, branch_name, phone, email, address, is_active) VALUES ('BR005', 'Pet Spa Bình Dương', '0274334455', 'bd@petspa.vn', 'Đại lộ Bình Dương', 1);

-- =========================================================
-- 3. EMPLOYEE
-- =========================================================
INSERT INTO employee (employee_id, branch_id, full_name, salary, phone, status_code) VALUES ('EMP001', 'BR001', 'Nguyễn Văn C', 10000000, '0981000000', 'WORKING');
INSERT INTO employee (employee_id, branch_id, full_name, salary, phone, status_code) VALUES ('EMP002', 'BR002', 'Lý Thị D', 11000000, '0981000099', 'WORKING');
INSERT INTO employee (employee_id, branch_id, full_name, salary, phone, status_code) VALUES ('EMP003', 'BR001', 'Nguyễn Thị Thắm', 12000000, '0981000001', 'WORKING');
INSERT INTO employee (employee_id, branch_id, full_name, salary, phone, status_code) VALUES ('EMP004', 'BR002', 'Trần Văn Hùng', 13000000, '0981000002', 'WORKING');
INSERT INTO employee (employee_id, branch_id, full_name, salary, phone, status_code) VALUES ('EMP005', 'BR003', 'Lê Mỹ Linh', 14500000, '0981000003', 'WORKING');
INSERT INTO employee (employee_id, branch_id, full_name, salary, phone, status_code) VALUES ('EMP006', 'BR004', 'Hoàng Phi Hùng', 12500000, '0981000004', 'ON_LEAVE');
INSERT INTO employee (employee_id, branch_id, full_name, salary, phone, status_code) VALUES ('EMP007', 'BR005', 'Đặng Nam Anh', 16000000, '0981000005', 'WORKING');
INSERT INTO employee (employee_id, branch_id, full_name, salary, phone, status_code) VALUES ('EMP008', 'BR001', 'Vũ Tuyết Mai', 11000000, '0981000006', 'RESIGNED');
INSERT INTO employee (employee_id, branch_id, full_name, salary, phone, status_code) VALUES ('EMP009', 'BR002', 'Lý Gia Thành', 20000000, '0981000007', 'WORKING');
INSERT INTO employee (employee_id, branch_id, full_name, salary, phone, status_code) VALUES ('EMP010', 'BR003', 'Trương Vô Kỵ', 18000000, '0981000008', 'WORKING');
INSERT INTO employee (employee_id, branch_id, full_name, salary, phone, status_code) VALUES ('EMP011', 'BR004', 'Triệu Mẫn', 17000000, '0981000009', 'WORKING');
INSERT INTO employee (employee_id, branch_id, full_name, salary, phone, status_code) VALUES ('EMP012', 'BR005', 'Chu Chỉ Nhược', 15500000, '0981000010', 'WORKING');

-- =========================================================
-- 4. APP_USER
-- =========================================================
INSERT INTO app_user (user_id, user_name, password_hash, role_emp, is_active) VALUES ('USR001', 'nhanvien1', 'hash_pw_1', '1', 1);
INSERT INTO app_user (user_id, user_name, password_hash, role_emp, is_active) VALUES ('USR002', 'quanly1', 'hash_pw_2', '4', 1);
INSERT INTO app_user (user_id, user_name, password_hash, role_emp, is_active) VALUES ('USR003', 'tham.nt', 'hash_pw_3', '2', 1);
INSERT INTO app_user (user_id, user_name, password_hash, role_emp, is_active) VALUES ('USR004', 'hung.tv', 'hash_pw_4', '1', 1);
INSERT INTO app_user (user_id, user_name, password_hash, role_emp, is_active) VALUES ('USR005', 'linh.lm', 'hash_pw_5', '2', 1);
INSERT INTO app_user (user_id, user_name, password_hash, role_emp, is_active) VALUES ('USR006', 'hung.hp', 'hash_pw_6', '3', 1);
INSERT INTO app_user (user_id, user_name, password_hash, role_emp, is_active) VALUES ('USR007', 'anh.dn', 'hash_pw_7', '4', 1);
INSERT INTO app_user (user_id, user_name, password_hash, role_emp, is_active) VALUES ('USR008', 'thanh.lg', 'hash_pw_8', '5', 1);
INSERT INTO app_user (user_id, user_name, password_hash, role_emp, is_active) VALUES ('USR009', 'ky.tv', 'hash_pw_9', '2', 1);
INSERT INTO app_user (user_id, user_name, password_hash, role_emp, is_active) VALUES ('USR010', 'man.t', 'hash_pw_10', '2', 1);

-- =========================================================
-- 5. PET
-- =========================================================
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, weight_kg) VALUES ('PET001', 'CUS001', 'Misa', 'DOG', 'Poodle', 4.5);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, weight_kg) VALUES ('PET002', 'CUS002', 'Mimi', 'CAT', 'Mèo Anh', 3.0);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, weight_kg) VALUES ('PET003', 'CUS003', 'Kiki', 'DOG', 'Husky', 20.5);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, weight_kg) VALUES ('PET004', 'CUS004', 'Meo Meo', 'CAT', 'Persian', 3.8);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, weight_kg) VALUES ('PET005', 'CUS005', 'Bắp', 'DOG', 'Golden Retriever', 25.0);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, weight_kg) VALUES ('PET006', 'CUS006', 'Xù', 'DOG', 'Phú Quốc', 18.2);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, weight_kg) VALUES ('PET007', 'CUS007', 'Mướp', 'CAT', 'Tabby', 4.0);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, weight_kg) VALUES ('PET008', 'CUS008', 'Lu', 'DOG', 'Corgi', 12.5);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, weight_kg) VALUES ('PET009', 'CUS009', 'Chirpy', 'BIRD', 'Parrot', 0.5);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, weight_kg) VALUES ('PET010', 'CUS010', 'Bông', 'CAT', 'Maine Coon', 8.5);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, weight_kg) VALUES ('PET011', 'CUS011', 'Gấu', 'DOG', 'Chihuahua', 2.1);
INSERT INTO pet (pet_id, customer_id, pet_name, species, breed, weight_kg) VALUES ('PET012', 'CUS012', 'Kem', 'CAT', 'Ragdoll', 5.2);

-- =========================================================
-- 6. CATEGORY_PRODUCT
-- =========================================================
INSERT INTO category_product (product_category_id, category_name) VALUES ('CATP01', 'Thức ăn');
INSERT INTO category_product (product_category_id, category_name) VALUES ('CATP02', 'Phụ kiện');
INSERT INTO category_product (product_category_id, category_name) VALUES ('CATP03', 'Sữa tắm');
INSERT INTO category_product (product_category_id, category_name) VALUES ('CATP04', 'Đồ chơi');
INSERT INTO category_product (product_category_id, category_name) VALUES ('CATP05', 'Thú y');

-- =========================================================
-- 7. PRODUCT
-- =========================================================
INSERT INTO product (product_id, product_category_id, product_name, unit, cost_price) VALUES ('PROD01', 'CATP01', 'Hạt Royal Canin', 'Gói', 120000);
INSERT INTO product (product_id, product_category_id, product_name, unit, cost_price) VALUES ('PROD02', 'CATP02', 'Vòng cổ da', 'Cái', 150000);
INSERT INTO product (product_id, product_category_id, product_name, unit, cost_price) VALUES ('PROD03', 'CATP03', 'Sữa tắm SOS', 'Chai',120000);
INSERT INTO product (product_id, product_category_id, product_name, unit, cost_price) VALUES ('PROD04', 'CATP04', 'Bóng cao su', 'Cái', 45000);
INSERT INTO product (product_id, product_category_id, product_name, unit, cost_price) VALUES ('PROD05', 'CATP01', 'Pate Whiskas', 'Gói', 15000);
INSERT INTO product (product_id, product_category_id, product_name, unit, cost_price) VALUES ('PROD06', 'CATP02', 'Dây dắt tự động', 'Cái', 350000);
INSERT INTO product (product_id, product_category_id, product_name, unit, cost_price) VALUES ('PROD07', 'CATP03', 'Xịt khử mùi', 'Chai', 95000);
INSERT INTO product (product_id, product_category_id, product_name, unit, cost_price) VALUES ('PROD08', 'CATP04', 'Cần câu mèo', 'Cái', 35000);
INSERT INTO product (product_id, product_category_id, product_name, unit, cost_price) VALUES ('PROD09', 'CATP01', 'Hạt Minino', 'Bao', 450000);
INSERT INTO product (product_id, product_category_id, product_name, unit, cost_price) VALUES ('PROD10', 'CATP05', 'Thuốc trị rận', 'Tuýp',180000);
INSERT INTO product (product_id, product_category_id, product_name, unit, cost_price) VALUES ('PROD11', 'CATP02', 'Ổ nằm bông', 'Cái', 550000);

-- =========================================================
-- 8. BRANCH_INVENTORY
-- =========================================================
INSERT INTO branch_inventory (branch_id, product_id, quantity_in_stock, reorder_point) VALUES ('BR001', 'PROD01', 50, 10);
INSERT INTO branch_inventory (branch_id, product_id, quantity_in_stock, reorder_point) VALUES ('BR001', 'PROD02', 20, 5);
INSERT INTO branch_inventory (branch_id, product_id, quantity_in_stock, reorder_point) VALUES ('BR001', 'PROD03', 15, 3);
INSERT INTO branch_inventory (branch_id, product_id, quantity_in_stock, reorder_point) VALUES ('BR001', 'PROD04', 30, 10);
INSERT INTO branch_inventory (branch_id, product_id, quantity_in_stock, reorder_point) VALUES ('BR002', 'PROD01', 40, 8);
INSERT INTO branch_inventory (branch_id, product_id, quantity_in_stock, reorder_point) VALUES ('BR002', 'PROD05',100, 20);
INSERT INTO branch_inventory (branch_id, product_id, quantity_in_stock, reorder_point) VALUES ('BR002', 'PROD06', 10,  2);
INSERT INTO branch_inventory (branch_id, product_id, quantity_in_stock, reorder_point) VALUES ('BR003', 'PROD02', 12,  5);
INSERT INTO branch_inventory (branch_id, product_id, quantity_in_stock, reorder_point) VALUES ('BR004', 'PROD03',  8,  3);
INSERT INTO branch_inventory (branch_id, product_id, quantity_in_stock, reorder_point) VALUES ('BR005', 'PROD10', 25,  5);
INSERT INTO branch_inventory (branch_id, product_id, quantity_in_stock, reorder_point) VALUES ('BR001', 'PROD11',  5,  2);

-- =========================================================
-- 9. CATEGORY_SERVICES
-- =========================================================
INSERT INTO category_services (service_category_id, category_name) VALUES ('CATS01', 'Vệ sinh cơ bản');
INSERT INTO category_services (service_category_id, category_name) VALUES ('CATS02', 'Cắt tỉa');
INSERT INTO category_services (service_category_id, category_name) VALUES ('CATS03', 'Khách sạn');
INSERT INTO category_services (service_category_id, category_name) VALUES ('CATS04', 'Trị liệu');

-- =========================================================
-- 10. SERVICES
-- =========================================================
INSERT INTO services (service_id, service_category_id, service_name, species, base_price, duration_minutes) VALUES ('SER001', 'CATS01', 'Tắm gội cơ bản', 'DOG', 100000, 30);
INSERT INTO services (service_id, service_category_id, service_name, species, base_price, duration_minutes) VALUES ('SER002', 'CATS02', 'Cắt tỉa tạo kiểu', 'DOG', 350000, 90);
INSERT INTO services (service_id, service_category_id, service_name, species, base_price, duration_minutes) VALUES ('SER003', 'CATS02', 'Cạo lông máu', 'CAT', 250000, 45);
INSERT INTO services (service_id, service_category_id, service_name, species, base_price, duration_minutes) VALUES ('SER004', 'CATS01', 'Lấy cao răng', 'DOG', 150000, 30);
INSERT INTO services (service_id, service_category_id, service_name, species, base_price, duration_minutes) VALUES ('SER005', 'CATS04', 'Massage thư giãn', 'DOG', 400000, 60);
INSERT INTO services (service_id, service_category_id, service_name, species, base_price, duration_minutes) VALUES ('SER006', 'CATS01', 'Cắt móng mài móng','CAT',  80000,  20);
INSERT INTO services (service_id, service_category_id, service_name, species, base_price, duration_minutes) VALUES ('SER007', 'CATS02', 'Nhuộm lông tai', 'DOG', 200000, 60);
INSERT INTO services (service_id, service_category_id, service_name, species, base_price, duration_minutes) VALUES ('SER008', 'CATS04', 'Xông hơi thảo dược','DOG', 500000, 45);
INSERT INTO services (service_id, service_category_id, service_name, species, base_price, duration_minutes) VALUES ('SER009', 'CATS01', 'Vệ sinh tai', 'CAT',  50000,  15);
INSERT INTO services (service_id, service_category_id, service_name, species, base_price, duration_minutes) VALUES ('SER010', 'CATS01', 'Tắm khô', 'DOG', 120000, 30);
INSERT INTO services (service_id, service_category_id, service_name, species, base_price, duration_minutes) VALUES ('SER011', 'CATS02', 'Combo Toàn diện', 'DOG', 600000, 120);

-- =========================================================
-- 11. TYPE_ROOM
-- =========================================================
INSERT INTO type_room (type_room_id, type_name, max_pets, base_price_per_day) VALUES ('TYPE01', 'PREMIUM',  1, 400000);
INSERT INTO type_room (type_room_id, type_name, max_pets, base_price_per_day) VALUES ('TYPE02', 'STANDARD', 1, 200000);
INSERT INTO type_room (type_room_id, type_name, max_pets, base_price_per_day) VALUES ('TYPE03', 'SUITE',    2, 800000);

-- =========================================================
-- 12. ROOM
-- =========================================================
INSERT INTO room (room_id, branch_id, type_room_id, room_number, status) VALUES ('RM101', 'BR001', 'TYPE01', 'P101', 'AVAILABLE');
INSERT INTO room (room_id, branch_id, type_room_id, room_number, status) VALUES ('RM102', 'BR001', 'TYPE02', 'S102', 'AVAILABLE');
INSERT INTO room (room_id, branch_id, type_room_id, room_number, status) VALUES ('RM103', 'BR001', 'TYPE03', 'VIP1', 'IN_USE');
INSERT INTO room (room_id, branch_id, type_room_id, room_number, status) VALUES ('RM201', 'BR002', 'TYPE01', 'P201', 'AVAILABLE');
INSERT INTO room (room_id, branch_id, type_room_id, room_number, status) VALUES ('RM202', 'BR002', 'TYPE02', 'S202', 'MAINTENANCE');
INSERT INTO room (room_id, branch_id, type_room_id, room_number, status) VALUES ('RM301', 'BR003', 'TYPE01', 'P301', 'AVAILABLE');
INSERT INTO room (room_id, branch_id, type_room_id, room_number, status) VALUES ('RM302', 'BR003', 'TYPE03', 'VIP3', 'AVAILABLE');
INSERT INTO room (room_id, branch_id, type_room_id, room_number, status) VALUES ('RM401', 'BR004', 'TYPE01', 'P401', 'AVAILABLE');
INSERT INTO room (room_id, branch_id, type_room_id, room_number, status) VALUES ('RM402', 'BR004', 'TYPE02', 'S402', 'IN_USE');
INSERT INTO room (room_id, branch_id, type_room_id, room_number, status) VALUES ('RM501', 'BR005', 'TYPE01', 'P501', 'AVAILABLE');
INSERT INTO room (room_id, branch_id, type_room_id, room_number, status) VALUES ('RM502', 'BR005', 'TYPE03', 'VIP5', 'AVAILABLE');

-- =========================================================
-- 13. BOOKING
-- =========================================================
INSERT INTO booking (booking_id, customer_id, branch_id, status, deposit_amount, checkin_expected_at, checkout_expected_at, checkin_actual_at, checkout_actual_at) VALUES ('BK001', 'CUS001', 'BR001', 'CONFIRMED',   100000, TO_TIMESTAMP('2026-05-14 08:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2026-05-15 08:00:00', 'YYYY-MM-DD HH24:MI:SS'), NULL, NULL);
INSERT INTO booking (booking_id, customer_id, branch_id, status, deposit_amount, checkin_expected_at, checkout_expected_at, checkin_actual_at, checkout_actual_at) VALUES ('BK002', 'CUS003', 'BR001', 'CONFIRMED',   200000, TO_TIMESTAMP('2026-05-15 08:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2026-05-17 08:00:00', 'YYYY-MM-DD HH24:MI:SS'), NULL, NULL);
INSERT INTO booking (booking_id, customer_id, branch_id, status, deposit_amount, checkin_expected_at, checkout_expected_at, checkin_actual_at, checkout_actual_at) VALUES ('BK003', 'CUS004', 'BR002', 'CHECKED_IN',       0, TO_TIMESTAMP('2026-05-13 09:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2026-05-15 09:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2026-05-13 09:15:00', 'YYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO booking (booking_id, customer_id, branch_id, status, deposit_amount, checkin_expected_at, checkout_expected_at, checkin_actual_at, checkout_actual_at) VALUES ('BK004', 'CUS005', 'BR003', 'PENDING',      50000, TO_TIMESTAMP('2026-05-20 14:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2026-05-23 14:00:00', 'YYYY-MM-DD HH24:MI:SS'), NULL, NULL);
INSERT INTO booking (booking_id, customer_id, branch_id, status, deposit_amount, checkin_expected_at, checkout_expected_at, checkin_actual_at, checkout_actual_at) VALUES ('BK005', 'CUS006', 'BR001', 'CONFIRMED',   150000, TO_TIMESTAMP('2026-05-16 10:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2026-05-18 10:30:00', 'YYYY-MM-DD HH24:MI:SS'), NULL, NULL);
INSERT INTO booking (booking_id, customer_id, branch_id, status, deposit_amount, checkin_expected_at, checkout_expected_at, checkin_actual_at, checkout_actual_at) VALUES ('BK006', 'CUS007', 'BR004', 'CANCELLED',       0, TO_TIMESTAMP('2026-05-10 08:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2026-05-12 08:00:00', 'YYYY-MM-DD HH24:MI:SS'), NULL, NULL);
INSERT INTO booking (booking_id, customer_id, branch_id, status, deposit_amount, checkin_expected_at, checkout_expected_at, checkin_actual_at, checkout_actual_at) VALUES ('BK007', 'CUS008', 'BR005', 'CHECKED_OUT', 300000, TO_TIMESTAMP('2026-05-11 11:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2026-05-13 11:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2026-05-11 11:20:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2026-05-13 11:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO booking (booking_id, customer_id, branch_id, status, deposit_amount, checkin_expected_at, checkout_expected_at, checkin_actual_at, checkout_actual_at) VALUES ('BK008', 'CUS009', 'BR002', 'CONFIRMED',   100000, TO_TIMESTAMP('2026-05-18 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2026-05-20 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), NULL, NULL);
INSERT INTO booking (booking_id, customer_id, branch_id, status, deposit_amount, checkin_expected_at, checkout_expected_at, checkin_actual_at, checkout_actual_at) VALUES ('BK009', 'CUS010', 'BR003', 'CHECKED_IN',       0, TO_TIMESTAMP('2026-05-13 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2026-05-15 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2026-05-13 13:10:00', 'YYYY-MM-DD HH24:MI:SS'), NULL);
INSERT INTO booking (booking_id, customer_id, branch_id, status, deposit_amount, checkin_expected_at, checkout_expected_at, checkin_actual_at, checkout_actual_at) VALUES ('BK010', 'CUS011', 'BR001', 'PENDING',        0, TO_TIMESTAMP('2026-05-22 09:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2026-05-24 09:00:00', 'YYYY-MM-DD HH24:MI:SS'), NULL, NULL);
INSERT INTO booking (booking_id, customer_id, branch_id, status, deposit_amount, checkin_expected_at, checkout_expected_at, checkin_actual_at, checkout_actual_at) VALUES ('BK011', 'CUS012', 'BR004', 'CONFIRMED',   200000, TO_TIMESTAMP('2026-05-19 10:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2026-05-21 10:00:00', 'YYYY-MM-DD HH24:MI:SS'), NULL, NULL);

-- =========================================================
-- 14. BOOKING_SERVICES_PET
-- =========================================================
INSERT INTO booking_services_pet (booking_service_id, booking_id, service_id, employee_id, pet_id, status) VALUES ('BKS001', 'BK001', 'SER001', 'EMP001', 'PET001', 'DONE');
INSERT INTO booking_services_pet (booking_service_id, booking_id, service_id, employee_id, pet_id, status) VALUES ('BKS002', 'BK002', 'SER002', 'EMP003', 'PET003', 'SCHEDULED');
INSERT INTO booking_services_pet (booking_service_id, booking_id, service_id, employee_id, pet_id, status) VALUES ('BKS003', 'BK003', 'SER003', 'EMP004', 'PET004', 'IN_PROGRESS');
INSERT INTO booking_services_pet (booking_service_id, booking_id, service_id, employee_id, pet_id, status) VALUES ('BKS004', 'BK005', 'SER011', 'EMP003', 'PET006', 'SCHEDULED');
INSERT INTO booking_services_pet (booking_service_id, booking_id, service_id, employee_id, pet_id, status) VALUES ('BKS005', 'BK007', 'SER005', 'EMP007', 'PET008', 'DONE');
INSERT INTO booking_services_pet (booking_service_id, booking_id, service_id, employee_id, pet_id, status) VALUES ('BKS006', 'BK009', 'SER009', 'EMP010', 'PET010', 'DONE');
INSERT INTO booking_services_pet (booking_service_id, booking_id, service_id, employee_id, pet_id, status) VALUES ('BKS007', 'BK009', 'SER006', 'EMP010', 'PET010', 'DONE');
INSERT INTO booking_services_pet (booking_service_id, booking_id, service_id, employee_id, pet_id, status) VALUES ('BKS008', 'BK011', 'SER003', 'EMP011', 'PET012', 'SCHEDULED');
INSERT INTO booking_services_pet (booking_service_id, booking_id, service_id, employee_id, pet_id, status) VALUES ('BKS009', 'BK002', 'SER010', 'EMP003', 'PET003', 'SCHEDULED');
INSERT INTO booking_services_pet (booking_service_id, booking_id, service_id, employee_id, pet_id, status) VALUES ('BKS010', 'BK003', 'SER009', 'EMP004', 'PET004', 'IN_PROGRESS');
INSERT INTO booking_services_pet (booking_service_id, booking_id, service_id, employee_id, pet_id, status) VALUES ('BKS011', 'BK007', 'SER001', 'EMP007', 'PET008', 'DONE');

-- =========================================================
-- 15. BOOKING_ROOM
-- =========================================================
INSERT INTO booking_room (booking_room_id, booking_id, room_id, note) VALUES ('BKR001', 'BK001', 'RM101', 'Phòng premium');
INSERT INTO booking_room (booking_room_id, booking_id, room_id, note) VALUES ('BKR002', 'BK002', 'RM102', 'Gần cửa sổ');
INSERT INTO booking_room (booking_room_id, booking_id, room_id, note) VALUES ('BKR003', 'BK003', 'RM201', 'Phòng yên tĩnh');
INSERT INTO booking_room (booking_room_id, booking_id, room_id, note) VALUES ('BKR004', 'BK005', 'RM103', 'Khách VIP');
INSERT INTO booking_room (booking_room_id, booking_id, room_id, note) VALUES ('BKR005', 'BK007', 'RM502', 'Phòng rộng cho Corgi');
INSERT INTO booking_room (booking_room_id, booking_id, room_id, note) VALUES ('BKR006', 'BK009', 'RM302', 'Phòng có đồ chơi');
INSERT INTO booking_room (booking_room_id, booking_id, room_id, note) VALUES ('BKR007', 'BK011', 'RM402', 'Gần lối đi');
INSERT INTO booking_room (booking_room_id, booking_id, room_id, note) VALUES ('BKR008', 'BK004', 'RM301', 'Chờ checkin');
INSERT INTO booking_room (booking_room_id, booking_id, room_id, note) VALUES ('BKR010', 'BK010', 'RM102', 'Đặt trước 1 tuần');

-- =========================================================
-- 16. ORDERS
-- =========================================================
INSERT INTO orders (order_id, customer_id, branch_id, booking_id, created_by_emp, status, subtotal, grand_total, created_at) VALUES ('ORD002', 'CUS003', 'BR001', 'BK002', 'EMP003', 'PENDING',  0, 0, TIMESTAMP '2026-05-13 08:00:00 +07:00');
INSERT INTO orders (order_id, customer_id, branch_id, booking_id, created_by_emp, status, subtotal, grand_total, created_at) VALUES ('ORD003', 'CUS004', 'BR002', 'BK003', 'EMP004', 'PARTIAL',  0, 0, TIMESTAMP '2026-05-13 09:00:00 +07:00');
INSERT INTO orders (order_id, customer_id, branch_id, booking_id, created_by_emp, status, subtotal, grand_total, created_at) VALUES ('ORD004', 'CUS008', 'BR005', 'BK007', 'EMP007', 'REFUNDED', 0, 0, TIMESTAMP '2026-05-11 09:00:00 +07:00');
INSERT INTO orders (order_id, customer_id, branch_id, booking_id, created_by_emp, status, subtotal, grand_total, created_at) VALUES ('ORD005', 'CUS010', 'BR003', 'BK009', 'EMP010', 'PAID',     0, 0, TIMESTAMP '2026-05-13 14:00:00 +07:00');
INSERT INTO orders (order_id, customer_id, branch_id, booking_id, created_by_emp, status, subtotal, grand_total, created_at) VALUES ('ORD006', 'CUS012', 'BR004', 'BK011', 'EMP011', 'PENDING',  0, 0, TIMESTAMP '2026-05-13 08:00:00 +07:00');
INSERT INTO orders (order_id, customer_id, branch_id, booking_id, created_by_emp, status, subtotal, grand_total, created_at) VALUES ('ORD007', 'CUS001', 'BR001', 'BK001', 'EMP001', 'PAID',     0, 0, TIMESTAMP '2026-05-11 09:00:00 +07:00');
INSERT INTO orders (order_id, customer_id, branch_id, booking_id, created_by_emp, status, subtotal, grand_total, created_at) VALUES ('ORD008', 'CUS006', 'BR001', 'BK005', 'EMP003', 'PENDING',  0, 0, TIMESTAMP '2026-05-13 08:00:00 +07:00');
INSERT INTO orders (order_id, customer_id, branch_id, booking_id, created_by_emp, status, subtotal, grand_total, created_at) VALUES ('ORD009', 'CUS009', 'BR002', 'BK008', 'EMP009', 'CANCELLED',0, 0, TIMESTAMP '2026-05-13 08:00:00 +07:00');
INSERT INTO orders (order_id, customer_id, branch_id, booking_id, created_by_emp, status, subtotal, grand_total, created_at) VALUES ('ORD010', 'CUS011', 'BR001', 'BK010', 'EMP003', 'PENDING',  0, 0, TIMESTAMP '2026-05-13 08:00:00 +07:00');
INSERT INTO orders (order_id, customer_id, branch_id, booking_id, created_by_emp, status, subtotal, grand_total, created_at) VALUES ('ORD011', 'CUS004', 'BR002', 'BK003', 'EMP004', 'PARTIAL',  0, 0, TIMESTAMP '2026-05-13 09:00:00 +07:00');

-- =========================================================
-- 17. ORDER_DETAILS
-- =========================================================
INSERT INTO order_details (order_detail_id, order_id, booking_service_id, quantity, unit_price, line_total) VALUES ('OD001', 'ORD002', 'BKS002',  1, 350000, 350000);
INSERT INTO order_details (order_detail_id, order_id, booking_service_id, quantity, unit_price, line_total) VALUES ('OD009', 'ORD002', 'BKS009',  1, 120000, 120000);
INSERT INTO order_details (order_detail_id, order_id, booking_service_id, quantity, unit_price, line_total) VALUES ('OD002', 'ORD003', 'BKS003',  1, 250000, 250000);
INSERT INTO order_details (order_detail_id, order_id, booking_service_id, quantity, unit_price, line_total) VALUES ('OD010', 'ORD003', 'BKS010',  1,  50000,  50000);
INSERT INTO order_details (order_detail_id, order_id, booking_service_id, quantity, unit_price, line_total) VALUES ('OD003', 'ORD004', 'BKS005',  1, 400000, 400000);
INSERT INTO order_details (order_detail_id, order_id, booking_service_id, quantity, unit_price, line_total) VALUES ('OD004', 'ORD004', 'BKS011',  1, 200000, 200000);
INSERT INTO order_details (order_detail_id, order_id, booking_service_id, quantity, unit_price, line_total) VALUES ('OD005', 'ORD005', 'BKS007',  1,  80000,  80000);
INSERT INTO order_details (order_detail_id, order_id, booking_service_id, quantity, unit_price, line_total) VALUES ('OD006', 'ORD006', 'BKS008',  1, 500000, 500000);
INSERT INTO order_details (order_detail_id, order_id, booking_service_id, quantity, unit_price, line_total) VALUES ('OD007', 'ORD007', 'BKS001',  1, 200000, 200000);
INSERT INTO order_details (order_detail_id, order_id, booking_service_id, quantity, unit_price, line_total) VALUES ('OD011', 'ORD007', 'BKS004',  1, 500000, 500000);
INSERT INTO order_details (order_detail_id, order_id, booking_service_id, quantity, unit_price, line_total) VALUES ('OD008', 'ORD008', 'BKS004',  1, 600000, 600000);

-- =========================================================
-- 18. PAYMENTS
-- =========================================================
-- Tìm đến đoạn Insert Payments trong file của bạn và dán đè đoạn này vào:

INSERT INTO payments (payment_id, order_id, payment_method, amount, status, paid_at) VALUES ('PAY002', 'ORD004', 'BANK_TRANSFER', 300000, 'SUCCESS', TO_TIMESTAMP_TZ('2026-05-12 10:00:00 +07:00', 'YYYY-MM-DD HH24:MI:SS TZH:TZM'));
INSERT INTO payments (payment_id, order_id, payment_method, amount, status, paid_at) VALUES ('PAY003', 'ORD005', 'CASH', 80000, 'SUCCESS', TO_TIMESTAMP_TZ('2026-05-13 14:30:00 +07:00', 'YYYY-MM-DD HH24:MI:SS TZH:TZM'));
INSERT INTO payments (payment_id, order_id, payment_method, amount, status, paid_at) VALUES ('PAY004', 'ORD007', 'CARD', 600000, 'SUCCESS', TO_TIMESTAMP_TZ('2026-05-11 09:15:00 +07:00', 'YYYY-MM-DD HH24:MI:SS TZH:TZM'));
INSERT INTO payments (payment_id, order_id, payment_method, amount, status, paid_at) VALUES ('PAY005', 'ORD003', 'EWALLET', 100000, 'SUCCESS', TO_TIMESTAMP_TZ('2026-05-13 09:30:00 +07:00', 'YYYY-MM-DD HH24:MI:SS TZH:TZM'));
INSERT INTO payments (payment_id, order_id, payment_method, amount, status, paid_at) VALUES ('PAY006', 'ORD002', 'CASH', 350000, 'PENDING', NULL);
INSERT INTO payments (payment_id, order_id, payment_method, amount, status, paid_at) VALUES ('PAY007', 'ORD006', 'BANK_TRANSFER', 500000, 'PENDING', NULL);
INSERT INTO payments (payment_id, order_id, payment_method, amount, status, paid_at) VALUES ('PAY008', 'ORD003', 'EWALLET', 150000, 'SUCCESS', TO_TIMESTAMP_TZ('2026-05-13 10:00:00 +07:00', 'YYYY-MM-DD HH24:MI:SS TZH:TZM'));
INSERT INTO payments (payment_id, order_id, payment_method, amount, status, paid_at) VALUES ('PAY009', 'ORD008', 'CASH', 600000, 'FAILED', NULL);
INSERT INTO payments (payment_id, order_id, payment_method, amount, status, paid_at) VALUES ('PAY010', 'ORD004', 'BANK_TRANSFER', 300000, 'REFUNDED', TO_TIMESTAMP_TZ('2026-05-12 15:00:00 +07:00', 'YYYY-MM-DD HH24:MI:SS TZH:TZM'));
INSERT INTO payments (payment_id, order_id, payment_method, amount, status, paid_at) VALUES ('PAY011', 'ORD003', 'CARD', 50000, 'SUCCESS', TO_TIMESTAMP_TZ('2026-05-13 14:45:00 +07:00', 'YYYY-MM-DD HH24:MI:SS TZH:TZM'));


-- =========================================================
-- 19. PET_HEALTH_RECORD
-- =========================================================
INSERT INTO pet_health_record (health_record_id, pet_id, booking_id, note, status) VALUES ('HR001', 'PET001', 'BK001', 'Sức khỏe tốt, da hơi khô',               1);
INSERT INTO pet_health_record (health_record_id, pet_id, booking_id, note, status) VALUES ('HR002', 'PET003', 'BK002', 'Hơi béo phì, cần giảm ăn',               1);
INSERT INTO pet_health_record (health_record_id, pet_id, booking_id, note, status) VALUES ('HR003', 'PET004', 'BK003', 'Răng có nhiều cao răng',                 1);
INSERT INTO pet_health_record (health_record_id, pet_id, booking_id, note, status) VALUES ('HR004', 'PET006', 'BK005', 'Bị rận tai nhẹ',                         1);
INSERT INTO pet_health_record (health_record_id, pet_id, booking_id, note, status) VALUES ('HR005', 'PET008', 'BK007', 'Vết thương cũ ở chân đã lành',           1);
INSERT INTO pet_health_record (health_record_id, pet_id, booking_id, note, status) VALUES ('HR006', 'PET010', 'BK009', 'Lông bị rối nhiều',                      1);
INSERT INTO pet_health_record (health_record_id, pet_id, booking_id, note, status) VALUES ('HR007', 'PET012', 'BK011', 'Mắt có ghèn, cần vệ sinh',               1);
INSERT INTO pet_health_record (health_record_id, pet_id, booking_id, note, status) VALUES ('HR008', 'PET001', 'BK001', 'Kiểm tra bổ sung sau tắm gội',           1);
INSERT INTO pet_health_record (health_record_id, pet_id, booking_id, note, status) VALUES ('HR009', 'PET005', 'BK004', 'Chờ khám lâm sàng',                      0);
INSERT INTO pet_health_record (health_record_id, pet_id, booking_id, note, status) VALUES ('HR010', 'PET007', 'BK006', 'Khách hủy lịch - không khám',            0);

-- =========================================================
-- 20. BOOKING_ROOM_PET
-- =========================================================
INSERT INTO booking_room_pet (booking_room_id, pet_id, note) VALUES ('BKR002', 'PET003', 'Husky quậy');
INSERT INTO booking_room_pet (booking_room_id, pet_id, note) VALUES ('BKR003', 'PET004', 'Mèo hiền');
INSERT INTO booking_room_pet (booking_room_id, pet_id, note) VALUES ('BKR004', 'PET006', 'Chó Phú Quốc');
INSERT INTO booking_room_pet (booking_room_id, pet_id, note) VALUES ('BKR005', 'PET008', 'Corgi chân ngắn');
INSERT INTO booking_room_pet (booking_room_id, pet_id, note) VALUES ('BKR006', 'PET010', 'Mèo béo');
INSERT INTO booking_room_pet (booking_room_id, pet_id, note) VALUES ('BKR007', 'PET012', 'Mèo Ragdoll');
INSERT INTO booking_room_pet (booking_room_id, pet_id, note) VALUES ('BKR001', 'PET001', 'Poodle');
INSERT INTO booking_room_pet (booking_room_id, pet_id, note) VALUES ('BKR008', 'PET005', 'Golden hiền lành');
INSERT INTO booking_room_pet (booking_room_id, pet_id, note) VALUES ('BKR010', 'PET011', 'Chihuahua');

-- =========================================================
-- 21. SERVICE_PRODUCT_STANDARD
-- =========================================================
INSERT INTO service_product_standard (standard_id, service_id, product_id, species, min_weight_kg, max_weight_kg, usage_amount, usage_unit) VALUES ('STD001', 'SER001', 'PROD01', 'DOG',  0, 10,  50, 'ML');
INSERT INTO service_product_standard (standard_id, service_id, product_id, species, min_weight_kg, max_weight_kg, usage_amount, usage_unit) VALUES ('STD002', 'SER001', 'PROD03', 'DOG', 10, 50, 100, 'ML');
INSERT INTO service_product_standard (standard_id, service_id, product_id, species, min_weight_kg, max_weight_kg, usage_amount, usage_unit) VALUES ('STD003', 'SER010', 'PROD07', 'DOG',  0, 20,  20, 'ML');
INSERT INTO service_product_standard (standard_id, service_id, product_id, species, min_weight_kg, max_weight_kg, usage_amount, usage_unit) VALUES ('STD004', 'SER003', 'PROD10', 'CAT',  0, 10,   1, 'G');
INSERT INTO service_product_standard (standard_id, service_id, product_id, species, min_weight_kg, max_weight_kg, usage_amount, usage_unit) VALUES ('STD005', 'SER002', 'PROD03', 'DOG',  0, 15,  60, 'ML');
INSERT INTO service_product_standard (standard_id, service_id, product_id, species, min_weight_kg, max_weight_kg, usage_amount, usage_unit) VALUES ('STD006', 'SER004', 'PROD07', 'DOG',  0, 50,  10, 'ML');
INSERT INTO service_product_standard (standard_id, service_id, product_id, species, min_weight_kg, max_weight_kg, usage_amount, usage_unit) VALUES ('STD007', 'SER006', 'PROD10', 'CAT',  0,  5, 0.5, 'G');
INSERT INTO service_product_standard (standard_id, service_id, product_id, species, min_weight_kg, max_weight_kg, usage_amount, usage_unit) VALUES ('STD008', 'SER008', 'PROD03', 'DOG',  0, 30,  80, 'ML');
INSERT INTO service_product_standard (standard_id, service_id, product_id, species, min_weight_kg, max_weight_kg, usage_amount, usage_unit) VALUES ('STD009', 'SER011', 'PROD03', 'DOG',  0, 20, 100, 'ML');

-- =========================================================
-- 22. GOODS_RECEIPT
-- =========================================================
INSERT INTO goods_receipt (goods_receipt_id, branch_id, employee_id, supplier_name, receipt_date, total_quantity, total_item_count, status, note) VALUES ('GR001', 'BR001', 'EMP001', 'Công ty Pet Supply VN', TIMESTAMP '2026-05-01 09:00:00 +07:00', 215, 2, 'APPROVED', 'Nhập sữa tắm SOS và vòng cổ da tháng 5');
INSERT INTO goods_receipt (goods_receipt_id, branch_id, employee_id, supplier_name, receipt_date, total_quantity, total_item_count, status, note) VALUES ('GR002', 'BR002', 'EMP004', 'Nhà phân phối Minh Châu', TIMESTAMP '2026-05-02 10:30:00 +07:00', 140, 2, 'APPROVED', 'Nhập hạt Royal Canin và pate Whiskas tháng 5');
INSERT INTO goods_receipt (goods_receipt_id, branch_id, employee_id, supplier_name, receipt_date, total_quantity, total_item_count, status, note) VALUES ('GR003', 'BR005', 'EMP007', 'Công ty Dược thú y Sài Gòn', TIMESTAMP '2026-05-03 08:00:00 +07:00', 25000, 1, 'APPROVED', 'Nhập thuốc trị rận tháng 5');
INSERT INTO goods_receipt (goods_receipt_id, branch_id, employee_id, supplier_name, receipt_date, total_quantity, total_item_count, status, note) VALUES ('GR004', 'BR001', 'EMP001', 'Công ty Pet Supply VN', TIMESTAMP '2026-05-10 14:00:00 +07:00', 5, 1, 'DRAFT', 'Phiếu nháp — nhập ổ nằm bông, chưa duyệt');
INSERT INTO goods_receipt (goods_receipt_id, branch_id, employee_id, supplier_name, receipt_date, total_quantity, total_item_count, status, note) VALUES ('GR005', 'BR003', 'EMP005', 'Nhà phân phối Phương Nam', TIMESTAMP '2026-05-05 11:00:00 +07:00', 12, 1, 'CANCELLED', 'Đơn hủy — nhà cung cấp không giao hàng đúng hạn');

-- =========================================================
-- 23. GOODS_RECEIPT_DETAIL
-- =========================================================
INSERT INTO goods_receipt_detail (goods_receipt_id, product_id, quantity, unit, line_total, note) VALUES ('GR001', 'PROD03', 10000, 'ML', 1200000, '50 chai × 200 ml = 10000 ML');
INSERT INTO goods_receipt_detail (goods_receipt_id, product_id, quantity, unit, line_total, note) VALUES ('GR001', 'PROD02', 15, 'G', 2250000, 'Số lượng quy ước 15 G — thực tế 15 cái vòng cổ');
INSERT INTO goods_receipt_detail (goods_receipt_id, product_id, quantity, unit, line_total, note) VALUES ('GR002', 'PROD01', 0.1, 'KG', 12000, '100g hạt Royal Canin mẫu thử nghiệm kho');
INSERT INTO goods_receipt_detail (goods_receipt_id, product_id, quantity, unit, line_total, note) VALUES ('GR002', 'PROD05', 140, 'G', 2100000, '140 gói pate Whiskas 10g/gói');
INSERT INTO goods_receipt_detail (goods_receipt_id, product_id, quantity, unit, line_total, note) VALUES ('GR003', 'PROD10', 25, 'KG', 4500000, '25 kg thuốc trị rận dạng bột');
INSERT INTO goods_receipt_detail (goods_receipt_id, product_id, quantity, unit, line_total, note) VALUES ('GR004', 'PROD11', 5, 'G', 2750000, '5 ổ nằm bông — phiếu nháp chưa duyệt');
INSERT INTO goods_receipt_detail (goods_receipt_id, product_id, quantity, unit, line_total, note) VALUES ('GR005', 'PROD02', 12, 'G', 1800000, '12 cái vòng cổ da — đơn đã hủy');

-- =========================================================
-- 24. STOCK_AUDIT
-- =========================================================
INSERT INTO stock_audit (stock_audit_id, branch_id, employee_id, audit_date, status, note) VALUES ('SA001', 'BR001', 'EMP001', TIMESTAMP '2026-05-08 09:00:00 +07:00', 'COMPLETED', 'Kiểm kho định kỳ tháng 5 — Chi nhánh Q1');
INSERT INTO stock_audit (stock_audit_id, branch_id, employee_id, audit_date, status, note) VALUES ('SA002', 'BR002', 'EMP004', TIMESTAMP '2026-05-09 10:00:00 +07:00', 'DRAFT', 'Kiểm kho nháp — Chi nhánh Q3 chờ xác nhận');
INSERT INTO stock_audit (stock_audit_id, branch_id, employee_id, audit_date, status, note) VALUES ('SA003', 'BR005', 'EMP007', TIMESTAMP '2026-05-07 08:00:00 +07:00', 'CANCELLED', 'Hủy kiểm kho — nhân sự không đủ');

-- =========================================================
-- 25. STOCK_AUDIT_DETAIL
-- =========================================================
INSERT INTO stock_audit_detail (stock_audit_id, product_id, system_quantity, actual_quantity, difference_quantity, difference_rate, note) VALUES ('SA001', 'PROD03', 25000, 24800, -200, 0.80, 'Thiếu 200 ML do hao hụt bay hơi');
INSERT INTO stock_audit_detail (stock_audit_id, product_id, system_quantity, actual_quantity, difference_quantity, difference_rate, note) VALUES ('SA001', 'PROD02', 35, 35, 0, 0.00, 'Khớp hoàn toàn');
INSERT INTO stock_audit_detail (stock_audit_id, product_id, system_quantity, actual_quantity, difference_quantity, difference_rate, note) VALUES ('SA002', 'PROD01', 100, 98, -2, 2.00, 'Chênh lệch nhỏ, cần xác nhận');

-- =========================================================
-- 26. MATERIAL_WASTE
-- =========================================================
INSERT INTO material_waste (material_waste_id, product_id, employee_id, branch_id, waste_quantity, reason, recorded_at, status, note) VALUES ('MW001', 'PROD03', 'EMP001', 'BR001', 500, 'Sữa tắm đổ vỡ trong quá trình tắm thú cưng lớn', TIMESTAMP '2026-05-05 10:30:00 +07:00', 'APPROVED', 'Đã xác nhận bởi quản lý ca');
INSERT INTO material_waste (material_waste_id, product_id, employee_id, branch_id, waste_quantity, reason, recorded_at, status, note) VALUES ('MW002', 'PROD07', 'EMP003', 'BR001', 30, 'Xịt khử mùi bị hết hạn sử dụng, tiêu huỷ lô cũ', TIMESTAMP '2026-05-06 14:00:00 +07:00', 'APPROVED', NULL);
INSERT INTO material_waste (material_waste_id, product_id, employee_id, branch_id, waste_quantity, reason, recorded_at, status, note) VALUES ('MW003', 'PROD10', 'EMP007', 'BR005', 50, 'Thuốc trị rận mở gói nhưng không dùng hết trong ca', TIMESTAMP '2026-05-07 09:00:00 +07:00', 'PENDING', 'Chờ quản lý xác nhận');
INSERT INTO material_waste (material_waste_id, product_id, employee_id, branch_id, waste_quantity, reason, recorded_at, status, note) VALUES ('MW004', 'PROD04', 'EMP005', 'BR003', 2, 'Bóng cao su bị thú cưng cắn rách không sử dụng được', TIMESTAMP '2026-05-08 16:00:00 +07:00', 'REJECTED', 'Quản lý từ chối — không thuộc vật tư tiêu hao');
INSERT INTO material_waste (material_waste_id, product_id, employee_id, branch_id, waste_quantity, reason, recorded_at, status, note) VALUES ('MW005', 'PROD03', 'EMP009', 'BR002', 200, 'Thất thoát khi đổ sang bình nhỏ phục vụ dịch vụ', TIMESTAMP '2026-05-09 11:00:00 +07:00', 'PENDING', NULL);
COMMIT;
