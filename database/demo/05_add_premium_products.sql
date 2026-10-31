-- Additive SB61-SB70 only. Existing products, carts, invoices and accounts are never updated/deleted.
-- PRICES/STOCK/FIT SCORES ARE CLASSROOM DEMO ASSUMPTIONS; see premium/sources.csv.
USE [DAVISYS_DEMO_SB60];
SET NOCOUNT ON;
SET XACT_ABORT ON;
IF DB_NAME() NOT LIKE N'DAVISYS[_]DEMO[_]%' THROW 51500,N'Chỉ chạy trên database demo riêng DAVISYS_DEMO_.',1;
DECLARE @Apply bit=0; -- 0 = preview; 1 = insert missing rows after reviewing.
DROP TABLE IF EXISTS #PremiumProducts;
DROP TABLE IF EXISTS #PremiumBrands;
DROP TABLE IF EXISTS #PremiumCategories;
CREATE TABLE #PremiumBrands(Code nvarchar(10) PRIMARY KEY, BrandName nvarchar(50) NOT NULL);
INSERT #PremiumBrands VALUES (N'ASUS',N'ASUS'),(N'APPLE',N'APPLE'),(N'LGE',N'LG'),(N'HP',N'HP');
CREATE TABLE #PremiumCategories(MALH nvarchar(10) PRIMARY KEY,TENLH nvarchar(50) NOT NULL);
INSERT #PremiumCategories VALUES (N'LAP',N'Laptop'),(N'MH',N'Màn hình'),(N'PC',N'Máy tính để bàn');
CREATE TABLE #PremiumProducts(MASP nvarchar(10) PRIMARY KEY,TENSP nvarchar(50) NOT NULL,MALH nvarchar(10) NOT NULL,MAHANG nvarchar(10) NOT NULL,SOLUONG int NOT NULL,GIANHAP float NOT NULL,GIABAN float NOT NULL,NGAYNHAP date NOT NULL,HINH nvarchar(50) NOT NULL,MOTA nvarchar(2000) NOT NULL);
INSERT #PremiumProducts VALUES
(N'SB0061',N'Laptop ASUS TUF Gaming A15 FA507NV',N'LAP',N'ASUS',8,19920000,24900000,N'2026-10-25',N'SB0061',N'Thông tin mẫu hãng: Dòng 2023; Ryzen 7 7735HS, GeForce RTX 4060 Laptop; RAM/SSD tùy SKU. Giá, tồn kho và điểm là giả định demo BTL, không phải báo giá bán hiện hành. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 94; Học tập: 85; Văn phòng: 78; Thiết kế đồ họa: 85; Làm việc đa nhiệm: 88. Phong cách demo: Hiện đại; Năng động. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.'),
(N'SB0062',N'Laptop ASUS ROG Strix G16 G614JV',N'LAP',N'ASUS',6,31920000,39900000,N'2026-10-25',N'SB0062',N'Thông tin mẫu hãng: Dòng G16 2023, màn hình 16 inch; CPU/GPU/RAM/SSD tùy SKU hãng. Giá, tồn kho và điểm là giả định demo BTL, không phải báo giá bán hiện hành. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 99; Học tập: 87; Văn phòng: 80; Thiết kế đồ họa: 92; Làm việc đa nhiệm: 95. Phong cách demo: Hiện đại; Năng động. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.'),
(N'SB0063',N'Apple MacBook Air 13 M3 16GB 512GB',N'LAP',N'APPLE',10,22320000,27900000,N'2026-10-25',N'SB0063',N'Thông tin mẫu hãng: MacBook Air 13 inch M3 (2024); cấu hình 16GB bộ nhớ, SSD 512GB. Phần mềm và game phải hỗ trợ macOS. Giá, tồn kho và điểm là giả định demo BTL, không phải báo giá bán hiện hành. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 45; Học tập: 94; Văn phòng: 96; Thiết kế đồ họa: 86; Làm việc đa nhiệm: 90. Phong cách demo: Hiện đại; Tối giản; Sang trọng. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.'),
(N'SB0064',N'Apple MacBook Pro 14 M3 Pro 18GB 512GB',N'LAP',N'APPLE',5,39920000,49900000,N'2026-10-25',N'SB0064',N'Thông tin mẫu hãng: MacBook Pro 14 inch M3 Pro (11/2023); cấu hình 18GB bộ nhớ, SSD 512GB. Game/phần mềm phải hỗ trợ macOS. Giá, tồn kho và điểm là giả định demo BTL, không phải báo giá bán hiện hành. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 55; Học tập: 92; Văn phòng: 93; Thiết kế đồ họa: 98; Làm việc đa nhiệm: 98. Phong cách demo: Hiện đại; Tối giản; Sang trọng. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.'),
(N'SB0065',N'Màn hình ASUS ProArt PA279CRV 27 inch 4K',N'MH',N'ASUS',12,11120000,13900000,N'2026-10-25',N'SB0065',N'Thông tin mẫu hãng: Màn 27 inch IPS, 3840x2160; USB-C cấp nguồn tới 96W; chỉ gồm màn hình, không kèm máy tính. Giá, tồn kho và điểm là giả định demo BTL, không phải báo giá bán hiện hành. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 65; Học tập: 88; Văn phòng: 94; Thiết kế đồ họa: 98; Làm việc đa nhiệm: 94. Phong cách demo: Hiện đại; Tối giản; Sang trọng. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.'),
(N'SB0066',N'Màn hình ASUS ROG Swift PG279QM 27 inch',N'MH',N'ASUS',8,15920000,19900000,N'2026-10-25',N'SB0066',N'Thông tin mẫu hãng: Màn 27 inch QHD 2560x1440, Fast IPS 240Hz; chỉ gồm màn hình, không kèm máy tính. Giá, tồn kho và điểm là giả định demo BTL, không phải báo giá bán hiện hành. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 98; Học tập: 84; Văn phòng: 82; Thiết kế đồ họa: 86; Làm việc đa nhiệm: 87. Phong cách demo: Hiện đại; Năng động. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.'),
(N'SB0067',N'Màn hình LG UltraFine 32UN880-B 31.5 inch',N'MH',N'LGE',9,11920000,14900000,N'2026-10-25',N'SB0067',N'Thông tin mẫu hãng: Màn 31.5 inch IPS UHD 4K, HDR10, USB-C và chân Ergo; chỉ gồm màn hình, không kèm máy tính. Giá, tồn kho và điểm là giả định demo BTL, không phải báo giá bán hiện hành. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 65; Học tập: 88; Văn phòng: 96; Thiết kế đồ họa: 96; Làm việc đa nhiệm: 98. Phong cách demo: Hiện đại; Tối giản; Sang trọng. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.'),
(N'SB0068',N'Máy tính Apple Mac mini M4 16GB 256GB',N'PC',N'APPLE',10,13520000,16900000,N'2026-10-25',N'SB0068',N'Thông tin mẫu hãng: Mac mini M4 (2024); cấu hình 16GB bộ nhớ, SSD 256GB. Không kèm màn hình, bàn phím, chuột; dùng macOS. Giá, tồn kho và điểm là giả định demo BTL, không phải báo giá bán hiện hành. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 45; Học tập: 92; Văn phòng: 96; Thiết kế đồ họa: 89; Làm việc đa nhiệm: 94. Phong cách demo: Hiện đại; Tối giản; Sang trọng. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.'),
(N'SB0069',N'Cây máy HP OMEN 45L GT22-1470se',N'PC',N'HP',4,47920000,59900000,N'2026-10-25',N'SB0069',N'Thông tin mẫu hãng: Máy bộ HP OMEN 45L GT22-1470se; cấu hình CPU/GPU/RAM/SSD theo tùy chọn hãng. Không kèm màn hình. Giá, tồn kho và điểm là giả định demo BTL, không phải báo giá bán hiện hành. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 99; Học tập: 85; Văn phòng: 80; Thiết kế đồ họa: 96; Làm việc đa nhiệm: 98. Phong cách demo: Hiện đại; Năng động. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.'),
(N'SB0070',N'Cây máy ASUS ROG G22CH',N'PC',N'ASUS',5,35920000,44900000,N'2026-10-25',N'SB0070',N'Thông tin mẫu hãng: Máy bộ ASUS ROG G22CH dạng nhỏ gọn khoảng 10L; cấu hình CPU/GPU/RAM/SSD tùy SKU hãng. Không kèm màn hình. Giá, tồn kho và điểm là giả định demo BTL, không phải báo giá bán hiện hành. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 96; Học tập: 88; Văn phòng: 85; Thiết kế đồ họa: 94; Làm việc đa nhiệm: 96. Phong cách demo: Hiện đại; Năng động. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.');

SELECT n.MASP,n.TENSP,n.MALH,b.BrandName AS HANG,n.GIABAN AS GIA_DEMO,
       CASE WHEN p.MASP IS NULL THEN N'Sẽ thêm' ELSE N'Đã tồn tại: cần khớp mẫu/giá/mô tả trước khi bỏ qua' END AS XU_LY
FROM #PremiumProducts n JOIN #PremiumBrands b ON n.MAHANG=b.Code
LEFT JOIN dbo.SANPHAM p ON p.MASP=n.MASP ORDER BY n.MASP;
IF @Apply=0 RETURN;
BEGIN TRY
 BEGIN TRANSACTION;
 IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID(N'dbo.SANPHAM') AND name=N'MOTA' AND system_type_id=TYPE_ID(N'nvarchar') AND (max_length=-1 OR max_length>=4000))
  THROW 51501,N'Cần MOTA nvarchar(2000) hoặc lớn hơn. Chạy migration trước trên DB demo.',1;
 -- Reuse an existing brand by name (HP normally exists as B12); do not overwrite any existing brand.
 IF EXISTS (SELECT 1 FROM #PremiumBrands b JOIN dbo.HANG h WITH (UPDLOCK,HOLDLOCK) ON h.MAHANG=b.Code
   WHERE UPPER(LTRIM(RTRIM(h.TENHANG)))<>b.BrandName
   AND NOT EXISTS(SELECT 1 FROM dbo.HANG h2 WHERE UPPER(LTRIM(RTRIM(h2.TENHANG)))=b.BrandName))
  THROW 51502,N'Mã hãng đề xuất đang thuộc hãng khác; dừng để đối chiếu.',1;
 IF EXISTS (SELECT 1 FROM #PremiumCategories c JOIN dbo.LOAIHANG l WITH (UPDLOCK,HOLDLOCK) ON l.MALH=c.MALH WHERE l.TENLH<>c.TENLH)
  THROW 51503,N'Mã loại LAP/MH/PC đang có tên khác; dừng để đối chiếu.',1;
 INSERT dbo.HANG(MAHANG,TENHANG)
 SELECT b.Code,b.BrandName FROM #PremiumBrands b WHERE NOT EXISTS(SELECT 1 FROM dbo.HANG h WITH (UPDLOCK,HOLDLOCK) WHERE UPPER(LTRIM(RTRIM(h.TENHANG)))=b.BrandName);
 INSERT dbo.LOAIHANG(MALH,TENLH)
 SELECT c.MALH,c.TENLH FROM #PremiumCategories c WHERE NOT EXISTS(SELECT 1 FROM dbo.LOAIHANG l WITH (UPDLOCK,HOLDLOCK) WHERE l.MALH=c.MALH);
 -- Change only temporary seed rows to resolved existing brand IDs.
 UPDATE n SET MAHANG=(SELECT TOP (1) h.MAHANG FROM dbo.HANG h JOIN #PremiumBrands b ON UPPER(LTRIM(RTRIM(h.TENHANG)))=b.BrandName WHERE b.Code=n.MAHANG ORDER BY h.MAHANG)
 FROM #PremiumProducts n;
 -- Never silently reuse an occupied product ID for a different product. Stock/date may differ after real demo use.
 IF EXISTS (SELECT 1 FROM #PremiumProducts n JOIN dbo.SANPHAM p WITH (UPDLOCK,HOLDLOCK) ON p.MASP=n.MASP
 WHERE p.TENSP<>n.TENSP OR p.MALH<>n.MALH OR p.MAHANG<>n.MAHANG OR p.GIABAN<>n.GIABAN
    OR p.GIANHAP<>n.GIANHAP OR p.HINH<>n.HINH OR p.MOTA IS NULL OR p.MOTA<>n.MOTA)
  THROW 51504,N'Mã SB0061-SB0070 đã có dữ liệu khác; không ghi đè. Cần đối chiếu.',1;
 INSERT dbo.SANPHAM(MASP,TENSP,MALH,MAHANG,SOLUONG,GIANHAP,GIABAN,NGAYNHAP,HINH,MOTA)
 SELECT n.MASP,n.TENSP,n.MALH,n.MAHANG,n.SOLUONG,n.GIANHAP,n.GIABAN,n.NGAYNHAP,n.HINH,n.MOTA FROM #PremiumProducts n
 WHERE NOT EXISTS(SELECT 1 FROM dbo.SANPHAM p WITH (UPDLOCK,HOLDLOCK) WHERE p.MASP=n.MASP);
 DECLARE @Inserted int=@@ROWCOUNT;
 COMMIT TRANSACTION;
 SELECT @Inserted AS InsertedPremiumProducts,10-@Inserted AS ExistingPremiumProducts;
END TRY
BEGIN CATCH
 IF XACT_STATE()<>0 ROLLBACK TRANSACTION;
 THROW;
END CATCH;
SELECT p.MASP,p.TENSP,p.MALH,h.TENHANG,p.GIABAN,p.SOLUONG,p.HINH
FROM dbo.SANPHAM p JOIN #PremiumProducts n ON n.MASP=p.MASP JOIN dbo.HANG h ON h.MAHANG=p.MAHANG ORDER BY p.MASP;
