# TASK-73 Coder Instruction

Status: Pending approval. Do not start until the PM explicitly assigns this task.

## Goal

Refactor the study schedule domain into the top-level logical module `study_schedule` while preserving every REST API contract and runtime behavior. Do not add Gradle dependencies or change project configuration.

## Required structure

Move all study-schedule production code under:

```text
study_schedule/
  domain/
  application/
    port/in/web/dto/
    port/in/server_to_server/dto/
    port/out/<foreign-domain>/dto/
    usecase/
  adapter/in/web/
  adapter/out/<persistence-or-foreign-domain>/
```

Use the physical package segments exactly `adapter/in`, `adapter/out`, `application/port/in`, and `application/port/out`. Kotlin package declarations/imports must escape only the keyword segment: `adapter.\`in\`` and `port.\`in\``. Never use `inbound` or `outbound` as a replacement.

## Implementation scope

1. Move the existing study-schedule domain models/events, application services and commands, REST API interfaces, controllers, persistence ports, Mongo adapters, and owned tests into `study_schedule` according to the required structure. Update imports and Spring component scanning without changing behavior.
2. Keep all existing endpoints, HTTP methods, paths, request/response JSON fields, validation, status codes, authorization, collection names, and persistence behavior unchanged. Do not introduce a database migration.
3. Put web request/response DTOs and server-to-server DTOs in the owning `study_schedule.application.port.\`in\`` package. Put each outbound port's DTOs in that exact outbound port's own `dto` package. A port must never import an adapter DTO/class.
4. Remove direct references from `study_schedule` to foreign domain entities, repositories, services, and controller DTOs. Introduce minimal study-schedule-owned outbound ports and DTO projections for member, study-group, file, user-feed, board-writer, or other foreign data required by schedule business logic. Adapters may access the same database directly, but must map results to the port-owned DTOs.
5. Remove external modules' direct references to `study_schedule` entities, repositories, services, or domain events. Expose narrow contracts through `study_schedule.application.port.\`in\`. For task/reminder/event-driven consumers, replace direct domain-event consumption with port contracts and port-owned event DTOs, preserving publication/handling behavior. For a caller that needs another module's capability, depend only on that module's `application.port.\`in\`` contract and its DTOs.
6. Preserve shared authentication behavior. `@LoginMember`, resolver, and security principal remain in `common`; if a conversion factory currently requires a module-specific type, replace it with an `of(...)` factory that accepts only the required raw values. Do not move common authentication code into `study_schedule`.
7. Update all affected production imports and tests. Remove obsolete old-layout files/packages after successful relocation. Do not touch unrelated user changes, especially `build.gradle.kts`.

## Acceptance criteria

- No production `study_schedule` package imports foreign domain entities/repositories/services/controller DTOs directly.
- No non-`study_schedule` module imports legacy or new study-schedule internals except the published `study_schedule.application.port.\`in\`` contracts and their DTOs.
- No `inbound`/`outbound` package exists; required `in`/`out` paths exist exactly.
- No port imports an adapter class or adapter-owned DTO.
- Existing REST contract and database schema are unchanged.
- `./gradlew :dymit-backend-api:build` succeeds and `git diff --check` is clean.
- Leave a concise Korean log at `.agent-dev/logs/CODER_<BRANCH>_<TIMESTAMP>.md` (500 characters maximum).
