# TASK-74 Reviewer Instruction — Board Modular-Monolith Refactoring

## Objective

Review the completed TASK-74 coder and tester outputs. Do not edit source, tests, configuration, backlog, or branches.

## Required review checklist

1. Verify every board production file is in the top-level `board` module and the exact physical package names are `adapter/in`, `adapter/out`, `application/port/in`, and `application/port/out`. Reject any `inbound` or `outbound` rename.
2. Verify every DTO in a board port signature belongs to that port's own `dto` package; reject port imports from adapter/controller packages.
3. Search for direct cross-module imports. Board must not use foreign domain entities in its domain/application contracts; foreign modules must not import board internals. Published `board.application.port.\`in\`` contracts and their DTOs are the only allowed board API for other modules.
4. Compare board REST mappings, HTTP methods/statuses, parameters, JSON request/response fields, validation, and `@LoginMember` behavior with the pre-refactoring code. Report any difference as blocking.
5. Verify Mongo collection names and persisted class compatibility; confirm whether a migration is required. Existing data must remain readable without one.
6. Review v1/v2 behavior, board-created push/feed/comment side effects, and server-notice writer payload compatibility.
7. Review test placement and isolation: `units/board`, BehaviorSpec, mocks, no integration tests.
8. Run or inspect `./gradlew :dymit-backend-api:build` and `git diff --check`; include results and an explicit APPROVED or CHANGES REQUESTED conclusion.

## Handoff

Read `roles/PROJECT_STRUCTURE.md` and your role rules first. Leave a Korean review log of at most 500 characters in `.agent-dev/logs/REVIEWER_<branch>_<timestamp>.md` with conclusion, risks, migration decision, and verification results.
