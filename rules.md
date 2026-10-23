# QUY TẮC PHÁT TRIỂN & QUY TRÌNH PULL REQUEST (PR) VÀO MAIN
**Dự án:** DAVISY - Phần mềm quản lý bán phụ kiện & thiết bị di động  
**Tính năng trọng điểm:** Tích hợp **Smart Setup Builder**  
**Nhóm thực hiện (4 thành viên):**  
1. **Vương** (Project Lead, Architecture & Cart Integration)  
2. **Việt** (OOP Core Algorithm & Strategy Pattern)  
3. **Tuấn** (GUI / UX Design & Swing Integration)  
4. **Phong** (Database, Metadata Filtering & SQL Migration)  

---

## 1. TỔNG QUAN DỰ ÁN & MỤC TIÊU TÍCH HỢP

### 1.1. Bối cảnh dự án DAVISY
DAVISY là ứng dụng Desktop Java Swing quản lý cửa hàng phụ kiện di động, kết nối CSDL SQL Server thông qua lớp tiện ích `JdbcHelper`. Ứng dụng đã có sẵn các luồng nghiệp vụ chuẩn: Đăng nhập/Phân quyền, Bán hàng, Quản lý sản phẩm, Giỏ hàng (`GIOHANG`, `GIOHANGTAM`), Hóa đơn và Thống kê.

### 1.2. Mục tiêu của tính năng "Smart Setup Builder"
Xây dựng tính năng gợi ý combo phụ kiện thông minh cho khách hàng dựa trên:
- **Ngân sách tối đa (Budget):** Tính bằng VNĐ (kiểu số nguyên `long` để tránh sai số tiền tệ).
- **Mục đích sử dụng (Purpose):** Ví dụ: Văn phòng, Gaming, Học tập, Livestream...
- **Phong cách thẩm mỹ (Style):** Ví dụ: Tối giản, Cá tính, Sang trọng...
- **Nhóm sản phẩm cần mua (Categories):** Ví dụ: Củ sạc, Cáp sạc, Tai nghe, Ốp lưng, Kính cường lực...

### 1.3. Luồng kiến trúc tổng thể (Data & Logic Flow)
```text
[Home.java] (Menu / Dashboard)
       │
       ▼
[SetupBuilderPanel] (Swing UI: Bộ lọc, hiển thị Combos, Đổi món)
       │
       ▼
[SetupBuilderFacade] (Facade Pattern - Điểm giao tiếp duy nhất giữa UI và Nghiệp vụ)
       │
 ┌─────┴───────────────────────────────────────────────────────┐
 │                                                             │
 ▼                                                             ▼
[SetupFilterDAO] (Phong)                      [SetupBuilderService] (Việt)
- Đọc DB & metadata MOTA                     - Backtracking / Combo Builder
- Map qua SetupProductMapper                  - Strategy Pattern:
                                                * BudgetStrategy
                                                * BalancedStrategy
                                                * PerformanceStrategy
                                                               │
                                                               ▼
                                                      [SetupCartDAO] (Vương)
                                                      - Ghi vào GIOHANG / GIOHANGTAM
                                                      - Database Transaction (ACID)
                                                      - Rollback nếu lỗi / hết tồn kho
```

---

## 2. PHÂN CÔNG VAI TRÒ & PHẠM VI TRÁCH NHIỆM

| Thành viên | Nhánh tính năng | Trách nhiệm chính | File/Package phụ trách chính |
|---|---|---|---|
| **Vương** *(Lead)* | `feature/setup-integration` & `integration/setup-builder` | - Kiến trúc tổng thể, Facade pattern<br>- Tích hợp giỏ hàng giao dịch (`SetupCartDAO`)<br>- Nhúng `SetupBuilderPanel` vào `Home.java`<br>- Điều phối, code review và merge PR vào `main` | `com.service.SetupBuilderFacade`<br>`com.service.SetupMoney`<br>`com.service.SetupProductMapper`<br>`com.dao.SetupCartDAO`<br>`com.gui.Home`<br>`test/com/...` |
| **Việt** | `feature/setup-algorithm` | - Thiết kế hướng đối tượng (OOP)<br>- Thuật toán sinh Combo và xếp hạng theo 3 Strategy<br>- Tính năng đổi phụ kiện cùng loại trong ngân sách<br>- Đảm bảo Model bất biến (Immutable) & không tràn số | `com.entity.SetupProduct`<br>`com.entity.SetupCombo`<br>`com.strategy.*`<br>`com.service.SetupBuilderService`<br>`test/com/service/*` |
| **Tuấn** | `feature/giao-dien` | - Thiết kế giao diện `SetupBuilderPanel`<br>- Form nhập Budget, chọn Mục đích, Phong cách, Nhóm SP<br>- Bảng kết quả hiển thị combo (Tổng tiền, điểm đánh giá)<br>- Tương tác đổi phụ kiện, thông báo trạng thái UX<br>- Giữ đồng bộ giao diện Dark/Light mode | `com.gui.SetupBuilderPanel`<br>`com.gui.SetupBuilder` (Form layout)<br>`test/com/gui/*` |
| **Phong** | `feature/setup-database` | - Truy vấn CSDL, đọc và lọc phụ kiện theo thuộc tính/metadata<br>- Xây dựng DAO trích xuất thông tin từ trường `MOTA`<br>- Viết script Migration mở rộng `MOTA` an toàn (idempotent)<br>- Tương thích SQL Server 2022 | `com.dao.SetupFilterDAO`<br>`com.entity.SetupFilterProduct`<br>`database/migrations/*`<br>`test/com/dao/*` |

---

## 3. QUY CHUẨN CODE (OOP & CLEAN CODE STANDARDS)

### 3.1. Nguyên lý Thiết kế Hướng đối tượng (OOP)
1. **Encapsulation & Immutability:**
   - Các thực thể cốt lõi `SetupProduct`, `SetupCombo` **phải là Immutable** (các thuộc tính `final`, không có setter, chỉ có getter, danh sách con bọc trong `Collections.unmodifiableList` hoặc `List.copyOf`).
   - Phân biệt rõ: Thuộc tính `soLuong` trong `SetupProduct` thể hiện **Tồn kho hiện tại trong DB**. Khi thêm vào Combo / Giỏ hàng, số lượng mua mặc định là **1 đơn vị**.
2. **Strategy Pattern:**
   - Interface `ComboStrategy` định nghĩa phương thức xếp hạng/lựa chọn sản phẩm.
   - Các class triển khai cụ thể: `BudgetStrategy` (giá thấp nhất), `BalancedStrategy` (cân bằng tỉ lệ điểm/giá), `PerformanceStrategy` (điểm đánh giá cao nhất).
   - Không hardcode logic if/else nhiều nhánh trong Service khi cần mở rộng chiến lược mới.
3. **Facade Pattern:**
   - Giao diện Swing (`SetupBuilderPanel`) **tuyệt đối không gọi trực tiếp SQL hay gọi thẳng DAO**.
   - Mọi thao tác lấy dữ liệu, sinh combo, đổi sản phẩm đều đi qua `SetupBuilderFacade`.

### 3.2. Chuẩn dữ liệu và Tính toán Tiền tệ
- **Không dùng `float`/`double` cho giá tiền mới:** Tất cả tính toán tiền tệ của Setup Builder dùng `long` (đơn vị VNĐ).
- **Phòng chống tràn số (Overflow Protection):** Mọi phép tính cộng tiền phải sử dụng `Math.addExact()` hoặc kiểm tra qua `SetupMoney` để ném ngoại lệ khi có nguy cơ tràn số.
- **Xử lý null & an toàn dữ liệu:**
   - Tên sản phẩm, mã nhóm, mô tả phải chuẩn hóa chuỗi (`trim()`, kiểm tra `null`).
   - Nếu tiêu chí lọc (mục đích/phong cách) không được chọn (chọn "Không lọc"), hệ thống mặc định lấy tất cả sản phẩm hợp lệ của nhóm đó.

### 3.3. Quy tắc Giao tác Cơ sở Dữ liệu (Database Transaction)
- Khi thêm một combo gồm nhiều sản phẩm vào giỏ hàng (`GIOHANG`, `GIOHANGTAM`), **bắt buộc phải thực hiện trong một Transaction duy nhất**:
  ```java
  connection.setAutoCommit(false);
  try {
      // 1. Kiểm tra tồn kho thực tế từng sản phẩm
      // 2. Insert/Update vào GIOHANGTAM
      // 3. Update tổng tiền GIOHANG
      connection.commit();
  } catch (Exception e) {
      connection.rollback();
      throw e;
  } finally {
      connection.setAutoCommit(true);
  }
  ```
- Nếu bất kỳ sản phẩm nào bị xóa, hết tồn kho hoặc giá bị thay đổi tại thời điểm commit, toàn bộ thao tác phải bị hủy (Rollback), không được để xảy ra trạng thái thêm dở dang 1 phần sản phẩm.

### 3.4. Quy tắc Lập trình Giao diện (Swing / Concurrency)
- **Không chạy tác vụ I/O, truy vấn Database hay thuật toán nặng trên Event Dispatch Thread (EDT).**
- Sử dụng `SwingWorker` hoặc background thread khi tải dữ liệu sản phẩm và khi chạy thuật toán sinh combo. Chỉ cập nhật giao diện (bảng, nhãn, nút) khi đã có kết quả (`done()` hoặc `SwingUtilities.invokeLater`).

---

## 4. QUY TRÌNH GIT VÀ QUY CHUẨN PULL REQUEST (PR)

### 4.1. Quy ước Đặt tên Nhánh & Commit
- **Tên nhánh:**
  - Tính năng: `feature/<ten-ngan-gon>` (Ví dụ: `feature/setup-algorithm`, `feature/setup-database`, `feature/giao-dien`)
  - Sửa lỗi: `fix/<ten-loi>`
  - Tích hợp: `integration/<ten-dot-tich-hop>`
- **Chuẩn commit message (Conventional Commits):**
  - `feat: <nội dung tính năng mới>`
  - `fix: <nội dung sửa lỗi>`
  - `refactor: <tối ưu, dọn dẹp mã nguồn không đổi chức năng>`
  - `test: <bổ sung/cập nhật unit test>`
  - `docs: <cập nhật tài liệu, hướng dẫn>`

### 4.2. Danh sách FILE TUYỆT ĐỐI KHÔNG COMMIT LÊN GIT
Để tránh conflict và hỏng cấu hình dự án NetBeans của đồng đội, **không bao giờ stage/commit các file sau:**
1. Thư mục biên dịch: `DAVISYS_1/build/`, `DAVISYS_1/dist/`, `*.class`, `*.jar` tự build.
2. Cấu hình NetBeans máy cá nhân:
   - `DAVISYS_1/nbproject/private/`
   - `DAVISYS_1/nbproject/build-impl.xml` *(trừ khi có người phụ trách thư viện Ant chính)*
   - `DAVISYS_1/nbproject/genfiles.properties`
   - `.settings/`, `.vscode/`, `.idea/`
3. Mật khẩu kết nối CSDL cá nhân:
   - File `DAVISYS_1/src/com/utils/JdbcHelper.java` chứa user/pass mặc định của nhóm (`sa` / `123` hoặc quy ước chung). Không commit pass riêng của máy mình!
4. Đặt file test sai vị trí:
   - Không tạo file test trong `DAVISYS_1/src/com/test/`! Toàn bộ JUnit Test phải nằm trong thư mục chuẩn: `DAVISYS_1/test/com/...`.

### 4.3. Quy trình Tạo PR vào Main
1. **Bước 1 - Đồng bộ nhánh với `main` mới nhất:**
   ```powershell
   git fetch origin
   git checkout feature/<nhanh-cua-ban>
   git merge origin/main
   # Giải quyết triệt để conflict (nếu có) trên máy mình trước khi đẩy lên remote
   ```
2. **Bước 2 - Chạy Build và Test cục bộ:**
   Đảm bảo dự án Clean & Build thành công trên NetBeans, toàn bộ JUnit test đều màu XANH (PASS).
3. **Bước 3 - Push nhánh lên GitHub và Mở Pull Request:**
   - **Base branch:** `main` (hoặc `integration/setup-builder` theo chỉ định của Lead).
   - **Compare branch:** Nhánh tính năng của bạn.
4. **Bước 4 - Điền thông tin PR theo mẫu:**
   - Tiêu đề PR rõ ràng.
   - Mô tả các thay đổi chính.
   - Bằng chứng kiểm thử (ảnh chụp kết quả JUnit / màn hình chạy thử).
5. **Bước 5 - Review và Merge:**
   - Bắt buộc phải có ít nhất **1 thành viên khác** hoặc **Trưởng nhóm (Vương)** review và Approve.
   - Tuyệt đối không tự ý merge khi chưa có phê duyệt.

---

## 5. CHECKLIST BẮT BUỘC TRƯỚC KHI TẠO PR

### 5.1. Checklist Chung (Dành cho cả 4 thành viên)
- [ ] Đã kéo `origin/main` mới nhất về merge/rebase và không còn xung đột (no merge conflicts).
- [ ] Không có file rác (`build/`, `dist/`, `.class`, `nbproject/private/`, cấu hình IDE cá nhân) trong git status.
- [ ] Không vô tình ghi đè mật khẩu cá nhân vào `JdbcHelper.java`.
- [ ] Code tuân thủ đặt tên tiếng Việt/Anh nhất quán với dự án hiện tại, có comment giải thích các logic nghiệp vụ phức tạp.
- [ ] Chạy lệnh build không có lỗi biên dịch (Compile Error = 0).
- [ ] Tất cả các bài kiểm thử tự động (Unit Test) chạy thành công 100%.

### 5.2. Checklist Riêng Từng Thành Viên

#### A. Đối với Việt (`feature/setup-algorithm`)
- [ ] Các class `SetupProduct` và `SetupCombo` đảm bảo tính bất biến (Immutable), không cung cấp setter.
- [ ] Cả 3 Strategy (`BudgetStrategy`, `BalancedStrategy`, `PerformanceStrategy`) trả về kết quả chính xác theo đúng mục tiêu của thuật toán.
- [ ] Xử lý an toàn khi không tìm được sản phẩm nào thỏa mãn ngân sách (trả về danh sách rỗng hoặc thông báo rõ ràng, không throw `NullPointerException`).
- [ ] Các phép cộng tiền sử dụng hàm an toàn tránh tràn số.
- [ ] Toàn bộ file test đã chuyển từ `src/com/test/` sang thư mục chuẩn `test/com/service/`.

#### B. Đối với Phong (`feature/setup-database`)
- [ ] `SetupFilterDAO` chỉ thực hiện đọc dữ liệu (`SELECT`), xử lý `ResultSet` an toàn và đóng connection/statement trong khối `try-with-resources`.
- [ ] Logic parse thuộc tính từ cột `MOTA` xử lý tốt các trường hợp chuỗi rỗng, null, định dạng sai mà không gây crash ứng dụng.
- [ ] Script migration `001_setup_description_capacity.sql` có tính chất lũy quyền (idempotent - chạy nhiều lần không lỗi), không làm mất mát hoặc thu hẹp dữ liệu cũ của bảng `SANPHAM`.
- [ ] Không commit file backup CSDL đè lên CSDL cũ đang chạy ổn định của nhóm.
- [ ] File test DAO nằm trong thư mục `test/com/dao/`.

#### C. Đối với Tuấn (`feature/giao-dien`)
- [ ] Giao diện Setup Builder được đóng gói thành `SetupBuilderPanel` (JPanel) để có thể nhúng trực tiếp vào `Home.java`, không mở cửa sổ JFrame rời rạc làm mất trải nghiệm người dùng.
- [ ] Không can thiệp sửa trực tiếp bằng tay vào phần mã được NetBeans sinh tự động (`// <editor-fold defaultstate="collapsed" desc="Generated Code">`) để tránh mất đồng bộ với file `.form`.
- [ ] Các nút bấm (Lọc sản phẩm, Tạo combo, Thêm vào giỏ hàng) có kiểm tra tính hợp lệ của dữ liệu đầu vào (Ví dụ: Budget phải là số dương, phải chọn ít nhất 1 nhóm sản phẩm).
- [ ] Các thông báo lỗi/thành công hiển thị thân thiện qua `MsgBox` hoặc `JOptionPane`.
- [ ] Không gọi câu lệnh SQL hay nghiệp vụ tính toán nặng trên giao diện (sử dụng `SetupBuilderFacade`).

#### D. Đối với Vương (`feature/setup-integration` & Lead)
- [ ] Kiểm tra tính toàn vẹn của file `Home.java` và tích hợp menu mở `SetupBuilderPanel` từ thanh điều hướng / drawer.
- [ ] Đảm bảo `SetupCartDAO` thực hiện thêm sản phẩm vào giỏ với đúng mã khách hàng (`MAKH`), mã giỏ hàng (`MAGH`), số lượng mỗi món mua là 1, và có đầy đủ cơ chế Rollback Transaction.
- [ ] Kiểm tra regression: các tính năng cũ (Bán hàng, Đăng nhập, Hóa đơn) vẫn hoạt động hoàn toàn bình thường sau khi tích hợp.
- [ ] Xác nhận file `integration-notes.md` được cập nhật đầy đủ trạng thái và hướng dẫn kiểm thử trước khi bấm Merge vào `main`.

---

## 6. QUY TRÌNH ROLLOUT & NGHIỆM THU TRÊN HỆ THỐNG THẬT

Sau khi toàn bộ code đã được merge vào `main`, nhóm thực hiện kiểm thử thực tế theo các bước:
1. **Chạy Migration CSDL (nếu cần):** Thực thi script `database/migrations/001_setup_description_capacity.sql` trên SQL Server để mở rộng dung lượng cột `MOTA`.
2. **Khởi chạy ứng dụng:** Mở dự án bằng NetBeans, chạy `com.gui.Login` -> đăng nhập tài khoản Quản lý/Nhân viên.
3. **Kiểm thử Luồng Setup Builder:**
   - Truy cập vào mục **Smart Setup Builder** trên trang Tổng quan/Drawer.
   - Thử nghiệm 3 trường hợp: Ngân sách thấp (tiết kiệm), Ngân sách vừa (cân bằng), Ngân sách cao (hiệu năng).
   - Đổi thử một phụ kiện trong combo sang phụ kiện khác cùng loại.
   - Nhấn "Thêm vào giỏ hàng" và chuyển sang màn hình Bán hàng để kiểm tra giỏ hàng thực tế.
4. **Kiểm thử Xung đột (Concurrency / Edge Cases):**
   - Thử thêm combo khi một món trong đó vừa bị chỉnh tồn kho về 0 trên CSDL -> Đảm bảo giao dịch báo lỗi và không thêm dở dang.
   - Thử hoàn tất hóa đơn từ giỏ hàng vừa được thêm bởi Setup Builder.

---
*Tài liệu này là quy chuẩn bắt buộc của nhóm. Mọi thành viên vui lòng đọc kỹ và tuân thủ để đảm bảo đồ án hoàn thành đúng tiến độ và đạt chất lượng cao nhất.*
