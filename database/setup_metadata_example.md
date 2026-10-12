# Mô tả dùng cho bộ lọc Setup Builder

Không cần migration để dùng bộ lọc giá, loại hàng và tồn kho: chọn **Không lọc mục đích** và **Không lọc phong cách**.
Khi bật mục đích/phong cách, chỉ những mô tả có nhãn đúng định dạng mới khớp. Không suy đoán từ tên sản phẩm.

Ví dụ dưới đây minh họa định dạng; điểm do người quản lý xác minh từ thông số sản phẩm, không tự gán cho hàng đang bán:

```text
Đặc điểm: USB-A; phím cơ.
Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 90; Học tập: 85; Văn phòng: 80; Thiết kế đồ họa: 70; Làm việc đa nhiệm: 85.
Phong cách demo: Hiện đại; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.
```

Ngưỡng mục đích là 70/100. Điểm được quy đổi sang thang 1–5 bằng làm tròn gần nhất, nửa đơn vị làm tròn lên;
điểm gốc /100 vẫn được hiển thị trong lý do. Tên mục đích/phong cách trong UI khớp các nhãn của bộ lọc.

Schema gốc `DAVISYS.sql` có `MOTA nvarchar(100)`; schema SQL Server 2022 ở nhánh database dùng `nvarchar(2000)`.
Migration `migrations/001_setup_description_capacity.sql` chỉ mở rộng cột khi cần, không xóa/seed/cập nhật sản phẩm.
Sao lưu, chạy trên DB thử nghiệm, kiểm tra dữ liệu rồi mới quyết định áp dụng thủ công. Codex không chạy script này.
Không dùng script cài mới để cập nhật một DB hiện hữu; bản cài mới SQLServer2022 của nhánh database có seed và các bảng đánh giá phụ không được DAO này sử dụng.
