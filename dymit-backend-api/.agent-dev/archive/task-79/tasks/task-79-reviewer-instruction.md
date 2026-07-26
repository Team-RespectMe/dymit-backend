# TASK-79 — Reviewer Instruction

## Objective

Review TASK-79 after Coder and Tester completion. Do not modify code or tests.

## Required review checks

1. Confirm the exact top-level Admin/Reminder layout and Kotlin keyword package syntax: `adapter.\`in\``, `adapter.out`, `application.port.\`in\``, `application.port.out`, `application.usecase`. Reject `inbound`/`outbound` substitutes.
2. Confirm all Admin and Reminder legacy production sources were migrated without compatibility wrappers, while general personal push notification code remains in its module.
3. Confirm every cross-domain Admin/Reminder dependency goes through a caller-owned, explicitly module-prefixed output port and DTO defined in that port’s `dto` package. Reject foreign domain/application/adapter DTO imports and boundary DTOs defined in adapters or `application/usecase/dto`.
4. Confirm the admin push REST contract is unchanged: path, HTTP 201, request JSON, authentication annotation, SpringDoc metadata, and payload semantics.
5. Confirm Quartz bean names, job identities, schedules/time zone, reminder time windows, batching, deduplication, and push/feed payload semantics are unchanged.
6. Inspect changed tests for BehaviorSpec/mocked-dependency-only unit tests and required coverage.
7. Independently run `../gradlew :dymit-backend-api:build`, `git diff --check`, and scans for stale legacy production imports/classes and forbidden `inbound`/`outbound` names.

Report APPROVED or actionable findings ordered P0/P1/P2. Include API/DB migration impact. Read `.agent-dev/roles/REVIEWER.md` and `PROJECT_STRUCTURE.md` first. Do not modify source/tests/config/backlog/branches/commits. Write a concise (<=500 chars) English log to `.agent-dev/logs/REVIEWER_<branch>_<timestamp>.md`.
