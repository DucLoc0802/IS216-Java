-- Tạm tắt trigger dễ gây lỗi khi xóa/insert lại seed
begin
   execute immediate 'ALTER TRIGGER TRG_SYNC_GOODS_RECEIPT_TOTALS DISABLE';
exception
   when others then
      null;
end;
/

begin
   execute immediate 'ALTER TRIGGER TRG_SYNC_ORDER_TOTALS DISABLE';
exception
   when others then
      null;
end;
/

begin
   execute immediate 'ALTER TRIGGER TRG_PAYMENT_LOGIC_SYNC DISABLE';
exception
   when others then
      null;
end;
/

begin
   execute immediate 'ALTER TRIGGER TRG_PREVENT_MANUAL_PAID_STATUS DISABLE';
exception
   when others then
      null;
end;
/

begin
   execute immediate 'ALTER TRIGGER TRG_BKS_INVENTORY_SYNC DISABLE';
exception
   when others then
      null;
end;
/

-- XÓA TỪ BẢNG CON TRƯỚC
delete from payments
 where payment_id like 'PAY1%';

delete from order_details
 where order_detail_id like 'OD1%';

delete from orders
 where order_id like 'ORD1%';

delete from booking_services
 where booking_service_id like 'BS1%';

delete from booking_rooms
 where booking_room_id like 'BRM1%';

delete from booking
 where booking_id like 'BK1%';

delete from goods_receipt_detail
 where goods_receipt_id like 'GR1%';

delete from goods_receipt
 where goods_receipt_id like 'GR1%';

delete from inventory_check_detail
 where inventory_check_id like 'IC1%';

delete from inventory_check
 where inventory_check_id like 'IC1%';

delete from inventory_loss
 where inventory_loss_id like 'IL1%';

delete from pet
 where pet_id like 'PET1%';

delete from customer
 where customer_id like 'CUS1%';

delete from branch_inventory
 where branch_id like 'BR1%'
    or product_id like 'PD1%';

delete from services
 where service_id like 'SV1%';

delete from category_services
 where service_category_id like 'SVC1%';

delete from product
 where product_id like 'PD1%';

delete from category_product
 where product_category_id like 'PCT1%';

delete from room
 where room_id like 'RM1%'
    or room_id like 'RM2%';

delete from type_room
 where type_room_id like 'TR1%';

delete from app_user
 where employee_id like 'EMP1%';

delete from employee
 where employee_id like 'EMP1%';

delete from branch
 where branch_id like 'BR1%';

commit;

-- Bật lại trigger
begin
   execute immediate 'ALTER TRIGGER TRG_SYNC_GOODS_RECEIPT_TOTALS ENABLE';
exception
   when others then
      null;
end;
/

begin
   execute immediate 'ALTER TRIGGER TRG_SYNC_ORDER_TOTALS ENABLE';
exception
   when others then
      null;
end;
/

begin
   execute immediate 'ALTER TRIGGER TRG_PAYMENT_LOGIC_SYNC ENABLE';
exception
   when others then
      null;
end;
/

begin
   execute immediate 'ALTER TRIGGER TRG_PREVENT_MANUAL_PAID_STATUS ENABLE';
exception
   when others then
      null;
end;
/

begin
   execute immediate 'ALTER TRIGGER TRG_BKS_INVENTORY_SYNC ENABLE';
exception
   when others then
      null;
end;
/