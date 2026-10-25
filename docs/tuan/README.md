# Bàn giao phần việc của Tuấn

## Kết quả

- Giao diện tích hợp: `SetupBuilderPanel` gọi Facade, tab so sánh, điểm /5, sắp xếp giá số, đổi món và đồng bộ sáng/tối.
- Bỏ ba file JFrame thử nghiệm khỏi thay đổi cuối; trả cấu hình NetBeans về đúng main. Lịch sử commit giao diện ban đầu vẫn còn trong nhánh PR.
- Báo cáo và hướng dẫn: [Word](output/Tuan_UI_UX_Huong_dan_Demo.docx), [PDF](output/Tuan_UI_UX_Huong_dan_Demo.pdf), [nguồn Markdown](bao-cao-ui-ux.md).
- Tám slide hỗ trợ UI/UX: [PowerPoint](output/Tuan_UI_UX_Demo.pptx), [PDF](output/Tuan_UI_UX_Demo.pdf). Việt ghép vào bài chung; đây không phải bộ 15–20 slide hoàn chỉnh thay phần của Việt.
- Bảy ảnh trong `assets/`: tiêu chí, kết quả, hộp thoại đổi món, kết quả đổi, dark mode, thêm giỏ thành công, Gaming.

## Kiểm chứng

Build JDK 17/Ant NetBeans 17 thành công. 66 bài kiểm thử thường PASS, gồm 7 bài GUI; 12 bài SQL opt-in SKIPPED. Kết quả riêng từ SQL Server thật: Học tập 1,5 triệu → 330.000; đổi SB0001 sang SB0003 → 430.000; giỏ `0000000001` có SB0003/SB0006/SB0011, mỗi món 1. Gaming 3 triệu → phương án tiết kiệm 1.720.000.

Ảnh được render tự động từ các component Swing với database demo, không phải ảnh thao tác thủ công toàn bộ ứng dụng. Hộp thoại đổi món được dựng từ danh sách lựa chọn thực. Chưa kiểm chứng lại Login/Home/checkout end-to-end trong phiên này. Không tính báo cáo kiểm chứng cũ của Vương là kết quả mới của Tuấn.

## Chạy lại

Tại thư mục gốc, dùng Ant đi kèm NetBeans:

```powershell
& '<NetBeans>/extide/ant/bin/ant.bat' -f DAVISYS_1/build.xml '-Dbuild.dir=build-integration' '-Ddist.dir=dist-integration' jar test
```

Tái tạo ảnh bằng fixture độc lập, không cần SQL Server (nhãn ảnh sẽ ghi rõ JDBC giả lập):

```powershell
Set-Location DAVISYS_1
java '-Djava.awt.headless=true' '-Dfile.encoding=UTF-8' -cp 'build-integration/classes;build-integration/test/classes;../library/*' com.gui.SetupBuilderEvidence 'build-integration/evidence-fixture'
```

Với SQL Server: dựng database độc lập bằng `database/demo/DAVISYS_Demo_Ready.sql`, đọc hướng dẫn dữ liệu của Phong trước. Khi dùng sqlcmd, thêm `-f 65001`; nếu không, dấu tiếng Việt trong metadata có thể bị lỗi dù lệnh SQL thành công. Không chạy lại script tạo mới vào database đã có.

`SetupBuilderEvidence` hỗ trợ `--sql-demo --read-only` sau đường dẫn ảnh; phải truyền JVM property `davisy.jdbc.url` chứa đúng `databaseName=DAVISYS_DEMO_SB60;` và thông tin kết nối riêng qua các property hiện có của JdbcHelper. Bỏ `--read-only` sẽ **thêm ba món vào giỏ demo**, chạy lại sẽ cộng số lượng. Không commit cấu hình cá nhân. Để lưu bằng chứng ban đầu, xuất ảnh mới sang `build-integration/`.

## Việc nhóm cần làm tiếp

1. Vương/Việt/Phong review PR #5; không tự merge khi chưa duyệt.
2. Việt ghép tám slide UI/UX vào phần OOP/giải thuật, thống nhất tổng 15–20 slide và lời dẫn.
3. Tuấn cùng nhóm tổng duyệt trên máy trình chiếu: Login → Home → Builder → Giỏ hàng → Hóa đơn; chụp bổ sung màn hình Giỏ hàng và hóa đơn thật.
4. PR #1 đã merged; mục Approve PR #1 trong kế hoạch cũ không còn là việc đang chờ.

Database `DAVISYS_DEMO_SB60` trên máy hiện được giữ lại để tổng duyệt, có giỏ mẫu 430.000 VNĐ. Database `DAVISYS` không bị sửa trong các bước này.
