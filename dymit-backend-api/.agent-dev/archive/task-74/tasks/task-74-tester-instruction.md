# TASK-74 Tester Instruction — Board Modular-Monolith Refactoring

## Objective

After the coder completes TASK-74, relocate/update board tests and add only the smallest missing unit coverage needed to prove that behavior and API mapping remain unchanged.

## Test scope

1. Move existing board unit tests under `src/test/.../units/board/...` so their packages mirror the top-level board module. Do not leave tests referring to legacy board packages.
2. Use Kotest `BehaviorSpec` and mocks for every dependency. Do not add integration or end-to-end tests.
3. Preserve and verify representative v1 and v2 board use-case behavior: post/category commands, authorization/permission decisions, cursor/list query mapping, and comment behavior.
4. Verify REST-adapter request validation and response field mapping only; do not test Spring integration.
5. Add focused tests for board-owned outbound adapters/DTO mapping where foreign study-group/member data is read, and verify no foreign entity is exposed through board port signatures.
6. Update server-notice unit tests if the removal of board `Writer`/`WriterVo` requires package or type changes, while preserving its serialized response behavior.

## Acceptance criteria

- All board tests compile from `units/board` and use the relocated module packages.
- Tests do not import legacy board paths or use integration infrastructure.
- Unit tests are deterministic and mock dependencies.
- `./gradlew :dymit-backend-api:test` (or the narrowest supported board test task) and `./gradlew :dymit-backend-api:build` pass.
- Do not alter production code, configuration, `BACKLOG.md`, or branches.

## Handoff

Read `roles/CODE.md`, `roles/PROJECT_STRUCTURE.md`, and your role rules first. Leave a Korean work log of at most 500 characters in `.agent-dev/logs/TESTER_<branch>_<timestamp>.md` including test files, command results, and any blocker.
