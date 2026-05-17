/*MENU
1. Customer
2. Branch 
3. Employee
4. App_user
5. Pet
6. Category_Product
7. Prodcut
8. Branch_Inventory
9. Category_Services
10. Services
11. Type_room
12. Room
13. Booking
14. Booking_Servieces
15. Booking_room
16. Booking_room_pet
17. Orders
18. Orders_details
19. Payments
20. Pet_health_record
21. Service_product_standard
22. Goods_receipt
23. Goods_receipt_detail
24. Stock_audit
25. Stock_audit_detail
26. Materrial_waste
*/
-- =========================================================
-- 1. CUSTOMER
-- =========================================================
create table customer (
   customer_id varchar2(10) not null,
   full_name   nvarchar2(120) not null,
   email       nvarchar2(254),
   cccd        nvarchar2(12),
   phone       nvarchar2(20) not null,
   address     nvarchar2(120),
   note        clob,
   created_at  timestamp(6) with time zone default systimestamp,
   updated_at  timestamp(6) with time zone default systimestamp,
   constraint pk_customer primary key ( customer_id ),
   constraint uq_customer_phone unique ( phone ),
   constraint uq_customer_email unique ( email ),
   constraint uq_customer_cccd  unique ( cccd )
);
-- =========================================================
-- 2. BRANCH
-- =========================================================
create table branch (
   branch_id   varchar2(10) not null,
   branch_name nvarchar2(120) not null,
   phone       varchar2(20),
   email       varchar2(254),
   address     nvarchar2(120) not null,
   is_active   number(1) default 1 not null,
   constraint pk_branch primary key ( branch_id ),
   constraint uq_branch_email unique ( email ),
   constraint ck_branch_is_active check ( is_active in ( 0,
                                                         1 ) )
    -- 0: là ngừng hoạt động, 1 là đang hoạt động
);
-- =========================================================
-- 3. EMPLOYEE
-- =========================================================
create table employee (
   employee_id varchar2(10) not null,
   branch_id   varchar2(10) not null,
   full_name   nvarchar2(120) not null,
   salary      number(12,2) not null,
   email       varchar2(254),
   phone       varchar2(20) not null,
   hire_date   timestamp(6) with time zone,
   status_code nvarchar2(20) not null,
   note        clob,
   constraint pk_employee primary key ( employee_id ),
   constraint uq_employee_email unique ( email ),
   constraint uq_employee_phone unique ( phone ),
   constraint fk_employee_branch foreign key ( branch_id )
      references branch ( branch_id ),
   constraint ck_employee_status
      check ( status_code in ( 'WORKING',
                               'ON_LEAVE',
                               'RESIGNED' ) )
    --Working: đang làm việc, On_Leave: đang trong kì nghỉ phép, Resigned: đã nghỉ việc
);
-- =========================================================
-- 4. APP_USER
-- =========================================================
create table app_user (
   employee_id   varchar2(10) not null,
   password_hash nvarchar2(255),
   role_emp      number(1) not null,
   user_name     nvarchar2(254) not null,
   is_active     number(1) default 1 not null,
   last_login    timestamp(6) with time zone,
   created_at    timestamp(6) with time zone default systimestamp,
   updated_at    timestamp(6) with time zone default systimestamp,
   constraint pk_app_user primary key ( employee_id ),
   constraint fk_app_user_employee foreign key ( employee_id )
      references employee ( employee_id ),
   constraint uq_app_user_username unique ( user_name ),
   constraint ck_app_user_is_active check ( is_active in ( 0,
                                                           1 ) ),
    --0: INACTIVE; 1: ACTIVE
   constraint ck_app_user_role
      check ( role_emp in ( 0,
                            1,
                            2,
                            3,
                            4 ) )
    -- 0 = Admin: admin
    -- 1 = Receptionist: nhân viên lễ tân
    -- 2 = Pet Care Staff: nhân viên chăm sóc thú cưng
    -- 3 = Branch Manager: nhân viên quản lý chi nhánh
    -- 4 = CEO: giám đốc
);
-- =========================================================
-- 5. PET
-- =========================================================
create table pet (
   pet_id       varchar2(10) not null,
   customer_id  varchar2(10) not null,
   pet_name     nvarchar2(20) not null,
   species      nvarchar2(30) not null,
   breed        nvarchar2(60),
   sex          nvarchar2(10),
   weight_kg    number(5,2),
   special_note clob,
   created_at   timestamp(6) with time zone default systimestamp,
   updated_at   timestamp(6) with time zone default systimestamp,
   constraint pk_pet primary key ( pet_id ),
   constraint fk_pet_customer foreign key ( customer_id )
      references customer ( customer_id ),
   constraint ck_pet_weight check ( weight_kg is null
       or weight_kg > 0 ),
   constraint ck_pet_sex
      check ( sex is null
          or sex in ( 'Male',
                      'Female' ) )
);
-- =========================================================
-- 6. CATEGORY_PRODUCT: loại sản phẩm
-- =========================================================
create table category_product (
   product_category_id varchar2(10) not null,
   category_name       nvarchar2(100) not null,
   created_at          timestamp(6) with time zone default systimestamp,
   updated_at          timestamp(6) with time zone default systimestamp,
   constraint pk_category_product primary key ( product_category_id ),
   constraint uq_category_product_name unique ( category_name )
);
-- =========================================================
-- 7. PRODUCT
-- =========================================================
create table product (
   product_id          varchar2(10) not null,
   product_category_id varchar2(10) not null,
   product_name        nvarchar2(160) not null,
   unit                varchar2(30),
   cost_price          number(12,2) not null,
   created_at          timestamp(6) with time zone default systimestamp,
   updated_at          timestamp(6) with time zone default systimestamp,
   constraint pk_product primary key ( product_id ),
   constraint fk_product_category foreign key ( product_category_id )
      references category_product ( product_category_id ),
   constraint ck_product_cost_price check ( cost_price >= 0 )
);
-- =========================================================
-- 8. BRANCH_INVENTORY
-- =========================================================
create table branch_inventory (
   branch_id         varchar2(10) not null,
   product_id        varchar2(10) not null,
   quantity_in_stock number(10) default 0 not null, -- số lượng tồn kho
   reorder_point     number(10),-- số lượng cảnh báo để nhập hàng lại 
   last_updated      timestamp(6) with time zone default systimestamp,
   constraint pk_branch_inventory primary key ( branch_id,
                                                product_id ),
   constraint fk_inventory_branch foreign key ( branch_id )
      references branch ( branch_id ),
   constraint fk_inventory_product foreign key ( product_id )
      references product ( product_id ),
   constraint ck_inventory_qty check ( quantity_in_stock >= 0 ),
   constraint ck_inventory_reorder
      check ( reorder_point is null
          or reorder_point >= 0 )
);
-- =========================================================
-- 9. CATEGORY_SERVICES: tên loại dịch vụ   
-- =========================================================
create table category_services (
   service_category_id varchar2(10) not null,
   category_name       nvarchar2(80) not null,
   note                clob,
   created_at          timestamp(6) with time zone default systimestamp,
   updated_at          timestamp(6) with time zone default systimestamp,
   constraint pk_category_services primary key ( service_category_id ),
   constraint uq_category_services_name unique ( category_name )
);
-- =========================================================
-- 10. SERVICES
-- =========================================================
create table services (
   service_id          varchar2(10) not null,
   service_category_id varchar2(10) not null,
   service_name        nvarchar2(120) not null,
   species             nvarchar2(20) not null,
   description_sv      clob,
   base_price          number(12,2) default 0 not null,
   duration_minutes    number(4),-- thời lượng thực hiện dịch vụ 
   is_active           number(1) default 1 not null,
   created_at          timestamp(6) with time zone default systimestamp,
   updated_at          timestamp(6) with time zone default systimestamp,
   constraint pk_services primary key ( service_id ),
   constraint fk_services_category foreign key ( service_category_id )
      references category_services ( service_category_id ),
   constraint ck_services_price check ( base_price >= 0 ),
   constraint ck_services_duration check ( duration_minutes is null
       or duration_minutes > 0 ),
   constraint ck_services_active check ( is_active in ( 0,
                                                        1 ) )
    --0: INACTIVE; 1: ACTIVE
);
-- =========================================================
-- 11. TYPE_ROOM
-- =========================================================
create table type_room (
   type_room_id       varchar2(10) not null,
   type_name          nvarchar2(80) not null,
   note               clob,
   max_pets           number(2) not null,
   max_weight_kg      number(5,2),
   base_price_per_day number(12,2) not null,
   is_active          number(1) default 1 not null,
   created_at         timestamp(6) with time zone default systimestamp,
   updated_at         timestamp(6) with time zone default systimestamp,
   constraint pk_type_room primary key ( type_room_id ),
   constraint ck_type_room_max_pets check ( max_pets > 0 ),
   constraint ck_type_room_max_weight check ( max_weight_kg is null
       or max_weight_kg > 0 ),
   constraint ck_type_room_active check ( is_active in ( 0,
                                                         1 ) ),
    --0: INACTIVE; 1:ACTIVE
   constraint ck_type_room_price check ( base_price_per_day >= 0 ),
   constraint ck_type_room_name
      check ( type_name in ( 'STANDARD',
                             'PREMIUM',
                             'SUITE' ) )
    --STANDARD: Phòng tiêu chuẩn
    --PREMIUM: Thuộc loại phòng vip, đầy đủ tiện nghi
    --SUITE: Phòng rộng hơn 2 phòng còn lại, đầy đủ tiện nghi hơn
);
-- =========================================================
-- 12. ROOM
-- =========================================================
create table room (
   room_id      varchar2(10) not null,
   branch_id    varchar2(10) not null,
   type_room_id varchar2(10) not null,
   room_number  nvarchar2(10) not null,
   status       nvarchar2(20) not null,
   created_at   timestamp(6) with time zone default systimestamp not null,
   constraint pk_room primary key ( room_id ),
   constraint fk_room_branch foreign key ( branch_id )
      references branch ( branch_id ),
   constraint fk_room_type foreign key ( type_room_id )
      references type_room ( type_room_id ),
   constraint uq_room_number unique ( branch_id,
                                       room_number ),
   constraint ck_room_status
      check ( status in ( 'AVAILABLE',
                          'IN_USE',
                          'MAINTENANCE' ) )
    --AVAILABLE: Trống
    --IN_USE: Đang hoạt động
    --MAINTENANCE: Bảo trì
);

-- =========================================================
-- 13. BOOKING
-- =========================================================
create table booking (
   booking_id           varchar2(10) not null,
   customer_id          varchar2(10) not null,
   branch_id            varchar2(10) not null,
   checkin_expected_at  timestamp(6) with time zone,
   checkout_expected_at timestamp(6) with time zone,
   status               nvarchar2(20) not null,
   deposit_amount       number(12,2),
   special_note         clob,
   created_at           timestamp(6) with time zone default systimestamp,
   updated_at           timestamp(6) with time zone default systimestamp,
   constraint pk_booking primary key ( booking_id ),
   constraint fk_booking_customer foreign key ( customer_id )
      references customer ( customer_id ),
   constraint fk_booking_branch foreign key ( branch_id )
      references branch ( branch_id ),
   constraint ck_booking_status
      check ( status in ( 'PENDING',
                          'CONFIRMED',
                          'CHECKED_IN',
                          'CHECKED_OUT',
                          'CANCELLED' ) ),-- t nghĩ là nên xóa pending đi 
   constraint ck_booking_deposit
      check ( deposit_amount is null
          or deposit_amount >= 0 ),
   constraint ck_booking_time
      check ( checkin_expected_at is null
          or checkout_expected_at is null
          or checkout_expected_at > checkin_expected_at )
);

-- =========================================================
-- 14. BOOKING_SERVICES
-- =========================================================
create table booking_services (
   booking_service_id varchar2(10) not null,
   booking_id         varchar2(10) not null,
   service_id         varchar2(10),
   pet_id            varchar2(10),
   employee_id        varchar2(10),
   scheduled_at       timestamp(6) with time zone,
   status             nvarchar2(20) not null,
   note               clob,
   created_at         timestamp(6) with time zone default systimestamp,
   updated_at         timestamp(6) with time zone default systimestamp,
   constraint pk_booking_services primary key ( booking_service_id ),
   constraint fk_bks_booking foreign key ( booking_id )
      references booking ( booking_id ),
   constraint fk_bks_pet foreign key ( pet_id )
      references pet ( pet_id ),
   constraint fk_bks_service foreign key ( service_id )
      references services ( service_id ),
   constraint fk_bks_employee foreign key ( employee_id )
      references employee ( employee_id ),
   constraint ck_bks_status
      check ( status in ( 'PENDING',
                          'SCHEDULED',
                          'IN_PROGRESS',
                          'DONE',
                          'CANCELLED' ) )-- t cũng nghĩ nên xóa đi pending
);

-- =========================================================
-- 15. Booking_room
-- =========================================================
create table booking_room (
   booking_room_id varchar2(10) not null,
   booking_id      varchar2(10) not null,
   room_id         varchar2(10) not null,
   assigned_at     timestamp(6) with time zone default systimestamp,
   note            clob,
   constraint pk_booking_room primary key ( booking_room_id ),
   constraint fk_bkr_booking_id foreign key ( booking_id )
      references booking ( booking_id ),
   constraint fk_bkr_room_id foreign key ( room_id )
      references room ( room_id ),
   constraint uq_booking_room unique ( booking_id,
                                       room_id )
);

-- =========================================================
-- 16. Booking_room_pet
-- =========================================================
create table booking_room_pet (
   booking_room_id varchar2(10) not null,
   pet_id          varchar2(10) not null,
   assigned_at     timestamp(6) with time zone default systimestamp,
   note            clob,
   constraint pk_brp primary key ( booking_room_id,
                                   pet_id ),
   constraint fk_brp_pet foreign key ( pet_id )
      references pet ( pet_id ),
   constraint fk_brp_booking_room foreign key ( booking_room_id )
      references booking_room ( booking_room_id )
);

-- =========================================================
-- 17. ORDERS
-- =========================================================
create table orders (
   order_id       varchar2(10) not null,
   customer_id    varchar2(10) not null,
   branch_id      varchar2(10) not null,
   booking_id     varchar2(10) not null,
   created_by_emp varchar2(10) not null,
   status         varchar2(20) not null,
   subtotal       number(12,2) not null,
   grand_total    number(12,2) not null,
   created_at     timestamp(6) with time zone default systimestamp,
   constraint pk_orders primary key ( order_id ),
   constraint fk_orders_customer foreign key ( customer_id )
      references customer ( customer_id ),
   constraint fk_orders_branch foreign key ( branch_id )
      references branch ( branch_id ),
   constraint fk_orders_booking foreign key ( booking_id )
      references booking ( booking_id ),
   constraint fk_orders_employee foreign key ( created_by_emp )
      references employee ( employee_id ),
   constraint ck_orders_subtotal check ( subtotal >= 0 ),
   constraint ck_orders_total check ( grand_total >= 0 ),
   constraint ck_orders_total_logic check ( grand_total >= subtotal ),--cần xem lại vì mình không có thuế và giảm giá, nên để lại 1 cái thôi
   constraint ck_orders_status
      check ( status in ( 'PENDING',
                          'PAID',
                          'PARTIAL',
                          'CANCELLED',
                          'REFUNDED' ) )
    --PENDING: Hóa đơn đã tạo nhưng chưa thanh toán
    --PAID: Đã thanh toán đầy đủ
    --PARTIAL: Đã thanh toán 1 phần -> tiền cọc
    --CANCELLED: Hủy hóa đơn
    --REFUND: Hoàn tiền
);

-- =========================================================
-- 18. ORDER_DETAILS
-- =========================================================
create table order_details (
   order_detail_id varchar2(10) not null,
   booking_service_id varchar2(10),
   booking_room_id varchar2(10),
   service_id      varchar2(10),
   order_id        varchar2(10) not null,
   note            clob,
   quantity        number(10,2) default 1 not null,
   unit_price      number(12,2) not null,
   line_total      number(12,2) not null,
   created_at      timestamp(6) with time zone default systimestamp,
   constraint pk_order_details primary key ( order_detail_id ),
   constraint fk_od_booking_service foreign key ( booking_service_id )
      references booking_services ( booking_service_id ),
   constraint fk_od_booking_room foreign key ( booking_room_id )
      references booking_room ( booking_room_id ),
   constraint fk_od_service foreign key ( service_id )
      references services ( service_id ),
   constraint fk_od_order foreign key ( order_id )
      references orders ( order_id ),
   constraint ck_od_qty check ( quantity > 0 ),
   constraint ck_od_unit_price check ( unit_price >= 0 ),
   constraint ck_od_line_total check ( line_total >= 0 )
);--để ý công thức của linetotal

-- =========================================================
-- 19. PAYMENTS
-- =========================================================
create table payments (
   payment_id     varchar2(10) not null,
   order_id       varchar2(10) not null,
   payment_method varchar2(30) not null,
   provider       varchar2(50),
   amount         number(12,2) not null,
   status         varchar2(20) not null,
   paid_at        timestamp(6) with time zone,
   note           clob,
   created_at     timestamp(6) with time zone default systimestamp,
   updated_at     timestamp(6) with time zone default systimestamp,
   constraint pk_payments primary key ( payment_id ),
   constraint fk_payments_order foreign key ( order_id )
      references orders ( order_id ),
   constraint ck_payments_amount check ( amount > 0 ),
   constraint ck_payments_method
      check ( payment_method in ( 'CASH',
                                  'BANK_TRANSFER',
                                  'CARD',
                                  'EWALLET' ) ),
   constraint ck_payments_status
      check ( status in ( 'PENDING',
                          'SUCCESS',
                          'FAILED',
                          'REFUNDED' ) ),
    --Pending: đang chờ xử lý, chưa xác nhận thành công
   constraint ck_payment_status_paid_at
      check ( ( status in ( 'SUCCESS',
                            'REFUNDED' )
         and paid_at is not null )
          or ( status in ( 'PENDING',
                           'FAILED' )
         and paid_at is null ) )
);

-- =========================================================
-- 20. PET_HEALTH_RECORD
-- =========================================================
create table pet_health_record (
   health_record_id varchar2(10) not null,
   pet_id           varchar2(10) not null,
   booking_id       varchar2(10) not null,
   recorded_at      timestamp(6) with time zone default systimestamp,
   note             clob,
   status           number(1) default 1 not null,
   constraint hpr_pk_01 primary key ( health_record_id ),
   constraint hpr_fk_pet_01 foreign key ( pet_id )
      references pet ( pet_id ),
   constraint hpr_fk_booking_01 foreign key ( booking_id )
      references booking ( booking_id ),
   constraint hpr_ck_status_01 check ( status in ( 0,
                                                   1 ) )
);


-- =========================================================
-- 21. Service_product_standard
-- =========================================================
create table service_product_standard (
   service_product_standard_id varchar2(10) not null,
   service_id    varchar2(10) not null,
   product_id    varchar2(10) not null,
   species       varchar2(20) not null,
   min_weight_kg number(5,2) not null,
   max_weight_kg number(5,2) not null,
   usage_amount  number(10,2) not null,
   usage_unit    varchar2(10) not null,
   note          clob,
   created_at    timestamp(6) with time zone default systimestamp,
   updated_at    timestamp(6) with time zone default systimestamp,
   constraint pk_service_product_standard primary key ( service_product_standard_id ),
   constraint fk_sps_service foreign key ( service_id )
      references services ( service_id ),
   constraint fk_sps_product foreign key ( product_id )
      references product ( product_id ),
   constraint ck_sps_weight_min check ( min_weight_kg >= 0 ),
   constraint ck_sps_weight_max check ( max_weight_kg > 0 ),
   constraint ck_sps_weight_range check ( max_weight_kg > min_weight_kg ),
   constraint ck_sps_usage_amount check ( usage_amount > 0 ),
   constraint ck_sps_usage_unit
      check ( usage_unit in ( 'ML',
                              'L',
                              'G',
                              'KG' ) ),-- nhớ có function chỗ này
   constraint ck_sps_species check ( species in ( 'DOG',
                                                  'CAT' ) )
);


-- =========================================================
-- 22. GOODS_RECEIPT
-- =========================================================
create table goods_receipt (
   goods_receipt_id varchar2(10) not null,
   branch_id        varchar2(10) not null,
   employee_id      varchar2(10) not null,
   supplier_name    nvarchar2(120),
   receipt_date     timestamp(6) with time zone not null,
   total_quantity   number(12,2) default 0 not null,
   total_item_count number(10) default 0 not null,
   status           nvarchar2(20) not null,
   note             clob,
   created_at       timestamp(6) with time zone,
   updated_at       timestamp(6) with time zone,
   constraint pk_goods_receipt primary key ( goods_receipt_id ),
   constraint fk_gr_branch foreign key ( branch_id )
      references branch ( branch_id ),
   constraint fk_gr_employee foreign key ( employee_id )
      references employee ( employee_id ),
   constraint ck_gr_total_quantity check ( total_quantity >= 0 ),
   constraint ck_gr_total_item_count check ( total_item_count >= 0 ),
   constraint ck_gr_status
      check ( status in ( 'DRAFT',
                          'APPROVED',
                          'CANCELLED' ) )
);

-- =========================================================
-- 23. GOODS_RECEIPT_DETAIL
-- =========================================================
create table goods_receipt_detail (
   goods_receipt_id        varchar2(10) not null,
   product_id              varchar2(10) not null,
   quantity                number(12,2) not null,
   unit                    varchar2(20) not null,
   line_total              number(12,2) default 0 not null,
   note                    clob,
   created_at              timestamp(6) with time zone,
   updated_at              timestamp(6) with time zone,
   constraint pk_goods_receipt_detail primary key ( goods_receipt_id,
                                                        product_id ),
   constraint fk_grd_receipt foreign key ( goods_receipt_id )
      references goods_receipt ( goods_receipt_id ),
   constraint fk_grd_product foreign key ( product_id )
      references product ( product_id ),
   constraint ck_grd_quantity check ( quantity > 0 ),
   constraint ck_grd_line_total check ( line_total >= 0 ),
   constraint ck_grd_unit
      check ( upper(unit) in ( 'G',
                               'KG',
                               'ML',
                               'L' ) )
);
-- =========================================================
-- 24. STOCK_AUDIT
-- =========================================================
create table stock_audit (
   stock_audit_id varchar2(10) not null,
   branch_id      varchar2(10) not null,
   employee_id    varchar2(10) not null,
   audit_date     timestamp(6) with time zone not null,
   status         nvarchar2(20) not null,
   note           clob,
   created_at     timestamp(6) with time zone,
   updated_at     timestamp(6) with time zone,
   constraint pk_stock_audit primary key ( stock_audit_id),
   constraint fk_sa_branch foreign key ( branch_id )
      references branch ( branch_id ),
   constraint fk_sa_employee foreign key ( employee_id )
      references employee ( employee_id ),
   constraint ck_sa_status
      check ( status in ( 'DRAFT',
                          'COMPLETED',
                          'CANCELLED' ) )
);
-- =========================================================
-- 25. STOCK_AUDIT_DETAIL
-- =========================================================
create table stock_audit_detail (
   stock_audit_id        varchar2(10) not null,
   product_id            varchar2(10) not null,
   system_quantity       number(12,2) default 0 not null,
   actual_quantity       number(12,2) default 0 not null,
   difference_quantity   number(12,2),
   difference_rate       number(5,2),
   note                  clob,
   created_at            timestamp(6) with time zone,
   updated_at            timestamp(6) with time zone,
   constraint pk_stock_audit_detail primary key ( stock_audit_id,
                                                    product_id ),
   constraint fk_sad_audit foreign key ( stock_audit_id )
      references stock_audit ( stock_audit_id ),
   constraint fk_sad_product foreign key ( product_id )
      references product ( product_id ),
   constraint ck_sad_system_quantity check ( system_quantity >= 0 ),
   constraint ck_sad_actual_quantity check ( actual_quantity >= 0 ),
   constraint ck_sad_difference_rate
      check ( difference_rate is null
          or difference_rate >= 0 )
);
-- =========================================================
-- 26. MATERIAL_WASTE
-- =========================================================
create table material_waste (
   material_waste_id varchar2(10) not null,
   product_id        varchar2(10) not null,
   employee_id       varchar2(10) not null,
   branch_id         varchar2(10) not null,
   waste_quantity    number(12,2) not null,
   reason            clob,
   recorded_at       timestamp(6) with time zone not null,
   status            nvarchar2(20) not null,
   note              clob,
   created_at        timestamp(6) with time zone,
   updated_at        timestamp(6) with time zone,
   constraint pk_material_waste primary key ( material_waste_id ),
   constraint fk_mw_product foreign key ( product_id )
      references product ( product_id ),
   constraint fk_mw_employee foreign key ( employee_id )
      references employee ( employee_id ),
   constraint fk_mw_branch foreign key ( branch_id )
      references branch ( branch_id ),
   constraint ck_mw_waste_quantity check ( waste_quantity > 0 ),
   constraint ck_mw_status
      check ( status in ( 'PENDING',
                          'APPROVED',
                          'REJECTED' ) )
);
