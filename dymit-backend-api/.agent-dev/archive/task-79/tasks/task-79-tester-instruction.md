# TASK-79 — Tester Instruction

## Objective

After the Coder finishes TASK-79, migrate/add deterministic unit tests for the Admin and Reminder modular-monolith refactor. Do not modify production code.

## Required coverage

1. Migrate every Admin/Reminder-related existing unit test import/package to the new top-level modules; no test may depend on legacy `application.admin`, `application.reminder`, or Admin-specific `push_notification` classes.
2. Admin daily member-status use case: verify KST-to-UTC conversion, its member-status output port invocation, and mapping into Admin-owned DTOs. Mock dependencies.
3. Admin push REST adapter/use case: verify the unchanged endpoint mapping/status/request-to-command behavior, `@LoginMember`, and one personal-push dispatch per requested member with the existing title/body/event payload.
4. Reminder daily and hourly jobs: mock only Reminder-owned ports/event publisher/logger. Cover time windows, no-participant skip, missing-group skip, unique participant ids, event publication, and batch cursor behavior as practical using `BehaviorSpec`.
5. Reminder event payloads: preserve exact push and feed event names, text/data/resource values.

## Architecture checks

- Test only the exact packages `admin.adapter.\`in\``, `admin.application.port.\`in\``, `admin.application.port.out`, `admin.application.usecase`, `reminder.application.port.out`, `reminder.application.usecase`, `reminder.adapter.out`, and `reminder.domain` where they exist.
- Test DTOs crossing ports from their owner’s `application/port/.../dto` package. Do not introduce or import adapter DTOs or `application/usecase/dto` boundary DTOs.
- Do not use `inbound` or `outbound` package names.

Use unit tests only under `src/test/.../units`, Kotest `BehaviorSpec`, and mocks for all dependencies. Read `.agent-dev/roles/TESTER.md` and `CODING_INSTRUCTION.md` first. Run focused tests and `../gradlew :dymit-backend-api:build`; run `git diff --check`. Do not change source/config/backlog/branches/commits. Write a concise (<=500 chars) English log to `.agent-dev/logs/TESTER_<branch>_<timestamp>.md`.
