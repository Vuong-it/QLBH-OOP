# Phần CSDL của Phong: PR độc lập vào main

## Phạm vi và nguồn

Nhánh `feature/phong-database-main` được tạo từ `origin/main` tại `bd77a75` ngày 23/10/2026. Phần gốc của Phong ở `547b519` (`feature/setup-database`, lịch sử khác main). Bản này dùng DAO/DTO/parser đã được điều chỉnh trên `integration/setup-builder` tại `9b0d538` để giữ tương thích với PR #1.

- DAO lọc SANPHAM.MOTA, DTO SetupFilterProduct, parser và test CSDL.
- Migration mở rộng MOTA, ví dụ metadata.
- 60 ảnh và toàn bộ SQL/CSV demo từ PR #3 tại `2f19103`.
- Fixture JDBC FakeSetupDatabase được tái sử dụng từ bản tích hợp của Vương để chạy test không cần SQL Server. Đây là hạ tầng test chung, không phải đóng góp riêng của Phong.

Không đưa thuật toán, Strategy, Facade, giỏ hàng hoặc Swing UI vào PR cá nhân. Không đưa script cài mới toàn database từ nhánh cũ vào vì phạm vi này là migration/bổ sung cho database đang có.

## Phụ thuộc và thứ tự tích hợp

PR này build độc lập trên main. DAO mở connection qua DriverManager với cấu hình JdbcHelper hiện có và hỗ trợ cùng JVM properties davisy.jdbc.url/user/password như bản tích hợp; không yêu cầu phương thức JdbcHelper.openConnection chỉ có ở PR #1.

Test DAO kiểm tra mapping trực tiếp thay vì gọi Facade chưa có trên main. Khi tích hợp cùng PR #1, cần giữ test Facade của PR #1 bên cạnh test DAO này; hai PR có phần DAO/test/migration chung nên Vương cần giải quyết overlap, không tính là hai lần đóng góp riêng.

PR này thay thế phạm vi ảnh/SQL của PR #3 cho đích main và bổ sung phần DAO/metadata/test/migration. PR #3 vẫn được giữ mở để đối chiếu; không merge cả hai gói ảnh/SQL như hai thay đổi độc lập.

## Kiểm chứng tại lần chuẩn bị PR

- JDK Temurin 17.0.20: biên dịch toàn bộ Java trong DAVISYS_1/src với thư viện library/* thành công.
- JUnit 4.11: SetupFilterDAOTest + DescriptionFilterTest, 11 tests PASS.
- Test gồm parser Unicode, metadata thiếu/sai/trùng, lọc theo nhóm, nhiều dòng kết quả, null giá/tồn kho và đóng tài nguyên.
- git diff --check: PASS.
- Chưa chạy NetBeans Ant/JAR và chưa chạy giao diện end-to-end trong lần này.
- Chưa chạy hai script demo ở @Apply=0, @Apply=1, @Apply=1 lần hai hay migration trên SQL Server thử nghiệm. Kết nối localhost bằng Windows authentication không có credentials; SQL authentication cấu hình mặc định bị từ chối. Không thay đổi database hiện hữu.
- Ghi nhận cũ từ máy Phong nằm trong phong-demo-verification.md; không coi đó là bằng chứng mới cho script đóng gói.

Lệnh tái hiện từ thư mục repository (PowerShell; build ra thư mục tạm riêng):

```powershell
$validation = Join-Path $env:TEMP 'davisy-phong-validation'
New-Item -ItemType Directory -Force "$validation/classes" | Out-Null
Get-ChildItem DAVISYS_1/src -Recurse -Filter *.java | ForEach-Object { '"' + $_.FullName.Replace('\','/') + '"' } | Set-Content "$validation/sources.txt"
javac -encoding UTF-8 --release 17 -cp 'library/*' -d "$validation/classes" "@$validation/sources.txt"
javac -encoding UTF-8 --release 17 -cp "library/*;$validation/classes" -d "$validation/classes" DAVISYS_1/test/com/support/FakeSetupDatabase.java DAVISYS_1/test/com/dao/SetupFilterDAOTest.java DAVISYS_1/test/com/dao/DescriptionFilterTest.java
java -cp "library/*;$validation/classes" org.junit.runner.JUnitCore com.dao.SetupFilterDAOTest com.dao.DescriptionFilterTest
```

## Cần hoàn thành trước khi approve

Trên database thử nghiệm có sản phẩm demo: chạy preview, apply rồi apply lại cả hai script. Lưu số dòng và dữ liệu trước/sau; xác nhận không ghi đè hãng đã chỉnh, SB0012 thuộc TN, lần chạy lại không đổi dữ liệu. Chạy migration hai lần, đối chiếu kiểu/nullability/dung lượng MOTA và nội dung mô tả. Kiểm thử sâu transaction/rollback của SetupCartDAO vẫn theo phân công của Vương.
