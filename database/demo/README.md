# Bổ sung ảnh và hiệu chỉnh dữ liệu demo của Phong

Áp dụng cho database DAVISYS demo đang tồn tại, đã có SB0001–SB0060. Đây là script bổ sung, chưa phải bộ dựng database mới hoặc DAVISYS_Demo_Ready.sql hoàn chỉnh.

## Nội dung

- `DAVISYS_1/src/com/images/SB0001.PNG`–`SB0060.PNG`: 60 ảnh cần commit từ máy Phong; gói script này không chứa lại ảnh.
- `01_update_demo_brands.sql`: đối chiếu 30 ảnh có hãng; giữ SB0012 đã sửa Logitech, chỉ cập nhật tối đa 29 sản phẩm còn mang MAHANG=DEMO. Tái sử dụng hãng cùng tên, dừng nếu mã mới bị trùng hãng khác.
- `brand_image_mapping.csv`: kết quả đối chiếu đủ 60 ảnh; 30 ảnh chưa có hãng đủ rõ được giữ nguyên. Thiếu nhãn không có nghĩa là không thương hiệu.
- `02_fix_SB0012_category.sql`: sửa riêng tai nghe SB0012 từ BP sang TN, có kiểm tra danh mục và tên sản phẩm; chạy lại khi đã là TN không đổi dữ liệu.

## Cách dùng trên máy thành viên khác

1. Sao lưu database demo trước khi ghi. Đối chiếu mã sản phẩm với dữ liệu của nhóm.
2. Mở từng SQL trong SSMS, chạy toàn bộ với `@Apply=0` để xem trước.
3. Sau khi xác nhận, đổi `@Apply=1` trong chính file cần áp dụng và chạy toàn bộ.
4. SB0012 phải được gán Logitech riêng nếu dựng một database chưa có thay đổi đó; script hãng chủ động giữ nguyên SB0012 theo trạng thái máy Phong. Đây là điều kiện tiên quyết, không phải seed hoàn chỉnh.
5. Chạy DAVISY từ project DAVISYS_1, bảo đảm working directory là DAVISYS_1 để tìm `src/com/images/*.PNG`.
6. Kiểm tra Giỏ hàng hiện thẻ sản phẩm, Setup Builder tải dữ liệu, lọc Gaming/Hiện đại cho BP+CH và không trả SB0012 trong nhóm BP.

Trên máy Phong, thay đổi hãng và loại đã được thực hiện theo trao đổi. Không cần chạy lại chỉ để thêm file vào Git. Bản script đóng gói, nhất là chế độ chạy lặp, chưa được thực thi/kiểm chứng riêng trên SQL Server thử nghiệm.

Không thay đổi MOTA, giá, tồn kho, giỏ hoặc hóa đơn. Không sửa code Java và cấu hình NetBeans.

## Việc còn mở

- Kiểm tra loại của các tai nghe còn lại SB0011–SB0015, không tự sửa hàng loạt.
- Đối chiếu ảnh/tên SB0004 (TKL nhưng ảnh có phím số) và đầu nối SB0048 nếu còn lệch.
- Hoàn thiện seed metadata tối thiểu 25–30 sản phẩm, các nhóm bắt buộc và giỏ demo cho tài khoản được chỉ định trong kế hoạch.
- Kiểm tra thêm combo vào giỏ, rollback khi lỗi và luồng hóa đơn trên SQL Server thật.
- Sửa code thẻ sản phẩm để thiếu ảnh vẫn hiện sản phẩm; hiện ảnh lỗi có thể làm mất thẻ.
- Xác minh migration MOTA chạy lặp trên database thử nghiệm.
