# TASK-77 Tester Instruction

## Role and prerequisite

Read `roles/TESTER.md`, `roles/CODING_INSTRUCTION.md`, and `roles/PROJECT_STRUCTURE.md`. Communicate in English. Write only unit tests under `src/test/.../units` using Kotest `BehaviorSpec` and mocks; do not modify production code, configuration, `BACKLOG.md`, branches, commits, or external infrastructure. Leave a Korean `.agent-dev/logs/TESTER_<branch>_<timestamp>.md` log of at most 500 characters.

## Dependency

Begin only after the Coder reports TASK-77 implementation complete. Inspect the final code; do not assume package names other than the mandated Feed module structure.

## Test scope

Add focused unit coverage for:

1. Feed in-port/application behavior: member-feed cursor/query history behavior, ownership checks for delete/read, and unchanged response mapping where testable with mocks.
2. Personal common feed event consumed by the Feed event adapter: verify one Feed creation with preserved recipient, messages, event name, icon, and associated resources.
3. Group common feed event consumed by the Feed event adapter: verify group creation and excluded-member propagation.
4. Existing broadcast/special producer semantics that could regress during migration: preserve recipient count/exclusions and message/resource/event metadata.
5. The unchanged web API contract at controller level: exact route mapping/status and `@LoginMember`-to-command conversion without exposing `MemberInfo` to application code.

Use Feed-owned port DTOs and mocks. Do not use real Mongo/Firebase/network. Run the focused tests and `../gradlew :dymit-backend-api:build`. Report failures with reproducible commands and affected files; do not repair production code.
