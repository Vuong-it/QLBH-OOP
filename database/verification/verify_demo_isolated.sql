-- Isolated SQL Server engine verification. No writes to DAVISYS.
USE master;
IF DB_ID(N'Codex_DAVISYS_Verify_20261023') IS NOT NULL THROW 51000, 'Test database already exists; do not overwrite.', 1;
EXEC(N'CREATE DATABASE [Codex_DAVISYS_Verify_20261023]');
GO
USE [Codex_DAVISYS_Verify_20261023];
SET NOCOUNT ON;
SET XACT_ABORT ON;
IF OBJECT_ID(N'dbo.VerificationResults', N'U') IS NOT NULL THROW 51000, 'Verification already exists; use a fresh sandbox.', 1;
CREATE TABLE dbo.VerificationResults (Step int PRIMARY KEY, Scenario nvarchar(100), Result varchar(4), Detail nvarchar(500));
SELECT * INTO dbo.SourceSANPHAM FROM DAVISYS.dbo.SANPHAM;
SELECT * INTO dbo.SourceHANG FROM DAVISYS.dbo.HANG;
SELECT * INTO dbo.SourceLOAIHANG FROM DAVISYS.dbo.LOAIHANG;
SELECT * INTO dbo.HANG FROM dbo.SourceHANG;
SELECT * INTO dbo.LOAIHANG FROM dbo.SourceLOAIHANG;
SELECT TOP (0) * INTO dbo.SANPHAM FROM dbo.SourceSANPHAM;
ALTER TABLE dbo.SANPHAM ALTER COLUMN MOTA nvarchar(100) NOT NULL;
DECLARE @cols nvarchar(max), @selectCols nvarchar(max);
SELECT @cols=STRING_AGG(CAST(QUOTENAME(name) AS nvarchar(max)),','),
@selectCols=STRING_AGG(CAST(CASE WHEN name='MOTA' THEN 'LEFT(MOTA,100)' ELSE QUOTENAME(name) END AS nvarchar(max)),',')
FROM sys.columns WHERE object_id=OBJECT_ID('dbo.SourceSANPHAM');
EXEC(N'INSERT INTO dbo.SANPHAM ('+@cols+') SELECT '+@selectCols+' FROM dbo.SourceSANPHAM');
SELECT * INTO dbo.BeforeMigration FROM dbo.SANPHAM;
EXEC(N'-- MANUAL ONLY: back up the database, then test this on an isolated SQL Server copy.
-- Connect to the intended DAVISYS database. This script preserves all descriptions and rows.
SET XACT_ABORT ON;
IF DB_NAME() <> N''Codex_DAVISYS_Verify_20261023''
    THROW 50001, N''Choose the DAVISYS database explicitly before running this migration.'', 1;
IF OBJECT_ID(N''dbo.SANPHAM'', N''U'') IS NULL OR COL_LENGTH(N''dbo.SANPHAM'', N''MOTA'') IS NULL
    THROW 50002, N''dbo.SANPHAM.MOTA is missing.'', 1;
IF NOT EXISTS (SELECT 1 FROM sys.columns
               WHERE object_id = OBJECT_ID(N''dbo.SANPHAM'') AND name = N''MOTA''
                 AND system_type_id = TYPE_ID(N''nvarchar'') AND is_nullable = 0)
    THROW 50003, N''Expected a NOT NULL nvarchar MOTA column. Review the existing schema manually.'', 1;
BEGIN TRY
    BEGIN TRANSACTION;
    IF COL_LENGTH(N''dbo.SANPHAM'', N''MOTA'') <> -1 AND COL_LENGTH(N''dbo.SANPHAM'', N''MOTA'') < 4000
        ALTER TABLE dbo.SANPHAM ALTER COLUMN MOTA nvarchar(2000) NOT NULL;
    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
-- Repeated execution does not shrink a larger column or change any product metadata.
');

IF COL_LENGTH('dbo.SANPHAM','MOTA')<>4000 THROW 51001,'Migration did not widen to nvarchar(2000).',1;
IF EXISTS(SELECT * FROM dbo.BeforeMigration EXCEPT SELECT * FROM dbo.SANPHAM) OR EXISTS(SELECT * FROM dbo.SANPHAM EXCEPT SELECT * FROM dbo.BeforeMigration) THROW 51002,'Migration changed row content.',1;
INSERT dbo.VerificationResults VALUES(1,N'Migration: widen 100 to 2000','PASS',N'71 source product rows with descriptions shortened only in test fixture; all fixture values preserved.');
EXEC(N'-- MANUAL ONLY: back up the database, then test this on an isolated SQL Server copy.
-- Connect to the intended DAVISYS database. This script preserves all descriptions and rows.
SET XACT_ABORT ON;
IF DB_NAME() <> N''Codex_DAVISYS_Verify_20261023''
    THROW 50001, N''Choose the DAVISYS database explicitly before running this migration.'', 1;
IF OBJECT_ID(N''dbo.SANPHAM'', N''U'') IS NULL OR COL_LENGTH(N''dbo.SANPHAM'', N''MOTA'') IS NULL
    THROW 50002, N''dbo.SANPHAM.MOTA is missing.'', 1;
IF NOT EXISTS (SELECT 1 FROM sys.columns
               WHERE object_id = OBJECT_ID(N''dbo.SANPHAM'') AND name = N''MOTA''
                 AND system_type_id = TYPE_ID(N''nvarchar'') AND is_nullable = 0)
    THROW 50003, N''Expected a NOT NULL nvarchar MOTA column. Review the existing schema manually.'', 1;
BEGIN TRY
    BEGIN TRANSACTION;
    IF COL_LENGTH(N''dbo.SANPHAM'', N''MOTA'') <> -1 AND COL_LENGTH(N''dbo.SANPHAM'', N''MOTA'') < 4000
        ALTER TABLE dbo.SANPHAM ALTER COLUMN MOTA nvarchar(2000) NOT NULL;
    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
-- Repeated execution does not shrink a larger column or change any product metadata.
');

IF COL_LENGTH('dbo.SANPHAM','MOTA')<>4000 OR EXISTS(SELECT * FROM dbo.BeforeMigration EXCEPT SELECT * FROM dbo.SANPHAM) THROW 51003,'Migration repeat changed schema/content.',1;
INSERT dbo.VerificationResults VALUES(2,N'Migration: repeat','PASS',N'nvarchar(2000) NOT NULL preserved; no row changes.');
ALTER TABLE dbo.SANPHAM ALTER COLUMN MOTA nvarchar(max) NOT NULL;
EXEC(N'-- MANUAL ONLY: back up the database, then test this on an isolated SQL Server copy.
-- Connect to the intended DAVISYS database. This script preserves all descriptions and rows.
SET XACT_ABORT ON;
IF DB_NAME() <> N''Codex_DAVISYS_Verify_20261023''
    THROW 50001, N''Choose the DAVISYS database explicitly before running this migration.'', 1;
IF OBJECT_ID(N''dbo.SANPHAM'', N''U'') IS NULL OR COL_LENGTH(N''dbo.SANPHAM'', N''MOTA'') IS NULL
    THROW 50002, N''dbo.SANPHAM.MOTA is missing.'', 1;
IF NOT EXISTS (SELECT 1 FROM sys.columns
               WHERE object_id = OBJECT_ID(N''dbo.SANPHAM'') AND name = N''MOTA''
                 AND system_type_id = TYPE_ID(N''nvarchar'') AND is_nullable = 0)
    THROW 50003, N''Expected a NOT NULL nvarchar MOTA column. Review the existing schema manually.'', 1;
BEGIN TRY
    BEGIN TRANSACTION;
    IF COL_LENGTH(N''dbo.SANPHAM'', N''MOTA'') <> -1 AND COL_LENGTH(N''dbo.SANPHAM'', N''MOTA'') < 4000
        ALTER TABLE dbo.SANPHAM ALTER COLUMN MOTA nvarchar(2000) NOT NULL;
    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
-- Repeated execution does not shrink a larger column or change any product metadata.
');

IF COL_LENGTH('dbo.SANPHAM','MOTA')<>-1 THROW 51004,'Larger column was shrunk.',1;
ALTER TABLE dbo.SANPHAM ALTER COLUMN MOTA nvarchar(2000) NOT NULL;
UPDATE p SET MOTA=s.MOTA FROM dbo.SANPHAM p JOIN dbo.SourceSANPHAM s ON s.MASP=p.MASP;
INSERT dbo.VerificationResults VALUES(3,N'Migration: preserve larger column','PASS',N'nvarchar(max) preserved. Full source descriptions restored only in test fixture afterwards.');
CREATE TABLE #Expected(MASP nvarchar(10) PRIMARY KEY, SuggestedCode nvarchar(10), BrandName nvarchar(100));
INSERT #Expected VALUES (N'SB0001',N'B14',N'INLAND'),
(N'SB0002',N'B23',N'TECKNET'),
(N'SB0006',N'B12',N'HP'),
(N'SB0009',N'B26',N'VICTSING'),
(N'SB0010',N'B05',N'DAREU'),
(N'SB0012',N'B15',N'LOGITECH'),
(N'SB0013',N'B11',N'HAVIT'),
(N'SB0016',N'B22',N'STEELSERIES'),
(N'SB0017',N'B25',N'UGREEN'),
(N'SB0026',N'B06',N'DEEPCOOL'),
(N'SB0032',N'B09',N'FONTASTIC'),
(N'SB0033',N'B15',N'LOGITECH'),
(N'SB0034',N'B10',N'GENIUS'),
(N'SB0035',N'B07',N'DEPSTECH'),
(N'SB0036',N'B17',N'MAONO'),
(N'SB0038',N'B08',N'FIFINE'),
(N'SB0039',N'B24',N'TONOR'),
(N'SB0040',N'B17',N'MAONO'),
(N'SB0041',N'B10',N'GENIUS'),
(N'SB0042',N'B01',N'AMAZON BASICS'),
(N'SB0044',N'B04',N'CREATIVE'),
(N'SB0045',N'B18',N'MEETION'),
(N'SB0046',N'B16',N'MANHATTAN'),
(N'SB0047',N'B03',N'AXUMTEK'),
(N'SB0048',N'B21',N'STARTECH.COM'),
(N'SB0049',N'B19',N'PLUGABLE'),
(N'SB0057',N'B13',N'INIU'),
(N'SB0058',N'B02',N'ANKER'),
(N'SB0059',N'B25',N'UGREEN'),
(N'SB0060',N'B20',N'SANDBERG');
IF NOT EXISTS(SELECT 1 FROM dbo.HANG WHERE MAHANG='DEMO') INSERT dbo.HANG VALUES('DEMO',N'DEMO');
UPDATE p SET MAHANG='DEMO' FROM dbo.SANPHAM p JOIN #Expected m ON m.MASP=p.MASP WHERE p.MASP NOT IN ('SB0006','SB0012');
-- Exercise insertion as well as reuse: free INLAND's suggested code in the fixture before adding FKs.
UPDATE dbo.SANPHAM SET MAHANG='DEMO' WHERE MAHANG='B14';
UPDATE dbo.HANG SET MAHANG='X14',TENHANG=N'UNRELATED FIXTURE BRAND' WHERE MAHANG='B14';
UPDATE dbo.SANPHAM SET MALH='BP' WHERE MASP='SB0012';
ALTER TABLE dbo.HANG ADD PRIMARY KEY(MAHANG);
ALTER TABLE dbo.LOAIHANG ADD PRIMARY KEY(MALH);
ALTER TABLE dbo.SANPHAM ADD PRIMARY KEY(MASP);
ALTER TABLE dbo.SANPHAM ADD FOREIGN KEY(MAHANG) REFERENCES dbo.HANG(MAHANG);
ALTER TABLE dbo.SANPHAM ADD FOREIGN KEY(MALH) REFERENCES dbo.LOAIHANG(MALH);
SELECT * INTO dbo.BeforeBrands FROM dbo.SANPHAM;
SELECT * INTO dbo.BeforeBrandNames FROM dbo.HANG;
EXEC(N'-- DOI CHIEU HANG THEO 60 ANH NGUOI DUNG CUNG CAP (2026-10-22)
-- Anh minh hoa khong xac thuc hang hoa hay thong so. Chi gan hang doc duoc tren anh.
-- Mac dinh CHI XEM TRUOC. Doi @Apply thanh 1 de ghi sau khi xem ket qua va sao luu DB.
-- Giu nguyen SB0012 va moi san pham da duoc gan hang khac DEMO.
USE [Codex_DAVISYS_Verify_20261023];
SET NOCOUNT ON;
SET XACT_ABORT ON;
DECLARE @Apply bit = 0;

DECLARE @Map TABLE (
    MASP varchar(20) PRIMARY KEY,
    SuggestedCode varchar(3) NOT NULL,
    BrandName nvarchar(100) NOT NULL
);
INSERT INTO @Map (MASP, SuggestedCode, BrandName) VALUES
    (''SB0001'', ''B14'', N''INLAND''),
    (''SB0002'', ''B23'', N''TECKNET''),
    (''SB0006'', ''B12'', N''HP''),
    (''SB0009'', ''B26'', N''VICTSING''),
    (''SB0010'', ''B05'', N''DAREU''),
    (''SB0012'', ''B15'', N''LOGITECH''),
    (''SB0013'', ''B11'', N''HAVIT''),
    (''SB0016'', ''B22'', N''STEELSERIES''),
    (''SB0017'', ''B25'', N''UGREEN''),
    (''SB0026'', ''B06'', N''DEEPCOOL''),
    (''SB0032'', ''B09'', N''FONTASTIC''),
    (''SB0033'', ''B15'', N''LOGITECH''),
    (''SB0034'', ''B10'', N''GENIUS''),
    (''SB0035'', ''B07'', N''DEPSTECH''),
    (''SB0036'', ''B17'', N''MAONO''),
    (''SB0038'', ''B08'', N''FIFINE''),
    (''SB0039'', ''B24'', N''TONOR''),
    (''SB0040'', ''B17'', N''MAONO''),
    (''SB0041'', ''B10'', N''GENIUS''),
    (''SB0042'', ''B01'', N''AMAZON BASICS''),
    (''SB0044'', ''B04'', N''CREATIVE''),
    (''SB0045'', ''B18'', N''MEETION''),
    (''SB0046'', ''B16'', N''MANHATTAN''),
    (''SB0047'', ''B03'', N''AXUMTEK''),
    (''SB0048'', ''B21'', N''STARTECH.COM''),
    (''SB0049'', ''B19'', N''PLUGABLE''),
    (''SB0057'', ''B13'', N''INIU''),
    (''SB0058'', ''B02'', N''ANKER''),
    (''SB0059'', ''B25'', N''UGREEN''),
    (''SB0060'', ''B20'', N''SANDBERG'');

-- Bang xem truoc: khong thay doi du lieu.
SELECT m.MASP, sp.TENSP, sp.MAHANG AS MAHANG_HIEN_TAI,
       h.TENHANG AS HANG_HIEN_TAI, m.BrandName AS HANG_TREN_ANH,
       CASE WHEN sp.MASP IS NULL THEN N''Khong tim thay san pham''
            WHEN m.MASP = ''SB0012'' THEN N''Giu nguyen: da chinh Logitech''
            WHEN sp.MAHANG = ''DEMO'' THEN N''Se cap nhat khi @Apply=1''
            ELSE N''Giu nguyen: hang da duoc chinh'' END AS XU_LY
FROM @Map m
LEFT JOIN dbo.SANPHAM sp ON sp.MASP=m.MASP
LEFT JOIN dbo.HANG h ON h.MAHANG=sp.MAHANG
ORDER BY m.MASP;

IF @Apply=0
BEGIN
    PRINT N''CHI XEM TRUOC: chua ghi du lieu. Doi @Apply=1 de ap dung.'';
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
        WHERE sp.MAHANG=''DEMO'' AND m.MASP<>''SB0012''
          AND NOT EXISTS (SELECT 1 FROM dbo.HANG h
                          WHERE UPPER(LTRIM(RTRIM(h.TENHANG)))=m.BrandName)
          AND UPPER(LTRIM(RTRIM(occupied.TENHANG)))<>m.BrandName
    )
        THROW 50001, N''Ma hang Bxx da duoc dung cho hang khac. Gui loi de doi ma.'', 1;

    INSERT INTO dbo.HANG (MAHANG,TENHANG)
    SELECT DISTINCT m.SuggestedCode,m.BrandName
    FROM @Map m
    JOIN dbo.SANPHAM sp ON sp.MASP=m.MASP
    WHERE sp.MAHANG=''DEMO'' AND m.MASP<>''SB0012''
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
    WHERE sp.MAHANG=''DEMO'' AND sp.MASP<>''SB0012'';

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
');

IF EXISTS(SELECT * FROM dbo.BeforeBrands EXCEPT SELECT * FROM dbo.SANPHAM) OR EXISTS(SELECT * FROM dbo.SANPHAM EXCEPT SELECT * FROM dbo.BeforeBrands) OR EXISTS(SELECT * FROM dbo.HANG EXCEPT SELECT * FROM dbo.BeforeBrandNames) OR EXISTS(SELECT * FROM dbo.BeforeBrandNames EXCEPT SELECT * FROM dbo.HANG) THROW 51005,'Brand preview modified data.',1;
INSERT dbo.VerificationResults VALUES(4,N'Brands @Apply=0','PASS',N'Products and brands unchanged.');
EXEC(N'-- DOI CHIEU HANG THEO 60 ANH NGUOI DUNG CUNG CAP (2026-10-22)
-- Anh minh hoa khong xac thuc hang hoa hay thong so. Chi gan hang doc duoc tren anh.
-- Mac dinh CHI XEM TRUOC. Doi @Apply thanh 1 de ghi sau khi xem ket qua va sao luu DB.
-- Giu nguyen SB0012 va moi san pham da duoc gan hang khac DEMO.
USE [Codex_DAVISYS_Verify_20261023];
SET NOCOUNT ON;
SET XACT_ABORT ON;
DECLARE @Apply bit = 1;

DECLARE @Map TABLE (
    MASP varchar(20) PRIMARY KEY,
    SuggestedCode varchar(3) NOT NULL,
    BrandName nvarchar(100) NOT NULL
);
INSERT INTO @Map (MASP, SuggestedCode, BrandName) VALUES
    (''SB0001'', ''B14'', N''INLAND''),
    (''SB0002'', ''B23'', N''TECKNET''),
    (''SB0006'', ''B12'', N''HP''),
    (''SB0009'', ''B26'', N''VICTSING''),
    (''SB0010'', ''B05'', N''DAREU''),
    (''SB0012'', ''B15'', N''LOGITECH''),
    (''SB0013'', ''B11'', N''HAVIT''),
    (''SB0016'', ''B22'', N''STEELSERIES''),
    (''SB0017'', ''B25'', N''UGREEN''),
    (''SB0026'', ''B06'', N''DEEPCOOL''),
    (''SB0032'', ''B09'', N''FONTASTIC''),
    (''SB0033'', ''B15'', N''LOGITECH''),
    (''SB0034'', ''B10'', N''GENIUS''),
    (''SB0035'', ''B07'', N''DEPSTECH''),
    (''SB0036'', ''B17'', N''MAONO''),
    (''SB0038'', ''B08'', N''FIFINE''),
    (''SB0039'', ''B24'', N''TONOR''),
    (''SB0040'', ''B17'', N''MAONO''),
    (''SB0041'', ''B10'', N''GENIUS''),
    (''SB0042'', ''B01'', N''AMAZON BASICS''),
    (''SB0044'', ''B04'', N''CREATIVE''),
    (''SB0045'', ''B18'', N''MEETION''),
    (''SB0046'', ''B16'', N''MANHATTAN''),
    (''SB0047'', ''B03'', N''AXUMTEK''),
    (''SB0048'', ''B21'', N''STARTECH.COM''),
    (''SB0049'', ''B19'', N''PLUGABLE''),
    (''SB0057'', ''B13'', N''INIU''),
    (''SB0058'', ''B02'', N''ANKER''),
    (''SB0059'', ''B25'', N''UGREEN''),
    (''SB0060'', ''B20'', N''SANDBERG'');

-- Bang xem truoc: khong thay doi du lieu.
SELECT m.MASP, sp.TENSP, sp.MAHANG AS MAHANG_HIEN_TAI,
       h.TENHANG AS HANG_HIEN_TAI, m.BrandName AS HANG_TREN_ANH,
       CASE WHEN sp.MASP IS NULL THEN N''Khong tim thay san pham''
            WHEN m.MASP = ''SB0012'' THEN N''Giu nguyen: da chinh Logitech''
            WHEN sp.MAHANG = ''DEMO'' THEN N''Se cap nhat khi @Apply=1''
            ELSE N''Giu nguyen: hang da duoc chinh'' END AS XU_LY
FROM @Map m
LEFT JOIN dbo.SANPHAM sp ON sp.MASP=m.MASP
LEFT JOIN dbo.HANG h ON h.MAHANG=sp.MAHANG
ORDER BY m.MASP;

IF @Apply=0
BEGIN
    PRINT N''CHI XEM TRUOC: chua ghi du lieu. Doi @Apply=1 de ap dung.'';
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
        WHERE sp.MAHANG=''DEMO'' AND m.MASP<>''SB0012''
          AND NOT EXISTS (SELECT 1 FROM dbo.HANG h
                          WHERE UPPER(LTRIM(RTRIM(h.TENHANG)))=m.BrandName)
          AND UPPER(LTRIM(RTRIM(occupied.TENHANG)))<>m.BrandName
    )
        THROW 50001, N''Ma hang Bxx da duoc dung cho hang khac. Gui loi de doi ma.'', 1;

    INSERT INTO dbo.HANG (MAHANG,TENHANG)
    SELECT DISTINCT m.SuggestedCode,m.BrandName
    FROM @Map m
    JOIN dbo.SANPHAM sp ON sp.MASP=m.MASP
    WHERE sp.MAHANG=''DEMO'' AND m.MASP<>''SB0012''
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
    WHERE sp.MAHANG=''DEMO'' AND sp.MASP<>''SB0012'';

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
');

DECLARE @changed int=(SELECT COUNT(*) FROM dbo.SANPHAM p JOIN dbo.BeforeBrands b ON p.MASP=b.MASP WHERE p.MAHANG<>b.MAHANG);
IF @changed<>28 THROW 51006,'Expected exactly 28 eligible mapped products to change.',1;
IF EXISTS(SELECT 1 FROM dbo.SANPHAM p JOIN dbo.BeforeBrands b ON p.MASP=b.MASP WHERE p.MAHANG<>b.MAHANG AND (p.MASP NOT IN(SELECT MASP FROM #Expected) OR p.MASP IN('SB0006','SB0012'))) THROW 51007,'Protected/unmapped product overwritten.',1;
IF EXISTS(SELECT 1 FROM dbo.SANPHAM p JOIN #Expected m ON m.MASP=p.MASP JOIN dbo.HANG h ON p.MAHANG=h.MAHANG WHERE UPPER(LTRIM(RTRIM(h.TENHANG)))<>m.BrandName) THROW 51008,'Mapped brand mismatch.',1;
IF NOT EXISTS(SELECT 1 FROM dbo.HANG WHERE MAHANG='B14' AND TENHANG=N'INLAND') THROW 51009,'Missing brand not inserted.',1;
INSERT dbo.VerificationResults VALUES(5,N'Brands @Apply=1','PASS',N'28 eligible products updated; SB0006 HP and SB0012 Logitech preserved; missing INLAND inserted.');
SELECT * INTO dbo.AfterBrands FROM dbo.SANPHAM;
SELECT * INTO dbo.AfterBrandNames FROM dbo.HANG;
EXEC(N'-- DOI CHIEU HANG THEO 60 ANH NGUOI DUNG CUNG CAP (2026-10-22)
-- Anh minh hoa khong xac thuc hang hoa hay thong so. Chi gan hang doc duoc tren anh.
-- Mac dinh CHI XEM TRUOC. Doi @Apply thanh 1 de ghi sau khi xem ket qua va sao luu DB.
-- Giu nguyen SB0012 va moi san pham da duoc gan hang khac DEMO.
USE [Codex_DAVISYS_Verify_20261023];
SET NOCOUNT ON;
SET XACT_ABORT ON;
DECLARE @Apply bit = 1;

DECLARE @Map TABLE (
    MASP varchar(20) PRIMARY KEY,
    SuggestedCode varchar(3) NOT NULL,
    BrandName nvarchar(100) NOT NULL
);
INSERT INTO @Map (MASP, SuggestedCode, BrandName) VALUES
    (''SB0001'', ''B14'', N''INLAND''),
    (''SB0002'', ''B23'', N''TECKNET''),
    (''SB0006'', ''B12'', N''HP''),
    (''SB0009'', ''B26'', N''VICTSING''),
    (''SB0010'', ''B05'', N''DAREU''),
    (''SB0012'', ''B15'', N''LOGITECH''),
    (''SB0013'', ''B11'', N''HAVIT''),
    (''SB0016'', ''B22'', N''STEELSERIES''),
    (''SB0017'', ''B25'', N''UGREEN''),
    (''SB0026'', ''B06'', N''DEEPCOOL''),
    (''SB0032'', ''B09'', N''FONTASTIC''),
    (''SB0033'', ''B15'', N''LOGITECH''),
    (''SB0034'', ''B10'', N''GENIUS''),
    (''SB0035'', ''B07'', N''DEPSTECH''),
    (''SB0036'', ''B17'', N''MAONO''),
    (''SB0038'', ''B08'', N''FIFINE''),
    (''SB0039'', ''B24'', N''TONOR''),
    (''SB0040'', ''B17'', N''MAONO''),
    (''SB0041'', ''B10'', N''GENIUS''),
    (''SB0042'', ''B01'', N''AMAZON BASICS''),
    (''SB0044'', ''B04'', N''CREATIVE''),
    (''SB0045'', ''B18'', N''MEETION''),
    (''SB0046'', ''B16'', N''MANHATTAN''),
    (''SB0047'', ''B03'', N''AXUMTEK''),
    (''SB0048'', ''B21'', N''STARTECH.COM''),
    (''SB0049'', ''B19'', N''PLUGABLE''),
    (''SB0057'', ''B13'', N''INIU''),
    (''SB0058'', ''B02'', N''ANKER''),
    (''SB0059'', ''B25'', N''UGREEN''),
    (''SB0060'', ''B20'', N''SANDBERG'');

-- Bang xem truoc: khong thay doi du lieu.
SELECT m.MASP, sp.TENSP, sp.MAHANG AS MAHANG_HIEN_TAI,
       h.TENHANG AS HANG_HIEN_TAI, m.BrandName AS HANG_TREN_ANH,
       CASE WHEN sp.MASP IS NULL THEN N''Khong tim thay san pham''
            WHEN m.MASP = ''SB0012'' THEN N''Giu nguyen: da chinh Logitech''
            WHEN sp.MAHANG = ''DEMO'' THEN N''Se cap nhat khi @Apply=1''
            ELSE N''Giu nguyen: hang da duoc chinh'' END AS XU_LY
FROM @Map m
LEFT JOIN dbo.SANPHAM sp ON sp.MASP=m.MASP
LEFT JOIN dbo.HANG h ON h.MAHANG=sp.MAHANG
ORDER BY m.MASP;

IF @Apply=0
BEGIN
    PRINT N''CHI XEM TRUOC: chua ghi du lieu. Doi @Apply=1 de ap dung.'';
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
        WHERE sp.MAHANG=''DEMO'' AND m.MASP<>''SB0012''
          AND NOT EXISTS (SELECT 1 FROM dbo.HANG h
                          WHERE UPPER(LTRIM(RTRIM(h.TENHANG)))=m.BrandName)
          AND UPPER(LTRIM(RTRIM(occupied.TENHANG)))<>m.BrandName
    )
        THROW 50001, N''Ma hang Bxx da duoc dung cho hang khac. Gui loi de doi ma.'', 1;

    INSERT INTO dbo.HANG (MAHANG,TENHANG)
    SELECT DISTINCT m.SuggestedCode,m.BrandName
    FROM @Map m
    JOIN dbo.SANPHAM sp ON sp.MASP=m.MASP
    WHERE sp.MAHANG=''DEMO'' AND m.MASP<>''SB0012''
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
    WHERE sp.MAHANG=''DEMO'' AND sp.MASP<>''SB0012'';

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
');

IF EXISTS(SELECT * FROM dbo.AfterBrands EXCEPT SELECT * FROM dbo.SANPHAM) OR EXISTS(SELECT * FROM dbo.SANPHAM EXCEPT SELECT * FROM dbo.AfterBrands) OR EXISTS(SELECT * FROM dbo.HANG EXCEPT SELECT * FROM dbo.AfterBrandNames) OR EXISTS(SELECT * FROM dbo.AfterBrandNames EXCEPT SELECT * FROM dbo.HANG) THROW 51010,'Brand repeat modified data.',1;
INSERT dbo.VerificationResults VALUES(6,N'Brands @Apply=1 repeat','PASS',N'0 product or brand changes.');
EXEC(N'-- Manual, existing DAVISYS demo database only. Default: preview.
USE [Codex_DAVISYS_Verify_20261023];
SET NOCOUNT ON;
SET XACT_ABORT ON;
DECLARE @Apply bit = 0;

SELECT sp.MASP,sp.TENSP,sp.MALH,lh.TENLH
FROM dbo.SANPHAM sp
LEFT JOIN dbo.LOAIHANG lh ON lh.MALH=sp.MALH
WHERE sp.MASP=''SB0012'';
IF @Apply=0 RETURN;

BEGIN TRY
    BEGIN TRANSACTION;
    IF NOT EXISTS (SELECT 1 FROM dbo.LOAIHANG WHERE MALH=''TN'' AND TENLH LIKE N''%Tai nghe%'')
        THROW 50001, N''Chua xac nhan duoc TN la Tai nghe.'', 1;
    IF NOT EXISTS (SELECT 1 FROM dbo.SANPHAM WHERE MASP=''SB0012'' AND TENSP LIKE N''%Tai nghe%'')
        THROW 50002, N''Chua xac nhan duoc SB0012 la san pham tai nghe.'', 1;
    IF EXISTS (SELECT 1 FROM dbo.SANPHAM WHERE MASP=''SB0012'' AND (MALH IS NULL OR MALH NOT IN (''BP'',''TN'')))
        THROW 50003, N''Loai hien tai khac BP/TN. Can doi chieu thu cong.'', 1;
    UPDATE dbo.SANPHAM SET MALH=''TN'' WHERE MASP=''SB0012'' AND MALH=''BP'';
    DECLARE @Changed int=@@ROWCOUNT;
    COMMIT TRANSACTION;
    SELECT @Changed AS SO_SAN_PHAM_DA_SUA_LOAI;
END TRY
BEGIN CATCH
    IF XACT_STATE()<>0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;

SELECT sp.MASP,sp.TENSP,sp.MALH,lh.TENLH
FROM dbo.SANPHAM sp
LEFT JOIN dbo.LOAIHANG lh ON lh.MALH=sp.MALH
WHERE sp.MASP=''SB0012'';
');

IF NOT EXISTS(SELECT 1 FROM dbo.SANPHAM WHERE MASP='SB0012' AND MALH='BP') THROW 51011,'Category preview modified data.',1;
INSERT dbo.VerificationResults VALUES(7,N'Category @Apply=0','PASS',N'SB0012 still BP in test fixture.');
EXEC(N'-- Manual, existing DAVISYS demo database only. Default: preview.
USE [Codex_DAVISYS_Verify_20261023];
SET NOCOUNT ON;
SET XACT_ABORT ON;
DECLARE @Apply bit = 1;

SELECT sp.MASP,sp.TENSP,sp.MALH,lh.TENLH
FROM dbo.SANPHAM sp
LEFT JOIN dbo.LOAIHANG lh ON lh.MALH=sp.MALH
WHERE sp.MASP=''SB0012'';
IF @Apply=0 RETURN;

BEGIN TRY
    BEGIN TRANSACTION;
    IF NOT EXISTS (SELECT 1 FROM dbo.LOAIHANG WHERE MALH=''TN'' AND TENLH LIKE N''%Tai nghe%'')
        THROW 50001, N''Chua xac nhan duoc TN la Tai nghe.'', 1;
    IF NOT EXISTS (SELECT 1 FROM dbo.SANPHAM WHERE MASP=''SB0012'' AND TENSP LIKE N''%Tai nghe%'')
        THROW 50002, N''Chua xac nhan duoc SB0012 la san pham tai nghe.'', 1;
    IF EXISTS (SELECT 1 FROM dbo.SANPHAM WHERE MASP=''SB0012'' AND (MALH IS NULL OR MALH NOT IN (''BP'',''TN'')))
        THROW 50003, N''Loai hien tai khac BP/TN. Can doi chieu thu cong.'', 1;
    UPDATE dbo.SANPHAM SET MALH=''TN'' WHERE MASP=''SB0012'' AND MALH=''BP'';
    DECLARE @Changed int=@@ROWCOUNT;
    COMMIT TRANSACTION;
    SELECT @Changed AS SO_SAN_PHAM_DA_SUA_LOAI;
END TRY
BEGIN CATCH
    IF XACT_STATE()<>0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;

SELECT sp.MASP,sp.TENSP,sp.MALH,lh.TENLH
FROM dbo.SANPHAM sp
LEFT JOIN dbo.LOAIHANG lh ON lh.MALH=sp.MALH
WHERE sp.MASP=''SB0012'';
');

IF NOT EXISTS(SELECT 1 FROM dbo.SANPHAM WHERE MASP='SB0012' AND MALH='TN') THROW 51012,'SB0012 was not corrected.',1;
IF (SELECT COUNT(*) FROM (SELECT * FROM dbo.SANPHAM EXCEPT SELECT * FROM dbo.AfterBrands) d)<>1 THROW 51013,'Category apply affected unexpected rows.',1;
INSERT dbo.VerificationResults VALUES(8,N'Category @Apply=1','PASS',N'Exactly one changed row: SB0012 BP to TN.');
SELECT * INTO dbo.AfterCategory FROM dbo.SANPHAM;
EXEC(N'-- Manual, existing DAVISYS demo database only. Default: preview.
USE [Codex_DAVISYS_Verify_20261023];
SET NOCOUNT ON;
SET XACT_ABORT ON;
DECLARE @Apply bit = 1;

SELECT sp.MASP,sp.TENSP,sp.MALH,lh.TENLH
FROM dbo.SANPHAM sp
LEFT JOIN dbo.LOAIHANG lh ON lh.MALH=sp.MALH
WHERE sp.MASP=''SB0012'';
IF @Apply=0 RETURN;

BEGIN TRY
    BEGIN TRANSACTION;
    IF NOT EXISTS (SELECT 1 FROM dbo.LOAIHANG WHERE MALH=''TN'' AND TENLH LIKE N''%Tai nghe%'')
        THROW 50001, N''Chua xac nhan duoc TN la Tai nghe.'', 1;
    IF NOT EXISTS (SELECT 1 FROM dbo.SANPHAM WHERE MASP=''SB0012'' AND TENSP LIKE N''%Tai nghe%'')
        THROW 50002, N''Chua xac nhan duoc SB0012 la san pham tai nghe.'', 1;
    IF EXISTS (SELECT 1 FROM dbo.SANPHAM WHERE MASP=''SB0012'' AND (MALH IS NULL OR MALH NOT IN (''BP'',''TN'')))
        THROW 50003, N''Loai hien tai khac BP/TN. Can doi chieu thu cong.'', 1;
    UPDATE dbo.SANPHAM SET MALH=''TN'' WHERE MASP=''SB0012'' AND MALH=''BP'';
    DECLARE @Changed int=@@ROWCOUNT;
    COMMIT TRANSACTION;
    SELECT @Changed AS SO_SAN_PHAM_DA_SUA_LOAI;
END TRY
BEGIN CATCH
    IF XACT_STATE()<>0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;

SELECT sp.MASP,sp.TENSP,sp.MALH,lh.TENLH
FROM dbo.SANPHAM sp
LEFT JOIN dbo.LOAIHANG lh ON lh.MALH=sp.MALH
WHERE sp.MASP=''SB0012'';
');

IF EXISTS(SELECT * FROM dbo.SANPHAM EXCEPT SELECT * FROM dbo.AfterCategory) OR EXISTS(SELECT * FROM dbo.AfterCategory EXCEPT SELECT * FROM dbo.SANPHAM) THROW 51014,'Category repeat modified data.',1;
INSERT dbo.VerificationResults VALUES(9,N'Category @Apply=1 repeat','PASS',N'0 row changes; SB0012 remains TN.');
UPDATE dbo.SANPHAM SET MAHANG='DEMO' WHERE MASP='SB0001';
UPDATE dbo.HANG SET TENHANG=N'COLLISION FIXTURE' WHERE MAHANG='B14';
SELECT * INTO dbo.BeforeCollision FROM dbo.SANPHAM;
SELECT * INTO dbo.BeforeCollisionBrands FROM dbo.HANG;
DECLARE @collisionCaught bit=0;
BEGIN TRY
EXEC(N'-- DOI CHIEU HANG THEO 60 ANH NGUOI DUNG CUNG CAP (2026-10-22)
-- Anh minh hoa khong xac thuc hang hoa hay thong so. Chi gan hang doc duoc tren anh.
-- Mac dinh CHI XEM TRUOC. Doi @Apply thanh 1 de ghi sau khi xem ket qua va sao luu DB.
-- Giu nguyen SB0012 va moi san pham da duoc gan hang khac DEMO.
USE [Codex_DAVISYS_Verify_20261023];
SET NOCOUNT ON;
SET XACT_ABORT ON;
DECLARE @Apply bit = 1;

DECLARE @Map TABLE (
    MASP varchar(20) PRIMARY KEY,
    SuggestedCode varchar(3) NOT NULL,
    BrandName nvarchar(100) NOT NULL
);
INSERT INTO @Map (MASP, SuggestedCode, BrandName) VALUES
    (''SB0001'', ''B14'', N''INLAND''),
    (''SB0002'', ''B23'', N''TECKNET''),
    (''SB0006'', ''B12'', N''HP''),
    (''SB0009'', ''B26'', N''VICTSING''),
    (''SB0010'', ''B05'', N''DAREU''),
    (''SB0012'', ''B15'', N''LOGITECH''),
    (''SB0013'', ''B11'', N''HAVIT''),
    (''SB0016'', ''B22'', N''STEELSERIES''),
    (''SB0017'', ''B25'', N''UGREEN''),
    (''SB0026'', ''B06'', N''DEEPCOOL''),
    (''SB0032'', ''B09'', N''FONTASTIC''),
    (''SB0033'', ''B15'', N''LOGITECH''),
    (''SB0034'', ''B10'', N''GENIUS''),
    (''SB0035'', ''B07'', N''DEPSTECH''),
    (''SB0036'', ''B17'', N''MAONO''),
    (''SB0038'', ''B08'', N''FIFINE''),
    (''SB0039'', ''B24'', N''TONOR''),
    (''SB0040'', ''B17'', N''MAONO''),
    (''SB0041'', ''B10'', N''GENIUS''),
    (''SB0042'', ''B01'', N''AMAZON BASICS''),
    (''SB0044'', ''B04'', N''CREATIVE''),
    (''SB0045'', ''B18'', N''MEETION''),
    (''SB0046'', ''B16'', N''MANHATTAN''),
    (''SB0047'', ''B03'', N''AXUMTEK''),
    (''SB0048'', ''B21'', N''STARTECH.COM''),
    (''SB0049'', ''B19'', N''PLUGABLE''),
    (''SB0057'', ''B13'', N''INIU''),
    (''SB0058'', ''B02'', N''ANKER''),
    (''SB0059'', ''B25'', N''UGREEN''),
    (''SB0060'', ''B20'', N''SANDBERG'');

-- Bang xem truoc: khong thay doi du lieu.
SELECT m.MASP, sp.TENSP, sp.MAHANG AS MAHANG_HIEN_TAI,
       h.TENHANG AS HANG_HIEN_TAI, m.BrandName AS HANG_TREN_ANH,
       CASE WHEN sp.MASP IS NULL THEN N''Khong tim thay san pham''
            WHEN m.MASP = ''SB0012'' THEN N''Giu nguyen: da chinh Logitech''
            WHEN sp.MAHANG = ''DEMO'' THEN N''Se cap nhat khi @Apply=1''
            ELSE N''Giu nguyen: hang da duoc chinh'' END AS XU_LY
FROM @Map m
LEFT JOIN dbo.SANPHAM sp ON sp.MASP=m.MASP
LEFT JOIN dbo.HANG h ON h.MAHANG=sp.MAHANG
ORDER BY m.MASP;

IF @Apply=0
BEGIN
    PRINT N''CHI XEM TRUOC: chua ghi du lieu. Doi @Apply=1 de ap dung.'';
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
        WHERE sp.MAHANG=''DEMO'' AND m.MASP<>''SB0012''
          AND NOT EXISTS (SELECT 1 FROM dbo.HANG h
                          WHERE UPPER(LTRIM(RTRIM(h.TENHANG)))=m.BrandName)
          AND UPPER(LTRIM(RTRIM(occupied.TENHANG)))<>m.BrandName
    )
        THROW 50001, N''Ma hang Bxx da duoc dung cho hang khac. Gui loi de doi ma.'', 1;

    INSERT INTO dbo.HANG (MAHANG,TENHANG)
    SELECT DISTINCT m.SuggestedCode,m.BrandName
    FROM @Map m
    JOIN dbo.SANPHAM sp ON sp.MASP=m.MASP
    WHERE sp.MAHANG=''DEMO'' AND m.MASP<>''SB0012''
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
    WHERE sp.MAHANG=''DEMO'' AND sp.MASP<>''SB0012'';

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
');

END TRY BEGIN CATCH IF ERROR_NUMBER()<>50001 THROW; SET @collisionCaught=1; END CATCH;
IF @collisionCaught=0 OR @@TRANCOUNT<>0 OR EXISTS(SELECT * FROM dbo.SANPHAM EXCEPT SELECT * FROM dbo.BeforeCollision) OR EXISTS(SELECT * FROM dbo.BeforeCollision EXCEPT SELECT * FROM dbo.SANPHAM) OR EXISTS(SELECT * FROM dbo.HANG EXCEPT SELECT * FROM dbo.BeforeCollisionBrands) OR EXISTS(SELECT * FROM dbo.BeforeCollisionBrands EXCEPT SELECT * FROM dbo.HANG) THROW 51015,'Collision did not fail/rollback cleanly.',1;
INSERT dbo.VerificationResults VALUES(10,N'Brand collision rollback','PASS',N'Expected error 50001 caught; no persisted changes and no open transaction.');
IF EXISTS(SELECT * FROM DAVISYS.dbo.SANPHAM EXCEPT SELECT * FROM dbo.SourceSANPHAM) OR EXISTS(SELECT * FROM dbo.SourceSANPHAM EXCEPT SELECT * FROM DAVISYS.dbo.SANPHAM) OR EXISTS(SELECT * FROM DAVISYS.dbo.HANG EXCEPT SELECT * FROM dbo.SourceHANG) OR EXISTS(SELECT * FROM dbo.SourceHANG EXCEPT SELECT * FROM DAVISYS.dbo.HANG) OR EXISTS(SELECT * FROM DAVISYS.dbo.LOAIHANG EXCEPT SELECT * FROM dbo.SourceLOAIHANG) OR EXISTS(SELECT * FROM dbo.SourceLOAIHANG EXCEPT SELECT * FROM DAVISYS.dbo.LOAIHANG) THROW 51016,'Source tables changed during verification.',1;
INSERT dbo.VerificationResults VALUES(11,N'Source DAVISYS preserved','PASS',N'Exact bidirectional EXCEPT equality for SANPHAM, HANG and LOAIHANG.');
SELECT * FROM dbo.VerificationResults ORDER BY Step;
PRINT 'VERIFICATION_COMPLETE: 11/11 PASS; source DAVISYS unchanged.';
