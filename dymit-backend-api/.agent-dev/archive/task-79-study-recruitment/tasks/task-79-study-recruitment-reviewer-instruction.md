# TASK-79 — Reviewer Instruction

## Role and prerequisites

Act only as the Reviewer after the Coder and Tester complete. Read the
applicable role rules and both task instructions. Do not modify source, tests,
configuration, `BACKLOG.md`, branches, or commits. Leave a concise English log
(maximum 500 characters) in `.agent-dev/logs/`.

## Review scope

Review TASK-79 Study Recruitment modular-monolith refactoring.

1. Confirm the exact module paths use `adapter/in`, `adapter/out`,
   `application/port/in`, and `application/port/out` (Kotlin-escaped `in` in
   package declarations), with no `inbound`/`outbound` or legacy global
   Study Recruitment sources remaining.
2. Confirm all port contracts own their own DTOs in the respective `dto`
   package; no port or use case imports adapter DTOs, and web/port boundaries
   never expose the domain aggregate.
3. Confirm the Mongo collection and fields, filtering, descending cursor
   ordering, cursor handling, and `size + 1` behavior are unchanged.
4. Compare REST annotations/signature semantics, endpoint, parameters,
   defaults, authorization, status, JSON response shape, and OpenAPI metadata
   with the pre-refactor contract. No interface or observable behavior change
   is acceptable.
5. Inspect test adequacy and run `./gradlew :dymit-backend-api:build` plus
   `git diff --check` where practical. Clearly mark APPROVED or CHANGES
   REQUIRED, including risks and any preexisting failure.
