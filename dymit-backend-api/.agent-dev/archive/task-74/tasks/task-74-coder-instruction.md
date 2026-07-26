# TASK-74 Coder Instruction — Board Modular-Monolith Refactoring

## Objective

Refactor the existing board domain into the top-level `board` logical module without changing any board REST API contract or runtime behavior.

## Mandatory package layout

Move board-owned production code into these exact physical paths. Do not use `inbound` or `outbound` anywhere.

```text
board/
  domain/
  application/
    port/in/
    port/out/
    usecase/
  adapter/in/
  adapter/out/
```

Use Kotlin backticks only where needed: `adapter.\`in\`` and `port.\`in\``. Preserve the physical directory names as `in` and `out`.

## Scope

1. Move all board v1 and v2 controllers, REST API interfaces, request/response DTOs, services, use cases, command/query DTOs, persistence ports, Mongo adapters/repositories, domain models, and events from the legacy board paths into `board`.
2. Put every DTO used in a port signature in that owning port's `dto` package. A port must never import any adapter/controller DTO or class.
3. Keep all endpoints, HTTP methods/statuses, URL patterns, JSON fields, validation, authorization, pagination/cursor semantics, and v1/v2 behavior unchanged.
4. Preserve Mongo collection names and persisted-document compatibility. Retain or deliberately map existing aliases when a package move would otherwise break stored `_class` values; do not require a database migration.
5. Keep `@LoginMember` and its resolver in `common`. If conversion code currently accepts a concrete foreign-module type, replace it with an `of(...)` factory that receives only the required scalar data; do not change HTTP authentication behavior.
6. Remove direct board dependencies in external modules. In particular, replace server-notice's direct use of board `Writer`/`WriterVo` with server-notice-owned value/DTO types. External modules may import only board's published `application.port.\`in\`` contracts and their port-owned DTOs.
7. For board's dependency on study-group/member data, define board-owned outbound ports and DTOs under `board/application/port/out/<foreign-domain>/dto`; implement them under `board/adapter/out/<foreign-domain>`. Direct Mongo access is allowed only inside those adapters and must map to board-owned DTOs.
8. Keep push/feed integration behavior unchanged while removing board domain's direct imports of foreign-domain models. Define board-owned events/contracts or use application outbound ports as appropriate; foreign event payload classes must not leak into board domain.
9. Update all production imports and delete the legacy board package paths after relocation. Do not touch unrelated dirty `build.gradle.kts` changes, configuration, or test files.

## Acceptance criteria

- No production source remains under legacy board paths: `adapters/persistence/mongo/board`, `application/board`, `controllers/board`, `domain/board`, or `ports/persistence/board`.
- Board source uses only `board/adapter/in`, `board/adapter/out`, `board/application/port/in`, and `board/application/port/out`; no `inbound`/`outbound` package or directory exists.
- No `board/application/port/**` source imports `board/adapter/**` or adapter/controller DTOs.
- No non-board production module imports board domain, adapter, repository, service, controller, or DTO internals.
- REST mappings before/after are identical; Mongo collection names remain identical and no DB migration is required.
- `./gradlew :dymit-backend-api:compileKotlin`, `./gradlew :dymit-backend-api:compileTestKotlin`, and `./gradlew :dymit-backend-api:build` pass. Run `git diff --check`.

## Constraints and handoff

Read `roles/CODE.md`, `roles/PROJECT_STRUCTURE.md`, and your role rules first. Do not edit tests, configuration, `BACKLOG.md`, or branches. Leave a Korean work log of at most 500 characters in `.agent-dev/logs/CODER_<branch>_<timestamp>.md` with changed scope, compatibility decision, and verification results.
