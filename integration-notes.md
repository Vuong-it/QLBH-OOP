# Báo cáo tích hợp Smart Setup Builder

## A. Trạng thái thực hiện

**PARTIAL — mã tích hợp, build JAR và kiểm thử tự động đã hoàn tất; còn xác minh trên SQL Server/GUI toàn ứng dụng.**

- Repository: https://github.com/Vuong-it/QLBH-OOP
- Thư mục gốc được bảo toàn: `D:\BTL\OOP_Java\DAVISY-main`.
- Worktree: `C:\Users\Admin\.codex\worktrees\setup-builder-integration\DAVISY-main`.
- Nhánh local: `integration/setup-builder`.
- Base/đích: `origin/main` = `bd77a75fd80fe6a2ef3fd06ffa77faa8908c6562`.
- HEAD cuối cùng: commit chứa báo cáo này; dùng `git rev-parse HEAD` trong worktree để lấy SHA.
- Không sửa `main`, không push, không tạo PR, không chạy ứng dụng kết nối hay ghi DB thật.
- Ba tệp đã sửa ở checkout gốc còn nguyên nội dung; SHA256 trước/sau khớp:
  - `DAVISYS_1/nbproject/build-impl.xml`: `69AC3C07AE8C5FB8C837C4BD2C0C5B852655C50D33112D663F00279F828FCAD4`.
  - `DAVISYS_1/nbproject/genfiles.properties`: `9E8EBB9E7064220E8DD976D7D17B261622D3DE02C3A0BF3B682705FBB5592D32`.
  - `DAVISYS_1/src/com/utils/JdbcHelper.java`: `7669918716C0D501C114187515D83BAF094B763794633B521E5B84E1FC1A2204`.

## B. Nguồn gốc 4 nhánh và quyết định

| Nguồn | SHA đã xem | Nội dung tích hợp | Nội dung không lấy và lý do | Lịch sử / xử lý |
|---|---|---|---|---|
| `feature/giao-dien` | `9d893378a33fdef10ec3f992885d02ed0de1a3af` | Tiêu chí ngân sách/mục đích/phong cách, tông màu, bảng đề xuất, tổng/còn dư, đổi phụ kiện trong budget | `SetupBuilder` là JFrame rỗng; `SetupBuildercode` có logic sinh riêng/ngẫu nhiên và cart chỉ thông báo. Chuyển phần UI hữu ích vào panel dùng chung, không thêm cửa sổ cạnh tranh | Không chung ancestor với main; port hành vi/layout có chọn lọc |
| `feature/setup-algorithm` | `3d795674006b23aabb5d38b9f8251fdce40b5733` | Model bất biến, service backtracking, 3 strategy, replacement, signature và scenario kiểm thử | Không lấy bản cũ `SanPhamDAO`/`HoaDonEntity`; chuyển main test từ `src/com/test` sang JUnit trong `test` | Merge base `2a23b31974ca2514908bf8e452f449bc63b5f0e9`; port file, bổ sung kiểm tra dữ liệu/overflow/determinism |
| `feature/setup-database` | `547b519b716c1b97217cda46b57773a95c4473aa` | `SetupFilterDAO`, `SetupFilterProduct`, parser/normalizer, tiêu chí/điểm từ MOTA, kiểm thử mô tả và DAO | Không nhập lại DAO/entity cũ, `.gitignore` cũ hoặc toàn script cài mới/seed. Các bảng đánh giá phụ trong script không được DAO này dùng. Tách migration mở rộng MOTA và mẫu metadata | Không chung ancestor; port file và điều chỉnh adapter kết nối |
| `feature/setup-integration` | `59d871792439de91c83772d1039019a0829aa826` | Panel nhúng Home, điều hướng, persistence trên GIOHANG/GIOHANGTAM, refresh sau commit, validation | Không lấy models mutable, thuật toán greedy tạm, thay đổi generated Home.java không đồng bộ `.form`, giả định mọi MAGH là số điện thoại | Merge base là main hiện tại; port adapter và hunk ngoài generated code |

Không thực hiện merge lịch sử unrelated, chọn ours/theirs nguyên cây hay nhập code cũ đè main.

Ma trận điểm gọi cuối:

| Chức năng | Nguồn chính | Điểm gọi thực tế | Quyết định |
|---|---|---|---|
| Mở/đóng/refresh UI | GUI + integration | Home → `showSetupBuilder`, `hidePage`, `refreshSetupCartView` | Một panel tạo lazily; nút trên trang Tổng quan và mục drawer |
| Lọc/metadata/schema | database | Facade → FilterDAO → Mapper | Null tiêu chí nghĩa là bỏ lọc rõ ràng; bật tiêu chí thì metadata phải khớp |
| Sinh/rank/đổi combo | algorithm | Panel → Facade → Service → Strategies | Một lõi không cần SQL/Swing, 0–3 combo khác nhau |
| Khách hàng/cart/transaction | integration | Panel → CartDAO → GIOHANG/GIOHANGTAM | MAGH thật, MAKH, tài khoản đang hoạt động; một transaction cho tất cả dòng |

## C. Kiến trúc cuối cùng

`Home → SetupBuilderPanel → SetupBuilderFacade → SetupFilterDAO/SetupProductMapper → SetupBuilderService/strategies → chọn combo → SetupCartDAO → commit → refresh UI`.

- `SetupProduct`: một FQN, immutable, `long giaBan` VNĐ nguyên, điểm 1–5. Mã sản phẩm giữ nguyên hoa/thường khi truy vấn DB; mã nhóm chuẩn hóa Locale.ROOT. **`getSoLuong()` là tồn kho**, theo API nhánh algorithm. Mỗi dòng của combo mua **1 đơn vị**; UI thể hiện SL mua = 1, DAO không lấy tồn kho làm số lượng mua.
- `SetupCombo`: danh sách immutable, mỗi nhóm/mã một sản phẩm, tổng tiền/điểm được tính từ snapshot. `Math.addExact` kiểm tra overflow. Signature escape ký tự phân cách. Không có setters làm tổng bị stale.
- `SetupBuilderService`: chỉ thuật toán; budget dương, nhóm không trùng/rỗng, loại hàng thiếu hoặc budget thiếu trả danh sách rỗng. Loại hết hàng, deduplicate dữ liệu nhất quán, từ chối trùng mã mâu thuẫn. Tie-break có signature; Budget so sánh `long` chính xác. Trên 100.000 tổ hợp tiềm năng sẽ yêu cầu thu hẹp tiêu chí thay vì rank trên một mẫu không đầy đủ.
- Strategies giữ semantics nguồn: giá thấp nhất; cân bằng 70% fit + 30% tiết kiệm; tổng điểm cao nhất. Combo đã chọn không được đề xuất lại ở strategy sau.
- Facade/mapper tách SQL khỏi algorithm. SQL `FLOAT` chỉ chuyển sang long khi finite, dương, không lẻ VNĐ và trong miền integer double chính xác; không làm tròn giá. Điểm metadata /100 quy đổi gần nhất sang /5, nửa làm tròn lên; điểm gốc nằm trong lý do.
- Bộ lọc hỗ trợ năm mục đích và bốn phong cách. Không có metadata vẫn dùng được **Không lọc**; khi bật tiêu chí, không đoán fit từ tên/đặc điểm. Parser loại điểm lỗi, nhãn phủ định và metadata mơ hồ/trùng.
- UI kế thừa tiêu chí/layout/theme hữu ích của GUI branch. DB và sinh combo chạy trong `SwingWorker`; render, chọn và callback trên EDT. Đổi phụ kiện cùng loại, theo bộ lọc snapshot và budget. Thay đổi tiêu chí làm mất hiệu lực combo cũ. Sau commit xóa lựa chọn, tránh bấm thêm lặp vô ý.
- Cart DAO đọc danh sách giỏ thật của tài khoản đăng nhập. Trước khi ghi kiểm tra lại MAGH/MAKH/TENDN và tài khoản hoạt động, sản phẩm tồn tại, giá hiện tại, stock, số lượng hiện có; giá thay đổi yêu cầu tạo lại combo. Các mã lặp trong input DAO gộp thành số đơn vị tương ứng. Sắp xếp khóa sản phẩm; `UPDLOCK,HOLDLOCK` trên cart, sản phẩm và dòng cart; rollback khi bất kỳ dòng nào lỗi. Connection/statement/result set đóng đúng ownership.
- `JdbcHelper.openConnection()` là boundary chung, giữ cấu hình có sẵn và cho phép override JVM `davisy.jdbc.url/user/password`; không thêm host/mật khẩu hard-code mới. Ba tệp cấu hình/secret đã sửa ở thư mục gốc không bị chép sang worktree.
- Schema SANPHAM của hai script đã đối chiếu không có trạng thái ngừng bán riêng. Hàng đã xóa hoặc stock <= 0 bị loại/từ chối. Cart giữ semantics cũ: không giữ/trừ tồn kho toàn cửa hàng khi thêm giỏ; cần kiểm tra lại lúc thanh toán.
- `SanPhamDAO`, `HoaDonEntity`, `Home.form`, generated block của Home và cấu hình NetBeans của main được bảo toàn. Refresh đọc giỏ hiện tại theo số điện thoại như flow cũ, không chuyển khách hàng đang xem.

Tệp trong phạm vi: models `SetupProduct/SetupCombo/SetupFilterProduct`; DAOs `SetupFilterDAO/SetupCartDAO`; service `SetupBuilderService/SetupBuilderFacade/SetupProductMapper/SetupMoney`; bốn strategy; panel; hunk Home/JdbcHelper; 7 lớp JUnit và fixture JDBC; migration, ví dụ metadata, `.gitignore`, báo cáo này.

## D. Bằng chứng kiểm chứng

Máy có JDK Temurin **21.0.12**; dự án đặt source/target **17**. Ant **1.10.17** từ NetBeans đã có trên máy; dependencies lấy từ `library`, không cài global package.

| Kiểm tra / Lệnh | Kết quả | Chi tiết |
|---|---|---|
| Compile main trước tích hợp | PASS | Ant compile, 118 source files; phân biệt được lỗi base và tích hợp |
| Build JAR + JUnit | PASS | 58 tests, 0 failures, 0 errors, 0 skipped; JAR ở `DAVISYS_1/dist-integration/DAVISYS_1.jar` |
| Thuật toán | PASS | 26 tests: scenario nguồn, 3 chiến lược, budget/nhóm, hết hàng, signatures, ties, replacement, immutability, overflow, giá lớn, giới hạn tìm kiếm |
| Mapper/tiền | PASS | 9 tests: giá âm/lẻ/NaN/Infinity/mất chính xác, budget nguyên lớn, missing category/row, score |
| Parser/filter/facade | PASS | 11 tests: metadata sai/trùng/thiếu, 5 mục đích, 4 phong cách, parameter nhóm, fallback rõ ràng, mapping nhiều rows, resources |
| Cart/transaction JDBC giả lập | PASS | 10 tests: context, actual MAGH, một đơn vị/món, merge, stock, deleted product, live price, lỗi dòng giữa rollback toàn bộ, resource closure |
| Hóa đơn regression | PASS | 1 test giữ semantics giảm giá/điểm và clamp của main |
| Panel Swing với dữ liệu giả | PASS | 1 test mở panel → tải → chọn nhóm/khách → sinh → chọn → ghi → refresh EDT; truy vấn không chạy EDT; ảnh render đã xem |
| Git/static review | PASS | Diff whitespace, markers, imports/FQN, secrets mới, build output, generated code và hash các tệp gốc được kiểm tra trước commit |
| Home + Login + SQL Server end-to-end | NOT RUN | Chưa có DB thử nghiệm được cấp để chạy flow toàn ứng dụng |
| Khóa/concurrency SQL Server và migration | NOT RUN | JDBC fixture không chứng minh hành vi locking của engine; không chạy SQL vào DB thật |
| Bán hàng/tạo hóa đơn trên DB | NOT RUN | Compile callers và test công thức đã qua; chưa chạy nghiệp vụ DB thật |

Lệnh PowerShell tái hiện, chạy trong worktree:

```powershell
& 'D:\App\NetBeans IDE\extide\ant\bin\ant.bat' -f DAVISYS_1/build.xml '-Dbuild.dir=build-integration' '-Ddist.dir=dist-integration' jar test
git diff --check bd77a75fd80fe6a2ef3fd06ffa77faa8908c6562 HEAD
git status --short --branch
```

Báo cáo JUnit: `DAVISYS_1/build-integration/test/results/TEST-*.xml`. Preview bằng dữ liệu giả: `DAVISYS_1/build-integration/setup-preview.png`.
Các warning source 17 trên JDK21, deprecated/unchecked và macro NetBeans đã có không làm build/test thất bại.

## E. Việc còn lại và chuyển giao

1. Mở `DAVISYS_1` **trong worktree** bằng NetBeans. Cấu hình boundary JDBC theo DB thử nghiệm riêng; tránh commit cấu hình riêng hay secret. Chạy Login và đăng nhập tài khoản của DB thử nghiệm.
2. Với dữ liệu cũ, mở Tổng quan → Smart Setup Builder (hoặc drawer), để hai tiêu chí **Không lọc**, chọn các nhóm có hàng và budget đủ. Chọn giỏ của người đăng nhập; phải có giỏ đã tạo theo flow bán hàng cũ. Sinh, chọn, đổi cùng loại rồi thêm; kiểm tra GIOHANGTAM có một đơn vị/món và tổng màn hình được refresh khi đang xem khách đó.
3. Chỉ khi cần lọc mục đích/phong cách: sao lưu DB, chạy migration mở rộng MOTA thủ công trên bản thử nghiệm rồi nhập mô tả đã xác minh theo `database/setup_metadata_example.md`. Không tự gán fit cho dữ liệu thật. Kiểm tra các cặp tiêu chí; không yêu cầu mọi cặp phải có kết quả khi dữ liệu thực không đáp ứng.
4. Thử đổi giá/stock/xóa sản phẩm sau khi tạo combo; thao tác phải thất bại và không ghi một phần. Trên DB thử nghiệm, gây lỗi dòng thứ hai và xác nhận toàn transaction rollback; chạy hai thao tác đồng thời để kiểm chứng lock/không mất cập nhật. Fixture chỉ kiểm chứng contract Java, không thay thế bước này.
5. Kiểm tra menu Cài đặt/Giới thiệu/Đăng xuất và quay lại các màn hình bán hàng/giỏ/hóa đơn. Flow cũ vẫn dùng float/FLOAT; cần đối chiếu giá lớn, làm tròn và tổng hóa đơn khi nghiệm thu. Không tuyên bố toàn bộ regression đã qua chỉ từ test công thức.

Migration được lưu ở `database/migrations/001_setup_description_capacity.sql`: guard DB/schema, không thu nhỏ cột, idempotent, transaction + rollback. Không nhập/chạy script cài mới lên DB hiện hữu.

Lệnh push **chỉ đề xuất, chưa thực thi**, sau khi người dùng cho phép:

```powershell
git push -u origin integration/setup-builder
```

Gợi ý PR sau khi được phép push: base=`main`, head=`integration/setup-builder`.
Tiêu đề: `Integrate Smart Setup Builder strategies, filters and transactional cart`.
Mô tả: Nhúng một Setup Builder trong DAVISY để lọc theo tiêu chí, tạo tối đa ba combo trong budget, đổi phụ kiện cùng loại và ghi vào giỏ hiện hữu theo transaction. Giữ các bản vá DAO/hóa đơn trên main và metadata NetBeans; cung cấp migration MOTA thủ công.
Checklist: build/JUnit PASS; SQL Server locking/rollback, Home end-to-end, bán hàng/hóa đơn và migration trên DB thử nghiệm còn cần kiểm chứng.
