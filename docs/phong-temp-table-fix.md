# PR #6: repeat execution in one SSMS session

Verified 2026-10-24 on SQL Server 17.0.1000.7 (SSMS), connection SPID 55.

Both scripts 03 and 04 now drop #DemoProducts, #DemoCategories and #DemoBrands before CREATE TABLE, including after a preview RETURN left them alive.

| Stage | Scenario | SPID | Result |
|---|---|---|---|
| 1 | preview | 55 | PASS |
| 2 | apply | 55 | PASS |
| 3 | apply again | 55 | PASS |
| 4 | validate | 55 | PASS |
| 5 | validate again | 55 | PASS |

SSMS: Query executed successfully. PassedStages=5, Connections=1, SourceDAVISYS=UNCHANGED. Both apply runs reported InsertedProducts=0, UpdatedProducts=0. Both validator runs returned PASS, Products=60, Categories=12, RandomAssignedProducts=30, OutOfStock=3. All three temporary object IDs changed between consecutive stages.

The test created DAVISYS_DEMO_REPEAT_20261024_TEMP_FIX, ran the scripts as top-level GO batches on the same connection (no EXEC wrapper), checked all catalog columns against the 60 expected rows after each stage, and compared source DAVISYS.dbo.SANPHAM before/after. The test database remains available for inspection.

To reproduce from the repository root:

```powershell
./scripts/New-DemoRepeatCheck.ps1 -OutputPath ./repeat-check.sql
```

Open the generated file in SSMS connected to the local server and execute the whole file once. It requires permission to create a new demo database and read the existing DAVISYS product table. Use a new -Database name when repeating the whole test; the builder deliberately refuses to overwrite an existing database. The generator only writes SQL; it does not execute it. Its default database name includes a timestamp.

Login/Home end-to-end was not rerun for this SQL-only fix.
