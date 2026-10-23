-- MANUAL ONLY: back up the database, then test this on an isolated SQL Server copy.
-- Connect to the intended DAVISYS database. This script preserves all descriptions and rows.
SET XACT_ABORT ON;
IF DB_NAME() <> N'DAVISYS'
    THROW 50001, N'Choose the DAVISYS database explicitly before running this migration.', 1;
IF OBJECT_ID(N'dbo.SANPHAM', N'U') IS NULL OR COL_LENGTH(N'dbo.SANPHAM', N'MOTA') IS NULL
    THROW 50002, N'dbo.SANPHAM.MOTA is missing.', 1;
IF NOT EXISTS (SELECT 1 FROM sys.columns
               WHERE object_id = OBJECT_ID(N'dbo.SANPHAM') AND name = N'MOTA'
                 AND system_type_id = TYPE_ID(N'nvarchar') AND is_nullable = 0)
    THROW 50003, N'Expected a NOT NULL nvarchar MOTA column. Review the existing schema manually.', 1;
BEGIN TRY
    BEGIN TRANSACTION;
    IF COL_LENGTH(N'dbo.SANPHAM', N'MOTA') <> -1 AND COL_LENGTH(N'dbo.SANPHAM', N'MOTA') < 4000
        ALTER TABLE dbo.SANPHAM ALTER COLUMN MOTA nvarchar(2000) NOT NULL;
    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
-- Repeated execution does not shrink a larger column or change any product metadata.
