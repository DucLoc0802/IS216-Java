# Nguyen Van Minh - Customer + Pet Flow Work

## Ngay lam viec

- 09/05/2026: UC-CUS-01, UC-CUS-02, UC-PET-01, UC-PET-02.
- 16/05/2026: UC-CUS-03, UC-CUS-07, UC-PET-03, UC-PET-06, UC-PET-07.

## Tai lieu da doi chieu

- `docs/use-case.txt`: xac nhan nhom chuc nang Customer + Pet va cac UC can code.
- `docs/PhanChiaCongViec.docx`: xac nhan Nguyen Van Minh phu trach Customer + Pet.
- `docs/DoAnJava.docx`: chua co trong workspace tai thoi diem code, nen dac ta chi tiet duoc doi chieu theo `use-case.txt` va yeu cau trong prompt.

## Use case da trien khai

| UC | Ten use case | Man hinh/controller | DAO/BUS/model | Trang thai |
| --- | --- | --- | --- | --- |
| UC-CUS-01 | Tra cuu khach hang | `CustomerManagement.fxml`, `CustomerController` | `CustomerBUS`, `CustomerDAO`, `Customer` | Hoan thanh |
| UC-CUS-02 | Them khach hang | `CustomerManagement.fxml`, dialog trong `CustomerController` | `CustomerBUS`, `CustomerDAO`, `Customer` | Hoan thanh |
| UC-CUS-03 | Xem chi tiet khach hang | `CustomerController` | `CustomerBUS`, `PetDAO`, `Customer`, `Pet` | Hoan thanh |
| UC-CUS-07 | Lien ket/them thu cung cua khach hang | `CustomerController` | `PetBUS`, `PetDAO`, `Pet` | Hoan thanh |
| UC-PET-01 | Tra cuu thu cung | `PetManagement.fxml`, `PetController` | `PetBUS`, `PetDAO`, `CustomerDAO`, `Pet` | Hoan thanh |
| UC-PET-02 | Them thu cung | `PetManagement.fxml`, dialog trong `PetController` | `PetBUS`, `PetDAO`, `CustomerBUS`, `Pet` | Hoan thanh |
| UC-PET-03 | Xem chi tiet thu cung | `PetController` | `PetBUS`, `CustomerDAO`, `PetHealthRecordDAO`, `Pet` | Hoan thanh |
| UC-PET-06 | Lien ket chu so huu | `PetController` | `PetBUS`, `PetDAO`, `CustomerDAO` | Hoan thanh |
| UC-PET-07 | Ghi nhan tinh trang suc khoe | `PetController` | `PetBUS`, `PetHealthRecordDAO`, `PetHealthRecord` | Hoan thanh, can booking_id theo schema |

## File da sua

- `src/main/java/PetHotel/dao/CustomerDAO.java`: them search theo `customer_id` va ham `generateNextCustomerId()` sinh `CUS001`, `CUS002`, ...
- `src/main/java/PetHotel/dao/PetDAO.java`: them `findAll()`, search theo `pet_id`, `generateNextPetId()`, va `updateOwner()`.
- `src/main/java/PetHotel/bus/CustomerBUS.java`: khi them khach hang dung `CustomerDAO.generateNextCustomerId()`.
- `src/main/java/PetHotel/bus/PetBUS.java`: khi them thu cung dung `PetDAO.generateNextPetId()`, them `getAllPets()` va `linkOwner()`.
- `src/main/java/PetHotel/gui/controller/CustomerController.java`: noi UI voi BUS/DAO cho danh sach, tra cuu, them, xem chi tiet, them thu cung cho khach hang.
- `src/main/java/PetHotel/gui/controller/PetController.java`: noi UI voi BUS/DAO cho danh sach, tra cuu, them, chi tiet, lien ket chu, health record.
- `src/main/resources/PetHotel/gui/view/CustomerManagement.fxml`: cap nhat prompt tim kiem va nut them/lien ket thu cung.
- `src/main/resources/PetHotel/gui/view/PetManagement.fxml`: cap nhat prompt tim kiem va nut lien ket chu so huu.

## File tao moi

- `docs/flow-work/NguyenVanMinh-Customer-Pet-flow-work.md`: ghi lai flow-work, UC, file thay doi, luong nghiep vu va ghi chu merge.

## Luong them khach hang va sinh customer_id

1. Nguoi dung bam `Them Khach Hang` tren man hinh Customer.
2. Dialog hien ma du kien o o read-only.
3. `CustomerBUS.createCustomer()` validate ho ten, so dien thoai, email va trung phone/email.
4. `CustomerDAO.generateNextCustomerId()` doc cac ma dang `CUS%`, chi lay ma hop le `CUS[0-9]+`, tim so lon nhat va tang 1.
5. Format bang `CUS%03d`, vi du `CUS001`, `CUS010`, `CUS1000`.
6. `CustomerDAO.insert()` luu vao bang `customer`, danh sach reload va hien ma moi.

## Luong them thu cung va sinh pet_id

1. Nguoi dung bam `Them Thu Cung` tren man hinh Pet, hoac bam `Them/Lien ket Thu Cung` tu man hinh Customer.
2. Dialog hien ma du kien o o read-only.
3. Neu them tu Customer, chu so huu duoc gan san bang `customer_id` dang chon.
4. `PetBUS.createPet()` validate ten, loai, gioi tinh, can nang va customer ton tai.
5. `PetDAO.generateNextPetId()` doc cac ma dang `PET%`, chi lay ma hop le `PET[0-9]+`, tim so lon nhat va tang 1.
6. Format bang `PET%03d`, vi du `PET001`, `PET010`, `PET1000`.
7. `PetDAO.insert()` luu vao bang `pet`, danh sach reload va hien ma moi.

## Luong lien ket thu cung voi khach hang

- Tu Customer detail: bam nut them/lien ket thu cung, tao pet moi voi `customer_id` cua khach hang dang chon.
- Tu Pet detail: bam `Lien Ket Chu`, chon customer trong danh sach, neu khac chu hien tai thi hien xac nhan doi chu, sau do `PetBUS.linkOwner()` goi `PetDAO.updateOwner()`.
- Database hien tai dung quan he truc tiep `pet.customer_id`, khong co bang lien ket owner rieng.

## Luong tra cuu va xem chi tiet

- Customer search goi `CustomerBUS.searchCustomer()` va `CustomerDAO.search()` theo ma, ten, phone, email.
- Customer detail hien thong tin co ban va danh sach pet cua customer tu `PetDAO.findByCustomerId()`.
- Pet search goi `PetBUS.searchPet()` va `PetDAO.search()` theo ma pet, ten, loai, giong, ten chu.
- Pet detail hien thong tin pet, chu so huu tu `CustomerDAO.findById()`, va health record moi nhat tu `PetHealthRecordDAO`.

## Luong ghi nhan suc khoe

1. Chon pet tren man hinh Pet.
2. Bam `Suc Khoe` hoac `Them`.
3. Nhap `booking_id`, tinh trang va ghi chu.
4. `PetBUS.addHealthRecord()` validate pet, booking_id, note, status.
5. `PetHealthRecordDAO.insert()` luu vao bang `pet_health_record`.

## Database that va du lieu tam

- Dung database that qua DAO cho `customer`, `pet`, va `pet_health_record`.
- Khong hard-code sample data cho danh sach Customer/Pet.
- Cac thong tin chua co cot trong schema `pet` nhu ngay sinh/tuoi/mau long dang hien `-`.
- Health record can `booking_id` vi cot nay NOT NULL trong schema hien tai.

## Loi con ton tai / ghi chu

- `docs/DoAnJava.docx` chua co trong workspace nen chua doi chieu duoc file nay.
- UC sua/xoa/lich su dich vu Customer/Pet duoc giu ngoai pham vi va chi hien thong bao.
- `pet_health_record.booking_id` bat buoc NOT NULL, nen ghi nhan suc khoe ban dau van can nhap booking hop le.
- Cac cot ngay sinh, mau long cua pet chua co trong schema `pet`, UI hien "Chua co cot trong DB" thay vi de trong.

## Cap nhat UI va logic 15/05/2026

- Da sua logic sinh `customer_id` de bo qua ma legacy/random dang so qua lon nhu `CUS4031286`; ma hop le duoc doc theo prefix `CUS`, tach so, lay max va format `CUS%03d`.
- Da sua logic sinh `pet_id` de bo qua ma legacy/random dang so qua lon nhu `PET6146323`; ma hop le duoc doc theo prefix `PET`, tach so, lay max va format `PET%03d`.
- Da doi dong health trong ho so pet tu text mo ho "Chua ghi nhan" thanh nhan ro nghia: `Suc khoe: Chua ghi nhan`, `Suc khoe: Binh thuong`, `Suc khoe: Can theo doi`, hoac `Suc khoe: Co trieu chung bat thuong`.
- Da thiet ke lai form them khach hang bang modal rieng dong bo concept kem/nau: header, subtitle, avatar tron, card noi dung, input bo goc, loi hien trong form.
- Da thiet ke lai form them thu cung bang modal rieng: ma PET read-only, chon chu so huu, tu fill SDT chu, nhom thong tin pet va suc khoe ban dau.
- Da thiet ke lai chi tiet khach hang bang profile card rieng, khong dung Alert mac dinh cho chi tiet: avatar chu cai, thong tin KH, danh sach pet lien ket, nut nhanh.
- Da hoan thien ho so chi tiet thu cung ben phai: health co nhan ro nghia, owner hien `CUSxxx - ten chu`, khi chua chon pet thi xoa du lieu cu.
- Da them form ghi nhan suc khoe rieng: tinh trang tong quat, trieu chung, ghi chu, ngay ghi nhan, nguoi ghi nhan, booking_id.
- Da cap nhat `style.css` them cac class rieng cho modal Customer/Pet: `ph-modal-root`, `ph-profile-header`, `ph-profile-avatar`, `ph-content-card`, `ph-form-input`, `ph-list-row`, ...

## File sua trong dot cap nhat UI

- `src/main/java/PetHotel/dao/CustomerDAO.java`: loc bo ma CUS legacy/random qua lon khi sinh ID tiep theo.
- `src/main/java/PetHotel/dao/PetDAO.java`: loc bo ma PET legacy/random qua lon khi sinh ID tiep theo.
- `src/main/java/PetHotel/gui/controller/CustomerController.java`: thay dialog them/chi tiet bang modal/card styled, reload va select ban ghi moi.
- `src/main/java/PetHotel/gui/controller/PetController.java`: thay form them/lien ket/health bang modal styled, cap nhat health label ro nghia.
- `src/main/resources/PetHotel/gui/css/style.css`: them style rieng cho form/profile Customer + Pet.

## File tao moi trong dot cap nhat UI

- Khong tao FXML/CSS rieng moi; dung controller hien co va bo sung class vao `style.css`.

## Cap nhat Pet UI 15/05/2026

- Da bo panel `Ho So Thu Cung` co dinh ben phai trong `PetManagement.fxml`.
- Man hinh Quan ly Thu Cung hien chi con thong ke, filter/search, toolbar va bang danh sach full-width de tranh cat cot.
- Da chuyen luong xem chi tiet sang modal `Chi Tiet Thu Cung` trong `PetController`: double-click dong pet tren table de mo form chi tiet.
- Modal chi tiet pet dong bo concept kem/nau: header co line nhan mau nau, avatar tron chu cai dau ten pet, card thong tin pet va card chu so huu/suc khoe.
- Da bo nut `Lien Ket Chu` khoi toolbar chinh cua man hinh Thu Cung. Ly do: man hinh Khach Hang da co luong Them/Lien ket Thu Cung.
- Trong form chi tiet pet chi hien thong tin chu so huu; neu chua co thi hien `Chua lien ket chu so huu`.
- Da chuyen luong ghi nhan suc khoe vao form chi tiet pet: mo chi tiet pet -> bam `Ghi nhan suc khoe` -> luu -> reload table va mo lai chi tiet voi health moi nhat.
- Nut `Suc Khoe` tren toolbar chinh van giu nhu shortcut khi da chon pet; nut nay mo cung form ghi nhan suc khoe.
- Nut `Lich su dich vu` trong modal chi tiet bi disable va co tooltip `Chua trien khai`.
- Cot suc khoe trong table da rut gon thanh: `Chua ghi nhan`, `Binh thuong`, `Can theo doi`, `Bat thuong`; khong lap tien to `Suc khoe:` trong tung dong.

## File sua trong dot cap nhat Pet UI

- `src/main/resources/PetHotel/gui/view/PetManagement.fxml`: bo side detail panel, mo rong table, bo nut lien ket chu khoi toolbar chinh, tang width cac cot.
- `src/main/java/PetHotel/gui/controller/PetController.java`: bo phu thuoc vao panel phai, them modal chi tiet pet, dua health record flow vao modal chi tiet, rut gon health text tren table.

## Database va du lieu tam trong dot cap nhat Pet UI

- Chi tiet pet va owner van doc database that qua `PetDAO`, `CustomerDAO`, `PetHealthRecordDAO`.
- Ghi nhan suc khoe van luu database that qua bang `pet_health_record`.
- `booking_id` van bat buoc theo schema hien tai.
- Ngay sinh, tuoi, mau long van la du lieu tam/hien thi `Chua co cot trong DB` vi bang `pet` chua co cac cot nay.

## Cap nhat fix form Pet va ngay tham gia 15/05/2026

- Da fix loi cua so `Them Thu Cung` bi trang khi mo tu `Ho So Khach Hang`.
- Nguyen nhan: form them pet truoc do duoc dung truc tiep bang Java node trong controller, khong co FXML/controller rieng nen kho debug khi mo long tu modal chi tiet khach hang; da chuyen sang FXML that co root layout va controller rieng.
- Da tao `src/main/resources/PetHotel/gui/view/PetForm.fxml` lam form chung cho 2 mode:
  - Mo tu `Ho So Khach Hang`: owner duoc truyen vao va combobox owner bi disable/read-only.
  - Mo tu man hinh `Quan Ly Thu Cung`: user chon owner tu combobox.
- Da tao `src/main/java/PetHotel/gui/controller/PetFormController.java` xu ly load owner, set owner, validate, sinh PET ID preview, goi `PetBUS.createPet()`, callback reload.
- Da them log debug:
  - `Opening pet form from customer detail...`
  - customer id/name dang truyen.
  - FXML path `/PetHotel/gui/view/PetForm.fxml`.
  - `PetFormController initialized`.
  - owner duoc chon.
  - pet id preview va owner khi luu.
  - stack trace neu load/insert loi.
- Da sua `CustomerController` de nut `Them/Lien ket Thu Cung` load `PetForm.fxml`, truyen `Customer` vao `PetFormController.setOwner(customer)`, va dung callback reload danh sach khach hang/ho so khach hang sau khi luu.
- Da sua `PetController` de nut `Them Thu Cung` cung load `PetForm.fxml` va callback reload table Pet.
- Da sua ngay tham gia khach hang: sau khi tao customer, controller fetch lai customer tu DB bang `CustomerDAO.findById()` de co `created_at`; table va form chi tiet hien `dd/MM/yyyy`. Neu DB chua tra ve `created_at`, UI fallback ve ngay hien tai thay vi dau `-`.
- Da dam bao PET ID khi them moi van di qua `PetDAO.generateNextPetId()`, bo qua ma test/legacy sai format qua lon va format `PET%03d`.
- Sau khi luu pet:
  - Man hinh Pet reload table.
  - Ho so Khach Hang reload danh sach pet lien ket va so luong pet.

## File sua/tao trong dot fix form Pet

- Tao moi: `src/main/resources/PetHotel/gui/view/PetForm.fxml`.
- Tao moi: `src/main/java/PetHotel/gui/controller/PetFormController.java`.
- Sua: `src/main/java/PetHotel/gui/controller/CustomerController.java`.
- Sua: `src/main/java/PetHotel/gui/controller/PetController.java`.
- Build kiem tra: `mvn clean compile` thanh cong.

## Cap nhat toi uu form Them/Lien ket Thu Cung 15/05/2026

- Da fix loi form `Them/Lien ket Thu Cung` phai phong to/maximize moi hien du noi dung.
- Nguyen nhan chinh: `PetForm.fxml` dung root `VBox` khong co pref size ro rang, Stage dang mo bang kich thuoc nho hon form va JavaFX tu tinh layout chua on dinh khi modal duoc long tu `Ho So Khach Hang`.
- Da doi `PetForm.fxml` sang root `BorderPane` co `prefWidth=820`, `prefHeight=740`.
- Da dat `ScrollPane` o vung center voi `fitToWidth=true`, `fitToHeight=false`; footer `Luu/Huy` nam o bottom nen luon thay duoc, khong bi day khoi cua so.
- Da sua cac field read-only nhu ma pet, ngay sinh, mau long, SDT chu de `focusTraversable=false`, tranh viec ma `PET001` bi auto focus/select xanh khi mo form.
- Da set kich thuoc modal khi mo form:
  - Scene: `820 x 740`.
  - Stage: `width=820`, `height=740`.
  - Min size: `760 x 680`.
  - Goi `root.applyCss()`, `root.layout()`, `stage.centerOnScreen()` truoc khi show.
- Da toi uu `PetFormController.initialize()`:
  - Khong con tu dong query toan bo danh sach customer trong initialize.
  - Khi mo tu `Ho So Khach Hang`, controller chi nhan owner co san qua `setOwner(customer)` va khong load combobox owner tu DB.
  - Khi mo tu `Quan Ly Thu Cung`, `PetController` moi goi `prepareForPetManagement()` de load danh sach owner.
  - Sinh ma pet preview duoc dua sang `Task` nen khong khoa JavaFX Application Thread; nut Luu disabled den khi sinh ID xong.
- Da them log thoi gian cho luong mo form:
  - click add/link pet.
  - load FXML xong.
  - set owner xong.
  - generate pet id xong.
  - stage ready/closed.
- Focus mac dinh khi mo form la field `Ten thu cung`, khong phai field ma pet.
- Build kiem tra: `mvn clean compile` thanh cong.

## File sua trong dot toi uu form Pet

- `src/main/resources/PetHotel/gui/view/PetForm.fxml`: doi root layout sang `BorderPane`, set pref size, dat `ScrollPane` center va footer bottom, sua focus/read-only cho cac field.
- `src/main/java/PetHotel/gui/controller/PetFormController.java`: bo query owner khoi initialize, them mode `prepareForPetManagement()`, dung `Task` sinh pet id, set focus vao ten thu cung.
- `src/main/java/PetHotel/gui/controller/CustomerController.java`: set fixed size cho modal them/lien ket pet tu customer detail, goi `applyCss/layout/centerOnScreen`, them log timing.
- `src/main/java/PetHotel/gui/controller/PetController.java`: set fixed size cho modal them pet tu pet management, chi load owner list trong mode nay, them log timing.

## Cap nhat flow xem thu cung lien ket tu Ho So Khach Hang 15/05/2026

- Xac nhan tim khach hang bang 3 so cuoi SDT da on, khong sua search customer trong dot nay.
- Da giu luong lay thu cung lien ket bang database that qua `PetDAO.findByCustomerId(String customerId)`, query truc tiep theo cot `pet.customer_id` trong schema hien tai.
- Da sua `CustomerController` phan `Ho So Khach Hang`:
  - Khu vuc `Thu cung lien ket` hien tung dong/card pet tu DB.
  - Moi dong hien ma pet, ten pet, loai/giong, suc khoe ngan va trang thai hien thi.
  - Neu khong co pet thi hien `Chua co thu cung lien ket`.
  - Double-click dong pet hoac bam nut `Xem ho so` se lay `pet_id` va chuyen sang man hinh `Thu Cung`.
- Da bo sung dieu huong nho trong `MainController`:
  - `showPetManagement(String selectedPetId)` load `PetManagement.fxml`, cap nhat title topbar, active sidebar Pet va truyen `pet_id` vao `PetController`.
  - `getActiveInstance()` giup modal customer detail goi lai main content hien tai ma khong refactor kien truc.
- Da bo sung `SidebarController.setActivePetMenu()` de active menu `Thu Cung` khi chuyen tab tu ho so khach hang.
- Da bo sung `PetController.selectAndOpenPet(String petId)`:
  - Reload danh sach pet.
  - Select/highlight row co `pet_id`.
  - Scroll den row do.
  - Mo dialog/card `Chi Tiet Thu Cung` cho dung pet.
- Ho so thu cung hien bang dialog/card rieng trong `PetController`, khong dung panel phai co dinh.
- Ghi nhan suc khoe van thuc hien trong dialog chi tiet pet: bam `Ghi nhan suc khoe`, luu vao bang `pet_health_record`, reload table va mo lai ho so pet voi health moi nhat.
- Cot suc khoe tren table Pet tiep tuc hien ngan: `Chua ghi nhan`, `Binh thuong`, `Can theo doi`, `Bat thuong`; trong chi tiet hien nhan day du `Suc khoe: ...`.
- Sua thong tin thu cung:
  - BUS/DAO da co `PetBUS.updatePet()` va `PetDAO.update()` cho ten, loai, giong, gioi tinh, can nang, ghi chu.
  - Trong dot nay chua tao them form sua moi de tranh mo rong ngoai flow chinh; can noi UI edit rieng neu team yeu cau tiep.
- Xoa/ngung hoat dong thu cung:
  - Bang `pet` hien khong co cot `status/is_active`, nen chua the ngung hoat dong dung nghia qua DB.
  - Khong dung xoa cung trong flow le tan vi co rang buoc health/booking history; can bo sung cot trang thai neu muon UC-PET-05 dung nghiep vu.
- Build kiem tra: `mvn clean compile` thanh cong.

## File sua trong dot xem thu cung lien ket

- `src/main/java/PetHotel/gui/controller/CustomerController.java`: hien danh sach pet lien ket tu DB, them dong/card pet co nut `Xem ho so`, double-click pet de chuyen sang man Pet.
- `src/main/java/PetHotel/gui/controller/MainController.java`: them `showPetManagement(selectedPetId)` va active instance de dieu huong tu modal Customer detail sang Pet screen.
- `src/main/java/PetHotel/gui/controller/SidebarController.java`: them method active menu Pet khi dieu huong tu Customer detail.
- `src/main/java/PetHotel/gui/controller/PetController.java`: them `selectAndOpenPet(petId)` de select/highlight row va mo ho so pet tu pet_id duoc truyen.

## Cap nhat sua dung flow Pet trong Khach Hang 15/05/2026

- Da search va xac dinh file render sidebar/menu chinh:
  - `src/main/resources/PetHotel/gui/view/Sidebar.fxml`
  - Controller: `src/main/java/PetHotel/gui/controller/SidebarController.java`
  - Layout include sidebar: `src/main/resources/PetHotel/gui/view/MainDashboard.fxml`
  - Main content controller: `src/main/java/PetHotel/gui/controller/MainController.java`
- Da an muc `Thu Cung` khoi sidebar:
  - Trong `Sidebar.fxml`, node `menuPet` duoc set `visible=false`, `managed=false`.
  - Trong `SidebarController.initialize()`, goi `hideMenu(menuPet)` de dam bao moi role khong hien menu `Thu Cung`.
- Da chuyen flow xem thu cung vao trong `Ho So Khach Hang`:
  - Khong sua logic search customer, bao gom tim bang 3 so cuoi SDT.
  - `CustomerController.openCustomerDetail()` van load danh sach pet lien ket tu database qua `PetDAO.findByCustomerId()`/`CustomerBUS.getPetsOfCustomer()`.
  - Moi pet trong section `Thu cung lien ket` co thong tin ma pet, ten, loai/giong, suc khoe ngan, trang thai, nut `Xem chi tiet`.
- Da sua click pet trong `Ho So Khach Hang`:
  - Nut `Xem chi tiet` va double-click dong pet goi `CustomerController.openLinkedPetDetail(...)`.
  - Flow nay mo dialog/card `Chi Tiet Thu Cung` truc tiep trong `CustomerController`, khong dong `Ho So Khach Hang`.
  - Khong con goi dieu huong sang man hinh `PetManagement` trong flow click pet tu customer detail.
- Da bo viec chuyen tab Pet trong flow chinh:
  - Khong load lai `PetManagement.fxml`.
  - Khong active sidebar `Thu Cung`.
  - Khong clear search customer.
  - Dong dialog `Chi Tiet Thu Cung` thi van o nguyen `Ho So Khach Hang`.
- Da them dialog/card chi tiet pet trong `CustomerController`:
  - Hien ma pet, ten, loai, giong, can nang, chu so huu, SDT chu, ghi chu, suc khoe gan nhat, ngay ghi nhan.
  - Co nut `Ghi nhan suc khoe`, `Sua thong tin` (tam disable), `Dong`.
  - Ghi nhan suc khoe van luu database that qua `PetBUS.addHealthRecord()` va `pet_health_record`.
  - Sau khi luu suc khoe, refresh lai section `Thu cung lien ket` cua ho so khach hang, khong reload CustomerController.
- Da doi text cot thao tac trong table Customer tu `Chi tiet / them pet` thanh `Chi tiet`.
- Da doi nut them pet trong `Ho So Khach Hang` thanh nut phu `Them pet moi`, khong con la flow chinh.
- Build kiem tra: `mvn clean compile` thanh cong.

## File sua trong dot dua Pet vao Customer

- `src/main/resources/PetHotel/gui/view/Sidebar.fxml`: an node `menuPet` khoi sidebar bang `visible=false`, `managed=false`.
- `src/main/java/PetHotel/gui/controller/SidebarController.java`: goi `hideMenu(menuPet)` trong `initialize()` de dam bao menu Pet khong hien theo role.
- `src/main/java/PetHotel/gui/controller/CustomerController.java`: doi text thao tac table thanh `Chi tiet`, hien danh sach pet lien ket, mo dialog chi tiet pet tai cho, them flow ghi nhan suc khoe va refresh section pet.

## Cap nhat Customer detail va search/reset 15/05/2026

- Da an nut `Them/Lien ket Thu Cung` khoi toolbar man hinh `Quan Ly Khach Hang`.
  - File sua: `src/main/resources/PetHotel/gui/view/CustomerManagement.fxml`.
  - `btnPets` duoc set `visible=false`, `managed=false`; layout toolbar khong bi chiem cho.
- Da giu nut `Them Khach Hang` tren header, giu cac nut thao tac khac cua Customer.
- Da doi cot thao tac trong table Customer tu `Chi tiet / them pet` thanh `Chi tiet`.
- Da them chuc nang cap nhat thong tin khach hang trong `Ho So Khach Hang`.
  - Nut moi: `Cap nhat thong tin`, nam o footer cua dialog/card `Ho So Khach Hang`.
  - Form sua duoc tao trong `CustomerController.openEditCustomerForm(...)`, khong tao FXML moi.
  - Cac field sua: ho ten, SDT, email, dia chi, ghi chu.
  - `customer_id` va ngay tham gia read-only, khong cho sua.
- Da dung lai BUS/DAO co san:
  - `CustomerBUS.updateCustomer(...)` validate ho ten, SDT, email, trung phone/email.
  - `CustomerDAO.update(Customer, Connection)` update DB bang PreparedStatement, khong update `customer_id` hoac ngay tham gia.
- Sau khi luu cap nhat:
  - Fetch lai customer tu DB bang `CustomerDAO.findById(...)`.
  - Cap nhat customer trong `allCustomers` va danh sach table hien tai bang `updateCustomerInLists(...)`.
  - Refresh table va select lai customer vua cap nhat.
  - Mo lai `Ho So Khach Hang` voi thong tin moi, khong reload lai man hinh Customer va khong clear keyword search.
- Da bo nut them pet khoi footer flow chinh cua `Ho So Khach Hang`; flow chinh chi con `Cap nhat thong tin` va danh sach pet lien ket.
- Da them `allCustomers` trong `CustomerController` de giu danh sach goc khi search.
  - Search chi set ket qua vao `customers`.
  - Reset/Xoa loc goi `loadCustomers(null)` de lay lai danh sach that tu DB.
  - Cot so thu cung tiep tuc tinh theo `customer_id`, nen reset/search khong lam sai pet count.
- Section `Thu cung lien ket` tiep tuc lay theo `customer_id` hien tai va reload section sau khi ghi nhan suc khoe pet.
- Dong `Chi Tiet Thu Cung` van o nguyen `Ho So Khach Hang`, khong chuyen sang man `Quan Ly Thu Cung`.
- Build kiem tra: `mvn clean compile` thanh cong.

## File sua trong dot cap nhat Customer detail

- `src/main/resources/PetHotel/gui/view/CustomerManagement.fxml`: an nut `btnPets`, giu toolbar gon trong module Customer.
- `src/main/java/PetHotel/gui/controller/CustomerController.java`: them `allCustomers`, them form cap nhat customer, dung `CustomerBUS.updateCustomer`, update row/table hien tai, giu search keyword, bo nut them pet khoi footer ho so.

## Cap nhat Customer + Pet detail 16/05/2026

- Da phan biet ro 2 vi tri nut them pet:
  - Man hinh chinh `Quan Ly Khach Hang`: da xoa nut `Them/Lien ket Thu Cung`/`btnPets` khoi toolbar chinh trong `CustomerManagement.fxml`.
  - Form `Ho So Khach Hang`: da giu va dat nut phu `Them thu cung moi` trong button bar co dinh o bottom.
- Da sua layout `Ho So Khach Hang` trong `CustomerController.openCustomerDetail(...)`:
  - Root doi sang `BorderPane`.
  - Top la header ho so.
  - Center la `ScrollPane` chua thong tin khach hang va section danh sach pet.
  - Bottom la button bar co dinh gom `Cap nhat thong tin`, `Them thu cung moi`, `Dong`.
  - Stage dat kich thuoc `820 x 760`, min `760 x 650`, tranh viec khach nhieu pet day mat nut thao tac.
- Da dam bao khach co 1 pet, 3 pet hoac hon 5 pet van thay duoc nut thao tac; danh sach pet dai thi scroll o phan noi dung, footer khong bi day khoi cua so.
- Section pet trong ho so doi thanh `Danh sach thu cung`, co hint `Double-click hoac bam Xem ho so de xem chi tiet thu cung`.
- Moi pet hien dang row/card 2 dong:
  - Dong 1: `PETxxx - ten pet`.
  - Dong 2: `Loai / Giong · Suc khoe · Dang hoat dong`.
  - Nut `Xem ho so` co width co dinh de khong bi cat text.
- Double-click row pet hoac bam `Xem ho so` mo dialog/card `Chi Tiet Thu Cung` truc tiep trong `CustomerController`, khong chuyen man hinh va dong pet detail van o nguyen `Ho So Khach Hang`.
- Da them top tab trong `CustomerManagement.fxml`:
  - `Quan ly khach hang`: hien search/table khach hang hien tai.
  - `Danh sach thu cung`: load va nhung lai `PetManagement.fxml` vao `StackPane` cua man hinh Khach Hang.
  - Khong dua `Thu Cung` quay lai sidebar.
  - Style tab dung tong kem/nau, tab active dung trang thai selected cua `ToggleButton`.
- Da sua form them pet tu `Ho So Khach Hang`:
  - `PetForm.fxml` them `txtOwnerDisplay` read-only.
  - `PetFormController.setOwner(...)` an combobox owner, hien `CUSxxx - ten chu` read-only va fill `SDT chu`.
  - Khi luu, `PetBUS.createPet(...)` nhan dung `customer_id` cua owner hien tai.
  - Sau khi luu, callback chi refresh table customer va section pet trong ho so, cap nhat `So thu cung`, khong dong `Ho So Khach Hang` va khong clear search khach hang phia sau.
- Da giu va kiem tra chuc nang `Cap nhat thong tin`:
  - Nut nam trong footer co dinh cua ho so.
  - Form sua cho phep sua ho ten, SDT, email, dia chi, ghi chu.
  - Ma KH va ngay tham gia read-only.
  - Sau khi luu goi `CustomerBUS.updateCustomer(...)`, cap nhat row hien tai va mo lai ho so voi du lieu moi, khong clear search keyword.
- Da kiem tra search/reset:
  - `allCustomers` van giu list goc khi search.
  - Search chi set ket qua vao `customers`.
  - `Xoa loc` clear keyword va goi `loadCustomers(null)` de lay lai danh sach that tu DB.
  - Cot `Thu Cung` van tinh theo `customer_id` qua `CustomerDAO.countPets(...)`, search/reset khong lam mat pet count.
- Build kiem tra: `mvn clean compile` thanh cong, khong co loi compile. Maven co warning dependency model cua OpenJFX nhu truoc, khong chan build.

## File sua trong dot 16/05/2026

- `src/main/resources/PetHotel/gui/view/CustomerManagement.fxml`: xoa nut them/link pet khoi toolbar chinh, them top tab `Quan ly khach hang / Danh sach thu cung`, them `StackPane` cho tab content.
- `src/main/java/PetHotel/gui/controller/CustomerController.java`: them logic tab, nhung `PetManagement.fxml`, sua `Ho So Khach Hang` thanh `BorderPane` + `ScrollPane` + footer co dinh, refresh pet list/pet count tai cho, giu search khi them pet.
- `src/main/resources/PetHotel/gui/view/PetForm.fxml`: them field owner read-only `txtOwnerDisplay` cho mode them pet tu ho so khach hang.
- `src/main/java/PetHotel/gui/controller/PetFormController.java`: an combobox owner trong fixed-owner mode, hien owner read-only, van tu gan owner hien tai khi luu pet.
- `src/main/resources/PetHotel/gui/css/style.css`: them style tab Customer/Pet, footer co dinh, row/card pet 2 dong, nut neutral va embedded Pet view.
- `docs/flow-work/NguyenVanMinh-Customer-Pet-flow-work.md`: cap nhat flow-work dot nay.

## Cap nhat tab Customer/Pet, table Pet va dong bo du lieu 16/05/2026

- Da doi title theo tab trong `CustomerManagement.fxml` va `CustomerController`:
  - Tab `Quan ly khach hang`: title `Quan Ly Khach Hang`, subtitle `Quan ly thong tin khach hang`.
  - Tab `Danh sach thu cung`: title `Quan Ly Thu Cung`, subtitle `Danh sach thu cung toan he thong`.
- Da an/hien nut theo tab:
  - `Them Khach Hang` chi hien o tab `Quan ly khach hang`.
  - Khi chuyen sang tab `Danh sach thu cung`, nut `Them Khach Hang` bi an bang `visible=false`, `managed=false`.
  - `PetController.prepareEmbeddedView()` an header cua `PetManagement.fxml`, nen nut `Them Thu Cung` khong hien trong tab danh sach thu cung.
  - Tab `Danh sach thu cung` chi dung de xem, tim va double-click pet de mo chi tiet.
- Da chuan hoa text tieng Viet co dau trong cac form/dialog thuoc Customer + Pet:
  - Ho so khach hang, form cap nhat khach hang.
  - Chi tiet thu cung mo tu Ho So Khach Hang.
  - Chi tiet thu cung trong PetController.
  - Form Ghi Nhan Suc Khoe trong CustomerController va PetController.
- Da sua form `Ghi Nhan Suc Khoe`:
  - Ma thu cung, ten thu cung, ngay ghi nhan, nguoi ghi nhan la read-only va khong focus mac dinh.
  - Focus mac dinh chuyen vao combobox `Tinh trang`.
  - Booking field doi label thanh `Ma booking neu co`.
  - Them hint ro: bang `PET_HEALTH_RECORD` hien yeu cau `booking_id`, can nhap ma booking hop le neu luu bi bao loi.
  - Sau khi luu tu Pet detail, `PetController` reload danh sach pet va mo lai detail voi suc khoe moi nhat.
  - Sau khi luu tu Ho So Khach Hang, section danh sach pet cua customer duoc reload, pet count/table customer va tab pet neu da load cung duoc refresh.
- Da sua bang `Danh sach thu cung` trong `PetManagement.fxml`:
  - Chi con cac cot: `Ma thu cung`, `Ten thu cung`, `Loai / Giong`, `Chu so huu`, `Suc khoe`.
  - Da bo cot `Tuoi`, `Trang thai`, `Thao tac`.
  - Khong con cot hien `Double-click`; double-click row van mo form chi tiet thu cung.
  - Tang width cot de tranh cat ma PET va can trai text trong CSS.
- Da can lai filter bar thu cung:
  - Search box chiem rong nhat.
  - Combobox `Loai thu` va `Suc khoe` width 160.
  - Nut `Tim` width 90, `Xoa loc` width 100.
  - Tat ca cung mot hang va cung chieu cao.
- Da them phan trang khach hang 10 dong/trang trong `CustomerController`:
  - Them `CUSTOMER_PAGE_SIZE = 10`.
  - `customers` giu list filter/search day du, `pagedCustomers` la list dang hien tren table.
  - `Pagination` doi trang bang `updateCustomerPage(...)`.
  - Hien text `Hien thi X-Y / Y khach hang`.
  - Search cung phan trang 10 dong/trang, `Xoa loc` reload DB va quay ve trang 1.
  - Them khach hang xong reload va chuyen toi trang chua khach moi; cap nhat khach hang giu trang hien tai neu co the.
- Da sua dong bo du lieu sau khi them pet tu `Ho So Khach Hang`:
  - Sau khi `PetFormController` luu pet, `CustomerController` refresh page hien tai cua table customer.
  - Reload section danh sach pet va so thu cung trong Ho So Khach Hang.
  - Neu tab pet da duoc load, goi `PetController.reloadPetsFromDatabase()`.
  - Khi switch sang tab `Danh sach thu cung`, luon reload pet tu DB de pet moi hien ngay.
- Flow them khach hang van giu gon:
  - Nut `Them Khach Hang` nam o tab Customer.
  - Tao khach hang xong mo ngay `Ho So Khach Hang` cua khach moi de co nut `Them thu cung moi`.
  - Khong them pet roi rac tu tab danh sach thu cung.
- Build kiem tra: `mvn clean compile` thanh cong, khong co loi compile. Maven van co warning dependency model cua OpenJFX, khong chan build.

## File sua trong dot tab/table/health 16/05/2026

- `src/main/resources/PetHotel/gui/view/CustomerManagement.fxml`: them `titleLabel`, `subtitleLabel`, `btnAddCustomer`, `customerPageInfo`; giu tab Customer/Pet.
- `src/main/java/PetHotel/gui/controller/CustomerController.java`: title theo tab, an/hien nut them khach, nhung PetController o embedded mode, phan trang 10 dong/trang, reload Pet tab sau khi them pet, chuan hoa text va form health.
- `src/main/resources/PetHotel/gui/view/PetManagement.fxml`: bo cot tuoi/trang thai/thao tac, can lai filter bar, them `headerBar`/`btnAddPet` de an khi embed.
- `src/main/java/PetHotel/gui/controller/PetController.java`: bo logic cot du, them `prepareEmbeddedView()`, `reloadPetsFromDatabase()`, chuan hoa text detail/health, focus form health vao combobox tinh trang.
- `src/main/java/PetHotel/gui/controller/PetFormController.java`: doi text preview ma pet thanh `Đang tạo...` trong UI.
- `src/main/resources/PetHotel/gui/css/style.css`: them style subtitle/page info, can filter/table pet.
- `docs/flow-work/NguyenVanMinh-Customer-Pet-flow-work.md`: cap nhat flow-work dot nay.

## Cap nhat schema Customer moi 16/05/2026

- Da doc metadata bang `CUSTOMER` hien tai qua JDBC Thin dung cung thong tin `DBConnection`.
- Schema that doc duoc:
  - `CUSTOMER_ID` `VARCHAR2(10)` NOT NULL.
  - `FULL_NAME` `NVARCHAR2` NOT NULL.
  - `EMAIL` `NVARCHAR2` nullable, unique `UQ_CUSTOMER_EMAIL`.
  - `CCCD` `VARCHAR2(12)` NOT NULL, unique `UQ_CUSTOMER_CCCD`.
  - `PHONE` `NVARCHAR2` NOT NULL, unique `UQ_CUSTOMER_PHONE`.
  - `ADDRESS` `NVARCHAR2` nullable.
  - `NOTE` `CLOB` nullable.
  - `CREATED_AT`, `UPDATED_AT` la `TIMESTAMP(6) WITH TIME ZONE`.
- Ghi chu doi chieu: `sql/02_tables.sql` trong repo hien van la schema cu chua co cot `cccd`, nhung code da cap nhat theo metadata database hien tai, khong sua cau truc DB.
- Da cap nhat `Customer` model:
  - Co cac field `customerId`, `fullName`, `email`, `cccd`, `phone`, `address`, `note`, `createdAt`, `updatedAt`.
  - Them getter/setter cho `cccd`.
  - Giu constructor cu va them overload co `cccd` de khong pha code dang goi constructor cu.
- Da cap nhat `CustomerDAO`:
  - `SELECT` trong `findById`, `findAll`, `search` lay du `cccd`, `created_at`, `updated_at`.
  - `INSERT` dung cac cot `customer_id, full_name, email, cccd, phone, address, note`.
  - `UPDATE` cap nhat `full_name, email, cccd, phone, address, note` va set `updated_at = SYSTIMESTAMP`.
  - `mapRow()` map them `cccd`, `createdAt`, `updatedAt`.
  - Them `existsByCccd(...)`.
- Da cap nhat `CustomerBUS`:
  - `createCustomer(...)` va `updateCustomer(...)` nhan them tham so `cccd`.
  - Validate ho ten, SDT, CCCD bat buoc 12 chu so theo schema DB hien tai, email dung dinh dang.
  - Neu email ket thuc `@gmail.co` thi bao can kiem tra vi co the thieu `.com`.
  - Pre-check unique phone/email/cccd va map loi DB unique thanh thong bao rieng.
- Da cap nhat form `Them Khach Hang` trong `CustomerController`:
  - Co `Ma KH` read-only, `Ho ten`, `So dien thoai`, `Can cuoc cong dan`, `Email`, `Dia chi`, `Ghi chu`, `Ngay tham gia`.
  - Tao khach hang xong reload table va mo ngay `Ho So Khach Hang` cua khach moi.
- Da cap nhat form `Cap Nhat Thong Tin Khach Hang`:
  - Co `Ma KH` read-only, `Ho ten`, `So dien thoai`, `Can cuoc cong dan`, `Email`, `Dia chi`, `Ghi chu`, `Ngay tham gia`, `Cap nhat lan cuoi`.
  - Khi luu khong update `customer_id`, khong doi `created_at`, co refresh row hien tai va mo lai ho so voi du lieu moi, khong clear search keyword.
- Da cap nhat `Ho So Khach Hang`:
  - Hien `CCCD`, `Email`, `Dia chi`, `Ngay tham gia`, `Cap nhat lan cuoi`, `So thu cung`, `Ghi chu`.
  - Field null hien `Chua cap nhat`, ghi chu null hien `-`, ngay null hien `-`, khong hien text ky thuat `null`.
- Da cap nhat search Customer:
  - Giu tim theo 3 so cuoi SDT bang pattern suffix.
  - Giu tim theo ma KH, ten, SDT, email.
  - Them tim theo `CCCD`.
  - Placeholder doi thanh `Tim theo ma KH, ten, SDT, 3 so cuoi SDT, CCCD...`.
  - Khong pha phan trang 10 khach/trang.
- Da kiem tra dong bo voi Pet:
  - Khong sua flow Pet trong dot schema Customer nay.
  - Danh sach pet lien ket trong ho so customer van lay theo `customer_id`.
  - Them pet tu ho so customer van refresh so thu cung/table/tab pet nhu dot truoc.
- Xu ly loi unique:
  - Trung phone: `So dien thoai da ton tai trong he thong.`
  - Trung email: `Email da ton tai trong he thong.`
  - Trung CCCD: `CCCD da ton tai trong he thong.`
  - Loi DB khac: `Loi database khi luu khach hang. Vui long kiem tra lai.`
- Build kiem tra: `mvn clean compile` thanh cong. Maven van co warning dependency model cua OpenJFX, khong chan build.

## File sua trong dot schema Customer 16/05/2026

- `src/main/java/PetHotel/model/Customer.java`: them field/constructor/getter/setter `cccd`, cap nhat schema comment theo DB hien tai.
- `src/main/java/PetHotel/dao/CustomerDAO.java`: them cot `cccd` vao insert/update/select/search/map va them `existsByCccd(...)`.
- `src/main/java/PetHotel/bus/CustomerBUS.java`: them validation CCCD, normalize optional field, map unique phone/email/cccd, them overload tao/sua customer co CCCD.
- `src/main/java/PetHotel/gui/controller/CustomerController.java`: them CCCD vao form them/sua/ho so, hien created/updated, giu refresh khach hang sau khi luu.
- `src/main/resources/PetHotel/gui/view/CustomerManagement.fxml`: cap nhat placeholder search co CCCD va 3 so cuoi SDT.
- `docs/flow-work/NguyenVanMinh-Customer-Pet-flow-work.md`: cap nhat flow-work dot schema Customer.

## Cap nhat fix PetForm trang va table Customer 16/05/2026

- Da fix loi form `Them thu cung moi` mo ra man trang, chi hien noi dung sau khi maximize/resize.
- Nguyen nhan xac dinh trong flow UI:
  - `PetForm.fxml` da dung `BorderPane`, nhung Stage duoc mo khi root/scene chua duoc ep min/pref size va chua force layout/render pass on dinh.
  - `GridPane` trong form chua co `ColumnConstraints`, nen lan layout dau co the tinh kich thuoc noi dung khong on dinh; resize cua so moi kich hoat layout lai.
- Da sua thu tu mo form:
  - Load FXML bang `FXMLLoader`.
  - Lay `PetFormController`.
  - Goi `setOwner(customer)` hoac `prepareForPetManagement()` va `setOnSaved(...)` truoc khi show.
  - Tao `Scene`, set vao `Stage`.
  - Ep root `minSize=760x680`, `prefSize=820x740`.
  - Goi `applyCss()`, `layout()`, `snapshot(null, null)`, `sizeToScene()`, `centerOnScreen()`.
  - Sau do moi goi `showAndWait()`.
- Da cap nhat `PetForm.fxml`:
  - Root `BorderPane` co `minWidth=760`, `minHeight=680`, `prefWidth=820`, `prefHeight=740`.
  - Content card co min/pref width ro rang.
  - `GridPane` co `ColumnConstraints` cho cot label va cot input, cot input `hgrow=ALWAYS`.
- Da sua ca 2 noi mo `PetForm.fxml`:
  - Tu `Ho So Khach Hang` trong `CustomerController`.
  - Tu tab/danh sach Pet trong `PetController`.
- Da xoa cot `Thao Tac` khoi bang khach hang:
  - Xoa `colActions` trong `CustomerManagement.fxml`.
  - Xoa field `colActions` va value factory `Chi tiet` trong `CustomerController`.
  - Bang customer khong con hien chu `Chi tiet`.
- Bang customer hien chi con 6 cot:
  - `Ma KH`, `Ho Ten`, `SDT`, `Email`, `Thu Cung`, `Ngay Tham Gia`.
- Da giu hanh vi mo ho so khach hang bang double-click row qua `onTableClick(...)`.
- Da can lai bang customer:
  - Dat `TableView.CONSTRAINED_RESIZE_POLICY`.
  - Dat `fixedCellSize=40`.
  - Set `minWidth/prefWidth` ro cho 6 cot.
  - Them style rieng `ph-customer-table` de header/cell can trai dong nhat va padding dong deu.
- Sau khi them pet thanh cong:
  - `CustomerController` van refresh page customer hien tai.
  - Reload section pet trong `Ho So Khach Hang`, cap nhat so thu cung.
  - Neu tab Pet da load thi goi reload tab Pet.
- Build kiem tra: `mvn clean compile` thanh cong. Maven co warning deprecated API do `TableView.CONSTRAINED_RESIZE_POLICY` va warning dependency model OpenJFX, khong chan build.

## File sua trong dot fix PetForm/table Customer 16/05/2026

- `src/main/resources/PetHotel/gui/view/PetForm.fxml`: them min/pref size root, them constraints cho GridPane/content de layout on dinh ngay lan dau mo.
- `src/main/java/PetHotel/gui/controller/CustomerController.java`: sua thu tu chuan bi Stage PetForm, force CSS/layout/snapshot truoc show; bo `colActions`; cau hinh 6 cot table customer.
- `src/main/java/PetHotel/gui/controller/PetController.java`: dung cung cach chuan bi Stage PetForm khi mo form them pet tu tab Pet.
- `src/main/resources/PetHotel/gui/view/CustomerManagement.fxml`: xoa cot `Thao Tac`, them style `ph-customer-table`, set width ro cho 6 cot con lai.
- `src/main/resources/PetHotel/gui/css/style.css`: them style rieng cho table customer de header/cell/row can deu.
- `docs/flow-work/NguyenVanMinh-Customer-Pet-flow-work.md`: cap nhat flow-work dot fix UI nay.

## Cap nhat form Ghi Nhan Suc Khoe 16/05/2026

- Da sua form `Ghi Nhan Suc Khoe` trong ca hai luong:
  - Mo tu `Ho So Khach Hang` trong `CustomerController`.
  - Mo tu danh sach/chi tiet Pet trong `PetController`.
- Nguyen nhan lag/khong muot:
  - Form dung `TextArea` style chung, chua co cau hinh rieng cho health note nen de lo scrollbar noi bo va layout cua TextArea tinh lai nang khi go.
  - Khong phat hien listener/query DB theo tung phim; cac query/reload chi chay khi mo detail hoac sau khi bam luu.
  - CSS radius warning dot truoc da duoc sua, tranh console warning lap lai khi render UI.
- Da doi hai o nhap health sang TextArea nhe hon:
  - `Trieu chung bat thuong`: `healthArea(2, 70)`.
  - `Ghi chu suc khoe`: `healthArea(3, 90)`.
  - Bat `wrapText=true`.
  - Dat min/pref/max height co dinh de layout khong nhay.
  - Them style `ph-health-text-area` an scrollbar ngang/doc noi bo cua TextArea.
  - Hai o nay de trong cho user nhap ghi chu binh thuong, khong co validate/query realtime.
- Da chuyen validation ve luc bam `Luu ghi nhan`:
  - Bat buoc chon `Tinh trang tong quat`.
  - Neu `Can theo doi` hoac `Bat thuong` thi bat buoc nhap `Trieu chung` hoac `Ghi chu`.
  - Neu `Binh thuong` thi cho phep luu khi ghi chu trong; note luu van co dong `Tinh trang: Binh thuong`.
  - `booking_id` duoc validate truoc khi goi BUS.
- Xu ly booking_id:
  - Theo `sql/02_tables.sql`, `pet_health_record.booking_id` dang `NOT NULL`.
  - UI doi label tu `Ma booking neu co` thanh `Ma booking *`.
  - Hint doi thanh `Bang PET_HEALTH_RECORD hien yeu cau booking_id. Vui long nhap ma booking hop le.`
  - `PetBUS.addHealthRecord(...)` doi message validation thanh ro nghia: `Ma booking khong duoc de trong vi bang PET_HEALTH_RECORD hien yeu cau booking_id.`
  - Khong tu sua schema DB.
- Logic luu health record:
  - Van di qua `PetBUS.addHealthRecord(...)`.
  - `PetHealthRecordDAO.insert(...)` dung `INSERT INTO pet_health_record (...) VALUES (..., SYSTIMESTAMP, ...)`.
  - Khong co UPDATE/ghi de ban ghi cu.
  - Moi lan bam luu hop le se tao `health_record_id` moi qua `IDGenerator.nextHealthRecordId()`.
  - Du lieu cu trong `pet_health_record` duoc giu lai de lam lich su.
- Da bo sung alias DAO theo ten ro nghia:
  - `insertHealthRecord(PetHealthRecord record)`.
  - `getLatestHealthRecordByPetId(String petId)`.
  - `getHealthRecordsByPetId(String petId)`.
- Da sua query latest:
  - `findLatestByPetId(...)` lay theo `ORDER BY recorded_at DESC FETCH FIRST 1 ROW ONLY`.
  - `PetController` va `CustomerController` lay suc khoe gan nhat qua `PetBUS.getLatestHealthRecord(...)`.
- Sau khi luu:
  - Pet tab reload danh sach va mo lai Pet Detail voi suc khoe moi nhat.
  - Neu ghi nhan tu `Ho So Khach Hang`, callback reload section danh sach pet cua customer, cap nhat suc khoe hien thi, khong dong Customer Detail.
- Build kiem tra: `mvn clean compile` thanh cong. Maven co warning deprecated API cua JavaFX resize policy va warning dependency model OpenJFX, khong chan build.

## File sua trong dot health record 16/05/2026

- `src/main/java/PetHotel/gui/controller/CustomerController.java`: lam gon form health trong flow Customer Detail, them `healthArea`, validation khi bam luu, lay latest health qua BUS.
- `src/main/java/PetHotel/gui/controller/PetController.java`: lam gon form health trong flow Pet Detail, them `healthArea`, validation khi bam luu.
- `src/main/java/PetHotel/bus/PetBUS.java`: doi message validation `booking_id` bat buoc cho ro nghia.
- `src/main/java/PetHotel/dao/PetHealthRecordDAO.java`: giu insert-only, them alias method, sua latest query theo `recorded_at DESC`.
- `src/main/resources/PetHotel/gui/css/style.css`: them style `ph-health-text-area` de wrap text, giu height on dinh va an scrollbar noi bo.
- `docs/flow-work/NguyenVanMinh-Customer-Pet-flow-work.md`: cap nhat flow-work dot health record.

## Cap nhat toi uu lag Health/Pet tab va table Customer 16/05/2026

- Da tiep tuc sua form `Ghi Nhan Suc Khoe` vi TextArea van thao tac chua muot trong mot so may.
- Da doi hai o nhap chinh tu TextArea sang TextField nhe:
  - `Trieu chung bat thuong`: `healthField("Nhap trieu chung neu co")`.
  - `Ghi chu suc khoe`: `healthField("Nhap ghi chu suc khoe")`.
  - TextField dam bao nhap lieu binh thuong, khong co scrollbar noi bo, khong bi overlay/scrollpane che.
  - `Ma booking *` van la TextField nhap duoc binh thuong.
  - Cac field read-only van chi gom ma pet, ten pet, ngay ghi nhan, nguoi ghi nhan.
- Da kiem tra khong co listener nang tren `textProperty()` cua cac o health:
  - Khong query DB khi user dang go.
  - Khong reload form khi user dang go.
  - Khong validate realtime.
  - Chi validate khi bam `Luu ghi nhan`.
- Logic luu suc khoe van la insert moi:
  - `PetBUS.addHealthRecord(...)` tao `health_record_id` moi.
  - `PetHealthRecordDAO.insert(...)` thuc hien `INSERT INTO pet_health_record`.
  - Khong update/ghi de ban ghi cu.
  - Ban ghi cu duoc giu lai de lam lich su.
- `booking_id` hien van bat buoc theo schema hien tai:
  - UI hien `Ma booking *`.
  - Neu bo trong thi validation truoc khi goi BUS/DAO: bang `PET_HEALTH_RECORD` hien yeu cau booking_id.
  - Khong tu sua database schema.
- Da toi uu tab `Danh sach thu cung`:
  - Nguyen nhan load cham: `PetController` truoc do query DB trong cell factory:
    - Moi cell owner goi `CustomerDAO.findById(...)`.
    - Moi cell health goi latest health record.
    - `countMonitoringPets()` lai goi health label tung pet, tao N+1 query va block JavaFX Application Thread.
  - Da them cache bulk:
    - `ownerNameByCustomerId`: load mot lan tu `CustomerBUS.getAllCustomers()`.
    - `latestHealthByPetId`: load mot lan tu `PetHealthRecordDAO.findLatestByAllPetIds()`.
  - Cell factory cua Pet table chi doc map trong RAM, khong query DB.
  - `PetHealthRecordDAO.findLatestByAllPetIds()` dung analytic query `ROW_NUMBER() OVER (PARTITION BY pet_id ORDER BY recorded_at DESC, health_record_id DESC)` de lay latest health cho tat ca pet trong mot query.
  - `PetController.loadPetsAsync(...)` dung JavaFX `Task` background thread, UI khong bi block khi bấm tab Pet.
  - Khi dang load, `pageInfo` hien `Dang tai danh sach thu cung...`.
  - Them `dataLoaded`, `needsPetRefresh`, `loadingPets`:
    - Neu tab da load va khong co thay doi, `refreshIfNeeded()` dung cache hien co.
    - Sau khi them pet/luu health record thi goi `markNeedsRefresh()`.
    - Khi can reload that thi chay background Task.
  - `CustomerController.onShowPetTab(...)` khong goi reload bat buoc moi lan bam tab nua, ma goi `petTabController.refreshIfNeeded()`.
  - Da them log:
    - `[PetTab] switch start`
    - `[PetTab] load data start`
    - `[PetTab] load data done in Xms`
    - `[PetTab] render done in Xms`
- Da chuyen bang Khach Hang sang style gan voi bang Thu Cung:
  - Table Customer them class `ph-pet-table` va `ph-customer-table`.
  - Row height `38`.
  - Width cot theo yeu cau:
    - `Ma KH`: 120.
    - `Ho Ten`: 260.
    - `SDT`: 160.
    - `Email`: 300.
    - `Thu Cung`: 100.
    - `Ngay Tham Gia`: 160.
  - Van khong co cot `Thao Tac`.
  - Double-click row van mo `Ho So Khach Hang`.
- Da kiem tra CSS radius:
  - Khong con radius dang so tran/token sai cho `-fx-border-radius`/`-fx-background-radius`.
- Build kiem tra: `mvn clean compile` thanh cong. Maven co warning deprecated API cua JavaFX resize policy va warning dependency model OpenJFX, khong chan build.

## File sua trong dot toi uu Health/Pet tab/table Customer 16/05/2026

- `src/main/java/PetHotel/gui/controller/PetController.java`: doi health input sang TextField nhe, load Pet tab bang Task background, cache owner/latest health, dung `needsPetRefresh`.
- `src/main/java/PetHotel/gui/controller/CustomerController.java`: doi health input sang TextField nhe, switch Pet tab goi `refreshIfNeeded()`, mark refresh khi co thay doi Pet/Health, can lai cot Customer table.
- `src/main/java/PetHotel/dao/PetHealthRecordDAO.java`: them `findLatestByAllPetIds()` de lay latest health cho tat ca pet bang mot query.
- `src/main/resources/PetHotel/gui/view/CustomerManagement.fxml`: gan style table Customer giong Pet table, set width 6 cot theo yeu cau.
- `src/main/resources/PetHotel/gui/css/style.css`: them `ph-health-text-field`, can row height Customer table.
- `docs/flow-work/NguyenVanMinh-Customer-Pet-flow-work.md`: cap nhat flow-work dot nay.

## Cap nhat fix triet de HealthForm va table Customer 17/05/2026

- Da thay form `Ghi Nhan Suc Khoe` duoc dung programmatic trong `PetController`/`CustomerController` bang FXML/controller rieng:
  - FXML moi: `src/main/resources/PetHotel/gui/view/HealthRecordForm.fxml`.
  - Controller moi: `src/main/java/PetHotel/gui/controller/HealthRecordFormController.java`.
- Nguyen nhan form health bi trang lan dau:
  - Form cu duoc lap truc tiep bang Java node trong controller va set scene/show ngay trong event handler.
  - Root form khong co FXML rieng/min-pref size rieng, nen render pass dau co the chua layout day du; sau khi dong/mo lai hoac resize thi JavaFX layout lai nen moi hien.
  - Form cu cung de logic UI/save/reload nam chung trong controller, kho bat loi initialize/render.
- Da sua thu tu mo form dung yeu cau:
  - `FXMLLoader` load `/PetHotel/gui/view/HealthRecordForm.fxml`.
  - Lay `HealthRecordFormController`.
  - Goi `controller.setPet(pet)`.
  - Goi `controller.setOnSaved(...)`.
  - Tao `Scene(root, 760, 660)`.
  - Set scene vao stage.
  - Goi `root.applyCss()`, `root.layout()`, `root.snapshot(null, null)`.
  - Goi `stage.sizeToScene()`, `stage.centerOnScreen()`.
  - Sau do moi `showAndWait()`.
- Da them log bat buoc:
  - `[HealthForm] open start`.
  - `[HealthForm] initialized`.
  - `[HealthForm] fxml loaded`.
  - `[HealthForm] pet set: PETxxx`.
  - `[HealthForm] scene set`.
  - `[HealthForm] shown`.
  - Catch loi co `e.printStackTrace()` va alert loi ro rang.
- Da fix input khong nhap duoc:
  - `HealthRecordForm.fxml` dung TextField that cho `Trieu chung bat thuong`, `Ghi chu suc khoe`, `Ma booking *`.
  - Cac field nay `editable=true`, `disable=false`, `mouseTransparent=false`, `focusTraversable=true`.
  - Khong co ScrollPane/Pane/Rectangle overlay len input.
  - Focus mac dinh dat vao `Ghi chu suc khoe`, khong focus vao ma pet read-only.
  - Khong co listener textProperty/query DB/reload form khi user dang go.
- Logic luu health record:
  - `HealthRecordFormController.onSave()` chi validate khi bam `Luu ghi nhan`.
  - Neu booking rong bao: `Bang PET_HEALTH_RECORD hien yeu cau ma booking. Vui long nhap ma booking hop le.`
  - Goi `PetBUS.addHealthRecord(...)`.
  - `PetBUS` tao ID moi va `PetHealthRecordDAO.insert(...)` thuc hien INSERT moi.
  - Khong update/xoa/ghi de record cu.
  - Pet Detail van lay latest health record de hien thi suc khoe gan nhat.
- Da chuyen bang Customer/Pet sang style chung:
  - Them CSS class `management-table`.
  - `PetManagement.fxml` va `CustomerManagement.fxml` cung dung `data-table management-table ph-pet-table`.
  - Customer table van them `ph-customer-table` de tinh chinh rieng.
  - Header ro hon, row compact, grid line nhe, cell/header can trai dong nhat.
  - Row height set 36 trong controller va CSS.
  - Cot Customer giu dung 6 cot: `Ma KH`, `Ho Ten`, `SDT`, `Email`, `Thu Cung`, `Ngay Tham Gia`.
  - Da bo cot `Thao Tac`; double-click row van mo `Ho So Khach Hang`.
- Da kiem tra CSS radius:
  - Khong con `-fx-border-radius`/`-fx-background-radius` dang so tran hoac token sai.
- Build kiem tra: `mvn clean compile` thanh cong. Maven co warning deprecated API cua JavaFX resize policy va warning dependency model OpenJFX, khong chan build.

## File sua/tao trong dot 17/05/2026

- Tao moi: `src/main/resources/PetHotel/gui/view/HealthRecordForm.fxml`.
- Tao moi: `src/main/java/PetHotel/gui/controller/HealthRecordFormController.java`.
- Sua: `src/main/java/PetHotel/gui/controller/PetController.java`.
- Sua: `src/main/java/PetHotel/gui/controller/CustomerController.java`.
- Sua: `src/main/resources/PetHotel/gui/view/PetManagement.fxml`.
- Sua: `src/main/resources/PetHotel/gui/view/CustomerManagement.fxml`.
- Sua: `src/main/resources/PetHotel/gui/css/style.css`.
- Sua: `docs/flow-work/NguyenVanMinh-Customer-Pet-flow-work.md`.

## Cap nhat menu Thu Cung cho Nhan Vien Cham Soc 17/05/2026

- Da xac nhan role `Nhan Vien Cham Soc` dang map bang `Role.PET_CARE_STAFF`, gia tri DB `role_emp = 2`.
- Da sua sidebar cho role cham soc:
  - `SidebarController.applyRolePermissions(...)` goi `showMenu(menuPet)` va `showMenu(menuGrooming)` cho role `PET_CARE_STAFF`.
  - Cac menu khac nhu Customer, Booking, Room, Invoice, Kho, Admin van bi an voi role nay.
  - `menuPet` van nam trong nhom `QUAN LY NGHIEP VU`, dung node co san trong `Sidebar.fxml`.
- Da sua action menu `pet`:
  - Khi bam `Thu Cung`, `SidebarController.onMenu(...)` goi `MainController.showPetManagement(null)`.
  - `MainController.showPetManagement(...)` load `src/main/resources/PetHotel/gui/view/PetManagement.fxml`.
  - Controller duoc dung lai la `PetController`.
  - Topbar duoc set ve `Thu Cung` / `Danh sach thu cung toan he thong`.
  - Sidebar active dung menu `Thu Cung`.
- Da cap nhat luong vao man hinh dau cho role cham soc:
  - `MainController.initialize()` neu user hien tai la `PET_CARE_STAFF` thi mo thang man hinh `PetManagement.fxml`, khong de mac dinh Dashboard bi an.
- Da gioi han thao tac tren man hinh Thu Cung cho role cham soc:
  - `PetController.applyRolePermissions()` an/disable nut `Them Thu Cung` khi session la `Role.PET_CARE_STAFF`.
  - Nhan vien cham soc van xem danh sach pet, tim/loc pet, double-click de mo chi tiet pet.
  - Nut/flow `Ghi Nhan Suc Khoe` trong chi tiet pet van duoc giu.
  - Man hinh PetManagement chi hien cac cot: `Ma thu cung`, `Ten thu cung`, `Loai / Giong`, `Chu so huu`, `Suc khoe`.
  - Khong co cot `Trang thai`, khong co cot `Thao tac`, khong hien nut `Them Thu Cung` cho role cham soc.
- Form `Ghi Nhan Suc Khoe` tiep tuc dung `HealthRecordForm.fxml`/`HealthRecordFormController`, da fix tu dot truoc:
  - Mo lan dau khong bi trang.
  - Nhap duoc Trieu chung, Ghi chu, Ma booking.
  - Luu bang INSERT health record moi, khong ghi de record cu.
  - Sau khi luu reload Pet Detail va danh sach pet.
- Build kiem tra: `mvn clean compile` thanh cong. Maven chi co warning OpenJFX/deprecated API, khong co loi compile.

## File sua trong dot menu Thu Cung role cham soc 17/05/2026

- `src/main/java/PetHotel/gui/controller/SidebarController.java`: hien `menuPet` cho role `PET_CARE_STAFF`, menu `pet` load `PetManagement`.
- `src/main/java/PetHotel/gui/controller/MainController.java`: role cham soc vao thang man hinh Pet, cap nhat topbar Pet.
- `src/main/java/PetHotel/gui/controller/PetController.java`: an/disable `Them Thu Cung` cho role cham soc.
- `docs/flow-work/NguyenVanMinh-Customer-Pet-flow-work.md`: cap nhat flow-work dot nay.

## Cap nhat sua quyen xem Pet cho Nhan Vien Cham Soc 17/05/2026

- Loi hien tai:
  - Khi role `Nhan Vien Cham Soc` bam menu `Thu Cung`, man hinh bao `Khong tai duoc danh sach thu cung`.
  - Nguyen nhan nam trong `src/main/java/PetHotel/bus/PetBUS.java`:
    - `getAllPets()` dang goi `authBUS.requireRole(Role.RECEPTIONIST)`.
    - `searchPet()` dang goi `authBUS.requireRole(Role.RECEPTIONIST)`.
    - `getPetDetail()` dang goi `authBUS.requireRole(Role.RECEPTIONIST)`.
    - `getHealthRecords()` va `getLatestHealthRecord()` cung yeu cau `RECEPTIONIST`.
- Da sua permission check:
  - Them helper `requirePetViewPermission()` trong `PetBUS`.
  - Helper cho phep user co role:
    - `Role.RECEPTIONIST`.
    - `Role.PET_CARE_STAFF`.
    - Cac role quan ly/admin van pass theo logic `AppUser.hasRole(...)` hien co.
  - Ap dung helper cho:
    - `getAllPets()`.
    - `searchPet(...)`.
    - `getPetDetail(...)`.
    - `getHealthRecords(...)`.
    - `getLatestHealthRecord(...)`.
- Khong mo rong quyen quan ly Pet:
  - `createPet(...)` van yeu cau `RECEPTIONIST`.
  - `updatePet(...)` van yeu cau `RECEPTIONIST`.
  - `linkOwner(...)` van yeu cau `RECEPTIONIST`.
  - `deletePet(...)` van yeu cau `BRANCH_MANAGER`.
  - `getPetServiceHistory(...)` van giu theo quyen `RECEPTIONIST`.
- Khong mo quyen Customer management cho role cham soc:
  - `PetController.loadPetsAsync(...)` khong goi `CustomerBUS.getAllCustomers()` nua vi ham nay dung quyen Customer/Receptionist.
  - Man hinh Pet chi lay ten chu so huu bang `CustomerDAO.findAll()` de render cot `Chu so huu`.
  - Sidebar van an menu `Khach Hang` voi role `PET_CARE_STAFF`.
- Role mapping da kiem tra:
  - `Role.PET_CARE_STAFF` map DB `role_emp = 2`.
  - `Role.RECEPTIONIST` map DB `role_emp = 1`.
  - `Role.BRANCH_MANAGER` map DB `role_emp = 3`.
- Role `Nhan Vien Cham Soc` hien duoc:
  - Xem danh sach thu cung.
  - Tim kiem thu cung.
  - Double-click mo chi tiet thu cung.
  - Xem suc khoe gan nhat.
  - Ghi nhan suc khoe thu cung bang INSERT health record moi.
- Role `Nhan Vien Cham Soc` khong duoc:
  - Them thu cung.
  - Sua thong tin pet.
  - Xoa pet.
  - Doi chu so huu.
  - Quan ly khach hang.
- Da them guard trong `PetController.onAddPet(...)`:
  - Neu action them pet bi goi voi role `PET_CARE_STAFF` thi hien thong bao gioi han quyen.
  - Khong roi xuong loi `Yeu cau quyen: RECEPTIONIST`.
- Kiem tra:
  - Static check role Le tan: van pass `requirePetViewPermission()` qua `Role.RECEPTIONIST`, cac flow them pet tu Customer Detail khong bi doi quyen.
  - Static check role Nhan vien cham soc: pass `requirePetViewPermission()` qua `Role.PET_CARE_STAFF`, `btnAddPet` da bi an/disable.
  - Static check role Quan ly: van pass theo `AppUser.hasRole(...)`, khong mat quyen xem Pet.
  - Build: `mvn clean compile` thanh cong. Chi co warning OpenJFX/deprecated API, khong co loi compile.

## File sua trong dot sua quyen Pet role cham soc 17/05/2026

- `src/main/java/PetHotel/bus/PetBUS.java`: tach quyen view Pet/health-read khoi quyen Receptionist, cho `PET_CARE_STAFF` xem danh sach/detail/search/latest health.
- `src/main/java/PetHotel/gui/controller/PetController.java`: lay owner name bang `CustomerDAO.findAll()` trong man hinh Pet, tranh goi `CustomerBUS.getAllCustomers()` voi role cham soc.
- `docs/flow-work/NguyenVanMinh-Customer-Pet-flow-work.md`: cap nhat flow-work dot nay.

## Cap nhat phan quyen Ghi Nhan Suc Khoe 17/05/2026

- Yeu cau nghiep vu moi:
  - Chi `Nhan Vien Cham Soc` duoc thao tac va luu `Ghi Nhan Suc Khoe`.
  - `Le Tan` va `Quan Ly Chi Nhanh` chi duoc xem ho so thu cung va suc khoe gan nhat.
- Role mapping da kiem tra:
  - `role_emp = 2` -> `Role.PET_CARE_STAFF`.
  - `role_emp = 1` -> `Role.RECEPTIONIST`.
  - `role_emp = 3` -> `Role.BRANCH_MANAGER`.
- Da sua BUS:
  - `PetBUS.addHealthRecord(...)` khong dung `authBUS.requireRole(Role.PET_CARE_STAFF)` nua vi `AppUser.hasRole(...)` cho manager pass quyen nhan vien.
  - Them `requirePetHealthRecordPermission()` check exact `user.getRole() == Role.PET_CARE_STAFF`.
  - Neu khong dung role thi throw: `Chi nhan vien cham soc duoc ghi nhan suc khoe thu cung.`
  - Logic luu health record van la INSERT moi qua `PetHealthRecordDAO.insert(...)`, khong update/xoa record cu.
- Da sua UI Pet Detail:
  - `PetController.openPetDetail(...)` chi hien nut `Ghi nhan suc khoe` khi `SessionManager.hasRole(Role.PET_CARE_STAFF)`.
  - Voi Le tan/Quan ly, nut health bi hidden/managed false/disable.
  - `PetController.openHealthForm(...)` co guard, neu bi goi truc tiep bang role khong hop le thi thong bao `Chi nhan vien cham soc duoc ghi nhan suc khoe thu cung.`
- Da sua UI Pet Detail trong Customer flow:
  - `CustomerController.openPetDetailDialog(...)` cung an/disable nut `Ghi nhan suc khoe` voi role khong phai `PET_CARE_STAFF`.
  - `CustomerController.openCustomerPetHealthForm(...)` co guard tuong tu, tranh mo form health nham role.
- Da sua form Health:
  - `HealthRecordForm.fxml` them `fx:id="btnSave"` cho nut `Luu ghi nhan`.
  - `HealthRecordFormController` them check role exact qua `SessionManager.hasRole(Role.PET_CARE_STAFF)`.
  - Neu khong phai nhan vien cham soc:
    - Disable `ComboBox Tinh trang`.
    - Disable cac input `Trieu chung`, `Ghi chu`, `Ma booking`.
    - Disable nut `Luu ghi nhan`.
    - Neu co goi save truc tiep thi bao loi ro.
  - Neu la nhan vien cham soc:
    - `ComboBox Tinh trang` enabled.
    - `Trieu chung`, `Ghi chu`, `Ma booking` editable/enabled.
    - `Luu ghi nhan` enabled.
    - Read-only chi gom `Ma thu cung`, `Ten thu cung`, `Ngay ghi nhan`, `Nguoi ghi nhan`.
- Nguoi ghi nhan:
  - Form Health tiep tuc lay tu `SessionManager.getInstance().getUserId()`, tuc employee_id dang dang nhap, vi du `EMP_CS01`.
- Kiem tra:
  - Static check role Nhan vien cham soc: thay nut health, mo form edit, save di qua `PetBUS.addHealthRecord(...)`.
  - Static check role Le tan/Quan ly: chi xem detail, khong thay nut health; neu goi save truc tiep van bi BUS/controller chan.
  - Build: `mvn clean compile` thanh cong. Chi co warning OpenJFX/deprecated API, khong co loi compile.

## File sua trong dot phan quyen Health 17/05/2026

- `src/main/java/PetHotel/bus/PetBUS.java`: them check exact role cho `addHealthRecord(...)`.
- `src/main/java/PetHotel/gui/controller/PetController.java`: an/guard nut `Ghi nhan suc khoe` trong Pet Detail.
- `src/main/java/PetHotel/gui/controller/CustomerController.java`: an/guard nut `Ghi nhan suc khoe` trong Pet Detail mo tu Customer.
- `src/main/java/PetHotel/gui/controller/HealthRecordFormController.java`: disable form/save voi role khong hop le, guard save.
- `src/main/resources/PetHotel/gui/view/HealthRecordForm.fxml`: them `fx:id="btnSave"`.
- `docs/flow-work/NguyenVanMinh-Customer-Pet-flow-work.md`: cap nhat flow-work dot nay.

## Cap nhat fix input HealthForm cho Nhan Vien Cham Soc 17/05/2026

- Van de:
  - Role `Nhan Vien Cham Soc` mo duoc form `Ghi Nhan Suc Khoe` nhung khong nhap duoc `Trieu chung`, `Ghi chu`, `Ma booking`.
- Nguyen nhan trong code:
  - Cac field nhap khong bi CSS/overlay chan.
  - Rui ro nam o `HealthRecordFormController.applyRolePermissions()`:
    - Mode edit/view-only duoc ap dung ngay trong `initialize()`.
    - Neu role context chua duoc doc dung tai thoi diem initialize, controller co the set `disable=true` cho 3 input va nut save.
    - Chua co log field-state nen kho xac dinh field bi disable/editable/mouseTransparent o buoc nao.
- Da sua FXML:
  - `HealthRecordForm.fxml` them `fx:id` cho parent:
    - `rootPane`.
    - `formContainer`.
    - `formGrid`.
  - Ep parent input khong chan event:
    - `disable="false"`.
    - `mouseTransparent="false"`.
  - Ep field can nhap:
    - `txtSymptom`: `editable="true"`, `disable="false"`, `mouseTransparent="false"`, `focusTraversable="true"`.
    - `txtNote`: `editable="true"`, `disable="false"`, `mouseTransparent="false"`, `focusTraversable="true"`.
    - `txtBookingId`: `editable="true"`, `disable="false"`, `mouseTransparent="false"`, `focusTraversable="true"`.
  - `cbStatus` cung duoc set `disable="false"`, `mouseTransparent="false"`, `focusTraversable="true"`.
- Da sua controller:
  - `HealthRecordFormController.setPet(...)` goi lai `applyRolePermissions()` sau khi pet/context da duoc truyen vao.
  - `canRecordHealth()` check exact role hien tai:
    - `SessionManager.getInstance().getCurrentUser().getRole() == Role.PET_CARE_STAFF`.
  - Voi role `PET_CARE_STAFF`, `configureEditableInput(...)` ep:
    - `setEditable(true)`.
    - `setDisable(false)`.
    - `setMouseTransparent(false)`.
    - `setFocusTraversable(true)`.
  - Parent form luon duoc ep interactive bang `keepFormParentsInteractive()`:
    - root/content/grid khong bi disable.
    - root/content/grid khong mouseTransparent.
  - Focus mac dinh van dat vao `txtNote` bang `Platform.runLater(...)`, khong focus vao ma pet read-only.
- Da them debug log bat buoc:
  - `[HealthForm] initialize role = ...`
  - `[HealthForm] setPet role = ...`
  - `[HealthForm] symptom disable=..., editable=..., mouseTransparent=...`
  - `[HealthForm] note disable=..., editable=..., mouseTransparent=...`
  - `[HealthForm] booking disable=..., editable=..., mouseTransparent=...`
  - `[HealthForm] save disable=...`
  - Khi role la `PET_CARE_STAFF`, expected log:
    - `disable=false`
    - `editable=true`
    - `mouseTransparent=false`
    - `save disable=false`
- Da kiem tra logic typing:
  - Khong co listener `textProperty()` tren `txtSymptom`, `txtNote`, `txtBookingId`.
  - Khong query DB khi dang go.
  - Khong reload form khi dang go.
  - Save chi chay trong `onSave()`.
- CSS:
  - `ph-health-text-field` chi set height.
  - `ph-form-input` chi set mau/border/padding.
  - Cac field edit khong dung class `ph-form-readonly`.
- Build: `mvn clean compile` thanh cong. Chi co warning OpenJFX/deprecated API, khong co loi compile.

## File sua trong dot fix input HealthForm 17/05/2026

- `src/main/java/PetHotel/gui/controller/HealthRecordFormController.java`: ep field edit cho role cham soc, them parent interactive guard, log field-state.
- `src/main/resources/PetHotel/gui/view/HealthRecordForm.fxml`: them fx:id parent, set disable/mouseTransparent/focusTraversable ro cho input.
- `docs/flow-work/NguyenVanMinh-Customer-Pet-flow-work.md`: cap nhat flow-work dot nay.

## Cap nhat chi sua 2 o nhap HealthForm 17/05/2026

- Pham vi sua lan nay chi gom 2 o:
  - `Trieu chung bat thuong`.
  - `Ghi chu suc khoe`.
- Da doi trong `HealthRecordForm.fxml`:
  - `txtSymptom` tu `TextField` sang `TextArea`.
  - `txtNote` tu `TextField` sang `TextArea`.
  - Hai field deu set:
    - `editable="true"`.
    - `disable="false"`.
    - `mouseTransparent="false"`.
    - `focusTraversable="true"`.
    - `wrapText="true"`.
  - Hai field dung style `ph-form-input` + `ph-health-text-area`, khong dung `ph-form-readonly`.
- Da sua trong `HealthRecordFormController.java`:
  - `txtSymptom` va `txtNote` doi kieu sang `TextArea`.
  - Helper edit/log doi sang `TextInputControl` de hai o TextArea va booking TextField cung duoc ep editable dung cach.
  - Khong them listener `textProperty`, khong reload/query khi dang go.
- Build kiem tra: `mvn clean compile` thanh cong. Chi co warning OpenJFX/deprecated API, khong co loi compile.

## Cap nhat rollback 2 o HealthForm ve TextField thuan 17/05/2026

- Tiep tuc loi: sau khi doi sang `TextArea`, 2 o `Trieu chung bat thuong` va `Ghi chu suc khoe` van khong nhap duoc tren may test.
- Chi sua dung 2 o nay:
  - `txtSymptom`: doi lai tu `TextArea` ve `TextField` thuan.
  - `txtNote`: doi lai tu `TextArea` ve `TextField` thuan.
- Ly do:
  - Loai bo hoan toan skin/scroll-pane noi bo cua JavaFX `TextArea` va CSS `.ph-health-text-area`.
  - Hai o hien dung class `ph-form-input` + `ph-health-text-field`, giong `txtBookingId` dang nhap duoc.
- Trang thai 2 field sau sua:
  - `editable="true"`.
  - `disable="false"`.
  - `mouseTransparent="false"`.
  - `focusTraversable="true"`.
- Controller:
  - `HealthRecordFormController` doi `txtSymptom` va `txtNote` ve `TextField`.
  - Van dung helper `TextInputControl`, khong them listener/reload/query khi go.
- Build kiem tra: `mvn clean compile` thanh cong. Chi co warning OpenJFX/deprecated API, khong co loi compile.

## Cap nhat cursor/input cho 2 o HealthForm 17/05/2026

- Van de tiep theo: hover vao `Trieu chung bat thuong` va `Ghi chu suc khoe` van hien cursor mac dinh, khong hien dau nhay nhap lieu.
- Chi sua 2 field nay:
  - `txtSymptom`.
  - `txtNote`.
- Da sua FXML:
  - Them `cursor="TEXT"`.
  - Them `pickOnBounds="true"`.
  - Them style class rieng `ph-health-edit-field`.
- Da sua CSS:
  - Them `.ph-health-edit-field { -fx-cursor: text; -fx-opacity: 1; }`.
- Da sua controller:
  - `ensureEditable(...)` va `configureEditableInput(...)` ep `setCursor(Cursor.TEXT)` va `setPickOnBounds(true)`.
  - Them `setupTextEntryField(...)` cho rieng `txtSymptom`/`txtNote`.
  - Khi mouse entered/pressed/clicked vao 2 o nay, controller ep cursor TEXT va `requestFocus()`.
  - Khong consume mouse/key event.
  - Log field-state in them `cursor=...`.
- Build kiem tra: `mvn clean compile` thanh cong. Chi co warning OpenJFX/deprecated API, khong co loi compile.

## Goi y commit message

`feat(customer-pet): implement auto-generated CUS and PET ids`

## File nen review truoc khi merge

- `src/main/java/PetHotel/gui/controller/CustomerController.java`
- `src/main/java/PetHotel/gui/controller/PetController.java`
- `src/main/java/PetHotel/dao/CustomerDAO.java`
- `src/main/java/PetHotel/dao/PetDAO.java`
- `src/main/java/PetHotel/bus/CustomerBUS.java`
- `src/main/java/PetHotel/bus/PetBUS.java`

## Cap nhat HealthRecord input/DB connection 17/05/2026

- Nguyen nhan input runtime: `HealthRecordFormController` co nhieu lop cau hinh quyen chong nhau; view-only dang dung `setDisable(!editable)` nen field co the mat hit-test/cursor. Da thay bang `editMode` ro rang va view-only chi dung `editable=false`, khong disable field/parent.
- Da them hit-test log trong `HealthRecordFormController`:
  - `[HealthHitTest] moved target = ...`
  - `[HealthHitTest] clicked target = ...`
  - `[HealthHitTest] entered txtSymptom`
  - `[HealthHitTest] entered txtNote`
- `HealthRecordForm.fxml`: `txtSymptom` va `txtNote` co `disable=false`, `focusTraversable=true`, `pickOnBounds=true`, va style `ph-form-input` + `ph-health-text-field` + `ph-health-edit-field`.
- `PetController` va `CustomerController`: mo form theo dung thu tu load FXML -> setPet -> setEditMode -> setOnSaved -> tao Scene/Stage -> show. Nhan vien cham soc edit, le tan/quan ly view-only.
- `DBConnection.getConnection()` khong con return null; loi ket noi Oracle duoc nem thanh `SQLException` voi thong diep ro URL/user va huong kiem tra service/JDBC/username/password.
- `PetHealthRecordDAO.insert()` dung try-with-resources, kiem tra connection null/closed va them `bookingExists(...)`.
- `PetBUS.addHealthRecord(...)` validate `booking_id` ton tai truoc khi insert; neu sai bao `Ma booking khong ton tai.`
- Build kiem tra: `mvn clean compile` thanh cong. Chi con warning OpenJFX/deprecated API, khong co loi compile.
- Test connection rieng sau build: Oracle driver load duoc nhung listener tra `ORA-12518`, vi vay chua the test INSERT that tren DB local trong phien nay.
