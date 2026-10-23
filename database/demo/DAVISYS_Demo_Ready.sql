-- Separate application demo: repository schema/procedures + SB60 catalog + fictional actor/cart.
-- Never overwrites an existing database. No real account/customer/order data copied.
USE master;
SET NOCOUNT ON; SET XACT_ABORT ON;
DECLARE @Database sysname=N'DAVISYS_DEMO_SB60';
DECLARE @QuotedDatabase nvarchar(258)=QUOTENAME(@Database);
IF @Database NOT LIKE N'DAVISYS[_]DEMO[_]%' OR @Database COLLATE Latin1_General_100_BIN2 LIKE '%[^A-Z0-9_]%' THROW 51200,N'Tên database phải bắt đầu DAVISYS_DEMO_ và chỉ có A-Z, 0-9, _.',1;
IF DB_ID(@Database) IS NOT NULL THROW 51201,N'Database đã tồn tại; không ghi đè. Dùng script 03 hoặc tên demo mới.',1;
EXEC(N'CREATE DATABASE '+@QuotedDatabase);
EXEC(N'USE '+@QuotedDatabase+N'; CREATE TABLE LOAIHANG
(
	MALH NVARCHAR(10) NOT NULL PRIMARY KEY,
	TENLH NVARCHAR(50) NOT NULL
)

CREATE TABLE HANG
(
	MAHANG NVARCHAR(10) NOT NULL PRIMARY KEY,
	TENHANG NVARCHAR(50) NOT NULL,
)

CREATE TABLE SANPHAM
(
	MASP NVARCHAR(10) NOT NULL PRIMARY KEY,
	TENSP NVARCHAR(50) NOT NULL,
	MALH NVARCHAR(10) NOT NULL,
	MAHANG NVARCHAR(10) NOT NULL,
	SOLUONG INT NOT NULL,
	GIANHAP FLOAT NOT NULL,
	GIABAN FLOAT NOT NULL,
	NGAYNHAP DATE NOT NULL,
	HINH NVARCHAR(50) NOT NULL,
	MOTA NVARCHAR(2000) NOT NULL
)

CREATE TABLE KHACHHANG
(
	MAKH NVARCHAR(10) NOT NULL PRIMARY KEY,
	HOTEN NVARCHAR(50) NOT NULL,
	DIENTHOAI NVARCHAR(20) NOT NULL,
	DIACHI NVARCHAR(50) NOT NULL,
	TICHDIEM INT NOT NULL
)

CREATE TABLE HOADON
(
	MAHD NVARCHAR(10) NOT NULL PRIMARY KEY,
	TENDN NVARCHAR(50) NOT NULL,
	MAKH NVARCHAR(10) NOT NULL,
	MAGH NVARCHAR(20) NOT NULL,
	NGAYLAP DATE NOT NULL,
	TONGTIEN FLOAT NOT NULL,
	PHANTRAMGG INT NOT NULL,
	TRUTIENTICHDIEM FLOAT NOT NULL,
	THANHTIEN FLOAT NOT NULL,
	TRANGTHAI bit
)

CREATE TABLE CHITIETHOADON
(
	MACTHD NVARCHAR(10) NOT NULL PRIMARY KEY,
	MAHD NVARCHAR(10) NOT NULL,
	MASP NVARCHAR(10) NOT NULL,
	MAHANG NVARCHAR(10) NOT NULL,
	MALH NVARCHAR(10) NOT NULL,
	NGAYLAP DATE NOT NULL,
	TENSP NVARCHAR(50) NOT NULL,
	TENHANG NVARCHAR(50) NOT NULL,
	TENLH NVARCHAR(50) NOT NULL,
	NGAYNHAP DATE NOT NULL,
	GIANHAP FLOAT NOT NULL,
	GIABAN FLOAT NOT NULL,
	SOLUONG INT NOT NULL
)

CREATE TABLE GIOHANG
(
	MAGH NVARCHAR(20) NOT NULL PRIMARY KEY,
	MAKH NVARCHAR(10) NOT NULL,
	TENDN NVARCHAR(50) NOT NULL,

)

CREATE TABLE GIOHANGTAM
(
	MAGH NVARCHAR (20) NOT NULL,
	MASP NVARCHAR(10) NOT NULL,
	SOLUONG INT,
	CONSTRAINT GIOHANGTAM_pk PRIMARY KEY (MAGH, MASP)
)

CREATE TABLE CHUCVU
(
	MACV INT NOT NULL PRIMARY KEY,
	TENCV NVARCHAR(50) NOT NULL,
	MOTA NVARCHAR(100) NOT NULL
)

CREATE TABLE TAIKHOAN
(
	TENDN NVARCHAR(50) NOT NULL PRIMARY KEY,
	MACV INT NOT NULL,
	TENNV NVARCHAR(50) NOT NULL,
	EMAIL NVARCHAR(50) NOT NULL,
	MATKHAU NVARCHAR(50) NOT NULL,
	DIACHI NVARCHAR(50) NOT NULL,
	DIENTHOAI NVARCHAR(50) NOT NULL,
	NGAYSINH DATE NOT NULL,
	GIOITINH BIT NOT NULL,
	TRANGTHAI BIT NOT NULL
)

ALTER TABLE SANPHAM ADD CONSTRAINT FK_SANPHAM_LOAIHANG FOREIGN KEY (MALH) REFERENCES LOAIHANG(MALH);
ALTER TABLE SANPHAM ADD CONSTRAINT FK_SANPHAM_HANG FOREIGN KEY (MAHANG) REFERENCES HANG(MAHANG);


ALTER TABLE CHITIETHOADON ADD CONSTRAINT FK_CTHD_SANPHAM FOREIGN KEY (MASP) REFERENCES SANPHAM(MASP);
ALTER TABLE CHITIETHOADON ADD CONSTRAINT FK_HOADON_CHITIETHOADON FOREIGN KEY (MAHD) REFERENCES HOADON(MAHD);

ALTER TABLE GIOHANGTAM ADD CONSTRAINT FK_GIOHANG_GIOHANGTAM FOREIGN KEY (MAGH) REFERENCES GIOHANG(MAGH);
ALTER TABLE GIOHANG ADD CONSTRAINT FK_GIOHANG_KHACHHANG FOREIGN KEY (MAKH) REFERENCES KHACHHANG(MAKH);
ALTER TABLE GIOHANG ADD CONSTRAINT FK_GIOHANG_TAIKHOAN FOREIGN KEY (TENDN) REFERENCES TAIKHOAN(TENDN);

ALTER TABLE GIOHANGTAM ADD  CONSTRAINT FK_GIOHANG_SANPHAM FOREIGN KEY (MASP) REFERENCES SANPHAM (MASP);

ALTER TABLE HOADON ADD CONSTRAINT FK_HOADON_GIOHANG FOREIGN KEY (MAGH) REFERENCES GIOHANG(MAGH);
ALTER TABLE HOADON ADD CONSTRAINT FK_HOADON_TAIKHOAN FOREIGN KEY (TENDN) REFERENCES TAIKHOAN(TENDN);
ALTER TABLE HOADON ADD CONSTRAINT FK_HOADON_KHACHHANG FOREIGN KEY (MAKH) REFERENCES KHACHHANG(MAKH);

ALTER TABLE TAIKHOAN ADD CONSTRAINT FK_TAIKHOAN_CHUCVU FOREIGN KEY (MACV) REFERENCES CHUCVU(MACV);


');
EXEC(N'USE '+@QuotedDatabase+N'; EXEC(N''CREATE PROC sp_NHANVIENXX
AS
BEGIN
	SELECT
		TK.TENNV,
		COUNT(HD.MAHD) SL
	FROM HOADON HD
		JOIN TAIKHOAN TK ON TK.TENDN = HD.TENDN
		WHERE TK.MACV = ''''2''''
	GROUP BY TK.TENNV
	ORDER BY SL DESC
END'');');

EXEC(N'USE '+@QuotedDatabase+N'; EXEC(N''CREATE PROC sp_ThongKeDoanhThu_DMY
	(@Day INT,
	@Month INT,
	@Year INT)
AS
BEGIN
	SELECT
		HD.NGAYLAP,
		SUM(HD.THANHTIEN) TONGTIENHOMNAY
	FROM HOADON HD
	WHERE DAY(NGAYLAP) = @Day AND MONTH(NGAYLAP) = @Month AND YEAR(NGAYLAP) = @Year
	GROUP BY HD.NGAYLAP
END'');');

EXEC(N'USE '+@QuotedDatabase+N'; EXEC(N''CREATE PROC sp_ThongKeDoanhThu_MY
	(@Month INT,
	@Year INT)
AS
BEGIN
	SELECT
		MONTH(NGAYLAP) THANG,
		SUM(HD.THANHTIEN) TONGTIEN
	FROM HOADON HD
	WHERE MONTH(NGAYLAP) = @Month AND YEAR(NGAYLAP) = @Year
	GROUP BY MONTH(NGAYLAP)
END'');');

EXEC(N'USE '+@QuotedDatabase+N'; EXEC(N''CREATE PROC sp_ThongKeDoanhThu_Y
	(@Year INT)
AS
BEGIN
	SELECT
		YEAR(NGAYLAP) NAM,
		SUM(HD.THANHTIEN) TONGTIEN
	FROM HOADON HD
	WHERE YEAR(NGAYLAP) = @Year
	GROUP BY YEAR(NGAYLAP)
END'');');

EXEC(N'USE '+@QuotedDatabase+N'; EXEC(N''CREATE PROC sp_ThongKeDoanhThu_M_BarChart
	(@Year INT)
AS
BEGIN
	SELECT
		MONTH(NGAYLAP) THANG,
		SUM(HD.THANHTIEN) TONGTIEN
	FROM HOADON HD
	WHERE YEAR(NGAYLAP) = @Year
	GROUP BY MONTH(NGAYLAP)
END'');');

EXEC(N'USE '+@QuotedDatabase+N'; EXEC(N''CREATE PROC sp_ThongKeDoanhThu_Y_BarChart

AS
BEGIN
	SELECT
		YEAR(NGAYLAP) NAM,
		SUM(HD.THANHTIEN) TONGTIEN
	FROM HOADON HD
	GROUP BY YEAR(NGAYLAP)
END'');');

EXEC(N'USE '+@QuotedDatabase+N'; EXEC(N''CREATE PROC sp_ThongKeDoanhThu_SP_DMY
	(@Day int,
	@Month int,
	@Year int)
AS
BEGIN
	SELECT
		SP.MASP,
		SP.TENSP,
		COUNT(HD.MAHD) LUOTBAN
	FROM HOADON HD
		JOIN CHITIETHOADON CTHD ON HD.MAHD = CTHD.MAHD
		JOIN SANPHAM SP ON SP.MASP = CTHD.MASP
	WHERE DAY(HD.NGAYLAP) = @Day AND MONTH(HD.NGAYLAP) = @Month AND YEAR(HD.NGAYLAP) = @Year
	GROUP BY SP.MASP, SP.TENSP
	ORDER BY LUOTBAN DESC
END'');');

EXEC(N'USE '+@QuotedDatabase+N'; EXEC(N''CREATE PROC sp_ThongKeDoanhThu_SP_MY
	(@Month int,
	@Year int)
AS
BEGIN
	SELECT
		SP.MASP,
		SP.TENSP,
		COUNT(HD.MAHD) LUOTBAN
	FROM HOADON HD
		JOIN CHITIETHOADON CTHD ON HD.MAHD = CTHD.MAHD
		JOIN SANPHAM SP ON SP.MASP = CTHD.MASP
	WHERE MONTH(HD.NGAYLAP) = @Month AND YEAR(HD.NGAYLAP) = @Year
	GROUP BY SP.MASP, SP.TENSP
	ORDER BY LUOTBAN DESC
END'');');

EXEC(N'USE '+@QuotedDatabase+N'; EXEC(N''CREATE PROC sp_ThongKeDoanhThu_SP_Y
	(@Year int)
AS
BEGIN
	SELECT
		SP.MASP,
		SP.TENSP,
		COUNT(HD.MAHD) LUOTBAN
	FROM HOADON HD
		JOIN CHITIETHOADON CTHD ON HD.MAHD = CTHD.MAHD
		JOIN SANPHAM SP ON SP.MASP = CTHD.MASP
	WHERE YEAR(HD.NGAYLAP) = @Year
	GROUP BY SP.MASP, SP.TENSP
	ORDER BY LUOTBAN DESC
END'');');

EXEC(N'USE '+@QuotedDatabase+N'; EXEC(N''CREATE PROC sp_ThongKeDoanhThu_SP_DMY_GD
	(@Day int,
	@Month int,
	@Year int)
AS
BEGIN
	SELECT
		SP.MASP,
		SP.TENSP,
		COUNT(HD.MAHD) LUOTBAN
	FROM HOADON HD
		JOIN CHITIETHOADON CTHD ON HD.MAHD = CTHD.MAHD
		JOIN SANPHAM SP ON SP.MASP = CTHD.MASP
	WHERE DAY(HD.NGAYLAP) = @Day AND MONTH(HD.NGAYLAP) = @Month AND YEAR(HD.NGAYLAP) = @Year
	GROUP BY SP.MASP, SP.TENSP
	ORDER BY LUOTBAN ASC
END'');');

EXEC(N'USE '+@QuotedDatabase+N'; EXEC(N''CREATE PROC sp_ThongKeDoanhThu_SP_MY_GD
	(@Month int,
	@Year int)
AS
BEGIN
	SELECT
		SP.MASP,
		SP.TENSP,
		COUNT(HD.MAHD) LUOTBAN
	FROM HOADON HD
		JOIN CHITIETHOADON CTHD ON HD.MAHD = CTHD.MAHD
		JOIN SANPHAM SP ON SP.MASP = CTHD.MASP
	WHERE MONTH(HD.NGAYLAP) = @Month AND YEAR(HD.NGAYLAP) = @Year
	GROUP BY SP.MASP, SP.TENSP
	ORDER BY LUOTBAN ASC
END'');');

EXEC(N'USE '+@QuotedDatabase+N'; EXEC(N''CREATE PROC sp_ThongKeDoanhThu_SP_Y_GD
	(@Year int)
AS
BEGIN
	SELECT
		SP.MASP,
		SP.TENSP,
		COUNT(HD.MAHD) LUOTBAN
	FROM HOADON HD
		JOIN CHITIETHOADON CTHD ON HD.MAHD = CTHD.MAHD
		JOIN SANPHAM SP ON SP.MASP = CTHD.MASP
	WHERE YEAR(HD.NGAYLAP) = @Year
	GROUP BY SP.MASP, SP.TENSP
	ORDER BY LUOTBAN ASC
END'');');

EXEC(N'USE '+@QuotedDatabase+N'; EXEC(N''CREATE PROC sp_ThongKeDoanhThu_SPLOAI_Y
	(@Year int)
AS
BEGIN
	SELECT
		LH.TENLH,
		COUNT(HD.MAHD) LUOTBAN
	FROM HOADON HD
		JOIN CHITIETHOADON CTHD ON HD.MAHD = CTHD.MAHD
		JOIN SANPHAM SP ON SP.MASP = CTHD.MASP
		JOIN LOAIHANG LH ON LH.MALH = SP.MALH
	WHERE YEAR(HD.NGAYLAP) = @Year
	GROUP BY LH.TENLH
	ORDER BY LUOTBAN DESC
END'');');
EXEC(N'USE '+@QuotedDatabase+N'; SET NOCOUNT ON; SET XACT_ABORT ON;
CREATE TABLE #DemoBrands(MAHANG nvarchar(10) PRIMARY KEY,TENHANG nvarchar(50) NOT NULL);
INSERT #DemoBrands VALUES (N''B01'',N''AMAZON BASICS''),(N''B02'',N''ANKER''),(N''B03'',N''AXUMTEK''),(N''B04'',N''CREATIVE''),(N''B05'',N''DAREU''),(N''B06'',N''DEEPCOOL''),(N''B07'',N''DEPSTECH''),(N''B08'',N''FIFINE''),(N''B09'',N''FONTASTIC''),(N''B10'',N''GENIUS''),(N''B11'',N''HAVIT''),(N''B12'',N''HP''),(N''B13'',N''INIU''),(N''B14'',N''INLAND''),(N''B16'',N''MANHATTAN''),(N''B17'',N''MAONO''),(N''B18'',N''MEETION''),(N''B19'',N''PLUGABLE''),(N''B20'',N''SANDBERG''),(N''B21'',N''STARTECH.COM''),(N''B22'',N''STEELSERIES''),(N''B23'',N''TECKNET''),(N''B24'',N''TONOR''),(N''B25'',N''UGREEN''),(N''B26'',N''VICTSING''),(N''DEMO'',N''Chưa xác định (demo)''),(N''D01'',N''PhongKey (demo)''),(N''D02'',N''PhongClick (demo)''),(N''D03'',N''PhongLight (demo)''),(N''D04'',N''PhongCool (demo)''),(N''D05'',N''PhongStand (demo)''),(N''D06'',N''PhongLink (demo)''),(N''D07'',N''PhongPad (demo)''),(N''D08'',N''PhongSound (demo)''),(N''D09'',N''PhongVoice (demo)''),(N''D10'',N''PhongPower (demo)''),(N''D11'',N''PhongAudio (demo)''),(N''D12'',N''PhongCam (demo)''),(N''LG'',N''LOGITECH'');
CREATE TABLE #DemoCategories(MALH nvarchar(10) PRIMARY KEY,TENLH nvarchar(50) NOT NULL);
INSERT #DemoCategories VALUES (N''BP'',N''Bàn phím''),(N''CH'',N''Chuột''),(N''DB'',N''Đèn bàn''),(N''DTN'',N''Đế tản nhiệt''),(N''GD'',N''Giá đỡ laptop''),(N''HUB'',N''Hub và bộ chuyển đổi''),(N''LC'',N''Lót chuột''),(N''LOA'',N''Loa máy tính''),(N''MC'',N''Micro''),(N''SDP'',N''Sạc dự phòng''),(N''TN'',N''Tai nghe''),(N''WC'',N''Webcam'');
CREATE TABLE #DemoProducts(MASP nvarchar(10) PRIMARY KEY,TENSP nvarchar(50) NOT NULL,MALH nvarchar(10) NOT NULL,MAHANG nvarchar(10) NOT NULL,SOLUONG int NOT NULL,GIANHAP float NOT NULL,GIABAN float NOT NULL,NGAYNHAP date NOT NULL,HINH nvarchar(50) NOT NULL,MOTA nvarchar(2000) NOT NULL);
INSERT #DemoProducts (MASP,TENSP,MALH,MAHANG,SOLUONG,GIANHAP,GIABAN,NGAYNHAP,HINH,MOTA) VALUES
(N''SB0001'',N''Bàn phím văn phòng có dây'',N''BP'',N''B14'',22,105000,150000,N''2025-12-15'',N''SB0001'',N''Đặc điểm demo (cần xác minh thông số): USB-A; phím màng; dùng học tập và nhập liệu. Nhu cầu demo (điểm /100; 0 = tạm loại): Văn phòng: 90; Làm việc đa nhiệm: 85; Học tập: 82; Thiết kế đồ họa: 75; Gaming: 40. Phong cách demo: Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0002'',N''Bàn phím không dây nhỏ gọn'',N''BP'',N''B23'',29,224000,320000,N''2025-12-15'',N''SB0002'',N''Đặc điểm demo (cần xác minh thông số): Kết nối 2.4 GHz; nhỏ gọn; dùng pin. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 90; Văn phòng: 86; Thiết kế đồ họa: 83; Làm việc đa nhiệm: 82; Gaming: 42. Phong cách demo: Hiện đại; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0003'',N''Bàn phím học tập đa phương tiện'',N''BP'',N''D01'',36,175000,250000,N''2025-12-15'',N''SB0003'',N''Đặc điểm demo (cần xác minh thông số): USB-A; phím tắt âm lượng; phù hợp học tập. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 92; Làm việc đa nhiệm: 91; Thiết kế đồ họa: 86; Văn phòng: 85; Gaming: 50. Phong cách demo: Hiện đại; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0004'',N''Bàn phím Gaming có cụm phím số'',N''BP'',N''D01'',43,455000,650000,N''2025-12-15'',N''SB0004'',N''Đặc điểm demo (cần xác minh thông số): USB-A; kiểu switch chưa xác minh; có cụm phím số; có đèn. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 95; Thiết kế đồ họa: 80; Làm việc đa nhiệm: 75; Học tập: 72; Văn phòng: 65. Phong cách demo: Hiện đại; Năng động. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0005'',N''Bàn phím cơ Gaming full-size'',N''BP'',N''D01'',50,665000,950000,N''2025-12-15'',N''SB0005'',N''Đặc điểm demo (cần xác minh thông số): USB-A; phím cơ; có cụm số; có đèn. Nhu cầu demo (điểm /100; 0 = tạm loại): Làm việc đa nhiệm: 94; Gaming: 92; Thiết kế đồ họa: 84; Văn phòng: 78; Học tập: 70. Phong cách demo: Năng động; Sang trọng. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0006'',N''Chuột văn phòng có dây'',N''CH'',N''B12'',21,56000,80000,N''2025-12-15'',N''SB0006'',N''Đặc điểm demo (cần xác minh thông số): USB-A; 1200 DPI; thiết kế cơ bản. Nhu cầu demo (điểm /100; 0 = tạm loại): Văn phòng: 85; Học tập: 80; Làm việc đa nhiệm: 80; Thiết kế đồ họa: 70; Gaming: 42. Phong cách demo: Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0007'',N''Chuột không dây nhỏ gọn'',N''CH'',N''D02'',28,105000,150000,N''2025-12-15'',N''SB0007'',N''Đặc điểm demo (cần xác minh thông số): 2.4 GHz; dùng pin; dễ mang theo. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 90; Làm việc đa nhiệm: 85; Văn phòng: 82; Thiết kế đồ họa: 80; Gaming: 45. Phong cách demo: Hiện đại; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0008'',N''Chuột không dây yên tĩnh'',N''CH'',N''D02'',35,175000,250000,N''2025-12-15'',N''SB0008'',N''Đặc điểm demo (cần xác minh thông số): 2.4 GHz; nút bấm ít tiếng ồn. Nhu cầu demo (điểm /100; 0 = tạm loại): Văn phòng: 96; Học tập: 92; Làm việc đa nhiệm: 92; Thiết kế đồ họa: 86; Gaming: 48. Phong cách demo: Sang trọng; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0009'',N''Chuột Gaming có dây'',N''CH'',N''B26'',42,315000,450000,N''2025-12-15'',N''SB0009'',N''Đặc điểm demo (cần xác minh thông số): USB-A; chỉnh DPI; có nút chức năng. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 95; Thiết kế đồ họa: 89; Làm việc đa nhiệm: 80; Học tập: 75; Văn phòng: 65. Phong cách demo: Năng động. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0010'',N''Chuột Gaming không dây'',N''CH'',N''B05'',49,595000,850000,N''2025-12-15'',N''SB0010'',N''Đặc điểm demo (cần xác minh thông số): 2.4 GHz; pin sạc; chỉnh DPI. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 93; Thiết kế đồ họa: 90; Làm việc đa nhiệm: 86; Học tập: 80; Văn phòng: 70. Phong cách demo: Hiện đại; Năng động; Sang trọng. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0011'',N''Tai nghe có dây cơ bản'',N''TN'',N''D11'',20,70000,100000,N''2025-12-15'',N''SB0011'',N''Đặc điểm demo (cần xác minh thông số): Jack 3.5 mm; tai nghe in-ear; có mic. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 78; Văn phòng: 70; Làm việc đa nhiệm: 65; Gaming: 50; Thiết kế đồ họa: 45. Phong cách demo: Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0012'',N''Tai nghe học trực tuyến có mic'',N''TN'',N''LG'',27,154000,220000,N''2025-12-15'',N''SB0012'',N''Đặc điểm demo (cần xác minh thông số): đầu cắm 3,5 mm; mic cần; phù hợp học và họp. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 96; Làm việc đa nhiệm: 92; Văn phòng: 91; Gaming: 70; Thiết kế đồ họa: 68. Phong cách demo: Hiện đại; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế''),
(N''SB0013'',N''Tai nghe Bluetooth'',N''TN'',N''B11'',34,280000,400000,N''2025-12-15'',N''SB0013'',N''Đặc điểm demo (cần xác minh thông số): Bluetooth; pin sạc; có mic tích hợp. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 86; Làm việc đa nhiệm: 82; Văn phòng: 82; Thiết kế đồ họa: 66; Gaming: 55. Phong cách demo: Hiện đại; Năng động. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0014'',N''Tai nghe Gaming có mic'',N''TN'',N''D11'',41,455000,650000,N''2025-12-15'',N''SB0014'',N''Đặc điểm demo (cần xác minh thông số): USB-A; chụp tai; mic cần; có đèn. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 96; Học tập: 82; Làm việc đa nhiệm: 80; Văn phòng: 72; Thiết kế đồ họa: 65. Phong cách demo: Năng động. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0015'',N''Tai nghe chụp tai cách âm'',N''TN'',N''D11'',48,630000,900000,N''2025-12-15'',N''SB0015'',N''Đặc điểm demo (cần xác minh thông số): Jack 3.5 mm; đệm kín; cách âm thụ động. Nhu cầu demo (điểm /100; 0 = tạm loại): Văn phòng: 93; Học tập: 91; Làm việc đa nhiệm: 90; Thiết kế đồ họa: 82; Gaming: 78. Phong cách demo: Sang trọng; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0016'',N''Lót chuột nhỏ'',N''LC'',N''B22'',19,21000,30000,N''2025-12-15'',N''SB0016'',N''Đặc điểm demo (cần xác minh thông số): Kích thước 20 x 18 cm; bề mặt vải. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 78; Văn phòng: 75; Làm việc đa nhiệm: 70; Thiết kế đồ họa: 65; Gaming: 55. Phong cách demo: Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0017'',N''Lót chuột chống trượt'',N''LC'',N''B25'',26,49000,70000,N''2025-12-15'',N''SB0017'',N''Đặc điểm demo (cần xác minh thông số): Kích thước 30 x 25 cm; đế cao su. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 88; Thiết kế đồ họa: 87; Văn phòng: 86; Làm việc đa nhiệm: 85; Gaming: 80. Phong cách demo: Hiện đại; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0018'',N''Lót chuột kê cổ tay'',N''LC'',N''D07'',33,84000,120000,N''2025-12-15'',N''SB0018'',N''Đặc điểm demo (cần xác minh thông số): Kê cổ tay mềm; phù hợp nhập liệu. Nhu cầu demo (điểm /100; 0 = tạm loại): Văn phòng: 95; Làm việc đa nhiệm: 92; Học tập: 86; Thiết kế đồ họa: 72; Gaming: 58. Phong cách demo: Sang trọng; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0019'',N''Lót chuột Gaming lớn'',N''LC'',N''D07'',40,140000,200000,N''2025-12-15'',N''SB0019'',N''Đặc điểm demo (cần xác minh thông số): Kích thước 80 x 30 cm; bề mặt vải. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 96; Thiết kế đồ họa: 91; Học tập: 83; Làm việc đa nhiệm: 83; Văn phòng: 75. Phong cách demo: Năng động. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0020'',N''Lót bàn phím và chuột toàn bàn'',N''LC'',N''D07'',0,245000,350000,N''2025-12-15'',N''SB0020'',N''Đặc điểm demo (cần xác minh thông số): Kích thước 90 x 40 cm; đế chống trượt. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 92; Làm việc đa nhiệm: 92; Thiết kế đồ họa: 92; Học tập: 91; Văn phòng: 90. Phong cách demo: Hiện đại; Năng động; Sang trọng. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0021'',N''Giá đỡ laptop nhựa gấp gọn'',N''GD'',N''D05'',18,63000,90000,N''2025-12-15'',N''SB0021'',N''Đặc điểm demo (cần xác minh thông số): Dùng laptop 11-15 inch; chất liệu nhựa. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 85; Văn phòng: 80; Làm việc đa nhiệm: 76; Thiết kế đồ họa: 70; Gaming: 55. Phong cách demo: Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0022'',N''Giá đỡ laptop kim loại cố định'',N''GD'',N''D05'',25,126000,180000,N''2025-12-15'',N''SB0022'',N''Đặc điểm demo (cần xác minh thông số): Dùng laptop 11-15 inch; chân chống trượt. Nhu cầu demo (điểm /100; 0 = tạm loại): Văn phòng: 90; Học tập: 87; Làm việc đa nhiệm: 87; Thiết kế đồ họa: 82; Gaming: 65. Phong cách demo: Hiện đại; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0023'',N''Giá đỡ laptop nhôm điều chỉnh'',N''GD'',N''D05'',32,245000,350000,N''2025-12-15'',N''SB0023'',N''Đặc điểm demo (cần xác minh thông số): Dùng laptop 11-17 inch; chỉnh độ cao. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 96; Làm việc đa nhiệm: 95; Thiết kế đồ họa: 94; Văn phòng: 94; Gaming: 78. Phong cách demo: Hiện đại; Sang trọng. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0024'',N''Giá đỡ laptop kèm đế xoay'',N''GD'',N''D05'',39,350000,500000,N''2025-12-15'',N''SB0024'',N''Đặc điểm demo (cần xác minh thông số): Dùng laptop 11-17 inch; đế xoay. Nhu cầu demo (điểm /100; 0 = tạm loại): Làm việc đa nhiệm: 97; Văn phòng: 96; Thiết kế đồ họa: 92; Học tập: 91; Gaming: 72. Phong cách demo: Hiện đại; Sang trọng. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0025'',N''Giá đỡ laptop nâng cao'',N''GD'',N''D05'',46,525000,750000,N''2025-12-15'',N''SB0025'',N''Đặc điểm demo (cần xác minh thông số): Dùng laptop 11-17 inch; nâng cao màn hình. Nhu cầu demo (điểm /100; 0 = tạm loại): Làm việc đa nhiệm: 96; Văn phòng: 95; Thiết kế đồ họa: 93; Học tập: 92; Gaming: 70. Phong cách demo: Sang trọng; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0026'',N''Đế tản nhiệt một quạt'',N''DTN'',N''B06'',17,84000,120000,N''2025-12-15'',N''SB0026'',N''Đặc điểm demo (cần xác minh thông số): Nguồn USB-A; dùng laptop 11-15 inch. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 80; Văn phòng: 78; Làm việc đa nhiệm: 68; Gaming: 65; Thiết kế đồ họa: 65. Phong cách demo: Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0027'',N''Đế tản nhiệt hai quạt'',N''DTN'',N''D04'',24,154000,220000,N''2025-12-15'',N''SB0027'',N''Đặc điểm demo (cần xác minh thông số): Nguồn USB-A; dùng laptop 11-17 inch. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 85; Văn phòng: 83; Làm việc đa nhiệm: 82; Gaming: 80; Thiết kế đồ họa: 80. Phong cách demo: Hiện đại; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0028'',N''Đế tản nhiệt bốn quạt'',N''DTN'',N''D04'',31,245000,350000,N''2025-12-15'',N''SB0028'',N''Đặc điểm demo (cần xác minh thông số): Nguồn USB-A; dùng laptop 11-17 inch. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 92; Thiết kế đồ họa: 90; Làm việc đa nhiệm: 86; Học tập: 83; Văn phòng: 76. Phong cách demo: Hiện đại; Năng động. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0029'',N''Đế tản nhiệt chỉnh độ cao'',N''DTN'',N''D04'',38,315000,450000,N''2025-12-15'',N''SB0029'',N''Đặc điểm demo (cần xác minh thông số): Nguồn USB-A; chỉnh độ cao; 11-17 inch. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 94; Làm việc đa nhiệm: 93; Văn phòng: 92; Thiết kế đồ họa: 87; Gaming: 85. Phong cách demo: Hiện đại; Sang trọng. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0030'',N''Đế tản nhiệt Gaming nhiều quạt'',N''DTN'',N''D04'',0,455000,650000,N''2025-12-15'',N''SB0030'',N''Đặc điểm demo (cần xác minh thông số): Nguồn USB-A; nhiều quạt; có đèn. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 96; Thiết kế đồ họa: 88; Làm việc đa nhiệm: 84; Học tập: 82; Văn phòng: 72. Phong cách demo: Năng động. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0031'',N''Webcam HD cơ bản'',N''WC'',N''D12'',16,140000,200000,N''2025-12-15'',N''SB0031'',N''Đặc điểm demo (cần xác minh thông số): USB-A; độ phân giải HD; gắn màn hình. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 78; Văn phòng: 75; Làm việc đa nhiệm: 65; Gaming: 52; Thiết kế đồ họa: 35. Phong cách demo: Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0032'',N''Webcam Full HD'',N''WC'',N''B09'',23,245000,350000,N''2025-12-15'',N''SB0032'',N''Đặc điểm demo (cần xác minh thông số): USB-A; độ phân giải Full HD. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 90; Văn phòng: 88; Làm việc đa nhiệm: 85; Gaming: 68; Thiết kế đồ họa: 50. Phong cách demo: Hiện đại; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0033'',N''Webcam Full HD có mic'',N''WC'',N''LG'',30,350000,500000,N''2025-12-15'',N''SB0033'',N''Đặc điểm demo (cần xác minh thông số): USB-A; Full HD; có mic tích hợp. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 96; Làm việc đa nhiệm: 94; Văn phòng: 93; Gaming: 72; Thiết kế đồ họa: 58. Phong cách demo: Hiện đại. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0034'',N''Webcam góc rộng'',N''WC'',N''B10'',37,525000,750000,N''2025-12-15'',N''SB0034'',N''Đặc điểm demo (cần xác minh thông số): USB-A; Full HD; góc rộng; dùng họp nhóm. Nhu cầu demo (điểm /100; 0 = tạm loại): Làm việc đa nhiệm: 96; Văn phòng: 95; Học tập: 87; Gaming: 82; Thiết kế đồ họa: 60. Phong cách demo: Hiện đại; Sang trọng. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0035'',N''Webcam có nắp che riêng tư'',N''WC'',N''B07'',44,455000,650000,N''2025-12-15'',N''SB0035'',N''Đặc điểm demo (cần xác minh thông số): USB-A; Full HD; nắp che ống kính. Nhu cầu demo (điểm /100; 0 = tạm loại): Văn phòng: 96; Học tập: 94; Làm việc đa nhiệm: 93; Gaming: 76; Thiết kế đồ họa: 62. Phong cách demo: Sang trọng; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0036'',N''Micro cài áo'',N''MC'',N''B17'',15,56000,80000,N''2025-12-15'',N''SB0036'',N''Đặc điểm demo (cần xác minh thông số): Jack 3.5 mm TRRS; cần cổng tương thích. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 84; Văn phòng: 75; Làm việc đa nhiệm: 70; Gaming: 42; Thiết kế đồ họa: 38. Phong cách demo: Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0037'',N''Micro để bàn cơ bản'',N''MC'',N''D09'',22,126000,180000,N''2025-12-15'',N''SB0037'',N''Đặc điểm demo (cần xác minh thông số): Jack 3.5 mm; chân đế; cần cổng mic. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 82; Văn phòng: 80; Làm việc đa nhiệm: 76; Gaming: 66; Thiết kế đồ họa: 45. Phong cách demo: Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0038'',N''Micro USB học trực tuyến'',N''MC'',N''B08'',29,245000,350000,N''2025-12-15'',N''SB0038'',N''Đặc điểm demo (cần xác minh thông số): USB-A; nút tắt mic; chân đế. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 96; Văn phòng: 88; Làm việc đa nhiệm: 87; Gaming: 82; Thiết kế đồ họa: 62. Phong cách demo: Hiện đại; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0039'',N''Micro USB họp trực tuyến'',N''MC'',N''B24'',36,385000,550000,N''2025-12-15'',N''SB0039'',N''Đặc điểm demo (cần xác minh thông số): USB-A; ưu tiên giọng nói; nút tắt mic. Nhu cầu demo (điểm /100; 0 = tạm loại): Văn phòng: 97; Làm việc đa nhiệm: 96; Học tập: 91; Gaming: 76; Thiết kế đồ họa: 58. Phong cách demo: Hiện đại; Sang trọng. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0040'',N''Micro USB Gaming'',N''MC'',N''B17'',0,595000,850000,N''2025-12-15'',N''SB0040'',N''Đặc điểm demo (cần xác minh thông số): USB-A; chân đế; chỉnh âm lượng; có đèn. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 96; Học tập: 85; Làm việc đa nhiệm: 83; Văn phòng: 79; Thiết kế đồ họa: 67. Phong cách demo: Năng động. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0041'',N''Loa mini USB'',N''LOA'',N''B10'',50,84000,120000,N''2025-12-15'',N''SB0041'',N''Đặc điểm demo (cần xác minh thông số): Nguồn USB-A; âm thanh jack 3.5 mm. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 77; Văn phòng: 75; Làm việc đa nhiệm: 65; Gaming: 50; Thiết kế đồ họa: 45. Phong cách demo: Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0042'',N''Loa để bàn cơ bản'',N''LOA'',N''B01'',21,154000,220000,N''2025-12-15'',N''SB0042'',N''Đặc điểm demo (cần xác minh thông số): Nguồn USB-A; âm thanh jack 3.5 mm. Nhu cầu demo (điểm /100; 0 = tạm loại): Văn phòng: 87; Học tập: 84; Làm việc đa nhiệm: 78; Gaming: 66; Thiết kế đồ họa: 60. Phong cách demo: Hiện đại; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0043'',N''Loa stereo nhỏ gọn'',N''LOA'',N''D08'',28,280000,400000,N''2025-12-15'',N''SB0043'',N''Đặc điểm demo (cần xác minh thông số): Nguồn USB-A; âm thanh jack 3.5 mm. Nhu cầu demo (điểm /100; 0 = tạm loại): Văn phòng: 91; Học tập: 90; Làm việc đa nhiệm: 85; Gaming: 82; Thiết kế đồ họa: 75. Phong cách demo: Hiện đại; Sang trọng. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0044'',N''Loa Bluetooth để bàn'',N''LOA'',N''B04'',35,455000,650000,N''2025-12-15'',N''SB0044'',N''Đặc điểm demo (cần xác minh thông số): Bluetooth; nguồn USB; dễ chuyển thiết bị. Nhu cầu demo (điểm /100; 0 = tạm loại): Văn phòng: 93; Học tập: 91; Làm việc đa nhiệm: 88; Gaming: 72; Thiết kế đồ họa: 72. Phong cách demo: Hiện đại; Sang trọng. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0045'',N''Loa Gaming'',N''LOA'',N''B18'',42,665000,950000,N''2025-12-15'',N''SB0045'',N''Đặc điểm demo (cần xác minh thông số): Nguồn USB-A; âm thanh jack 3.5 mm; có đèn. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 96; Học tập: 78; Làm việc đa nhiệm: 75; Thiết kế đồ họa: 70; Văn phòng: 70. Phong cách demo: Năng động. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0046'',N''Hub USB-A bốn cổng'',N''HUB'',N''B16'',49,70000,100000,N''2025-12-15'',N''SB0046'',N''Đặc điểm demo (cần xác minh thông số): USB-A; thêm 4 cổng USB; không xuất hình. Nhu cầu demo (điểm /100; 0 = tạm loại): Văn phòng: 89; Làm việc đa nhiệm: 88; Học tập: 86; Gaming: 75; Thiết kế đồ họa: 75. Phong cách demo: Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0047'',N''Hub USB-C bốn cổng'',N''HUB'',N''B03'',20,126000,180000,N''2025-12-15'',N''SB0047'',N''Đặc điểm demo (cần xác minh thông số): Máy cần USB-C truyền dữ liệu; không HDMI. Nhu cầu demo (điểm /100; 0 = tạm loại): Làm việc đa nhiệm: 91; Văn phòng: 91; Học tập: 90; Thiết kế đồ họa: 83; Gaming: 74. Phong cách demo: Hiện đại; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0048'',N''Bộ chuyển USB-C sang HDMI'',N''HUB'',N''B21'',27,175000,250000,N''2025-12-15'',N''SB0048'',N''Đặc điểm demo (cần xác minh thông số): Máy cần USB-C hỗ trợ xuất hình DisplayPort. Nhu cầu demo (điểm /100; 0 = tạm loại): Thiết kế đồ họa: 94; Văn phòng: 92; Làm việc đa nhiệm: 89; Học tập: 88; Gaming: 68. Phong cách demo: Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0049'',N''Hub USB-C có HDMI'',N''HUB'',N''B19'',34,315000,450000,N''2025-12-15'',N''SB0049'',N''Đặc điểm demo (cần xác minh thông số): HDMI cần USB-C hỗ trợ xuất hình DisplayPort. Nhu cầu demo (điểm /100; 0 = tạm loại): Làm việc đa nhiệm: 97; Thiết kế đồ họa: 96; Văn phòng: 96; Học tập: 95; Gaming: 81. Phong cách demo: Hiện đại; Sang trọng. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0050'',N''Hub USB-C đa cổng có PD'',N''HUB'',N''D06'',20,525000,750000,N''2025-12-15'',N''SB0050'',N''Đặc điểm demo (cần xác minh thông số): HDMI cần DP Alt Mode; sạc cần máy hỗ trợ USB-C PD. Nhu cầu demo (điểm /100; 0 = tạm loại): Làm việc đa nhiệm: 98; Văn phòng: 97; Học tập: 93; Thiết kế đồ họa: 93; Gaming: 84. Phong cách demo: Hiện đại; Sang trọng. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0051'',N''Đèn bàn LED cơ bản'',N''DB'',N''D03'',48,70000,100000,N''2025-12-15'',N''SB0051'',N''Đặc điểm demo (cần xác minh thông số): Nguồn USB-A; ánh sáng trắng. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 80; Văn phòng: 78; Làm việc đa nhiệm: 75; Thiết kế đồ họa: 70; Gaming: 48. Phong cách demo: Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0052'',N''Đèn bàn chỉnh độ sáng'',N''DB'',N''D03'',19,126000,180000,N''2025-12-15'',N''SB0052'',N''Đặc điểm demo (cần xác minh thông số): Nguồn USB-A; chỉnh nhiều mức sáng. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 90; Văn phòng: 88; Làm việc đa nhiệm: 83; Thiết kế đồ họa: 82; Gaming: 62. Phong cách demo: Hiện đại; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0053'',N''Đèn học tập chỉnh màu'',N''DB'',N''D03'',26,210000,300000,N''2025-12-15'',N''SB0053'',N''Đặc điểm demo (cần xác minh thông số): Nguồn USB-A; chỉnh màu và độ sáng. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 97; Thiết kế đồ họa: 96; Văn phòng: 92; Làm việc đa nhiệm: 89; Gaming: 72. Phong cách demo: Hiện đại. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0054'',N''Đèn kẹp bàn'',N''DB'',N''D03'',33,315000,450000,N''2025-12-15'',N''SB0054'',N''Đặc điểm demo (cần xác minh thông số): Nguồn USB-A; tay đèn điều chỉnh; kẹp bàn. Nhu cầu demo (điểm /100; 0 = tạm loại): Văn phòng: 95; Học tập: 94; Làm việc đa nhiệm: 93; Thiết kế đồ họa: 92; Gaming: 76. Phong cách demo: Hiện đại; Sang trọng. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0055'',N''Đèn màn hình'',N''DB'',N''D03'',40,455000,650000,N''2025-12-15'',N''SB0055'',N''Đặc điểm demo (cần xác minh thông số): Nguồn USB-A; kẹp màn hình; không dùng màn cong. Nhu cầu demo (điểm /100; 0 = tạm loại): Thiết kế đồ họa: 97; Văn phòng: 97; Làm việc đa nhiệm: 94; Học tập: 90; Gaming: 87. Phong cách demo: Sang trọng; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0056'',N''Sạc dự phòng 10000mAh cơ bản'',N''SDP'',N''D10'',47,105000,150000,N''2025-12-15'',N''SB0056'',N''Đặc điểm demo (cần xác minh thông số): USB-A đầu ra; chỉ sạc điện thoại và phụ kiện. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 78; Văn phòng: 75; Làm việc đa nhiệm: 60; Gaming: 32; Thiết kế đồ họa: 30. Phong cách demo: Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0057'',N''Sạc dự phòng 10000mAh nhỏ gọn'',N''SDP'',N''B13'',18,175000,250000,N''2025-12-15'',N''SB0057'',N''Đặc điểm demo (cần xác minh thông số): USB-A đầu ra; dễ mang theo; không sạc laptop. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 90; Văn phòng: 86; Làm việc đa nhiệm: 72; Gaming: 35; Thiết kế đồ họa: 35. Phong cách demo: Năng động; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0058'',N''Sạc dự phòng 10000mAh có PD'',N''SDP'',N''B02'',25,280000,400000,N''2025-12-15'',N''SB0058'',N''Đặc điểm demo (cần xác minh thông số): USB-C PD 20W; không đáp ứng sạc laptop. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 94; Văn phòng: 91; Làm việc đa nhiệm: 81; Thiết kế đồ họa: 45; Gaming: 42. Phong cách demo: Hiện đại. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0059'',N''Sạc dự phòng 20000mAh'',N''SDP'',N''B25'',32,315000,450000,N''2025-12-15'',N''SB0059'',N''Đặc điểm demo (cần xác minh thông số): USB-A đầu ra; dung lượng lớn; không sạc laptop. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 91; Văn phòng: 90; Làm việc đa nhiệm: 78; Gaming: 40; Thiết kế đồ họa: 40. Phong cách demo: Năng động. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.''),
(N''SB0060'',N''Sạc dự phòng 20000mAh có PD'',N''SDP'',N''B20'',39,455000,650000,N''2025-12-15'',N''SB0060'',N''Đặc điểm demo (cần xác minh thông số): USB-C PD 30W; chỉ laptop hỗ trợ và đủ công suất. Nhu cầu demo (điểm /100; 0 = tạm loại): Học tập: 96; Văn phòng: 95; Làm việc đa nhiệm: 88; Thiết kế đồ họa: 58; Gaming: 46. Phong cách demo: Hiện đại; Sang trọng. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.'');
IF EXISTS(SELECT 1 FROM #DemoBrands s JOIN dbo.HANG p ON p.MAHANG=s.MAHANG WHERE p.TENHANG<>s.TENHANG) THROW 51101,N''Mã hãng đang có tên khác. Đối chiếu thủ công; không ghi đè danh mục.'',1;
IF EXISTS(SELECT 1 FROM #DemoCategories s JOIN dbo.LOAIHANG p ON p.MALH=s.MALH WHERE p.TENLH<>s.TENLH) THROW 51102,N''Mã loại đang có tên khác. Đối chiếu thủ công.'',1;
BEGIN TRY
BEGIN TRANSACTION;
IF COL_LENGTH(N''dbo.SANPHAM'',N''MOTA'')<>-1 AND COL_LENGTH(N''dbo.SANPHAM'',N''MOTA'')<4000
 ALTER TABLE dbo.SANPHAM ALTER COLUMN MOTA nvarchar(2000) NOT NULL;
INSERT dbo.HANG(MAHANG,TENHANG) SELECT s.MAHANG,s.TENHANG FROM #DemoBrands s WHERE NOT EXISTS(SELECT 1 FROM dbo.HANG p WITH(UPDLOCK,HOLDLOCK) WHERE p.MAHANG=s.MAHANG);
INSERT dbo.LOAIHANG(MALH,TENLH) SELECT s.MALH,s.TENLH FROM #DemoCategories s WHERE NOT EXISTS(SELECT 1 FROM dbo.LOAIHANG p WITH(UPDLOCK,HOLDLOCK) WHERE p.MALH=s.MALH);
UPDATE p SET [TENSP]=s.[TENSP],[MALH]=s.[MALH],[MAHANG]=s.[MAHANG],[SOLUONG]=s.[SOLUONG],[GIANHAP]=s.[GIANHAP],[GIABAN]=s.[GIABAN],[NGAYNHAP]=s.[NGAYNHAP],[HINH]=s.[HINH],[MOTA]=s.[MOTA] FROM dbo.SANPHAM p JOIN #DemoProducts s ON s.MASP=p.MASP WHERE EXISTS(SELECT p.[TENSP],p.[MALH],p.[MAHANG],p.[SOLUONG],p.[GIANHAP],p.[GIABAN],p.[NGAYNHAP],p.[HINH],p.[MOTA] EXCEPT SELECT s.[TENSP],s.[MALH],s.[MAHANG],s.[SOLUONG],s.[GIANHAP],s.[GIABAN],s.[NGAYNHAP],s.[HINH],s.[MOTA]);
DECLARE @Updated int=@@ROWCOUNT;
INSERT dbo.SANPHAM(MASP,TENSP,MALH,MAHANG,SOLUONG,GIANHAP,GIABAN,NGAYNHAP,HINH,MOTA) SELECT s.[MASP],s.[TENSP],s.[MALH],s.[MAHANG],s.[SOLUONG],s.[GIANHAP],s.[GIABAN],s.[NGAYNHAP],s.[HINH],s.[MOTA] FROM #DemoProducts s WHERE NOT EXISTS(SELECT 1 FROM dbo.SANPHAM p WITH(UPDLOCK,HOLDLOCK) WHERE p.MASP=s.MASP);
DECLARE @Inserted int=@@ROWCOUNT;
IF EXISTS(SELECT MASP,TENSP,MALH,MAHANG,SOLUONG,GIANHAP,GIABAN,NGAYNHAP,HINH,MOTA FROM #DemoProducts EXCEPT SELECT MASP,TENSP,MALH,MAHANG,SOLUONG,GIANHAP,GIABAN,NGAYNHAP,HINH,MOTA FROM dbo.SANPHAM) THROW 51103,N''Dữ liệu sau ghi không khớp catalog.'',1;
COMMIT TRANSACTION;
SELECT @Inserted AS InsertedProducts,@Updated AS UpdatedProducts;
END TRY BEGIN CATCH IF XACT_STATE()<>0 ROLLBACK; THROW; END CATCH;
INSERT dbo.CHUCVU VALUES(0,N''Admin'',N''Tài khoản trình diễn giả lập'');
INSERT dbo.TAIKHOAN VALUES(N''demo_admin'',0,N''Nhân viên Demo'',N''demo@example.invalid'',N''DemoOnly-2026'',N''Dữ liệu trình diễn'',N''0000000000'',''20000101'',1,1);
INSERT dbo.KHACHHANG VALUES(N''DEMO001'',N''Khách Demo'',N''0000000001'',N''Dữ liệu trình diễn'',0);
INSERT dbo.GIOHANG VALUES(N''0000000001'',N''DEMO001'',N''demo_admin'');
CREATE TABLE dbo.DEMO_HANDOVER_VERSION(Version nvarchar(30) PRIMARY KEY,Dataset nvarchar(100) NOT NULL);
INSERT dbo.DEMO_HANDOVER_VERSION VALUES(N''SB60-v1'',N''Phong demo snapshot + documented image corrections'');
');
EXEC(N'USE '+@QuotedDatabase+N'; SELECT DB_NAME() AS DemoDatabase,COUNT(*) AS Products FROM dbo.SANPHAM; SELECT MALH,COUNT(*) AS Products FROM dbo.SANPHAM GROUP BY MALH;');
