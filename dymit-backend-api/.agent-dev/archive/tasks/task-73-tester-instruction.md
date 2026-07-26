# TASK-73 Tester Instruction

Status: Pending approval. Start only after the Coder completes and the PM assigns this task.

## Objective

Write only unit tests for the TASK-73 study-schedule modular-monolith refactor. Use Kotest `BehaviorSpec`, keep tests under `src/test/.../units/study_schedule`, and mock every dependency.

## Required coverage

1. Controller/web adapter tests: verify `@LoginMember` input forwarding, request-to-command conversion, validation-facing request/response mapping, and unchanged response shapes for schedule, participant, comment, and attachment endpoints.
2. Application tests: verify that the schedule use cases call only their declared ports and preserve existing authorization, participant, comment, attachment, cancellation, and role behavior. Mock port DTOs rather than foreign entities.
3. Outbound adapter tests: mock `MongoTemplate`/foreign access and verify query criteria, collection names, and mappings into the DTO owned by the outbound port. Do not use a real database.
4. Server-to-server/event contract tests: verify task and reminder consumers receive equivalent port-owned event DTO data and no longer require schedule domain-event classes.
5. Add regression tests for every refactor bug found. Do not alter production code or configuration.

## Verification and report

- Run focused unit tests and `./gradlew :dymit-backend-api:build`.
- Report failures with reproducible commands and affected files to the PM/Coder; do not change production code to fix them.
- Leave a concise Korean log at `.agent-dev/logs/TESTER_<BRANCH>_<TIMESTAMP>.md` (500 characters maximum).
