# TASK-79 Reviewer Instruction: Task Domain Modularization

## Mission

Independently review the completed Coder and Tester changes for TASK-79. Do not modify code or tests.

Read `AGENTS.md`, `.agent-dev/roles/REVIEWER.md`, `.agent-dev/roles/PROJECT_STRUCTURE.md`, both preceding agent logs, and these task instructions. Do not commit, change branches, edit backlog/task instructions, or approve without evidence. Leave a concise English log (500 characters or fewer) in `.agent-dev/logs/`.

## Required review checks

1. Verify the complete Task implementation is under the exact `task/domain`, `task/application/port/\`in\``, `task/application/port/\`out\``, `task/application/usecase`, `task/adapter/\`in\``, and `task/adapter/\`out\`` structure. Reject `inbound`/`outbound` naming and legacy production wrappers.
2. Verify port/use-case DTOs are defined under their owning Task port `dto` packages and no port/use case imports adapter DTOs.
3. Verify Task has no direct external-domain entity imports; Study Group/Study Schedule collaboration is through port DTOs. Verify common `LoginMember`/`MemberInfo` usage remains valid and no foreign-domain conversion is introduced.
4. Compare controller mappings, parameter names, JSON request/response shapes, validation, auth, status codes, Swagger annotations, errors, response wrappers, pagination, and semantics against the prior implementation. The public Study Task API must be unchanged.
5. Verify Mongo mappings/query/order/deletion behavior, files, events, notification handling, schedule synchronization, and authorization/business rules have not regressed.
6. Confirm legacy Task source/test references were removed, test migration is meaningful, `git diff --check` has no new Task-related whitespace errors, and `../gradlew :dymit-backend-api:build` passes.

Return `APPROVED` only if all checks pass; otherwise return `CHANGES REQUESTED` with precise file paths and required corrections.
