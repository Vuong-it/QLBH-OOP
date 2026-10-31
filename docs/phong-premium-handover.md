# Bàn giao đợt bổ sung 10 sản phẩm giá cao — Phong

Base: main tại ac1ccec513fa608faad80530a269b7eaf26bbcc3. Phạm vi: dữ liệu và ảnh, không chỉnh thuật toán/giao diện, không sửa JdbcHelper hay cấu hình NetBeans.

- 4 laptop ASUS/Apple, 3 màn hình ASUS/LG, 3 máy tính để bàn Apple/HP/ASUS; mã SB0061–SB0070.
- 10 ảnh từ trang chính thức hãng, đã xem contact sheet; URL nguồn, SHA-256, giá giả định và giới hạn cấu hình được ghi tại database/demo/premium/.
- Script 05 mặc định preview, chỉ thêm thiếu trên DB DAVISYS_DEMO_ riêng, transaction/rollback và từ chối trùng mã khác nội dung. Không UPDATE/DELETE sản phẩm hiện có, không chạm giỏ/hóa đơn/tài khoản.
- Script 06 kiểm tra 10 dòng/3 nhóm, tên hãng thực, giá và metadata; cho phép tồn kho giảm do checkout.
- MOTA đầy đủ 5 mục đích và ít nhất một phong cách; điểm/phong cách là đánh giá giả định của nhóm, không phải benchmark hãng. Apple không được gán điểm Gaming đủ ngưỡng nhằm ép có kết quả.
- Các file baseline SB60 không đổi, kể cả products.tsv, bộ dựng DB, validator và DemoCatalogTest; thêm một test riêng cho phần mở rộng.

## Kiểm tra trước bàn giao

- Biên dịch toàn bộ Java main + test bằng Eclipse ECJ 3.40.0, target Java 17: PASS. Không thay đổi code production. Ba warning manifest Class-Path thư viện cũ không gây lỗi biên dịch.
- JUnit 4.11: 69 tests PASS, 0 failures; bao gồm 3 test mới PremiumCatalogTest (ảnh/metadata, LAP+MH 60 triệu, PC+MH 80 triệu và ngân sách thiếu), các test cũ SB60/DAO/service/cart/panel/hóa đơn. Chạy headless, JDBC fixture; không chạy bộ SQL Server opt-in.
- Đã mở contact sheet và xem đủ 10 ảnh; PNG đều đọc được bằng Java ImageIO.
- git diff --check: PASS. SB0001–SB0060 và toàn bộ file cũ không đổi.
- Chưa kết nối hoặc ghi SQL Server thật từ môi trường soạn gói; chưa cập nhật DB trên Windows của Phong. Chưa xác nhận SQL chạy lặp bằng engine thật, chỉ thiết kế INSERT-only và guard xung đột.

## Điểm dừng đợt

Phong áp dụng preview → Apply=1 trên DB demo và chép ảnh vào project mới, tải lại dữ liệu rồi chụp một combo có LAP/MH hoặc PC/MH. Gửi Vương review dữ liệu/ảnh. Các kiểm thử checkout sâu và thay đổi thuật toán không thuộc đợt bổ sung này.
