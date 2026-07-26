# BaseAggregateRoot/ProfileImageType migration — Tester Instruction

After the Coder moves `BaseAggregateRoot` to `common` and removes the shared `domain.ProfileImageType`, update unit tests only.

Replace every old `domain.BaseAggregateRoot` or `domain.ProfileImageType` test import/reference with the correct owner-local type. Use each module’s enum in its own fixtures. At cross-module boundaries, construct the caller-owned DTO and enum, never pass one module’s enum as another module’s type. Update legacy Task/controller test fixtures to use the Task-owned profile DTO/type where production changed that boundary.

Do not change production code/config/backlog/branches/commits. Keep tests deterministic and retain their existing assertions/coverage. Run focused affected tests, `../gradlew :dymit-backend-api:build`, `git diff --check`, and scans proving no old `domain.BaseAggregateRoot` or `domain.ProfileImageType` import/reference remains in `src/test`. Write a <=500-character English log in `.agent-dev/logs/TESTER_<branch>_<timestamp>.md`.
