# TASK-78 — Tester Instruction

## Objective

After the Coder finishes TASK-78, migrate and extend unit tests for the File, Report, and Server Notice modular-monolith refactor.

## Test scope

1. Move legacy File, Report, and Server Notice unit tests to module-oriented test packages and update imports to the exact `file`, `report`, and `server_notice` packages.
2. Verify File upload/status behavior, including request validation and preserved multipart/controller mappings.
3. Verify Report creation, status transitions, cursor/list behavior, and persistence adapter behavior.
4. Verify Server Notice create/update/delete/get/list behavior, login/admin command mapping, pagination/cursor behavior, and persistence adapter behavior.
5. Add focused architecture/contract tests where useful to prove that application ports use port-owned DTOs and no File/Report/Server Notice application code imports foreign domain entities or adapter DTOs.
6. Verify consumers of File (Task, Member, Study Schedule where affected) retain their existing behavior through the File output-port DTO contract.

## Rules

- Read `.agent-dev/roles/TESTER.md`, `.agent-dev/roles/CODING_INSTRUCTION.md`, and `.agent-dev/roles/PROJECT_STRUCTURE.md` first.
- Use unit tests with mocks/BehaviorSpec only; do not change production code or project configuration.
- Preserve pre-existing test coverage; do not remove tests merely to make the build pass.
- Run the focused suites and `../gradlew :dymit-backend-api:build`; report results.
- Leave a concise Korean log under `.agent-dev/logs/` (max 500 characters).
