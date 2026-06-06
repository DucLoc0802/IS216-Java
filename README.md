<p align="center">
  <a href="https://www.uit.edu.vn/" title="Trường Đại học Công nghệ Thông tin" style="border: 5;">
    <img src="https://i.imgur.com/WmMnSRt.png" alt="Trường Đại học Công nghệ Thông tin | University of Information Technology">
  </a>
</p>

<!-- Title -->
<h1 align="center"><b>IS216 - LẬP TRÌNH JAVA</b></h1>

## BẢNG MỤC LỤC
* [Giới thiệu môn học](#gioithieumonhoc)
* [Giảng viên hướng dẫn](#giangvien)
* [Thành viên nhóm](#thanhvien)
* [Đồ án môn học](#doan)
* [Liên kết Repository](#repository)
* [Yêu cầu hệ thống](#yeucau)
* [Hướng dẫn cài đặt & Cấu hình](#caidat)
* [Cấu hình Cơ sở dữ liệu Oracle](#database)
* [Hướng dẫn chạy ứng dụng](#khoidong)
* [Tài khoản đăng nhập kiểm thử](#taikhoan)

## GIỚI THIỆU MÔN HỌC
<a name="gioithieumonhoc"></a>

* **Tên môn học**: Lập trình Java
* **Mã môn học**: IS216
* **Lớp học**: IS216.Q21
* **Năm học**: 2025-2026

## GIẢNG VIÊN HƯỚNG DẪN
<a name="giangvien"></a>

* ThS. **Tạ Việt Phương** - *phuongtv@uit.edu.vn*

## THÀNH VIÊN NHÓM
<a name="thanhvien"></a>

| STT | MSSV | Họ và Tên | Github | Email |
|-----|:----:|----------:|-------:|------:|
| 1 | 24520978 | Dương Đức Lộc | [DucLoc](https://github.com/DucLoc0802) | 24520978@gm.uit.edu.vn |
| 2 | 24521034 | Châu Gia Lương | [GiaLuong](https://github.com/24520134GiaLuong) | 24521034@gm.uit.edu.vn |
| 3 | 24521045 | Phương Thiên Lộc | [ThLoc](https://github.com/thloc3706) | 24520986@gm.uit.edu.vn |
| 4 | 24521081 | Nguyễn Văn Minh | [VanMinh](https://github.com/24521081-ui) | 24521081@gm.uit.edu.vn |
| 5 | 24521093 | Nguyễn Thế Mỹ | [TheMy](https://github.com/themy130806-eng) | 24521093@gm.uit.edu.vn |

## ĐỒ ÁN MÔN HỌC
<a name="doan"></a>

* **Đồ án nhóm**: Hệ thống quản lý chuỗi khách sạn thú cưng Pet Hotel
* **Tên ứng dụng**: PetHotel Management System
* **Mô tả**:  
  Ứng dụng hỗ trợ quản lý hoạt động của chuỗi khách sạn thú cưng, bao gồm quản lý khách hàng, hồ sơ thú cưng, booking đặt phòng, lịch grooming, dịch vụ, sản phẩm, hóa đơn, thanh toán, báo cáo và phân quyền người dùng theo vai trò.

* **Công nghệ sử dụng**:
  - Ngôn ngữ: Java
  - Framework giao diện: JavaFX với FXML
  - Hệ quản trị cơ sở dữ liệu: Oracle Database
  - Kết nối cơ sở dữ liệu: Oracle JDBC Driver
  - Quản lý thư viện & Build: Maven
  - Mô hình tổ chức: OOP, MVC, DAO, BUS, Controller

## LIÊN KẾT REPOSITORY
<a name="repository"></a>

* **Repository URL**: [https://github.com/DucLoc0802/IS216-Java](https://github.com/DucLoc0802/IS216-Java)

* **Git Clone Command**:

```bash
git clone https://github.com/DucLoc0802/IS216-Java.git
cd IS216-Java
```

## YÊU CẦU HỆ THỐNG
<a name="yeucau"></a>

Trước khi cài đặt và chạy dự án, máy tính cần đáp ứng các yêu cầu tối thiểu sau:

- **Hệ điều hành**: Windows 10/11, macOS hoặc Linux.  
  Khuyến nghị sử dụng Windows 10/11 để thuận tiện khi chạy ứng dụng và cấu hình môi trường.

- **Java Development Kit (JDK)**: Phiên bản **21** trở lên.  
  JDK được sử dụng để biên dịch và chạy ứng dụng Java/JavaFX.

- **JavaFX**: Phiên bản **21** hoặc phiên bản tương thích với cấu hình Maven của project.  
  JavaFX được sử dụng để xây dựng giao diện desktop cho các màn hình như đăng nhập, lễ tân, CEO, booking, hóa đơn và báo cáo.

- **Cơ sở dữ liệu**: Oracle Database 19c/21c/23ai hoặc Oracle Database Free/Express Edition.  
  Oracle Database được sử dụng để lưu trữ dữ liệu tập trung của hệ thống Pet Hotel.

- **Công cụ quản lý thư viện và build**: Maven hoặc Maven Wrapper.  
  Maven dùng để tải thư viện, biên dịch source code và chạy ứng dụng.

- **Công cụ quản trị cơ sở dữ liệu**: SQL Developer, DBeaver hoặc SQLPlus.  
  Các công cụ này dùng để tạo user/schema, chạy script SQL, kiểm tra bảng và dữ liệu mẫu.

- **Công cụ quản lý mã nguồn**: Git.  
  Git được sử dụng để clone repository, quản lý phiên bản mã nguồn và phối hợp làm việc nhóm.

- **IDE khuyến nghị**: IntelliJ IDEA, NetBeans, Eclipse hoặc Visual Studio Code.  
  Khuyến nghị sử dụng IntelliJ IDEA để mở project Maven và chạy ứng dụng JavaFX thuận tiện hơn.

- **Bộ nhớ RAM**: Tối thiểu 4GB, khuyến nghị 8GB trở lên.  
  RAM cao hơn giúp chạy IDE, Maven, JavaFX và Oracle Database ổn định hơn.

- **Dung lượng ổ đĩa**: Tối thiểu 2GB trống cho source code, thư viện và dữ liệu mẫu.  
  Nếu cài Oracle Database local, máy cần thêm dung lượng trống cho database.

## HƯỚNG DẪN CÀI ĐẶT & CẤU HÌNH
<a name="caidat"></a>

### Bước 1: Clone dự án về máy

Mở Terminal/Command Prompt và chạy lệnh sau:

```bash
git clone https://github.com/DucLoc0802/IS216-Java.git
cd IS216-Java
```

### Bước 2: Mở project bằng IDE

Có thể mở project bằng IntelliJ IDEA, NetBeans, Eclipse hoặc Visual Studio Code.

Nếu sử dụng IntelliJ IDEA:

1. Chọn **Open Project**.
2. Chọn thư mục project vừa clone.
3. Chờ Maven tự động tải các thư viện cần thiết.
4. Kiểm tra phiên bản JDK đang sử dụng là JDK 21 hoặc phiên bản phù hợp với project.

### Bước 3: Cấu hình môi trường ứng dụng

Trong project, thông tin kết nối cơ sở dữ liệu có thể được cấu hình trong file cấu hình hoặc trong lớp kết nối database, ví dụ:

```text
config.properties
database.properties
DBConnection.java
```

Kiểm tra và chỉnh sửa các thông số kết nối Oracle cho phù hợp với máy đang chạy:

```properties
db.username=PET_HOTEL
db.password=123456

# Các thông số dưới đây có thể thay đổi tùy cấu hình Oracle trên máy:
db.host=localhost
db.port=1521
db.service=freepdb1
```

## CẤU HÌNH CƠ SỞ DỮ LIỆU ORACLE
<a name="database"></a>

Ứng dụng sử dụng Oracle Database để lưu trữ dữ liệu của hệ thống quản lý chuỗi khách sạn thú cưng. Thầy có thể thực hiện theo các bước dưới đây để khởi tạo cơ sở dữ liệu.

### Bước 1: Tạo User/Schema mới

Kết nối vào Oracle Database bằng tài khoản quản trị như `SYSTEM` hoặc `SYSDBA`, sau đó chạy lệnh:

```sql
CREATE USER PET_HOTEL IDENTIFIED BY 123456;
GRANT CONNECT, RESOURCE, CREATE VIEW, UNLIMITED TABLESPACE TO PET_HOTEL;
```

### Bước 2: Chạy các tập lệnh SQL

Kết nối vào Oracle bằng tài khoản `PET_HOTEL` vừa tạo. Sau đó mở thư mục `sql/` trong project và chạy các script SQL theo đúng thứ tự.

Ví dụ:

```text
sql/01_drop_objects.sql
sql/02_tables.sql
sql/03_functions.sql
sql/04_triggers.sql
sql/05_procedures.sql
sql/06_sample_data.sql
```

> **Lưu ý:** Tên file SQL thực tế có thể khác tùy project. Cần chạy script tạo bảng trước, sau đó mới chạy trigger, procedure và dữ liệu mẫu.

### Bước 3: Kiểm tra dữ liệu mẫu

Sau khi chạy script SQL, có thể kiểm tra nhanh bằng một số câu lệnh:

```sql
SELECT * FROM APP_USER;
SELECT * FROM CUSTOMER;
SELECT * FROM PET;
SELECT * FROM BOOKING;
SELECT * FROM ORDERS;
SELECT * FROM PAYMENT;
```

Nếu các bảng có dữ liệu, cơ sở dữ liệu đã được khởi tạo thành công.

## HƯỚNG DẪN CHẠY ỨNG DỤNG
<a name="khoidong"></a>

Thầy có thể khởi động ứng dụng JavaFX theo các cách sau:

### Cách 1: Chạy bằng Maven Wrapper

Trên Windows:

```bash
.\mvnw.cmd clean javafx:run
```

Trên macOS/Linux:

```bash
chmod +x mvnw
./mvnw clean javafx:run
```

### Cách 2: Chạy bằng Maven đã cài sẵn

```bash
mvn clean javafx:run
```

### Cách 3: Chạy trực tiếp từ IDE

1. Mở project bằng IntelliJ IDEA, NetBeans hoặc Eclipse.
2. Chờ Maven tải đầy đủ thư viện.
3. Kiểm tra cấu hình JDK.
4. Chọn class main của ứng dụng.
5. Bấm **Run** để khởi động hệ thống.

## TÀI KHOẢN ĐĂNG NHẬP KIỂM THỬ
<a name="taikhoan"></a>

Sau khi ứng dụng khởi động thành công, thầy có thể đăng nhập bằng tài khoản mẫu đã được chuẩn bị trong cơ sở dữ liệu.

Ví dụ:

```text
Tài khoản: admin
Mật khẩu: admin
```

Một số tài khoản theo vai trò có thể được chuẩn bị trong dữ liệu mẫu:

```text
Role Lễ tân:
Tài khoản: letan
Mật khẩu: letan

Role CEO / Quản trị:
Tài khoản: ceo
Mật khẩu: ceo
```

> **Lưu ý:** Tài khoản đăng nhập thực tế phụ thuộc vào dữ liệu trong file SQL mẫu của project. Nếu đăng nhập không thành công, cần kiểm tra lại bảng tài khoản trong cơ sở dữ liệu.

## CHỨC NĂNG CHÍNH CỦA HỆ THỐNG

Hệ thống quản lý chuỗi khách sạn thú cưng Pet Hotel hỗ trợ các nhóm chức năng chính sau:

- Đăng nhập và phân quyền người dùng.
- Quản lý khách hàng.
- Quản lý hồ sơ thú cưng.
- Quản lý booking đặt phòng.
- Quản lý lịch grooming.
- Quản lý dịch vụ.
- Quản lý sản phẩm/vật tư.
- Quản lý hóa đơn.
- Thanh toán hóa đơn.
- Báo cáo và thống kê.
- Quản lý tài khoản và phân quyền đối với vai trò CEO/quản trị.

## GHI CHÚ

Đồ án được xây dựng nhằm vận dụng kiến thức lập trình Java, lập trình hướng đối tượng, JavaFX, kết nối cơ sở dữ liệu Oracle và tổ chức project theo mô hình nhiều lớp.

Trong quá trình phát triển, nhóm sử dụng GitHub để quản lý mã nguồn, theo dõi lịch sử thay đổi và phối hợp giữa các thành viên.
