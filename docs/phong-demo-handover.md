# Biên bản bàn giao demo SB60-v1 của Phong

Bộ bàn giao hoàn chỉnh nằm tại [database/demo/README.md](../database/demo/README.md). Nhánh xuất phát từ main f956e37 sau khi Vương đã merge PR #1 và #4.

## Các điểm đã chốt

- Hãng: giữ nguyên 30 sản phẩm đã có hãng; 30 sản phẩm còn lại được gán ngẫu nhiên vào 26 hãng có sẵn của nhóm đã xác định. Không thêm hãng mới. Xem assigned_demo_brands.md và bảng quyết định từng mã. Phép gán này phục vụ demo, không xác minh hãng thật của 30 sản phẩm theo ảnh.
- Loại: 12 nhóm, mỗi nhóm 5; SB0011–SB0015 đều là TN. SB0012 giữ LG/LOGITECH và TN.
- SB0004: bỏ TKL vì ảnh có cụm phím số; tên mới Bàn phím Gaming có cụm phím số. Không kết luận loại switch từ ảnh.
- SB0012: sửa đặc điểm USB-A thành đầu cắm 3,5 mm theo ảnh Logitech H111; giữ điểm demo của nhóm. Không đổi giá/tồn kho snapshot.
- SB0048: đã xem ảnh lớn, đầu USB-C và cổng HDMI phù hợp tên; giữ nguyên. Không suy ra độ phân giải/khả năng tương thích khi chưa có model/thông số.
- Metadata: cả 60 dòng có đủ 5 điểm mục đích và ít nhất 1 trong 4 phong cách, đọc được bằng SetupFilterDAO. Điểm/phong cách là giả định trình diễn của nhóm, không phải chứng nhận chất lượng/thông số hãng. Đặc điểm nguồn được ghi rõ cần xác minh thông số.

Các mã đã gán hãng giả lập (trước đây chưa rõ hãng): SB0003, SB0004, SB0005, SB0007, SB0008, SB0011, SB0014, SB0015, SB0018, SB0019, SB0020, SB0021, SB0022, SB0023, SB0024, SB0025, SB0027, SB0028, SB0029, SB0030, SB0031, SB0037, SB0043, SB0050, SB0051, SB0052, SB0053, SB0054, SB0055, SB0056.

## Phân biệt database demo và fixture

| Bộ dữ liệu | Mục đích | Có dùng để trình diễn? |
|---|---|---|
| DAVISYS_DEMO_SB60 | 60 sản phẩm/ảnh thật trong bộ demo của Phong, metadata và tài khoản/giỏ giả lập riêng | Có, catalog bàn giao |
| Codex_DAVISYS_Verify_20261023 | Cố ý rút ngắn MOTA, reset DEMO/BP, tạo trùng mã để kiểm thử lỗi | Không; trạng thái cuối còn ca lỗi |
| DAVISYS_SETUP_REVIEW_* / FakeSetupDatabase | Fixture của unit/integration test; reset dữ liệu khi chạy test | Không |
| DAVISYS trên máy Phong | Nguồn snapshot, không bị script dựng mới/cập nhật ghi vào | Không dùng làm đích thử gói |

Không trỏ bộ integration test phá dữ liệu vào DAVISYS_DEMO_SB60. Dùng database review riêng theo hướng dẫn của Vương.

## Bao phủ bộ lọc

Số sản phẩm còn hàng, điểm mục đích >=70, khớp phong cách theo metadata demo:

| Mục đích | Hiện đại | Tối giản | Năng động | Sang trọng |
|---|---:|---:|---:|---:|
| Gaming | 20 | 10 | 8 | 16 |
| Học tập | 30 | 33 | 11 | 19 |
| Văn phòng | 29 | 33 | 9 | 19 |
| Thiết kế đồ họa | 20 | 19 | 7 | 15 |
| Làm việc đa nhiệm | 30 | 28 | 11 | 19 |

Đây là số sản phẩm đơn lẻ, không phải số combo. Mỗi nhóm/cặp bộ lọc có thể không có hàng phù hợp; chọn Không lọc khi cần demo toàn nhóm.

## Kiểm chứng đã thực hiện

- Biên dịch main mới nhất bằng JDK 17: PASS.
- DemoCatalogTest: PASS; kiểm tra đủ 60 mã liên tục, 60 ảnh, 12×5 nhóm, 30 sản phẩm được gán ngẫu nhiên từ hãng có sẵn và không còn mã DEMO, 3 món hết hàng; 300 điểm mục đích và phong cách đọc qua parser thật; SB0004/SB0012 đúng quyết định.
- SQL Server 2025 (bản 23/10, trước lần bổ sung hãng giả lập): dựng mới thành công, InsertedProducts=60, UpdatedProducts=0; đủ 13 thủ tục và giỏ/tài khoản demo.
- Bộ kiểm chứng cập nhật của bản 23/10: 7/7 PASS. Preview không ghi; cố ý đưa SB0004/SB0012 về mô tả cũ trong DB demo rồi apply sửa đúng 2 dòng; repeat 0 dòng; tên hãng xung đột bị từ chối; giữ 30 sản phẩm lúc đó chưa rõ hãng và 3 món hết hàng; nguồn DAVISYS không đổi.
- Đã xem trực quan cả 60 ảnh, xem riêng ảnh lớn SB0013 và SB0048, đối chiếu SB0012. Đây là đối chiếu nhãn/nhóm hình, không xác thực hàng hóa thật.
- Chưa chạy lại Login/Home end-to-end bằng bộ SB60 mới trong lần này; phần nghiệm thu GUI/checkout của Vương trên main có báo cáo riêng.

## Bổ sung hãng ngày 24/10/2026

Đã sửa theo đúng yêu cầu: không tự đặt tên hãng. 30 sản phẩm chưa rõ hãng được gán ngẫu nhiên từ 26 hãng đã có trong 30 sản phẩm xác định trước đây. Kết quả đã cố định trong catalog/SQL để chạy lặp nhất quán; 30 sản phẩm có hãng trước đây không đổi. Xem bảng gán đầy đủ tại assigned_demo_brands.md (trong database/demo). Không đổi metadata, ảnh, giá, tồn kho hoặc loại. DemoCatalogTest và kiểm tra đồng nhất SQL đạt; chưa áp dụng lần đổi hãng này vào SQL Server đang chạy.
