-- DOI CHIEU HANG THEO 60 ANH NGUOI DUNG CUNG CAP (2026-10-22)
-- Anh minh hoa khong xac thuc hang hoa hay thong so. Chi gan hang doc duoc tren anh.
-- Mac dinh CHI XEM TRUOC. Doi @Apply thanh 1 de ghi sau khi xem ket qua va sao luu DB.
-- Giu nguyen SB0012 va moi san pham da duoc gan hang khac DEMO.
USE [DAVISYS];
SET NOCOUNT ON;
SET XACT_ABORT ON;
DECLARE @Apply bit = 0;

DECLARE @Map TABLE (
    MASP varchar(20) PRIMARY KEY,
    SuggestedCode varchar(3) NOT NULL,
    BrandName nvarchar(100) NOT NULL
);
INSERT INTO @Map (MASP, SuggestedCode, BrandName) VALUES
    ('SB0001', 'B14', N'INLAND'),
    ('SB0002', 'B23', N'TECKNET'),
    ('SB0006', 'B12', N'HP'),
    ('SB0009', 'B26', N'VICTSING'),
    ('SB0010', 'B05', N'DAREU'),
    ('SB0012', 'B15', N'LOGITECH'),
    ('SB0013', 'B11', N'HAVIT'),
    ('SB0016', 'B22', N'STEELSERIES'),
    ('SB0017', 'B25', N'UGREEN'),
    ('SB0026', 'B06', N'DEEPCOOL'),
    ('SB0032', 'B09', N'FONTASTIC'),
    ('SB0033', 'B15', N'LOGITECH'),
    ('SB0034', 'B10', N'GENIUS'),
    ('SB0035', 'B07', N'DEPSTECH'),
    ('SB0036', 'B17', N'MAONO'),
    ('SB0038', 'B08', N'FIFINE'),
    ('SB0039', 'B24', N'TONOR'),
    ('SB0040', 'B17', N'MAONO'),
    ('SB0041', 'B10', N'GENIUS'),
    ('SB0042', 'B01', N'AMAZON BASICS'),
    ('SB0044', 'B04', N'CREATIVE'),
    ('SB0045', 'B18', N'MEETION'),
    ('SB0046', 'B16', N'MANHATTAN'),
    ('SB0047', 'B03', N'AXUMTEK'),
    ('SB0048', 'B21', N'STARTECH.COM'),
    ('SB0049', 'B19', N'PLUGABLE'),
    ('SB0057', 'B13', N'INIU'),
    ('SB0058', 'B02', N'ANKER'),
    ('SB0059', 'B25', N'UGREEN'),
    ('SB0060', 'B20', N'SANDBERG');

-- Bang xem truoc: khong thay doi du lieu.
SELECT m.MASP, sp.TENSP, sp.MAHANG AS MAHANG_HIEN_TAI,
       h.TENHANG AS HANG_HIEN_TAI, m.BrandName AS HANG_TREN_ANH,
       CASE WHEN sp.MASP IS NULL THEN N'Khong tim thay san pham'
            WHEN m.MASP = 'SB0012' THEN N'Giu nguyen: da chinh Logitech'
            WHEN sp.MAHANG = 'DEMO' THEN N'Se cap nhat khi @Apply=1'
            ELSE N'Giu nguyen: hang da duoc chinh' END AS XU_LY
FROM @Map m
LEFT JOIN dbo.SANPHAM sp ON sp.MASP=m.MASP
LEFT JOIN dbo.HANG h ON h.MAHANG=sp.MAHANG
ORDER BY m.MASP;

IF @Apply=0
BEGIN
    PRINT N'CHI XEM TRUOC: chua ghi du lieu. Doi @Apply=1 de ap dung.';
    RETURN;
END;

BEGIN TRY
    BEGIN TRANSACTION;
    -- Chi them cac hang can cho san pham con mang DEMO.
    -- Neu ma de xuat da thuoc hang khac, dung va rollback de tranh gan nham.
    IF EXISTS (
        SELECT 1 FROM @Map m
        JOIN dbo.SANPHAM sp ON sp.MASP=m.MASP
        JOIN dbo.HANG occupied ON occupied.MAHANG=m.SuggestedCode
        WHERE sp.MAHANG='DEMO' AND m.MASP<>'SB0012'
          AND NOT EXISTS (SELECT 1 FROM dbo.HANG h
                          WHERE UPPER(LTRIM(RTRIM(h.TENHANG)))=m.BrandName)
          AND UPPER(LTRIM(RTRIM(occupied.TENHANG)))<>m.BrandName
    )
        THROW 50001, N'Ma hang Bxx da duoc dung cho hang khac. Gui loi de doi ma.', 1;

    INSERT INTO dbo.HANG (MAHANG,TENHANG)
    SELECT DISTINCT m.SuggestedCode,m.BrandName
    FROM @Map m
    JOIN dbo.SANPHAM sp ON sp.MASP=m.MASP
    WHERE sp.MAHANG='DEMO' AND m.MASP<>'SB0012'
      AND NOT EXISTS (SELECT 1 FROM dbo.HANG h
                      WHERE UPPER(LTRIM(RTRIM(h.TENHANG)))=m.BrandName);

    UPDATE sp
    SET MAHANG=brand.MAHANG
    OUTPUT inserted.MASP, deleted.MAHANG AS MAHANG_CU,
           inserted.MAHANG AS MAHANG_MOI
    FROM dbo.SANPHAM sp
    JOIN @Map m ON m.MASP=sp.MASP
    CROSS APPLY (
        SELECT TOP (1) h.MAHANG FROM dbo.HANG h
        WHERE UPPER(LTRIM(RTRIM(h.TENHANG)))=m.BrandName
        ORDER BY h.MAHANG
    ) brand
    WHERE sp.MAHANG='DEMO' AND sp.MASP<>'SB0012';

    DECLARE @Changed int=@@ROWCOUNT;
    COMMIT TRANSACTION;
    SELECT @Changed AS SO_SAN_PHAM_DA_CAP_NHAT;
END TRY
BEGIN CATCH
    IF XACT_STATE()<>0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;

SELECT sp.MASP,sp.TENSP,sp.MAHANG,h.TENHANG
FROM dbo.SANPHAM sp
JOIN @Map m ON m.MASP=sp.MASP
LEFT JOIN dbo.HANG h ON h.MAHANG=sp.MAHANG
ORDER BY sp.MASP;
