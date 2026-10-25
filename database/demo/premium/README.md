# Bổ sung 10 sản phẩm máy tính và màn hình — 25/10/2026

Đợt bàn giao riêng theo yêu cầu Vương/Phong: thêm vào 60 sản phẩm hiện có, không đổi thuật toán hay dữ liệu SB0001–SB0060. Mã mới SB0061–SB0070, 4 laptop (LAP), 3 màn hình (MH), 3 máy tính để bàn (PC). Mac mini thuộc máy tính để bàn, không gọi là cây tower.

## Danh mục và giá demo

| Mã | Mẫu có thật / thương hiệu | Nhóm | Giá giả định demo (VNĐ) |
|---|---|---|---:|
| SB0061 | ASUS TUF Gaming A15 FA507NV | LAP | 24.900.000 |
| SB0062 | ASUS ROG Strix G16 G614JV | LAP | 39.900.000 |
| SB0063 | Apple MacBook Air 13 M3 16GB/512GB | LAP | 27.900.000 |
| SB0064 | Apple MacBook Pro 14 M3 Pro 18GB/512GB | LAP | 49.900.000 |
| SB0065 | ASUS ProArt PA279CRV 27 inch 4K | MH | 13.900.000 |
| SB0066 | ASUS ROG Swift PG279QM 27 inch QHD | MH | 19.900.000 |
| SB0067 | LG UltraFine 32UN880-B 31.5 inch 4K | MH | 14.900.000 |
| SB0068 | Apple Mac mini M4 16GB/256GB | PC | 16.900.000 |
| SB0069 | HP OMEN 45L GT22-1470se | PC | 59.900.000 |
| SB0070 | ASUS ROG G22CH | PC | 44.900.000 |

Tên mẫu/nhãn hiệu và ảnh lấy từ trang hãng; xem `sources.csv` cho URL từng dòng. Giá nhập, giá bán, tồn kho, điểm /100 và phong cách là giả định trình diễn BTL, KHÔNG phải báo giá hiện hành, thông số benchmark hay tồn kho được hãng xác nhận. Các dòng ASUS/HP có nhiều cấu hình: không suy ra CPU/GPU/RAM/SSD chỉ từ ảnh; MOTA ghi rõ cấu hình tùy SKU. Ảnh Apple MacBook Pro do hãng dùng chung cho dòng M3 Pro/Max. Ảnh giữ nội dung gốc, chỉ chuyển PNG/thu nhỏ trên nền trắng để ứng dụng đọc offline; không tạo ảnh AI, không gán hãng ngẫu nhiên.

Nguồn ảnh thuộc các hãng tương ứng, được ghi nhận nguồn cho demo học tập nội bộ; không có tuyên bố ảnh là tài sản do nhóm sáng tác hoặc đã có giấy phép thương mại.

## Áp dụng vào bản đang chạy trên máy Phong

1. Chép 10 ảnh `DAVISYS_1/src/com/images/SB0061.PNG`–`SB0070.PNG` vào cùng đường dẫn của project đang chạy. Giữ working directory tại DAVISYS_1.
2. Trong SSMS mở `database/demo/05_add_premium_products.sql`. Mặc định USE DAVISYS_DEMO_SB60 và `@Apply=0`; F5 toàn bộ để xem trước 10 dòng.
3. Kiểm tra đúng DB demo và các mẫu. Đổi `@Apply=1`, F5 toàn bộ. Lần đầu bình thường InsertedPremiumProducts=10. Chạy lại không đặt lại tồn kho; nếu đã khớp thì InsertedPremiumProducts=0.
4. Script chỉ INSERT hãng/loại/sản phẩm chưa tồn tại. HP tái sử dụng mã hãng theo tên đang có (thường B12). Hãng LG dùng mã đề xuất LGE, phân biệt mã LG/LOGITECH cũ. Mã sản phẩm hoặc loại trùng khác nội dung sẽ THROW và rollback, không ghi đè.
5. Chạy `database/demo/06_validate_premium_products.sql`, mong đợi PASS/10 sản phẩm/3 nhóm mới, nhóm SB0001–SB0060 vẫn có 60 sản phẩm. Script này chỉ đọc; không đòi tồn kho bằng số gốc sau thanh toán.
6. Đóng/mở lại ứng dụng hoặc nhấn Tải lại dữ liệu. Giỏ hàng dùng ảnh từ source; khi chạy JAR/build mới có thể cần Clean and Build để copy tài nguyên.

Không chạy 03_apply_catalog.sql để thêm 10 sản phẩm: script 03 có mục đích reset snapshot 60 dòng cũ. Không chỉnh DAVISYS_Demo_Ready.sql: bộ dựng cũ vẫn tạo SB60; muốn dựng mới đủ 70 thì chạy bộ dựng SB60 trước, rồi 05 với Apply=1. Baseline products.tsv, manifest và test SB60 được giữ nguyên. Tổng 70 là khi DB ban đầu có đúng 60 sản phẩm, không phải ép xóa những sản phẩm khác.

## Chạy thử và dừng bàn giao

- Laptop: chọn LAP + MH, Gaming + Hiện đại, ngân sách 60.000.000. Có laptop ASUS; MacBook không được chấm Gaming >=70 chỉ để lấp dữ liệu.
- Cây máy: chọn PC + MH + BP + CH, Gaming + Hiện đại, ngân sách 90.000.000.
- Làm việc/đồ họa: PC + MH hoặc LAP + MH, Thiết kế đồ họa + Hiện đại, ngân sách 80.000.000; có Apple để so sánh.
- Không chọn cả LAP và PC trừ khi muốn mua cả hai, vì thuật toán chọn một món mỗi nhóm đã chọn.
- Ngân sách là trần tổng combo. Tiết kiệm vẫn chọn rẻ nhất; Trải nghiệm vẫn chọn điểm cao nhất. Bổ sung dữ liệu không đổi ý nghĩa chiến lược hoặc bắt khách tiêu hết tiền.
- Chỉ chụp danh sách sản phẩm và một combo ngân sách cao để Vương review đợt này. Không mở thêm yêu cầu rollback/checkout.

## Kiểm chứng

Xem `docs/phong-premium-handover.md`. Script SQL chưa được thực thi trên SQL Server trong môi trường soạn gói; cần xem trước/áp dụng trên DB demo máy Phong. Unit test không thay thế kiểm chứng SQL Server.
