# Gói bàn giao demo SB0001–SB0060 — Phong

## Nội dung và trạng thái

Bản SB60-v1 lấy từ 60 sản phẩm demo đang có trong DAVISYS trên máy Phong ngày 23/10/2026, không lấy từ database fixture kiểm thử. Giá/tồn kho là snapshot dữ liệu demo, không phải giá thị trường hoặc tồn kho thương mại được chứng thực. Chỉ xuất sản phẩm/hãng/loại; không xuất tài khoản, mật khẩu, khách hàng, hóa đơn hay giỏ hàng thật.

Gói có đủ 60 sản phẩm, 12 nhóm × 5, 60 ảnh PNG, metadata cho 5 mục đích/4 phong cách, schema ứng dụng/khóa ngoại, 13 thủ tục thống kê, tài khoản và giỏ giả lập. Ba món hết hàng SB0020/SB0030/SB0040 được giữ để minh họa bộ lọc tồn kho. 30 sản phẩm thiếu hãng đã được gán tên hãng giả lập có hậu tố (demo) theo yêu cầu bổ sung.

- `DAVISYS_Demo_Ready.sql`: tạo database riêng mới, schema/thủ tục/dữ liệu và tài khoản/giỏ demo. Không ghi đè database có sẵn.
- `03_apply_catalog.sql`: cập nhật 60 dòng trong database riêng, mặc định chỉ xem trước.
- `04_validate_catalog.sql`: kiểm tra chỉ đọc, đối chiếu chính xác catalog sau dựng/cập nhật.
- `products.tsv`: danh mục đầy đủ, Unicode UTF-8; mở được trong Excel bằng dấu phân cách Tab.
- `catalog_decisions.csv`: quyết định hãng, loại và trường hợp đặc biệt của đủ 60 mã.
- `image_manifest.tsv`: đường dẫn, kích thước và SHA-256 đủ 60 ảnh trong `DAVISYS_1/src/com/images`.
- `../../docs/phong-demo-handover.md`: biên bản đối chiếu và kiểm chứng.

Hai script 01/02 và brand_image_mapping.csv là tài liệu/bản vá của PR #3/#4 trước đây. Không cần chạy chúng sau gói dựng mới. Chúng vẫn nhắm DAVISYS theo thiết kế cũ; không dùng để dựng database riêng mới.

## Dựng mới trên máy thành viên

1. Kéo nhánh PR bàn giao mới nhất hoặc giải nén gói bàn giao. Dùng project DAVISYS_1 trên main đã tích hợp Setup Builder.
2. Mở SSMS và kết nối SQL Server bằng tài khoản có quyền tạo database.
3. Mở `DAVISYS_Demo_Ready.sql`, chạy toàn bộ. Mặc định tạo `DAVISYS_DEMO_SB60`. Muốn tên khác, chỉ đổi biến @Database thành tên mới bắt đầu DAVISYS_DEMO_. Script từ chối nếu tên đó đã tồn tại; không xóa/ghi đè database.
4. Mở `04_validate_catalog.sql`, đổi dòng USE nếu tên khác rồi chạy. Kỳ vọng PASS, Products=60, Categories=12, FictionalBrandProducts=30, OutOfStock=3.
5. Đặt đủ 60 ảnh đúng đường dẫn `DAVISYS_1/src/com/images/SBxxxx.PNG`. Giữ HINH=SBxxxx (không gồm đuôi); chạy ứng dụng với working directory DAVISYS_1.
6. Trong NetBeans, chọn project → Properties → Run → VM Options. Đặt `-Ddavisy.jdbc.url=jdbc:sqlserver://localhost:1433;databaseName=DAVISYS_DEMO_SB60;encrypt=true;trustServerCertificate=true`, cùng `-Ddavisy.jdbc.user=<tài khoản SQL của máy>` và `-Ddavisy.jdbc.password=<mật khẩu SQL của máy>`. Dấu <...> phải được thay trên máy; không commit cấu hình riêng. Đây là kết nối SQL Server, khác tài khoản đăng nhập ứng dụng bên dưới.
7. Mở Login, đăng nhập ứng dụng bằng `demo_admin` / `DemoOnly-2026`. Đây là tài khoản giả lập công khai chỉ trong DB demo, không phải tài khoản SQL Server hay tài khoản thật của Phong. Giỏ khách Demo có điện thoại/MAGH `0000000001`, MAKH=DEMO001.
8. Mở Smart Setup Builder, tải dữ liệu, chọn BP+CH và ngân sách 1.000.000; không lọc trước, rồi tăng ngân sách lên 2.000.000 và thử Gaming + Hiện đại. Chọn khách Demo để thêm combo. Không cần seed khách/giỏ từ database thật.

Bộ dựng có đủ bảng/thủ tục của schema hiện có; không seed hóa đơn lịch sử hay xác nhận mọi màn hình nghiệp vụ. Bằng chứng kiểm chứng bên dưới không thay thế nghiệm thu toàn ứng dụng.

## Cập nhật database demo riêng đang có

1. Sao lưu DB riêng cần cập nhật. Chọn đúng bản thử/demo, không chọn DAVISYS đang dùng.
2. Trong 03_apply_catalog.sql đổi dòng USE sang tên riêng bắt đầu DAVISYS_DEMO_. Database phải có schema DAVISY hiện có.
3. Chạy @Apply=0: xem 60 dòng INSERT/UPDATE/UNCHANGED. Bước này không ghi dữ liệu.
4. Chỉ khi muốn đưa 60 dòng về snapshot bàn giao, đổi @Apply=1 và chạy toàn bộ. Nó cập nhật cả giá/tồn kho của 60 mã; không dùng trên giỏ đang bán hoặc kho thật. Không xóa sản phẩm khác, tài khoản, giỏ hay hóa đơn. Tên hãng/loại trùng mã nhưng khác nội dung sẽ bị từ chối để đối chiếu thủ công.
5. Chạy lại @Apply=1: khi không có thao tác bán hàng xen giữa, InsertedProducts=0 và UpdatedProducts=0. Chạy 04_validate_catalog.sql.

## Các điểm đã chốt

- Hãng: 30 sản phẩm có hãng xác định được giữ nguyên. 30 sản phẩm còn lại đã được gán 12 tên hãng do nhóm tự đặt theo yêu cầu ngày 24/10/2026, có hậu tố (demo). Xem database/demo/fictional_brands.md để phân biệt hãng giả lập với hãng xác định từ ảnh.
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
- DemoCatalogTest: PASS; kiểm tra đủ 60 mã liên tục, 60 ảnh, 12×5 nhóm, 30 sản phẩm thuộc hãng giả lập và không còn mã DEMO, 3 món hết hàng; 300 điểm mục đích và phong cách đọc qua parser thật; SB0004/SB0012 đúng quyết định.
- SQL Server 2025 (bản 23/10, trước lần bổ sung hãng giả lập): dựng mới thành công, InsertedProducts=60, UpdatedProducts=0; đủ 13 thủ tục và giỏ/tài khoản demo.
- Bộ kiểm chứng cập nhật của bản 23/10: 7/7 PASS. Preview không ghi; cố ý đưa SB0004/SB0012 về mô tả cũ trong DB demo rồi apply sửa đúng 2 dòng; repeat 0 dòng; tên hãng xung đột bị từ chối; giữ 30 sản phẩm lúc đó chưa rõ hãng và 3 món hết hàng; nguồn DAVISYS không đổi.
- Đã xem trực quan cả 60 ảnh, xem riêng ảnh lớn SB0013 và SB0048, đối chiếu SB0012. Đây là đối chiếu nhãn/nhóm hình, không xác thực hàng hóa thật.
- Chưa chạy lại Login/Home end-to-end bằng bộ SB60 mới trong lần này; phần nghiệm thu GUI/checkout của Vương trên main có báo cáo riêng.

## Bổ sung hãng ngày 24/10/2026

Đã gán 12 hãng giả lập cho đúng 30 dòng còn thiếu, giữ nguyên hãng của 30 dòng đã xác định. Cả 60 dòng hiện có hãng cụ thể; tên tự đặt đều có hậu tố (demo). Catalog, bảng quyết định và cả ba SQL dựng/cập nhật/kiểm tra đã đồng bộ. SQL thực tế 7/7 ở trên là kết quả bản trước; thay đổi tên hãng lần này được kiểm tra bằng đối chiếu dữ liệu/SQL và DemoCatalogTest, chưa chạy lại trên SQL Server. Muốn áp dụng vào database demo riêng đã dựng, chạy script 03 ở preview rồi @Apply=1.
