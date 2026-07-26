# TASK-79 — Tester Instruction

## Role and prerequisites

Act only as the Tester after the Coder has completed the implementation. Read
the applicable role and coding rules, the Coder instruction, and this file.
Modify only `src/test` or test support files; never modify production code,
configuration, `BACKLOG.md`, branches, or commits. Leave a concise English log
(maximum 500 characters) in `.agent-dev/logs/`.

## Objective

Migrate existing Study Recruitment unit tests to the new
`study_recruitment` module packages and add only focused unit coverage needed
to prove the refactor preserved the REST and application behavior.

## Required checks

1. Keep tests under `src/test/.../units`, use Kotest `BehaviorSpec`, and mock
   dependencies. Do not add integration or end-to-end tests.
2. Update package/import references from the legacy global layers to the exact
   `study_recruitment.adapter.\`in\`.web`, `study_recruitment.adapter.out`,
   `study_recruitment.application.port.\`in\``, and
   `study_recruitment.application.port.out` boundaries.
3. Verify the web adapter preserves query conversion, response mapping, and
   cursor/size response behavior without changing the HTTP contract.
4. Verify the application use case maps output-port DTOs to input-port DTOs,
   uses `size + 1`, and preserves the cursor and deletion-filter semantics
   delegated to the output adapter.
5. Verify the Mongo adapter's query behavior with mocks if existing unit-test
   conventions allow it. Do not depend on a live MongoDB.
6. Confirm no test still imports a legacy Study Recruitment production package.
7. Run focused tests and `./gradlew :dymit-backend-api:build` where practical.
   Record exact commands, results, and failures (including whether preexisting)
   in the work log.
