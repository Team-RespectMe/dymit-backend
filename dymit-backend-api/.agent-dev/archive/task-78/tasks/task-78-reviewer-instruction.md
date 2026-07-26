# TASK-78 — Reviewer Instruction

## Objective

Review TASK-78 after Coder and Tester completion. Do not modify code or tests.

## Review checklist

1. Confirm the exact package convention: `file`, `report`, `server_notice`; `application/port/in`, `application/port/out`, `adapter/in`, and `adapter/out` (backticked Kotlin keyword package segments). Reject `inbound`, `outbound`, `adapters`, root `controllers`, or legacy duplicate modules.
2. Confirm every DTO crossing an application port is defined under that port’s own `dto` package. Reject application ports importing DTOs from adapters/controllers/legacy packages or foreign domain modules.
3. Confirm foreign domain access occurs through explicit output ports and port-owned DTOs; output adapters may access the database directly.
4. Confirm all File, Report, and Server Notice REST interfaces and behavior remain unchanged: mappings, methods/statuses, request/response JSON, validation, auth, multipart handling, OpenAPI annotations, and cursor pagination.
5. Confirm Mongo persistence behavior remains unchanged: collection/document shape, queries, sort order, and serialization.
6. Confirm LoginMember/common authenticated-member extraction does not couple common code to Member, and `of(...)` receives direct values rather than foreign member types.
7. Confirm tests were migrated/added, relevant build results are green, and `git diff --check` passes.

Report `APPROVED` or `CHANGES REQUESTED` with prioritized, actionable findings. Leave a concise Korean log under `.agent-dev/logs/` (max 500 characters).
