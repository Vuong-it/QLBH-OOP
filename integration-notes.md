# Báo cáo tích hợp Smart Setup Builder — #1 + #4

Ngày kiểm chứng: 23/10/2026 (Asia/Bangkok).
Trạng thái: **build và các kiểm chứng dưới đây PASS trên database thử nghiệm; chờ review/Approve trước merge main.**

## Bản tích hợp và phạm vi

- PR #1: https://github.com/Vuong-it/QLBH-OOP/pull/1, nhánh đích `integration/setup-builder`, điểm bắt đầu `9b0d53860798af929944263987d247089257679c`.
- Tích hợp PR #4 tại `2081bfc0daca7b528326047f9c8e683728aa400a` bằng merge có giữ lịch sử đóng góp; không merge PR #3 riêng.
- Base main kiểm tra: `bd77a75fd80fe6a2ef3fd06ffa77faa8908c6562`. Không merge vào main trong bước này.
- Hai conflict add/add được xử lý: giữ `SetupFilterDAO` sử dụng `JdbcHelper::openConnection`; giữ cả test mapping DAO độc lập của #4 và test Facade của #1. DTO/parser/fixture/migration chung chỉ có một bản.
- Nhận 60 ảnh và SQL/CSV/tài liệu demo từ #4. Script demo vẫn mặc định preview; không tự chạy vào database đang dùng.
- `JdbcHelper.openConnection` của #1 tiếp tục là boundary chung cho DAO cũ/mới, hỗ trợ JVM properties `davisy.jdbc.url/user/password`. Không lấy cấu hình/mật khẩu local vào Git.

## Lỗi checkout phát hiện và sửa

Luồng cũ dùng `listGHT` dùng chung cho nhiều giỏ, lấy dòng 0 khi tạo header/detail, tải lại toàn bộ giỏ giữa vòng lặp; có thể ghi sai khách/sản phẩm khi mua nhiều món. Header, detail, trừ kho, xóa giỏ và cộng điểm dùng các connection autocommit riêng. Mã hóa đơn lấy COUNT+1 có thể trùng sau khi xóa dữ liệu. Màn hình bán hàng còn giả định MAGH=số điện thoại và tính tổng bằng int.

Bản này thay đường checkout bằng `CheckoutDAO`:

- Một transaction cho xác thực đúng MAGH/MAKH/tài khoản đang hoạt động, đọc/khóa giỏ và tồn kho, kiểm tra tổng hiển thị, ghi header và từng detail, trừ kho, xóa đúng giỏ và cộng điểm. Bất kỳ lỗi nào đều rollback toàn bộ.
- Snapshot từng dòng của đúng giỏ; không đọc lại list Swing dùng chung giữa vòng lặp. Lưu đúng ngày nhập và dữ liệu sản phẩm hiện tại vào chi tiết hóa đơn.
- ID dựa trên MAX phần số, khóa bảng khi cấp ID trong transaction, không dựa trên số dòng.
- Màn hình bán hàng đọc/thêm/sửa/xóa theo mã giỏ thực và tài khoản. Nếu cùng số điện thoại có nhiều giỏ, ưu tiên giỏ đã chọn ở Builder; nếu không xác định được duy nhất thì không cho checkout nhầm.
- Tổng màn hình dùng checked long. Các cột/reader hóa đơn cũ vẫn dùng FLOAT/float: từ chối giá/tổng không biểu diễn chính xác ở boundary này thay vì âm thầm làm tròn. Test có tổng 3.000.000.000 VNĐ và giá 16.777.217 VNĐ không thể biểu diễn đúng bằng float.
- Checkout chạy trong SwingWorker, lấy context đã hiển thị và xác thực lại trong transaction; callback refresh kiểm tra cả số điện thoại và mã giỏ để không nhận kết quả tải cũ.
- Bỏ đường insertCTHD cũ có thể ghi dở dang. Không thay Home.form hoặc block initComponents do NetBeans sinh.

## Build và kiểm thử tự động

JDK Temurin 21.0.12, source/target 17; Ant 1.10.17 của NetBeans. Build JAR thành công. **71 tests PASS, 0 failures, 0 errors, 0 skipped** trong lần chạy cuối có bật SQL integration.

| Bộ test | Số test | Kết quả |
|---|---:|---|
| SetupBuilderServiceTest | 26 | PASS |
| SetupMoneyTest | 9 | PASS |
| SetupFilterDAOTest (DAO + Facade) | 7 | PASS |
| DescriptionFilterTest | 5 | PASS |
| SetupCartDAOTest (fixture JDBC) | 10 | PASS |
| SetupBuilderPanelTest | 1 | PASS |
| HoaDonRegressionTest | 1 | PASS |
| SetupSqlServerIntegrationTest (SQL Server thật) | 12 | PASS |

12 test SQL Server: sinh/đổi combo và thêm đúng giỏ với SL=1; thay giá sau sinh; stock=0 sau sinh; xóa sản phẩm sau sinh; lỗi dòng giỏ thứ hai rollback dòng đầu; hai thread/kết nối thêm đồng thời giữ đủ SL=2 cho cả 3 dòng; checkout 3 món giữ nguyên giỏ khách khác; lỗi detail thứ hai rollback cả header/detail/kho/giỏ/điểm; từ chối tổng đổi hoặc owner sai; stock đổi trước checkout không ghi dở dang; cấp ID khi có khoảng trống và giữ chính xác tổng lớn hơn int; từ chối làm tròn float.

Bằng chứng XML: `DAVISYS_1/build-integration/test/results/TEST-*.xml`. Output build/test không commit.

## Kiểm chứng GUI và đối chiếu SQL

Đã chạy ứng dụng Windows thật qua Login với tài khoản **giả lập** trên `DAVISYS_SETUP_REVIEW_20261023_A`, dùng Computer Use. Database này được tạo mới từ schema/constraints/procedures của repo, không sao chép tài khoản, khách hàng, giỏ hoặc hóa đơn thật.

- Login → Home: PASS, tài khoản qa_setup, tên Nhân viên thử, vai trò Admin.
- Home → Builder: PASS, tải 6 sản phẩm giả lập và 3 nhóm BP/CH/TN.
- Gaming + Hiện đại, chọn 3 nhóm: tạo 3 combo; combo tiết kiệm SB0001 + SB0006 + SB0012 = 300.000 VNĐ.
- Đổi bàn phím sang combo đã được gợi ý khác: hệ thống từ chối combo trùng đúng thông báo. Đổi chuột SB0006 (80.000) → SB0008 (250.000): thành công, tổng 470.000 VNĐ, dư 9.530.000 trên budget 10 triệu.
- Chọn giỏ `GH-SETUP-1` cho khách thử một, số điện thoại 0900000001: thêm thành công. SQL đối chiếu đúng SB0001/SB0008/SB0012, mỗi dòng SL=1; MAGH khác số điện thoại vẫn hoạt động.
- Drawer → Giỏ hàng → nhập số điện thoại và xác nhận: hiển thị đúng 3 món, tổng 470.000 VNĐ.
- Xác nhận đơn hàng: SQL ghi HD01 đúng TENDN=qa_setup, MAKH=QA1, MAGH=GH-SETUP-1, tổng/thành tiền 470.000; 3 detail đúng mã/giá/SL; giỏ rỗng; tồn kho của 3 món 20→19; điểm khách 0→4.
- Drawer → Hóa đơn → mở chi tiết: hiển thị đủ 3 món, đúng khách/nhân viên/ảnh sản phẩm.
- Cài đặt, Giới thiệu, quay về Home, vào lại Builder bằng drawer: PASS. Chỉ mở trang Cài đặt, không thay cấu hình.
- Đăng xuất: đã bấm xác nhận bằng tài khoản thử nghiệm; kiểm tra Login sau đăng xuất ở cuối phiên.

GUI đã chạy trên bản checkout/transaction mới; các tinh chỉnh cuối về cached cart context và từ chối float rounding được compile và chạy đầy đủ test sau đó. Không coi đây là kiểm thử toàn bộ chức năng CRUD của ứng dụng.

## Cô lập dữ liệu và bảo toàn checkout gốc

- Mọi thao tác ghi chỉ vào `DAVISYS_SETUP_REVIEW_20261023_A` hoặc database review #4 đã tạo riêng ở bước trước. Script test từ chối URL/database không có tiền tố review được chỉ định.
- Hai demo script có `USE [DAVISYS]`; việc kiểm chứng #4 trước đó dùng bản nội dung test đổi đúng đích database, không sửa file SQL gốc. Migration và demo SQL không thay đổi ở lần tích hợp này, bằng chứng #4 vẫn áp dụng cho đúng blob đó.
- Hash toàn bộ HANG/LOAIHANG/SANPHAM trong DAVISYS gốc vẫn bằng baseline trước review: `9C923EE0399C78BBD4DBF2418E6E2B725A55189071E0558B98A3D42B154A972C`.
- Checkout gốc `D:\BTL\OOP_Java\DAVISY-main` vẫn giữ nguyên 5 file local chưa commit; không checkout/reset/stash chúng. Công việc thực hiện tại worktree riêng được gắn với chat.
- Không commit build/dist, cấu hình NetBeans hoặc credentials thật. Không gửi comment, review hay tin nhắn dưới danh nghĩa người khác.

## Tái hiện test SQL

```powershell
# Tạo database mới, không ghi đè database đã tồn tại; cần quyền CREATE DATABASE.
./scripts/New-SetupReviewDatabase.ps1 -Database DAVISYS_SETUP_REVIEW_NEW

# Đặt biến chỉ cho process hiện tại; dùng credentials thử nghiệm của máy bạn.
$env:DAVISY_TEST_URL='jdbc:sqlserver://localhost:1433;databaseName=DAVISYS_SETUP_REVIEW_NEW;encrypt=true;trustServerCertificate=true'
$env:DAVISY_TEST_USER='<sql-test-user>'
$env:DAVISY_TEST_PASSWORD='<sql-test-password>'
& 'D:\App\NetBeans IDE\extide\ant\bin\ant.bat' -f DAVISYS_1/build.xml '-Dbuild.dir=build-integration' '-Ddist.dir=dist-integration' jar test
Remove-Item Env:DAVISY_TEST_URL,Env:DAVISY_TEST_USER,Env:DAVISY_TEST_PASSWORD
```

Không đặt các biến trên: 59 test thường chạy, 12 test SQL opt-in được skip; skip không phải PASS. Bộ test SQL reset dữ liệu trong database review giữa các test; không dùng nó trên database demo cần giữ dữ liệu. Script tạo schema không chạy seed từ repo; test tự tạo fixture giả lập.

## Điều kiện còn lại trước merge

1. Review/Approve bản head mới của #1; review cũ của Phong tại 9b0d538 chưa bao gồm CheckoutDAO và các sửa lỗi lần này. #4 cũng chưa được gửi Approve trong phiên làm việc này.
2. Chọn thứ tự merge #4 rồi #1 để giữ PR đóng góp riêng; #1 đã chứa ancestor #4 nên không cần áp lại gói ảnh/SQL. Không merge trùng #3.
3. Bản dữ liệu demo thật SB0001–SB0060 của Phong chưa có trên máy này. Trước trình diễn/rollout cần đối chiếu metadata/hãng/loại, tiền đề Logitech của SB0012 và các dòng chưa xác định hãng; không dùng fixture kiểm thử như bộ demo hoàn chỉnh.
4. Chưa kiểm thử máy in vật lý, webcam/QR, gửi email, toàn bộ CRUD/role matrix hoặc các thao tác sửa chi tiết hóa đơn/đổi điểm/giảm giá qua GUI. Công thức hóa đơn hiện có đã có regression test; không tuyên bố mọi nghiệp vụ cũ đều đã nghiệm thu.
5. Schema hóa đơn cũ còn FLOAT/float và cấp ID khóa bảng; bản này bảo vệ độ chính xác/atomicity trong phạm vi checkout, chưa thay kiến trúc tiền tệ hay tối ưu tải lớn toàn hệ thống.
