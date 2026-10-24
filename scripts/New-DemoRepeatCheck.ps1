param(
 [ValidatePattern('^DAVISYS_DEMO_REPEAT_[A-Z0-9_]+$')]
 [string]$Database=('DAVISYS_DEMO_REPEAT_'+(Get-Date -Format 'yyyyMMdd_HHmmss')),
 [string]$OutputPath=(Join-Path $env:TEMP 'davisy-demo-repeat-check.sql')
)
# Generates SQL only. Execute the whole output in one SSMS connection.
# Keep catalog scripts as top-level GO batches; EXEC would hide the temp-table lifetime bug.
$ErrorActionPreference='Stop'
$repo=Split-Path -Parent $PSScriptRoot
$ready=Get-Content (Join-Path $repo 'database/demo/DAVISYS_Demo_Ready.sql') -Raw -Encoding UTF8
$update=Get-Content (Join-Path $repo 'database/demo/03_apply_catalog.sql') -Raw -Encoding UTF8
$validate=Get-Content (Join-Path $repo 'database/demo/04_validate_catalog.sql') -Raw -Encoding UTF8
$ready=$ready.Replace("N'DAVISYS_DEMO_SB60'","N'$Database'")
$update=$update.Replace('USE [DAVISYS_DEMO_SB60];',"USE [$Database];")
$validate=$validate.Replace('USE [DAVISYS_DEMO_SB60];',"USE [$Database];")
$apply=$update.Replace('DECLARE @Apply bit=0;','DECLARE @Apply bit=1;')
$sql=[Collections.Generic.List[string]]::new()
$sql.Add($ready)
$sql.Add('GO')
$sql.Add(@"
USE [$Database]; SET NOCOUNT ON;
CREATE TABLE #RepeatHistory(Stage int PRIMARY KEY,Scenario nvarchar(50),SessionId int,ProductsId int,CategoriesId int,BrandsId int,Result varchar(4));
SELECT * INTO #RepeatSourceSnapshot FROM DAVISYS.dbo.SANPHAM;
"@)
$sql.Add('GO')
$scenarios=@('preview','apply','apply again','validate','validate again')
$batches=@($update,$apply,$apply,$validate,$validate)
for($i=0;$i -lt $batches.Count;$i++) {
 $stage=$i+1; $scenario=$scenarios[$i]
 $sql.Add("PRINT 'Stage $stage - $scenario';")
 $sql.Add($batches[$i]); $sql.Add('GO')
 $sql.Add(@"
IF (SELECT COUNT(*) FROM #DemoProducts)<>60 OR (SELECT COUNT(*) FROM #DemoCategories)<>12 OR (SELECT COUNT(*) FROM #DemoBrands)<>27 THROW 51601,'Temp tables not fully populated.',1;
IF EXISTS(SELECT * FROM #RepeatHistory WHERE SessionId<>@@SPID) THROW 51602,'Connection changed.',1;
IF EXISTS(SELECT * FROM #RepeatHistory WHERE Stage=$stage-1 AND (ProductsId=OBJECT_ID('tempdb..#DemoProducts') OR CategoriesId=OBJECT_ID('tempdb..#DemoCategories') OR BrandsId=OBJECT_ID('tempdb..#DemoBrands'))) THROW 51603,'A temp table was reused instead of recreated.',1;
IF EXISTS(SELECT * FROM #DemoProducts EXCEPT SELECT * FROM dbo.SANPHAM) OR EXISTS(SELECT * FROM dbo.SANPHAM EXCEPT SELECT * FROM #DemoProducts) THROW 51604,'Catalog data mismatch.',1;
INSERT #RepeatHistory VALUES($stage,N'$scenario',@@SPID,OBJECT_ID('tempdb..#DemoProducts'),OBJECT_ID('tempdb..#DemoCategories'),OBJECT_ID('tempdb..#DemoBrands'),'PASS');
"@)
 $sql.Add('GO')
}
$sql.Add(@'
IF (SELECT COUNT(*) FROM #RepeatHistory)<>5 THROW 51605,'Not all checkpoints passed.',1;
IF EXISTS(SELECT * FROM DAVISYS.dbo.SANPHAM EXCEPT SELECT * FROM #RepeatSourceSnapshot) OR EXISTS(SELECT * FROM #RepeatSourceSnapshot EXCEPT SELECT * FROM DAVISYS.dbo.SANPHAM) THROW 51606,'Source DAVISYS changed.',1;
SELECT * FROM #RepeatHistory ORDER BY Stage;
SELECT COUNT(*) AS PassedStages,COUNT(DISTINCT SessionId) AS Connections,N'UNCHANGED' AS SourceDAVISYS FROM #RepeatHistory;
'@)
[IO.File]::WriteAllText([IO.Path]::GetFullPath($OutputPath),($sql -join [Environment]::NewLine),[Text.UTF8Encoding]::new($false))
Write-Output "Generated $OutputPath for $Database. Run the whole file in one SSMS connection."
