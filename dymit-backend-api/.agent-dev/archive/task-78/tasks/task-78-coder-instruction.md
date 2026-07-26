# TASK-78 — Coder Instruction

## Objective

Refactor the legacy File, Report, and Server Notice code into logical modular-monolith modules without changing their REST APIs, runtime behavior, persistence collection/document contracts, or project dependencies.

## Required target structure

Create exactly these top-level modules under `net.noti_me.dymit.dymit_backend_api`:

- `file/{domain,application,adapter}`
- `report/{domain,application,adapter}`
- `server_notice/{domain,application,adapter}`

Within every module use the exact hexagonal paths below when applicable:

- `application/port/in/web/dto`
- `application/port/out/<dependency>/dto`
- `application/usecase`
- `adapter/in/web`
- `adapter/out/persistence`

Kotlin packages must use the keyword segments exactly with backticks: `.port.\`in\``, `.port.\`out\``, `.adapter.\`in\``, `.adapter.\`out\``. Do not use `inbound`, `outbound`, `adapters`, `controllers`, `ports`, or alternative naming.

## Implementation scope

1. Move all legacy production code and all related tests from the old root-layer packages into their owning module. Remove obsolete duplicate source paths; do not retain compatibility wrappers.
2. Move File REST API/request/response models to `file/application/port/in/web` and `dto`, and its controller to `file/adapter/in/web`. Preserve endpoint paths, HTTP methods/statuses, request field names/validation, multipart behavior, OpenAPI metadata, and response JSON exactly.
3. Move Report and Server Notice REST APIs similarly. Preserve all admin/login authorization behavior and all pagination/cursor behavior exactly.
4. Move each repository interface to the owning module’s `application/port/out/persistence`; move Mongo implementations to `adapter/out/persistence`. Preserve Mongo collection names, queries, indexes, sorting, and serialization behavior.
5. Place all values crossing an application port in the DTO package owned by that exact port. No application port may import DTOs from an adapter, controller, legacy root package, or another domain module.
6. Replace direct File-domain references from Task, Study Schedule, Member, or other modules with File-owned output ports and port-owned DTOs. File itself must use output ports + DTOs for foreign Member/Task/Study Schedule data where it needs those domains. Direct database access inside an output adapter is allowed.
7. Extract `LoginMember`, its resolver, and its authenticated-member value to a neutral common module if they still couple to Member. Change any factory that accepts a member-domain type to an `of(...)` factory taking primitive/direct values. Update all affected controllers/resolver/configuration while preserving behavior.
8. Keep domain entities inside their owning module. Do not add dependencies or change build/configuration files.

## Required verification

- Migrate all existing File, Report, and Server Notice tests to the module-oriented test paths and update imports.
- Run `../gradlew :dymit-backend-api:compileKotlin` and the relevant test suites; report commands/results.
- Leave a concise Korean log under `.agent-dev/logs/` (max 500 characters).

## Guardrails

- Read `.agent-dev/roles/CODER.md`, `.agent-dev/roles/CODING_INSTRUCTION.md`, and `.agent-dev/roles/PROJECT_STRUCTURE.md` first.
- Do not commit, change branches, add dependencies, or alter unrelated behavior.
