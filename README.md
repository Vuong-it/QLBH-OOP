# DAVISY – Quản Lý Bán Hàng Phụ Kiện & Smart Setup Builder (QLBH-OOP)

![Java](https://img.shields.io/badge/Java-JDK%2017-orange)
![UI](https://img.shields.io/badge/UI-Java%20Swing-blue)
![Database](https://img.shields.io/badge/Database-SQL%20Server-red)
![Testing](https://img.shields.io/badge/Tests-JUnit%204.11-brightgreen)

Ứng dụng Desktop Java Swing quản lý cửa hàng bán phụ kiện & thiết bị công nghệ **DAVISY**, được phát triển và mở rộng trong khuôn khổ học phần **Lập trình Hướng đối tượng (OOP)**. Trọng tâm của phiên bản này là tích hợp phân hệ **Smart Setup Builder** — công cụ tư vấn và tự động ghép bộ phụ kiện (combo) theo ngân sách, mục đích sử dụng và phong cách thẩm mỹ, kết nối trực tiếp vào quy trình giỏ hàng và thanh toán giao dịch (ACID Transaction).

---

## Mục lục

1. [Giới thiệu dự án & Tính năng trọng điểm](#1-giới-thiệu-dự-án--tính-năng-trọng-điểm)
2. [Phân công 4 thành viên thực hiện](#2-phân-công-4-thành-viên-thực-hiện)
3. [Kiến trúc Hướng đối tượng & Luồng xử lý](#3-kiến-trúc-hướng-đối-tượng--luồng-xử-lý)
4. [Cấu trúc thư mục dự án](#4-cấu-trúc-thư-mục-dự-án)
5. [Hướng dẫn cài đặt & Chạy ứng dụng](#5-hướng-dẫn-cài-đặt--chạy-ứng-dụng)
6. [Tài khoản đăng nhập & Dữ liệu Demo](#6-tài-khoản-đăng-nhập--dữ-liệu-demo)
7. [Kiểm thử tự động (Unit & Integration Tests)](#7-kiểm-thử-tự-động-unit--integration-tests)
8. [Các chức năng nền tảng của hệ thống DAVISY](#8-các-chức-năng-nền-tảng-của-hệ-thống-davisy)
9. [Dự án gốc & Nguồn tham khảo](#9-dự-án-gốc--nguồn-tham-khảo)

---

## 1. Giới thiệu dự án & Tính năng trọng điểm

### 1.1. Bối cảnh
Hệ thống **DAVISY** cung cấp các nghiệp vụ quản lý cửa hàng bán lẻ phụ kiện: Đăng nhập phân quyền, Quản lý sản phẩm/hãng/loại hàng, Quản lý khách hàng, Giỏ hàng, Hóa đơn tích điểm và Thống kê doanh thu.

### 1.2. Tính năng mới: Smart Setup Builder
Nhằm giải quyết bài toán khách hàng muốn mua trọn bộ góc làm việc/học tập/gaming nhưng bị giới hạn ngân sách, nhóm đã thiết kế và tích hợp phân hệ **Smart Setup Builder**:
- **Nhập ngân sách mục tiêu (Budget):** Xử lý tiền tệ chính xác tuyệt đối bằng kiểu số nguyên `long` (VNĐ) qua `SetupMoney`, chống tràn số (`Math.addExact`) và sai số dấu phẩy động.
- **Lọc đa tiêu chí:**
  - *Mục đích sử dụng:* Học tập, Văn phòng, Gaming, Thiết kế đồ họa, Làm việc đa nhiệm (ngưỡng điểm phù hợp $\ge 70/100$).
  - *Phong cách thẩm mỹ:* Tối giản, Hiện đại, Năng động, Sang trọng.
  - *Nhóm sản phẩm đa chọn:* Bàn phím (`BP`), Chuột (`CH`), Tai nghe (`TN`), Lót chuột (`LC`), Giá đỡ laptop (`GD`), Đế tản nhiệt (`DTN`), Webcam (`WC`), Micro (`MC`), Loa (`LOA`), Hub chuyển đổi (`HUB`), Đèn bàn (`DB`), Sạc dự phòng (`SDP`).
- **Gợi ý tối đa 3 phương án Combo khác nhau (Strategy Pattern):**
  - **Tiết kiệm (`BudgetStrategy`):** Ưu tiên tổng chi phí thấp nhất.
  - **Cân bằng (`BalancedStrategy`):** Kết hợp hài hòa giữa độ phù hợp ($70\%$) và tỉ lệ tiết kiệm ngân sách ($30\%$).
  - **Trải nghiệm (`PerformanceStrategy`):** Ưu tiên tổng điểm đánh giá/độ phù hợp cao nhất trong trần ngân sách.
- **Đổi phụ kiện linh hoạt (`replaceProduct`):** Thay thế bất kỳ phụ kiện nào trong combo bằng sản phẩm cùng nhóm còn tồn kho mà không vượt ngân sách.
- **Thêm vào giỏ hàng & Thanh toán an toàn (`SetupCartDAO` & `CheckoutDAO`):** Thực thi trong một Database Transaction duy nhất với khóa dòng (`UPDLOCK, HOLDLOCK`), tự động rollback toàn bộ nếu bất kỳ sản phẩm nào hết tồn kho hoặc thay đổi giá.

---

## 2. Phân công 4 thành viên thực hiện

Dự án được phát triển theo quy trình phân nhánh Git (`feature/*` $\to$ Pull Request $\to$ Review $\to$ `main`) với phạm vi trách nhiệm rõ ràng cho 4 thành viên:

| Thành viên | GitHub | Nhánh phụ trách | Vai trò & Trách nhiệm chính | Các Package / File trọng tâm |
| :--- | :--- | :--- | :--- | :--- |
| **Vương** *(Project Lead)* | [@Vuong-it](https://github.com/Vuong-it) | `feature/setup-integration`<br>`integration/setup-builder` | - Thiết kế kiến trúc tổng thể & **Facade Pattern**<br>- Xây dựng giao dịch giỏ hàng (`SetupCartDAO`) và thanh toán nguyên tử (`CheckoutDAO`)<br>- Chuẩn hóa tiền tệ `SetupMoney`, ánh xạ `SetupProductMapper`<br>- Nhúng `SetupBuilderPanel` vào `Home.java`, điều phối review & tích hợp PR | `com.service.SetupBuilderFacade`<br>`com.service.SetupMoney`<br>`com.service.SetupProductMapper`<br>`com.dao.SetupCartDAO`<br>`com.dao.CheckoutDAO`<br>`com.gui.Home`<br>`test/com/dao/SetupCartDAOTest.java`<br>`test/com/dao/SetupSqlServerIntegrationTest.java` |
| **Việt** | [@TongViet1701](https://github.com/TongViet1701) | `feature/setup-algorithm` | - Thiết kế mô hình **OOP cốt lõi** và đảm bảo tính bất biến (**Immutability**)<br>- Xây dựng **Strategy Pattern** với 3 chiến lược xếp hạng combo<br>- Thuật toán **Backtracking** kết hợp cắt nhánh (Pruning) theo ngân sách và đổi phụ kiện cùng loại<br>- Xây dựng bộ Unit Test kiểm thử giải thuật & biên tiền tệ | `com.entity.SetupProduct`<br>`com.entity.SetupCombo`<br>`com.strategy.ComboStrategy`<br>`com.strategy.BudgetStrategy`<br>`com.strategy.BalancedStrategy`<br>`com.strategy.PerformanceStrategy`<br>`com.service.SetupBuilderService`<br>`test/com/service/*` |
| **Tuấn** | [@Deptraikemetao](https://github.com/Deptraikemetao) | `feature/giao-dien` | - Thiết kế giao diện người dùng **Swing (`SetupBuilderPanel`)**<br>- Xây dựng bộ lọc ngân sách, mục đích, phong cách, danh sách đa chọn nhóm phụ kiện<br>- Bảng hiển thị chi tiết combo, hộp thoại đổi phụ kiện trực quan<br>- Xử lý bất đồng bộ với `SwingWorker` (không chặn luồng EDT) | `com.gui.SetupBuilderPanel`<br>`test/com/gui/SetupBuilderPanelTest.java` |
| **Phong** | [@nguyenthephongntp2709-sys](https://github.com/nguyenthephongntp2709-sys) | `feature/setup-database`<br>`feature/phong-database-main` | - Thiết kế truy vấn CSDL & bộ phân tích **Regex Metadata** từ cột `SANPHAM.MOTA`<br>- Xây dựng `SetupFilterDAO` và thực thể `SetupFilterProduct`<br>- Viết script Migration mở rộng cột `MOTA` an toàn (idempotent)<br>- Chuẩn bị bộ dữ liệu chuẩn 60 sản phẩm (`SB0001`–`SB0060`) kèm 60 ảnh minh họa | `com.dao.SetupFilterDAO`<br>`com.entity.SetupFilterProduct`<br>`database/migrations/*`<br>`database/demo/*`<br>`test/com/dao/SetupFilterDAOTest.java`<br>`test/com/dao/DescriptionFilterTest.java`<br>`test/com/dao/DemoCatalogTest.java` |

---

## 3. Kiến trúc Hướng đối tượng & Luồng xử lý

### 3.1. Sơ đồ luồng kiến trúc (Data & Logic Flow)

```text
[Home.java] (Menu chính / Drawer điều hướng)
     │
     ▼
[SetupBuilderPanel] (Tuấn - Giao diện Swing & SwingWorker chạy nền)
     │
     ▼
[SetupBuilderFacade] (Vương - Facade Pattern: Điểm giao tiếp nghiệp vụ duy nhất)
     │
     ├─────────────────────────────────────────────────────────┐
     ▼                                                         ▼
[SetupFilterDAO] (Phong)                        [SetupBuilderService] (Việt)
- Đọc SANPHAM & LOAIHANG còn tồn kho            - Thuật toán Backtracking + Pruning
- Bóc tách Regex điểm Nhu cầu & Phong cách      - Áp dụng Strategy Pattern:
- Ánh xạ qua SetupProductMapper                   + BudgetStrategy (Tiết kiệm)
                                                  + BalancedStrategy (Cân bằng)
                                                  + PerformanceStrategy (Trải nghiệm)
                                                               │
                                                               ▼
                                                [SetupCartDAO & CheckoutDAO] (Vương)
                                                - Thêm 1 đơn vị/sản phẩm vào GIOHANGTAM
                                                - Khóa UPDLOCK/HOLDLOCK kiểm tra giá & kho
                                                - Giao dịch ACID, tự động Rollback nếu lỗi
```

### 3.2. Các nguyên lý OOP & Design Patterns áp dụng
- **Encapsulation & Immutability:** `SetupProduct` và `SetupCombo` có toàn bộ thuộc tính `final`, danh sách sản phẩm bên trong dùng `List.copyOf(...)` bất biến, phân biệt rõ số lượng tồn kho (`soLuong`) với số lượng mua trong mỗi combo ($1$ đơn vị/nhóm).
- **Strategy Pattern (`com.strategy.*`):** Tách biệt hoàn toàn công thức chấm điểm combo ra khỏi thuật toán vét cạn, dễ dàng bổ sung chiến lược tư vấn mới mà không sửa đổi `SetupBuilderService`.
- **Facade Pattern (`SetupBuilderFacade`):** Ẩn toàn bộ chi tiết kết nối CSDL, chuyển đổi thang điểm ($/100 \to /5$) và thuật toán phía sau một lớp giao tiếp duy nhất dành cho tầng GUI.
- **Concurrency & ACID Transaction:** Tách biệt luồng giao diện (EDT) bằng `SwingWorker`; bảo vệ tính toàn vẹn dữ liệu giỏ hàng và hóa đơn ở mức cơ sở dữ liệu.

---

## 4. Cấu trúc thư mục dự án

```text
QLBH-OOP/
├── DAVISYS_1/                        # Project Java Swing (NetBeans Ant Project)
│   ├── src/com/
│   │   ├── cart/                     # Component hiển thị thẻ sản phẩm trong giỏ hàng
│   │   ├── dao/                      # Tầng truy xuất CSDL (SetupFilterDAO, SetupCartDAO, CheckoutDAO,...)
│   │   ├── entity/                   # Thực thể nghiệp vụ (SetupProduct, SetupCombo, SetupFilterProduct,...)
│   │   ├── service/                  # Nghiệp vụ & Facade (SetupBuilderService, SetupBuilderFacade, SetupMoney,...)
│   │   ├── strategy/                 # Strategy Pattern (ComboStrategy, Budget/Balanced/PerformanceStrategy)
│   │   ├── gui/                      # Giao diện người dùng (Login, Home, SetupBuilderPanel, HoaDon,...)
│   │   ├── swing/ & ui/              # Custom Swing components & Drawer menu
│   │   ├── utils/                    # Tiện ích hệ thống (JdbcHelper, Auth, MsgBox, XDate, XImage)
│   │   └── images/                   # Tài nguyên ảnh sản phẩm (bao gồm SB0001.PNG .. SB0060.PNG)
│   └── test/com/                     # Bộ kiểm thử JUnit 4 (Service, DAO, GUI, Entity, FakeSetupDatabase)
├── database/
│   ├── DAVISYS.sql                   # Script CSDL gốc của hệ thống DAVISY
│   ├── setup_metadata_example.md     # Quy chuẩn định dạng chuỗi metadata trong cột SANPHAM.MOTA
│   ├── migrations/
│   │   └── 001_setup_description_capacity.sql  # Migration mở rộng cột MOTA lên NVARCHAR(2000)
│   └── demo/                         # Gói bàn giao dữ liệu Demo 60 sản phẩm (SB0001 - SB0060)
│       ├── DAVISYS_Demo_Ready.sql    # Script dựng mới CSDL demo độc lập (DAVISYS_DEMO_SB60)
│       ├── 03_apply_catalog.sql      # Script cập nhật catalog 60 sản phẩm (mặc định preview)
│       ├── 04_validate_catalog.sql   # Script kiểm tra tính toàn vẹn của gói dữ liệu demo
│       └── README.md                 # Hướng dẫn chi tiết sử dụng gói dữ liệu demo
├── library/                          # Toàn bộ thư viện .jar (JDBC, JUnit, ZXing, Apache POI, JFreeChart,...)
├── scripts/                          # Script PowerShell hỗ trợ tạo DB kiểm thử cô lập
├── docs/                             # Tài liệu biên bản kiểm chứng SQL & bàn giao
├── integration-notes.md              # Báo cáo tích hợp hệ thống & kiểm thử tự động
└── rules.md                          # Quy tắc phát triển, chuẩn code OOP & quy trình Pull Request
```

---

## 5. Hướng dẫn cài đặt & Chạy ứng dụng

### 5.1. Yêu cầu hệ thống
- **Java Development Kit (JDK):** Phiên bản **17** trở lên.
- **IDE:** Apache NetBeans (khuyến nghị NetBeans 15+) hỗ trợ Java Ant Project.
- **Hệ quản trị CSDL:** Microsoft SQL Server (2019 / 2022 / 2025) và SQL Server Management Studio (SSMS).

### 5.2. Khởi tạo Cơ sở dữ liệu
Bạn có thể chọn **1 trong 2 cách** dưới đây (khuyến nghị **Cách 1** để trải nghiệm đầy đủ 60 sản phẩm của Smart Setup Builder):

#### Cách 1: Dựng CSDL Demo 60 sản phẩm độc lập (Khuyến nghị)
1. Mở **SSMS**, kết nối vào SQL Server của bạn.
2. Mở file `database/demo/DAVISYS_Demo_Ready.sql` và nhấn **Execute**.
   - Script sẽ tạo một database mới tên là `DAVISYS_DEMO_SB60` (cam kết không ghi đè database `DAVISYS` sẵn có).
   - Tự động tạo đầy đủ bảng, khóa ngoại, 13 Stored Procedures thống kê, 60 sản phẩm (`SB0001`–`SB0060`) thuộc 12 nhóm phụ kiện, cùng tài khoản và giỏ hàng demo.
3. *(Tùy chọn)* Chạy file `database/demo/04_validate_catalog.sql` để xác nhận trạng thái `PASS` (60 sản phẩm, 12 nhóm, 3 sản phẩm hết hàng để test bộ lọc tồn kho).

#### Cách 2: Sử dụng CSDL DAVISYS gốc và chạy Migration
1. Chạy `database/DAVISYS.sql` trên SSMS để tạo CSDL `DAVISYS` mặc định.
2. Chạy tiếp `database/migrations/001_setup_description_capacity.sql` để mở rộng cột `SANPHAM.MOTA` lên `NVARCHAR(2000) NOT NULL`.
3. Nếu muốn nạp bộ 60 sản phẩm vào DB demo riêng, xem hướng dẫn tại [`database/demo/README.md`](database/demo/README.md).

### 5.3. Cấu hình kết nối & Chạy trên NetBeans IDE
Ứng dụng hỗ trợ cấu hình kết nối linh hoạt qua **JVM Options** trong `JdbcHelper` mà không cần sửa code hay commit mật khẩu cá nhân:
1. Mở NetBeans IDE $\to$ **File** $\to$ **Open Project** $\to$ chọn thư mục `DAVISYS_1`.
2. Nhấp chuột phải vào project `DAVISYS_1` $\to$ **Properties** $\to$ **Run**:
   - **Working Directory:** Để mặc định tại thư mục `DAVISYS_1` (để ứng dụng nạp đúng đường dẫn ảnh `src/com/images/`).
   - **VM Options** *(nếu dùng DB Demo `DAVISYS_DEMO_SB60`)*:
     ```text
     -Ddavisy.jdbc.url=jdbc:sqlserver://localhost:1433;databaseName=DAVISYS_DEMO_SB60;encrypt=true;trustServerCertificate=true -Ddavisy.jdbc.user=sa -Ddavisy.jdbc.password=<mat_khau_sql_cua_ban>
     ```
     *(Nếu không truyền VM Options, hệ thống mặc định kết nối tới `databaseName=DAVISYS` với user `sa` / mật khẩu `123`).*
3. Nhấn **Run Project (F6)** hoặc chạy trực tiếp file `com.gui.Login`.

---

## 6. Tài khoản đăng nhập & Dữ liệu Demo

### 6.1. Tài khoản trên CSDL Demo (`DAVISYS_DEMO_SB60`)
| Tên đăng nhập | Mật khẩu | Vai trò | Giỏ hàng khách Demo đi kèm |
| :--- | :--- | :--- | :--- |
| `demo_admin` | `DemoOnly-2026` | Admin | Khách hàng: `Khách Demo` (`MAKH = DEMO001`, SĐT / `MAGH = 0000000001`) |

**Kịch bản trải nghiệm nhanh Smart Setup Builder:**
1. Đăng nhập với tài khoản `demo_admin` / `DemoOnly-2026`.
2. Trên màn hình chính, nhấn nút **Smart Setup Builder** trên thanh tiêu đề hoặc mở từ **Menu Drawer** bên trái.
3. Nhấn **Tải lại dữ liệu**, giữ phím `Ctrl` chọn các nhóm sản phẩm (ví dụ: `BP` - Bàn phím, `CH` - Chuột, `TN` - Tai nghe).
4. Nhập ngân sách `2000000`, chọn Mục đích **Gaming**, Phong cách **Hiện đại** $\to$ nhấn **Tạo bộ phụ kiện**.
5. Xem 3 gợi ý combo (**Tiết kiệm**, **Cân bằng**, **Trải nghiệm**), thử chọn 1 dòng sản phẩm và nhấn **Đổi phụ kiện**.
6. Chọn giỏ hàng `Khách Demo – 0000000001` $\to$ nhấn **Thêm vào giỏ hàng**, sau đó mở mục **Giỏ hàng** (nhập SĐT `0000000001`) để xác nhận đơn hàng.

### 6.2. Tài khoản trên CSDL Gốc (`DAVISYS`)
| Họ và tên | Tên đăng nhập | Mật khẩu | Chức vụ | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| Trần Văn Nhuộm | `Admin` | `admin` | Quản trị (Admin) | Đang hoạt động |
| Trần Hữu Đang | `DangTH` | `123` | Quản lý | Đang hoạt động |
| Nguyễn Khánh Đan | `DanNK` | `123` | Nhân viên | Đang hoạt động |
| Đoàn Hiệp Sỹ | `SyDH` | `123` | Nhân viên | Đang hoạt động |
| Phùng Quốc Vinh | `VinhPQ` | `123` | Nhân viên | Đang hoạt động |
| Lê Bích Vi | `ViLB` | `123` | Nhân viên | Đang hoạt động |

---

## 7. Kiểm thử tự động (Unit & Integration Tests)

Dự án sở hữu bộ kiểm thử tự động JUnit 4 nằm tại `DAVISYS_1/test/com/` bao phủ toàn bộ logic thuật toán, bộ lọc metadata, giao dịch giỏ hàng và giao diện:

- `SetupBuilderServiceTest` (26 tests): Kiểm thử thuật toán Backtracking, 3 Strategy, thay thế phụ kiện, bắt lỗi tràn số và tính bất biến của dữ liệu.
- `SetupMoneyTest` (9 tests): Kiểm thử biên tiền tệ VNĐ và ánh xạ `SetupProductMapper`.
- `SetupFilterDAOTest` & `DescriptionFilterTest` (12 tests): Kiểm thử bóc tách Regex metadata tiếng Việt có dấu/không dấu, dữ liệu khuyết hoặc sai định dạng.
- `DemoCatalogTest` (1 test): Kiểm tra tính toàn vẹn của toàn bộ 60 sản phẩm demo, 60 file ảnh PNG và 300 điểm mục đích trong `products.tsv`.
- `SetupCartDAOTest` (10 tests): Kiểm thử giao dịch thêm combo vào giỏ hàng và cơ chế Rollback bằng `FakeSetupDatabase` (in-memory JDBC fixture, không phụ thuộc SQL Server).
- `SetupBuilderPanelTest` (1 test): Kiểm thử luồng tương tác giao diện trên EDT và `SwingWorker`.
- `HoaDonRegressionTest` (1 test): Kiểm thử hồi quy công thức tính giảm giá và tích điểm hóa đơn.
- `SetupSqlServerIntegrationTest` (12 tests, *opt-in*): Kiểm thử tích hợp trực tiếp trên SQL Server thật (đồng thời nhiều luồng, khóa dòng, rollback hóa đơn/tồn kho).

**Chạy bộ kiểm thử tiêu chuẩn (60 tests không cần SQL Server) trong NetBeans:**
- Nhấn chuột phải vào project `DAVISYS_1` $\to$ chọn **Test (Alt + F6)**.

---

## 8. Các chức năng nền tảng của hệ thống DAVISY

Bên cạnh phân hệ **Smart Setup Builder**, ứng dụng duy trì đầy đủ các tính năng quản lý bán hàng của hệ thống DAVISY:
- **Quản lý sản phẩm, hãng sản xuất & loại hàng:** Thêm/sửa/xóa, tìm kiếm nâng cao, sắp xếp A–Z/Z–A, nhập danh sách sản phẩm hàng loạt từ file Excel (`.xlsx`), xuất thông tin sản phẩm ra file Word (`.doc`) hoặc gửi qua Email cho khách hàng.
- **Tích hợp mã QR & Webcam:** Tự động sinh mã QR cho từng sản phẩm (thư viện `ZXing`), quét mã QR sản phẩm trực tiếp qua Webcam để thêm vào giỏ hàng và quét mã QR xác thực khi quên mật khẩu.
- **Quản lý bán hàng & Hóa đơn:** Quản lý giỏ hàng theo khách hàng, áp dụng phần trăm giảm giá và đổi điểm tích lũy, đọc số tiền bằng chữ tiếng Việt (`ReadMoney`), in hóa đơn thanh toán.
- **Quản lý tài khoản & Phân quyền:** Phân quyền rõ ràng giữa **Admin**, **Quản lý** và **Nhân viên**; quản lý hồ sơ nhân viên, chức vụ và khách hàng thân thiết.
- **Thống kê & Biểu đồ:** Thống kê doanh thu và sản phẩm bán chạy theo Ngày/Tháng/Năm trực quan bằng biểu đồ cột và biểu đồ tròn (`JFreeChart`), hỗ trợ xuất báo cáo ra file Excel.
- **Tùy biến giao diện:** Chuyển đổi linh hoạt giữa chế độ Sáng (**Light Mode**) và Tối (**Dark Mode**), tùy chỉnh ảnh banner và thư mục lưu mã QR.

---

## 9. Dự án gốc & Nguồn tham khảo

Dự án học phần OOP này được kế thừa, phát triển mở rộng và tái cấu trúc dựa trên nền tảng đồ án **DAVISY-PRO1014 (Phần mềm quản lý bán phụ kiện thiết bị di động DAVISY)**:

- **Nhóm tác giả dự án gốc (DAVISY TEAM – FPT Polytechnic Cần Thơ):**
  - [Trần Hữu Đang](https://github.com/Theanishtar) (Project Manager, Developer)
  - [Nguyễn Khánh Đan](https://github.com/nguyenkhanhdan) (BA Writer, Tester, Developer)
  - [Đoàn Hiệp Sỹ](https://github.com/DoanSy166) (DevOps, Developer)
  - [Phùng Quốc Vinh](https://github.com/Dinhisme) (Data Scientist, Developer)
  - [Lê Bích Vi](https://github.com/TheBank0911) (Business Analyst, Developer)
  - **Giảng viên hướng dẫn dự án gốc:** Thầy [Trần Văn Nhuộm](https://thaynhuom.edu.vn/)
- **Bài viết giới thiệu dự án gốc trên trang tin FPT Polytechnic:** [Dự án phần mềm quản lý bán phụ kiện thiết bị di động của sinh viên FPoly Cần Thơ](https://caodang.fpt.edu.vn/tin-tuc-poly/can-tho-tin-sinh-vien/du-an-phan-mem-quan-ly-ban-phu-kien-thiet-bi-di-dong-cua-sinh-vien-fpoly-can-tho-lot-mat-xanh-cua-giang-vien.html)
- **Video Demo dự án gốc:** [Xem trên YouTube](https://youtu.be/x7tL29kG5j8?si=6tEQbuafT8ae0QkT)
- **Thư viện & Tài liệu giao diện tham khảo:**
  - Custom Swing Components & Drawer UI: [DJ-Raven](https://github.com/DJ-Raven), [KeepToo](https://github.com/k33ptoo/)
  - Thư viện mã nguồn mở: `MSSQL JDBC Driver`, `ZXing` (QR Code), `Webcam Capture` (Sarxos), `Apache POI` (Excel), `JFreeChart` (Biểu đồ), `Jakarta Mail` (Email), `MigLayout`, `TimingFramework`, `JUnit 4`.