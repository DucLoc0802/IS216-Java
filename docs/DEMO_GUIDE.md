# Hướng Dẫn Demo Hệ Thống Pet Hotel

Tài liệu này dùng để demo nhanh cho giảng viên: tài khoản nào làm gì, đi từ bước nào sang bước nào, luồng nghiệp vụ ra sao và nên trình bày theo thứ tự nào.

## 1. Tài Khoản Demo

| Vai trò | Tài khoản | Mật khẩu | Nên demo |
|---|---|---|---|
| Admin hệ thống | `admin` | `admin` | Phân quyền, quản lý tài khoản |
| CEO | `ceo` | `ceo` | Báo cáo tổng quan toàn hệ thống |
| Quản lý chi nhánh Q.1 | `quanly` | `quanly` | Nhân viên, dịch vụ, phòng, kho, duyệt hao hụt, phân công grooming |
| Lễ tân Q.1 | `letan` | `letan` | Khách hàng, booking, hóa đơn, thanh toán |
| Nhân viên chăm sóc Q.1 | `chamsoc` | `chamsoc` | Lịch grooming được giao, hoàn thành dịch vụ, ghi nhận hao hụt |
| Nhân viên chăm sóc Q.1 khác | `user5` | `pass5` | Dùng cho dữ liệu demo `BKS106` |
| Lễ tân Q.2 | `user15` | `pass15` | Demo dữ liệu chi nhánh 2 nếu cần |

## 2. Có Bao Nhiêu Luồng Chính?

Hệ thống có 6 luồng chính:

1. Đăng nhập và phân quyền theo vai trò.
2. Lễ tân quản lý khách hàng, thú cưng, booking và hóa đơn.
3. Quản lý chi nhánh phân công grooming cho nhân viên chăm sóc.
4. Nhân viên chăm sóc thực hiện dịch vụ, xác nhận hoàn thành và tự động trừ vật tư theo định mức.
5. Quản lý kho: sản phẩm, tồn kho, nhập kho, kiểm kê, ghi nhận/duyệt hao hụt vật tư.
6. Quản trị và báo cáo: tài khoản, nhân viên, báo cáo doanh thu/vận hành.

Khi demo trên lớp, nên đi 4 luồng trọng tâm trước: lễ tân, grooming, kho vật tư, báo cáo/phân quyền. Hai luồng còn lại dùng để trả lời câu hỏi mở rộng.

## 3. Chuẩn Bị Dữ Liệu

Chạy lại seed nếu database chưa có dữ liệu demo mới:

1. `sql/01_drop_objects.sql`
2. `sql/02_tables.sql`
3. `sql/03_functions.sql`
4. `sql/04_triggers.sql`
5. `sql/05_procedures.sql`
6. `sql/07_seed_data.sql`

Dữ liệu demo quan trọng trong `sql/07_seed_data.sql`:

| Mã | Ý nghĩa | Dùng ở bước |
|---|---|---|
| `BKS105` | Lịch grooming ngày `30/05/2026`, chưa phân công | Quản lý phân công nhân viên |
| `BKS106` | Lịch grooming ngày `30/05/2026`, đã giao `EMP005`, đang thực hiện | Nhân viên `user5` hoàn thành và trừ vật tư |
| `SV008` | Tắm spa dưỡng lông chó, có định mức nhiều vật tư | Demo cấu hình định mức |
| `SV010` | Vệ sinh tai chuyên sâu chó, có định mức vật tư | Demo hoàn thành/trừ tồn kho nhanh |
| `MW003`, `MW007` | Phiếu hao hụt đang chờ duyệt ở chi nhánh Q.1 | Quản lý duyệt hao hụt |

Nếu không thấy `BKS105` hoặc `BKS106` trên màn hình grooming, nghĩa là database đang dùng dữ liệu cũ. Chạy lại `sql/07_seed_data.sql` sau khi tạo bảng.

## 4. Kịch Bản Demo 25 Phút

### Luồng 1: Đăng Nhập Và Phân Quyền

Đăng nhập lần lượt:

1. `ceo/ceo`: chỉ xem dashboard, báo cáo, tài khoản cấp cao.
2. `quanly/quanly`: thấy nghiệp vụ chi nhánh, kho, dịch vụ, nhân viên.
3. `letan/letan`: thấy khách hàng, booking, grooming, hóa đơn, sản phẩm nhưng không thấy giá nhập.
4. `chamsoc/chamsoc`: thấy lịch chăm sóc, sản phẩm tham khảo, hao hụt vật tư; không thấy hóa đơn hay nghiệp vụ tài chính.

Điểm cần nói với giảng viên: hệ thống không chỉ ẩn nút ở giao diện, tầng BUS cũng kiểm tra quyền trước khi thao tác.

### Luồng 2: Lễ Tân Tạo Booking Và Thanh Toán

Tài khoản: `letan/letan`.

Thứ tự thao tác:

1. Vào `Khách Hàng`: tìm khách hoặc thêm khách mới, kiểm tra thú cưng đi kèm.
2. Vào `Booking`: tạo booking phòng/dịch vụ cho thú cưng.
3. Check-in booking khi khách đến.
4. Sau khi dịch vụ/phòng đủ điều kiện tính tiền, vào `Hóa Đơn`.
5. Tạo hóa đơn từ booking hoặc dịch vụ đã hoàn thành.
6. Chọn hóa đơn chờ thanh toán, bấm `Thanh toán`.
7. Chọn `Tiền mặt`, nhập số tiền khách đưa lớn hơn số còn lại, ví dụ còn `350.000` thì nhập `500000`.
8. Hệ thống hiển thị tiền thối ngay trong dialog và cập nhật trạng thái sau khi xác nhận.

Điểm cần nói: đây là luồng thực tế của lễ tân, từ tiếp nhận khách đến thu tiền. Lễ tân không được xem giá nhập sản phẩm để bảo mật kinh doanh.

### Luồng 3: Quản Lý Phân Công Grooming

Tài khoản: `quanly/quanly`.

Thứ tự thao tác:

1. Vào `Grooming`.
2. Mở chức năng phân công nhân viên.
3. Chọn ngày `30/05/2026`.
4. Chọn công việc `BKS105`.
5. Chọn nhân viên chăm sóc, ví dụ `EMP005 - Nhân viên B1 5`.
6. Lưu phân công.

Kết quả mong đợi: `BKS105` chuyển từ `PENDING` sang `SCHEDULED`, có nhân viên phụ trách.

Điểm cần nói: quản lý chi nhánh điều phối nhân viên theo lịch, tránh giao trùng ca, và dữ liệu sau phân công sẽ hiện ở màn hình của nhân viên chăm sóc.

### Luồng 4: Nhân Viên Chăm Sóc Hoàn Thành Dịch Vụ Và Trừ Vật Tư

Cách demo nhanh: dùng `user5/pass5`, vì seed đã có `BKS106` đang ở trạng thái `IN_PROGRESS`.

Thứ tự thao tác:

1. Đăng nhập `user5/pass5`.
2. Vào `Grooming`.
3. Chọn ngày `30/05/2026`.
4. Tìm công việc `BKS106`.
5. Bấm `Hoàn thành`.
6. Dialog hiện danh sách vật tư từ bảng `service_product_standard`, ví dụ dung dịch vệ sinh tai và bông gòn.
7. Kiểm tra cột định mức, thực tế, tồn hiện tại.
8. Xác nhận hoàn thành.

Kết quả mong đợi:

- Trạng thái dịch vụ chuyển sang `DONE`.
- Tồn kho chi nhánh bị trừ theo số lượng thực tế.
- Nếu vật tư không đủ tồn, hệ thống rollback và báo lỗi.

Điểm cần nói: đây là phần nghiệp vụ quan trọng nhất của grooming. Nhân viên không tự nhập bừa vật tư từ đầu, hệ thống gợi ý theo định mức đã cấu hình cho dịch vụ, loài và cân nặng thú cưng.

### Luồng 5: Cấu Hình Định Mức Vật Tư Cho Dịch Vụ

Tài khoản: `quanly/quanly`.

Thứ tự thao tác:

1. Vào `Dịch Vụ`.
2. Chọn dịch vụ, ví dụ `SV008 - Tắm spa dưỡng lông chó`.
3. Mở cấu hình vật tư tiêu hao.
4. Xem các dòng định mức theo sản phẩm, loài, khoảng cân nặng, số lượng, đơn vị.
5. Có thể thêm/sửa/xóa định mức nếu là quản lý.

Điểm cần nói: định mức vật tư là dữ liệu chuẩn để khi nhân viên hoàn thành dịch vụ, hệ thống tự sinh danh sách vật tư cần trừ.

### Luồng 6: Hao Hụt Vật Tư Và Duyệt Kho

Phần nhân viên chăm sóc:

1. Đăng nhập `chamsoc/chamsoc`.
2. Vào `Hao Hụt Vật Liệu`.
3. Tạo phiếu hao hụt: chọn sản phẩm, nhập số lượng, lý do.
4. Phiếu mới ở trạng thái `PENDING`.

Phần quản lý:

1. Đăng nhập `quanly/quanly`.
2. Vào `Hao Hụt Vật Liệu`.
3. Lọc trạng thái `PENDING`.
4. Chọn `MW003` hoặc `MW007`.
5. Duyệt hoặc từ chối.

Kết quả mong đợi:

- Nếu duyệt, tồn kho bị trừ.
- Nếu từ chối, tồn kho không đổi.

Điểm cần nói: hao hụt không trừ kho ngay khi nhân viên báo, phải qua quản lý duyệt để tránh thất thoát.

### Luồng 7: Nhập Kho, Kiểm Kê, Sản Phẩm

Tài khoản: `quanly/quanly`.

Thứ tự thao tác nhập kho:

1. Vào `Nhập Kho`.
2. Tạo phiếu nhập mới.
3. Chọn nhà cung cấp, thêm sản phẩm và số lượng.
4. Lưu phiếu.
5. Duyệt phiếu.
6. Vào `Kho Hàng` kiểm tra tồn tăng.

Thứ tự thao tác bảo mật giá nhập:

1. Đăng nhập `quanly/quanly`: vào `Sản Phẩm`, quản lý thấy giá nhập.
2. Đăng xuất, đăng nhập `letan/letan`: vào `Sản Phẩm`, cột giá nhập bị ẩn.
3. Đăng nhập `chamsoc/chamsoc`: cột giá nhập cũng bị ẩn.

Điểm cần nói: quản lý kho có đủ dữ liệu để vận hành, còn lễ tân và nhân viên chăm sóc chỉ thấy thông tin cần thiết.

### Luồng 8: Báo Cáo Và Quản Trị

Tài khoản: `ceo/ceo` hoặc `admin/admin`.

Thứ tự thao tác:

1. Vào `Dashboard` để xem tổng quan.
2. Vào `Báo Cáo` để xem doanh thu, booking, dịch vụ, phòng, tồn kho.
3. Vào `Tài Khoản` bằng `admin/admin` hoặc `ceo/ceo` để xem quản lý tài khoản.

Điểm cần nói: CEO tập trung vào số liệu toàn hệ thống, Admin tập trung vào quản trị tài khoản và phân quyền.

## 5. Thứ Tự Nói Khi Thuyết Trình

1. "Hệ thống chia thành 5 vai trò: Admin, CEO, quản lý chi nhánh, lễ tân, nhân viên chăm sóc."
2. "Mỗi vai trò nhìn thấy menu và thao tác khác nhau."
3. "Luồng kinh doanh bắt đầu từ lễ tân tạo khách hàng/booking, sau đó quản lý phân công, nhân viên chăm sóc hoàn thành dịch vụ, hệ thống trừ vật tư, cuối cùng lễ tân lập và thanh toán hóa đơn."
4. "Kho được kiểm soát bằng nhập kho, kiểm kê, trừ vật tư theo dịch vụ và duyệt hao hụt."
5. "Thông tin nhạy cảm như giá nhập được ẩn với lễ tân và nhân viên chăm sóc."
6. "CEO và quản lý dùng báo cáo để theo dõi doanh thu, tình trạng phòng, dịch vụ và tồn kho."

## 6. Lỗi Thường Gặp Khi Demo

| Hiện tượng | Cách xử lý |
|---|---|
| Không thấy lịch grooming | Kiểm tra đúng ngày `30/05/2026` hoặc chạy lại seed |
| Không thấy `BKS105` | Chưa chạy seed mới trong `sql/07_seed_data.sql` |
| Không hoàn thành được dịch vụ | Dịch vụ phải ở trạng thái `IN_PROGRESS` và đúng nhân viên phụ trách |
| Không thấy vật tư tiêu hao | Kiểm tra dịch vụ có định mức trong `service_product_standard` |
| Thanh toán không được | Hóa đơn phải còn tiền và ở trạng thái chờ thanh toán |
| Lễ tân không thấy giá nhập | Đây là đúng nghiệp vụ bảo mật, không phải lỗi |

## 7. Kết Luận Demo

Chốt với giảng viên: hệ thống không chỉ quản lý danh mục, mà đã có luồng nghiệp vụ khép kín từ đặt phòng/dịch vụ, phân công nhân viên, hoàn thành grooming, trừ vật tư, ghi nhận hao hụt, nhập kho, lập hóa đơn, thanh toán và báo cáo.
