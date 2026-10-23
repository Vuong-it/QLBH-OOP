# SQL Server verification for Phong PR #4 and migration in PR #1

## Execution and scope

Executed through the SSMS session authenticated by Phong, on Microsoft SQL Server 2025 Standard Developer Edition 17.0.1000.7. No password was read, saved, or committed.

Created isolated database `Codex_DAVISYS_Verify_20261023`. Copied only SANPHAM (71 rows, including 60 SB demo products), HANG and LOAIHANG from DAVISYS. This is a product-data test fixture, not a full application database clone. Added primary keys and foreign keys for product brand/category checks; application triggers, other constraints, customer/cart/invoice tables were not cloned.

Test inputs are the scripts in PR #4 at 2081bfc. For isolation, the test harness substitutes only USE [DAVISYS] and the migration DB_NAME guard with the sandbox name; @Apply changes to 1 only for apply/repeat cases. Original shipping scripts remain unchanged. This is actual SQL Server execution, not a mocked JDBC result, but not a claim of a full application end-to-end run or SQL Server 2022 execution.

## Observed results

| Step | Scenario | Result | Evidence |
|---|---|---|---|
| 1 | Migration from nvarchar(100) to nvarchar(2000) | PASS | All 71 fixture rows preserved exactly; NOT NULL retained |
| 2 | Migration repeat | PASS | Schema and contents unchanged |
| 3 | Existing nvarchar(max) | PASS | Larger column not shrunk |
| 4 | Brand script @Apply=0 | PASS | Neither product nor brand rows changed |
| 5 | Brand script @Apply=1 | PASS | Exactly 28 eligible mapped products updated; SB0006 HP and SB0012 Logitech preserved; missing INLAND brand inserted |
| 6 | Brand script @Apply=1 again | PASS | Zero product/brand changes |
| 7 | Category script @Apply=0 | PASS | SB0012 stayed BP in fixture |
| 8 | Category script @Apply=1 | PASS | Exactly one row changed: SB0012 BP to TN |
| 9 | Category script @Apply=1 again | PASS | Zero row changes, SB0012 remains TN |
| 10 | Occupied brand code | PASS | Expected error 50001; no persisted product/brand changes and @@TRANCOUNT=0 |
| 11 | Original DAVISYS data | PASS | Bidirectional EXCEPT equality of SANPHAM/HANG/LOAIHANG against initial snapshots |

The final independent query returned TotalChecks=11, Passed=11. Source DAVISYS still had 71 products and MOTA nvarchar(2000) NOT NULL.

Fixture preparation deliberately shortened descriptions to 100 characters only in the sandbox to exercise widening, then restored full source descriptions. It reset eligible mapped brands to DEMO, retained HP/Logitech, freed the INLAND suggested code to exercise insertion, and set only fixture SB0012 to BP. Therefore 28 updates is the expected fixture count, not a claim that 28 production rows needed repair. The existing source already had corrected brands/category.

## Reproduction

Run database/verification/verify_demo_isolated.sql from an authenticated SSMS connection. The fixed sandbox name must be unused; use a fresh isolated name consistently to rerun. The harness intentionally does not drop databases or modify the source database. The sandbox and diagnostic snapshots/results are retained for reviewer inspection. The final collision test deliberately leaves collision fixture data in that sandbox; it is not intended as a ready application database.

The standalone SQL assertions stop on mismatches. Verify the final 11/11 table and query status; do not infer success merely from a created database.

## Remaining integration scope

This closes the missing preview/apply/repeat and migration evidence for Phong's PR. Earlier compilation/11 CSDL JUnit tests remain valid because shipping Java/SQL code has not changed.

PR #1 was compiled and its 58 JUnit tests passed in the earlier review. Real SetupCartDAO concurrency/middle-row failure, checkout/invoice behavior and Home/Login end-to-end remain unexecuted in this verification and remain with Vuong per the agreed division of work. The tested migration is identical to PR #1's migration apart from the test-only database guard substitution. No merge or blanket integration approval is implied.
