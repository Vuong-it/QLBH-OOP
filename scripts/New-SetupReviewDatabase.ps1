param(
    [string]$Server='localhost',
    [ValidatePattern('^DAVISYS_SETUP_REVIEW_[A-Z0-9_]+$')]
    [string]$Database=('DAVISYS_SETUP_REVIEW_'+(Get-Date -Format 'yyyyMMdd_HHmmss'))
)
# Creates a new disposable schema only; never overwrites an existing database.
$ErrorActionPreference='Stop'
$repo=Split-Path -Parent $PSScriptRoot
$db=$Database
$c=[System.Data.SqlClient.SqlConnection]::new(('Server={0};Database=master;Integrated Security=True;TrustServerCertificate=True' -f $Server));$c.Open()
function Run($sql){$cmd=$c.CreateCommand();$cmd.CommandText=$sql;$cmd.CommandTimeout=60;try{[void]$cmd.ExecuteNonQuery()}finally{$cmd.Dispose()}}
$cmd=$c.CreateCommand();$cmd.CommandText="SELECT COUNT(*) FROM sys.databases WHERE name='$db'";if([int]$cmd.ExecuteScalar() -ne 0){throw 'Review database already exists; never overwrite'};$cmd.Dispose()
Run "CREATE DATABASE [$db]";$c.ChangeDatabase($db)
$source=Get-Content (Join-Path $repo 'database/DAVISYS.sql') -Raw -Encoding UTF8
$schema=$source.Substring($source.IndexOf('CREATE TABLE LOAIHANG'));$schema=$schema.Substring(0,$schema.IndexOf('INSERT INTO LOAIHANG'))
if($schema -match '(?i)USE\s|CREATE DATABASE|DROP DATABASE'){throw 'Unsafe schema batch'}
Run $schema
$procStart=[regex]::Match($source,'(?im)^GO\s*\r?\nCREATE PROC').Index
if($procStart -le 0){throw 'Procedure section not found'}
foreach($batch in [regex]::Split($source.Substring($procStart),'(?im)^GO\s*\r?$')){if($batch.Trim()){Run $batch}}
$migration=Get-Content (Join-Path $repo 'database/migrations/001_setup_description_capacity.sql') -Raw -Encoding UTF8
Run ($migration.Replace("N'DAVISYS'","N'$db'"))
Write-Output "Created isolated $db with repository schema/constraints/procedures and migration; no real account/customer/cart data copied"
$c.Dispose()
