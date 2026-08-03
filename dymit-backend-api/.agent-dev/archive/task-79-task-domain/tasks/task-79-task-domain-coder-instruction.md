# TASK-79 Coder Instruction: Task Domain Modularization

## Mission

Refactor the legacy Task domain into the exact modular-monolith package structure specified below. Preserve every public Study Task REST API contract and existing behavior.

Read `AGENTS.md`, `.agent-dev/roles/CODER.md`, `.agent-dev/roles/CODING_INSTRUCTION.md`, and `.agent-dev/roles/PROJECT_STRUCTURE.md` before editing. Do not commit, change branches, or edit backlog/task instructions. Leave a concise English log (500 characters or fewer) in `.agent-dev/logs/`.

## Required target structure

Move the complete legacy Task implementation from `adapters/`, `application/task/`, `controllers/task/`, `domain/task/`, and `ports/persistence/task/` into:

```text
task/
  domain/
  application/
    port/
      in/
        web/
          dto/
      out/
        persistence/
          dto/
        file/
          dto/
    usecase/
  adapter/
    in/
      web/
  adapter/
    out/
      persistence/
        mongo/
      file/
```

Use escaped Kotlin package identifiers exactly as `port.\`in\`` and `adapter.\`in\``. Never use `inbound`, `outbound`, or substitute names. Retain the existing Task file port/adapter inside this same module and relocate it consistently.

## Architecture rules

1. Put REST API interfaces, requests, and responses in `task.application.port.\`in\`.web` and its `dto` package. Put controller implementations only in `task.adapter.\`in\`.web`.
2. Put each use-case interface in `task.application.usecase`; every use case exposes exactly one `execute(...)` function and accepts a command/input DTO.
3. Put all application-boundary commands, inputs, outputs, and persistence transfer DTOs inside the owning Task `application.port.in/out...dto` package. No Task port or use case may import a DTO from an adapter package.
4. Put Mongo repository implementations in `task.adapter.out.persistence.mongo`; define their Task-owned persistence ports in `task.application.port.out.persistence`.
5. Keep Task aggregate/entities/enums/domain events solely in `task.domain`. Preserve existing Spring/Mongo mappings, collection names, indexes, events, and business rules.
6. For Study Group and Study Schedule collaboration, depend only on their server-to-server port contracts and DTOs. Do not import their domain entities. If an existing external interaction lacks an appropriate DTO/port boundary, introduce a Task-owned output port plus DTO and implement its adapter against the database; do not expose foreign entities.
7. `LoginMember` and `MemberInfo` are already common components. Continue using them from `common`; do not relocate them or create a Task-specific authentication type. Any old conversion that requires a foreign domain class must become direct scalar/DTO construction (`of`/constructor style).
8. Remove obsolete legacy Task production packages and update every production reference. Do not leave compatibility wrappers in legacy packages.

## API and behavior invariants

Keep all paths, HTTP methods, parameters, JSON field names, validation, authentication/authorization, status codes, Swagger documentation, response wrappers, pagination, error behavior, and semantics unchanged, including:

- `/api/v1/study-groups/{groupId}/tasks` CRUD/detail/list;
- `/api/v1/study-groups/{groupId}/tasks/{taskId}/submissions` create/update/delete/withdraw/list/detail;
- submission-comment endpoints;
- `/api/v1/tasks/{taskId}/assignees`;
- task creation/update/deletion events, assignee synchronization, schedule synchronization/cancellation, deadline normalization, file attachment lifecycle, and notification preparation/publication.

Preserve Mongo query semantics, ordering, deletion rules, membership/leader authorization, task/submission state transitions, and `size + 1` behavior where it exists.

## Validation

Run `../gradlew :dymit-backend-api:compileKotlin` and `../gradlew :dymit-backend-api:assemble`. Report failures with the first relevant cause; do not modify tests yourself.
