# Thiết kế giao diện Smart Setup Builder

Phần đóng góp của Tuấn trong dự án DAVISY. Tài liệu trình bày thiết kế UI/UX, cách sử dụng, kịch bản trình diễn và kết quả kiểm chứng của giao diện tích hợp. Phân hệ hỗ trợ nhân viên chọn một bộ phụ kiện theo ngân sách và thêm vào giỏ khách hàng hiện có.

## Phạm vi và kiến trúc

SetupBuilderPanel là JPanel được nhúng vào Home bằng CardLayout. Các bản JFrame thử nghiệm đã được thay bằng giao diện tích hợp, tránh mở nhiều cửa sổ và tránh duy trì hai luồng nghiệp vụ khác nhau. Phần mã NetBeans sinh tự động trong Home không thay đổi.

Luồng sử dụng: Home → Smart Setup Builder → nhập tiêu chí → tạo và so sánh phương án → đổi phụ kiện nếu cần → chọn giỏ → thêm bộ phụ kiện → màn hình Giỏ hàng → xác nhận hóa đơn.

SetupBuilderPanel gọi SetupBuilderFacade để đọc sản phẩm, tạo combo, đổi món, lấy giỏ và thêm giỏ. Facade chuyển yêu cầu tới các lớp DAO và thuật toán tương ứng. Giao diện chỉ sử dụng CartContext như dữ liệu lựa chọn, không gọi trực tiếp phương thức DAO. Tuấn phụ trách trình bày và tương tác; thuật toán, dữ liệu và giao dịch giỏ hàng kế thừa các phần của Việt, Phong và Vương.

## Bố cục và phản hồi

Vùng trên chứa ngân sách, mục đích, phong cách và giỏ khách hàng. Danh sách bên trái hỗ trợ Ctrl để chọn nhiều nhóm. Vùng trung tâm hiển thị các tab Tiết kiệm, Cân bằng và Trải nghiệm cùng bảng chi tiết. Mỗi dòng tương ứng một phụ kiện, số lượng mua là 1; số lượng tồn kho không phải số lượng mua.

Bảng hiển thị mã, tên, nhóm, giá VNĐ, số lượng, điểm phù hợp trên 5 và lý do gợi ý. Giá được lưu là Long trong mô hình bảng để sắp xếp theo giá trị số; chỉ bộ hiển thị mới định dạng dấu phân cách hàng nghìn. Dòng tổng kết cho biết tổng tiền, ngân sách còn lại và điểm trung bình. Điểm này phản ánh metadata demo và phép quy đổi của hệ thống, không phải đánh giá độc lập về chất lượng sản phẩm.

Số tab thực tế từ 0 đến 3 vì thuật toán loại bỏ các bộ sản phẩm trùng nhau. Không có tab cho một phương án đồng nghĩa không có thêm bộ khác biệt đáp ứng tiêu chí; giao diện không tạo dữ liệu giả để đủ ba tab.

Các tác vụ đọc dữ liệu, sinh combo và ghi giỏ chạy bằng SwingWorker. Trong lúc xử lý, bộ lọc và các nút hành động bị khóa; phần cập nhật Swing chạy trên EDT. Đổi món chỉ dùng tập dữ liệu đã tải và phép thay thế trong bộ nhớ, không thực hiện I/O trên EDT.

Khi thay đổi ngân sách, mục đích, phong cách hoặc nhóm, kết quả cũ bị xóa để tránh thêm bộ không còn khớp tiêu chí. Tải lại dữ liệu cũng xóa kết quả trước khi truy vấn, kể cả khi truy vấn thất bại. Sau khi ghi giỏ thành công, giao diện xóa lựa chọn để hạn chế bấm thêm lặp ngoài ý muốn.

## Chế độ sáng và tối

Màu nền, chữ, bảng, vùng chọn và các ô nhập được cập nhật theo chế độ của Home. Việc đổi chế độ không thay đổi bộ phụ kiện đang chọn. Màu được áp dụng trong panel thay vì sửa mặc định UIManager toàn ứng dụng.

[IMAGE:05-dark-mode.png|Giao diện tối với bộ Học tập từ database demo]

## Hướng dẫn tạo bộ phụ kiện

1. Đăng nhập ứng dụng bằng tài khoản có giỏ khách hàng, mở Smart Setup Builder từ Home hoặc menu điều hướng.
2. Nhấn Tải lại dữ liệu. Nếu chưa có giỏ của tài khoản, tạo giỏ trong luồng bán hàng rồi quay lại tải lại.
3. Nhập ngân sách là số nguyên dương VNĐ, ví dụ 1500000. Không nhập dấu chấm, dấu phẩy, ký hiệu tiền hoặc chữ “triệu”.
4. Chọn mục đích và phong cách. Chọn Không lọc nếu muốn bỏ riêng tiêu chí đó.
5. Giữ Ctrl để chọn nhiều nhóm, ví dụ BP, CH và TN. Mỗi nhóm được chọn sẽ có một món trong bộ.
6. Nhấn Tạo bộ phụ kiện, chờ xử lý rồi chọn tab để so sánh các phương án.

[IMAGE:01-tieu-chi.png|Tiêu chí Học tập với ngân sách 1500000 VNĐ và ba nhóm BP CH TN]

[IMAGE:02-ket-qua.png|Phương án Tiết kiệm gồm SB0001 SB0006 SB0011 với tổng 330000 VNĐ]

## Hướng dẫn đổi phụ kiện và thêm giỏ

Chọn một dòng trên bảng rồi nhấn Đổi phụ kiện. Danh sách chỉ gồm các món cùng nhóm, còn hàng, phù hợp bộ lọc và đủ ngân sách sau khi thay món cũ. Vì vậy, một món vượt ngân sách sẽ không xuất hiện để chọn. Nếu bộ sau đổi trùng một gợi ý khác, ứng dụng yêu cầu chọn gợi ý đó hoặc đổi món khác.

[IMAGE:03-doi-phu-kien.png|Danh sách đổi bàn phím trong trần ngân sách]

Sau khi đổi, kiểm tra tổng tiền và ngân sách còn lại. Chọn đúng giỏ khách hàng, đối chiếu tên, điện thoại và mã giỏ rồi nhấn Thêm vào giỏ hàng. Nút chỉ bật khi có cả bộ phụ kiện và giỏ hợp lệ. Giao dịch phía DAO kiểm tra lại tồn kho và giá; lỗi làm rollback toàn bộ. Khi có thông báo thành công, chuyển sang Giỏ hàng và nhập số điện thoại của khách để kiểm tra trước khi thanh toán.

[IMAGE:04-sau-khi-doi.png|Đổi SB0001 thành SB0003 làm tổng tăng từ 330000 lên 430000 VNĐ]

[IMAGE:06-them-gio.png|Trạng thái thành công sau khi thêm ba món vào giỏ demo 0000000001]

## Kịch bản trình diễn

Tổng thời lượng đề xuất 5 đến 7 phút. Chuẩn bị database DAVISYS_DEMO_SB60, tài khoản demo_admin, giỏ 0000000001, ảnh sản phẩm và bản PDF dự phòng. Khi nhập script qua sqlcmd phải dùng -f 65001 để giữ dấu tiếng Việt. Database demo tách biệt với DAVISYS đang sử dụng.

Tình huống 1 Học tập trong 1,5 triệu. Nhập 1500000, chọn Học tập và Tối giản, chọn BP CH TN. Phương án Tiết kiệm trên catalog hiện tại gồm SB0001 giá 150000, SB0006 giá 80000 và SB0011 giá 100000; tổng 330000, dư 1170000, điểm trung bình 4,0/5. Giải thích một nhóm chọn một sản phẩm và so sánh ba tab. Chọn bàn phím rồi đổi sang SB0003 giá 250000; tổng mới 430000, dư 1070000, điểm trung bình khoảng 4,3/5. Thêm vào giỏ 0000000001 và kiểm tra mỗi dòng số lượng 1 nếu giỏ ban đầu rỗng. Nếu chạy lại trên cùng giỏ, số lượng được cộng thêm.

Tình huống 2 Gaming trong 3 triệu. Nhập 3000000, chọn Gaming và Hiện đại, giữ BP CH TN rồi tạo lại. Phương án Tiết kiệm đã kiểm chứng có tổng 1720000, dư 1280000 và điểm trung bình khoảng 4,7/5. Cặp bộ lọc này chỉ cho một phương án khác biệt trên catalog hiện tại. Muốn so sánh thêm, chọn Không lọc phong cách rồi tạo lại; chỉ chuyển tab Trải nghiệm khi tab đó xuất hiện. Kết quả phụ thuộc catalog, giá và tồn kho tại thời điểm chạy.

Tình huống 3 Đầu vào và giới hạn ngân sách. Nhập 0 để minh họa thông báo số tiền không hợp lệ; nhập 1 với ba nhóm để minh họa trạng thái không có bộ phù hợp. Tăng ngân sách và tạo lại. Khi đổi món, chỉ ra rằng danh sách đã loại món vượt trần; không hứa sẽ có hộp thoại “vượt ngân sách” cho món vốn không được hiển thị. Có thể thay một tiêu chí để cho thấy kết quả cũ bị xóa.

Lời dẫn mở đầu: “Phần em phụ trách là giao diện Smart Setup Builder. Nhân viên nhập nhu cầu và ngân sách, hệ thống trả các bộ phụ kiện khác nhau để so sánh, sau đó đổi món và đưa vào giỏ khách hàng.” Khi kết thúc: “Giao diện giữ luồng thao tác trong Home; việc kiểm tra tồn kho, giá và ghi giỏ được chuyển qua Facade tới lớp giao dịch.”

## Kiểm chứng và giới hạn

Bản giao diện được build bằng JDK 17 và Ant của NetBeans 17. Bộ kiểm thử hiện có 66 bài không cần SQL Server chạy thành công, trong đó 7 bài giao diện. 12 bài SQL integration opt-in chưa chạy trong lần kiểm thử tổng này; không tính các bài skip là PASS.

Các bài giao diện kiểm tra sinh bộ và ghi giỏ ngoài EDT, callback trên EDT, từ chối ngân sách/nhóm sai trước khi truy vấn, tab và sắp xếp giá số, đổi chế độ giữ kết quả, kết quả rỗng khóa hành động, đổi đúng dòng sau sắp xếp, lọc món thay thế theo ngân sách, rollback khi ghi lỗi và tải lại thất bại làm mất hiệu lực kết quả cũ.

Bằng chứng SQL riêng đã chạy trên DAVISYS_DEMO_SB60: tải catalog, tạo bộ Học tập, đổi bàn phím, ghi giỏ và tạo bộ Gaming. Truy vấn đối chiếu xác nhận giỏ 0000000001 có SB0003 giá 250000, SB0006 giá 80000 và SB0011 giá 100000, mỗi dòng số lượng 1, tổng 430000 VNĐ. Không thay đổi cơ sở dữ liệu DAVISYS.

Các hình là bản render tự động từ thành phần Swing chạy với SQL Server thật; hộp thoại đổi món được dựng từ danh sách lựa chọn thực trong bộ kiểm chứng. Đây không phải bằng chứng thao tác thủ công trọn luồng Login Home Giỏ hàng Hóa đơn. Cần tổng duyệt luồng đó trên máy trình chiếu và thay ảnh trạng thái giỏ bằng ảnh màn hình Giỏ hàng thực tế trước bảo vệ nếu nhóm yêu cầu ảnh end to end.

## Nguồn và bàn giao

Mã nguồn: DAVISYS_1/src/com/gui/SetupBuilderPanel.java, Home.java, com/service/SetupBuilderFacade.java. Kiểm thử: DAVISYS_1/test/com/gui/SetupBuilderPanelTest.java và SetupBuilderEvidence.java. Dữ liệu: database/demo/products.tsv và DAVISYS_Demo_Ready.sql. Phân công: rules.md và kế hoạch chi tiết thành viên do nhóm cung cấp.

Bộ slide hỗ trợ của Tuấn tập trung UI/UX và demo để Việt ghép vào slide chung. Không thay thế phần thiết kế OOP và thuật toán do Việt phụ trách. PR giao diện số 5 tiếp tục là nơi review đóng góp của Tuấn; PR tích hợp số 1 đã merged nên không còn bước Approve PR đó.
