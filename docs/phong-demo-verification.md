# Kiểm tra bản tích hợp trên máy Phong — 22/10/2026

## Phạm vi và nguồn bằng chứng

Nhánh kiểm tra ban đầu: review/phong-setup-builder, dựa trên integration/setup-builder tại 9b0d538. Nhánh bổ sung: feature/phong-demo-data.

Đây là ghi nhận từ log/ảnh và xác nhận của Phong trong quá trình chạy tại máy Windows; không phải kết quả SQL Server do môi trường soạn tài liệu tự chạy.

| Hạng mục | Bằng chứng / trạng thái |
|---|---|
| Build | Log BUILD SUCCESSFUL với JDK 17, 132 source Java |
| Test tự động | Phong xác nhận 100% sau khi tạo thư mục build-integration cho test ghi setup-preview.png; số 58 test theo báo cáo tích hợp |
| Đăng nhập | SSMS đăng nhập sa được; DAVISY mở trang Tổng quan với tài khoản Admin |
| Dữ liệu | Setup Builder báo tải 68 sản phẩm; chưa khẳng định tất cả đều có đủ metadata hoặc ảnh |
| Không lọc | Tạo 3 combo cho BP+CH; chuyển từ 230.000 sang 300.000 VNĐ và bảng cập nhật |
| Đổi phụ kiện | Chuột SB0006 80.000 → SB0008 250.000; combo 300.000 → 470.000; dư 9.530.000 trên ngân sách 10 triệu |
| Gaming + Hiện đại | Ảnh trả SB0012 70/100 và SB0010 93/100, tổng 1.070.000; khi đó SB0012 bị gán BP sai |
| Sửa loại | Sau sửa, Phong xác nhận SB0012 không còn trong kết quả nhóm BP; chưa có ảnh mới xác nhận toàn bộ combo sau sửa |
| Ảnh | Phong chép 60 PNG từ bản cũ sang bản tích hợp, xác nhận thẻ sản phẩm xuất hiện |
| Hãng | Phong xác nhận cập nhật; ảnh kiểm tra SB0006 hiện HP. SB0012 đã được chỉnh Logitech riêng. Chưa có log xác nhận đủ 29 dòng cập nhật |
| MOTA | Kết quả SQL: nvarchar(2000); đạt dung lượng đích của migration hiện tại. Nullability và hành vi chạy lặp chưa kiểm chứng |
| Giỏ / thanh toán / locking | Chưa kiểm chứng trên SQL Server thật |

## Giới hạn và việc tiếp theo

Chưa tuyên bố nghiệm thu toàn ứng dụng. Cần hoàn thành giỏ demo, thêm combo và kiểm tra số lượng, rollback, hóa đơn; kiểm tra thêm các cặp mục đích/phong cách và chuẩn hóa dữ liệu loại hàng.

Nhánh ảnh/script này bổ sung dữ liệu demo cho nhánh tích hợp. Nó không thay thế PR ghi nhận code DAO/metadata của feature/setup-database; nhánh cũ không có chung ancestor với main tại lần kiểm tra GitHub.

Chỉ stage ảnh đúng SBxxxx.PNG, các SQL/CSV/Markdown này. Giữ cấu hình NetBeans máy cá nhân, secret, build/dist, ảnh sao chép và ZIP nguồn ngoài commit.
