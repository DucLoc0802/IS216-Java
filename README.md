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

* **Tên đồ án**: Hệ thống quản lý khách sạn thú cưng Pet Hotel
* **Tên ứng dụng**: PetHotel Management System
* **Mô tả**:  
  Ứng dụng hỗ trợ quản lý các nghiệp vụ chính trong khách sạn thú cưng như quản lý khách hàng, thú cưng, booking, grooming, dịch vụ, sản phẩm, hóa đơn, thanh toán, báo cáo và phân quyền người dùng theo vai trò.

* **Công nghệ sử dụng**:
  - Ngôn ngữ lập trình: Java
  - Framework giao diện: JavaFX kết hợp FXML
  - Cơ sở dữ liệu: Oracle Database
  - Kết nối CSDL: Oracle JDBC Driver
  - Quản lý thư viện & build: Maven
  - Mô hình tổ chức code: OOP, MVC, DAO/BUS/Controller

## LIÊN KẾT REPOSITORY
<a name="repository"></a>

* **Repository URL**: [https://github.com/DucLoc0802/IS216-Java](https://github.com/DucLoc0802/IS216-Java)

* **Git Clone Command**:
```bash
git clone https://github.com/DucLoc0802/IS216-Java.git
cd IS216-Java
## YÊU CẦU HỆ THỐNG
<a name="yeucau"></a>

Dự án **Hệ thống quản lý chuỗi khách sạn thú cưng Pet Hotel** là ứng dụng desktop được xây dựng bằng Java và JavaFX, sử dụng Oracle Database để lưu trữ dữ liệu. Trước khi cài đặt và chạy ứng dụng, máy tính cần đáp ứng các yêu cầu hệ thống sau:

| STT | Thành phần | Yêu cầu | Ghi chú |
|-----|------------|---------|--------|
| 1 | Hệ điều hành | Windows 10/11, macOS hoặc Linux | Khuyến nghị sử dụng Windows 10/11 để thuận tiện khi chạy script và cấu hình môi trường. |
| 2 | Ngôn ngữ lập trình | Java | Ứng dụng được xây dựng bằng ngôn ngữ Java theo hướng lập trình hướng đối tượng. |
| 3 | Java Development Kit | JDK 21 trở lên | Cần cài JDK để biên dịch và chạy ứng dụng JavaFX. |
| 4 | Framework giao diện | JavaFX 21 | Dùng để xây dựng giao diện desktop cho các màn hình như đăng nhập, lễ tân, CEO, hóa đơn, booking và báo cáo. |
| 5 | Quản lý thư viện và build | Maven hoặc Maven Wrapper | Maven dùng để tải thư viện, biên dịch project và chạy ứng dụng. Nếu project có sẵn Maven Wrapper thì không bắt buộc cài Maven thủ công. |
| 6 | Cơ sở dữ liệu | Oracle Database 19c/21c/23ai hoặc Oracle Database Free/Express Edition | Dùng để lưu trữ dữ liệu tập trung của hệ thống Pet Hotel. |
| 7 | Driver kết nối CSDL | Oracle JDBC Driver / OJDBC11 | Cho phép ứng dụng Java kết nối và truy vấn dữ liệu từ Oracle Database. |
| 8 | Công cụ quản trị CSDL | SQL Developer, DBeaver hoặc SQLPlus | Dùng để tạo user/schema, chạy script SQL, kiểm tra bảng và dữ liệu mẫu. |
| 9 | Công cụ quản lý mã nguồn | Git | Dùng để clone repository, quản lý phiên bản source code và phối hợp làm việc nhóm. |
| 10 | IDE lập trình | IntelliJ IDEA, NetBeans, Eclipse hoặc Visual Studio Code | Khuyến nghị IntelliJ IDEA để mở project Maven và chạy ứng dụng JavaFX dễ hơn. |
| 11 | Bộ nhớ RAM | Tối thiểu 4GB, khuyến nghị 8GB trở lên | Giúp chạy IDE, JavaFX và Oracle Database ổn định hơn. |
| 12 | Dung lượng ổ đĩa | Tối thiểu 2GB trống cho project, thư viện và dữ liệu mẫu | Nếu cài Oracle Database local thì cần nhiều dung lượng hơn. |
| 13 | Kết nối mạng | Cần khi clone project và tải thư viện Maven | Sau khi tải đủ thư viện, ứng dụng có thể chạy trong môi trường local nếu database đã được cấu hình. |

### Cấu hình khuyến nghị

| Thành phần | Khuyến nghị |
|-----------|------------|
| Hệ điều hành | Windows 10/11 |
| JDK | JDK 21 |
| JavaFX | JavaFX 21 |
| Database | Oracle Database Free hoặc Oracle 21c |
| IDE | IntelliJ IDEA |
| Công cụ SQL | SQL Developer hoặc DBeaver |
| RAM | 8GB trở lên |
| Build tool | Maven Wrapper đi kèm project |

### Ghi chú

- Người dùng cần cài đặt **JDK 21** và cấu hình biến môi trường `JAVA_HOME` nếu chạy bằng dòng lệnh.
- Cần khởi động **Oracle Database** trước khi chạy ứng dụng.
- Thông tin kết nối cơ sở dữ liệu cần được cấu hình đúng với user/schema của project.
- Nếu project có file `mvnw.cmd` hoặc `mvnw`, có thể chạy bằng Maven Wrapper mà không cần cài Maven riêng.
- Các script SQL cần được chạy đúng thứ tự để tạo bảng, ràng buộc, dữ liệu mẫu và các thành phần liên quan.
- Hệ thống được thiết kế cho mô hình quản lý chuỗi khách sạn thú cưng, gồm nhiều vai trò như lễ tân, quản lý chi nhánh, CEO hoặc quản trị hệ thống.
