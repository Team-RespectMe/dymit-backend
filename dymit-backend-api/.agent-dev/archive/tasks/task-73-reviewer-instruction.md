# TASK-73 Reviewer Instruction

Status: Pending approval. Start only after Coder and Tester work is complete and the PM assigns this task.

## Review checklist

1. Verify the exact physical package layout: `study_schedule/adapter/in`, `adapter/out`, `application/port/in`, and `application/port/out`; Kotlin uses backticks only for package declarations/imports. Reject `inbound`/`outbound` substitutions.
2. Verify port ownership: every port-signature DTO is inside its owning port `dto` package; no port imports adapter classes/DTOs; cross-module access uses only the callee module's published `application.port.\`in\`` contract and DTOs.
3. Scan for direct foreign entity/repository/service/controller-DTO imports inside `study_schedule`, and direct study-schedule internal imports outside it. Confirm any database access to foreign data is isolated to a schedule adapter and mapped to schedule-owned DTOs.
4. Compare API mappings and request/response DTO fields to ensure all study-schedule endpoints, paths, HTTP methods, validation, status behavior, and authorization are unchanged.
5. Confirm event/task/reminder behavior is represented by explicit contracts rather than direct schedule domain-event dependencies.
6. Review test sufficiency, including mocked web, port, Mongo mapping, and event-contract coverage. Run `./gradlew :dymit-backend-api:build`, static package/import scans, and `git diff --check`.
7. Document affected files, API/DB migration impact, residual risks, and an explicit `APPROVED` or `BLOCKED` result. Do not modify code/tests/configuration.

Leave a concise Korean log at `.agent-dev/logs/REVIEWER_<BRANCH>_<TIMESTAMP>.md` (500 characters maximum).
