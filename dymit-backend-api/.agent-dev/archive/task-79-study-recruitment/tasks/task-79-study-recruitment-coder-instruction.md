# TASK-79 — Coder Instruction

## Role and reading order

Act only as the Coder. Read `.agent-dev/roles/CODING_INSTRUCTION.md`,
`.agent-dev/roles/PROJECT_STRUCTURE.md`, this instruction, and all applicable
role constraints before editing. Do not edit tests, build configuration,
`BACKLOG.md`, branches, or commits. Leave a concise English log (maximum 500
characters) in `.agent-dev/logs/`.

## Objective

Refactor the Study Recruitment feature into the modular-monolith structure
without changing its REST interface or observable behavior. No dependency or
database migration may be introduced.

## Required target structure

Move the feature from its legacy top-level packages into exactly these package
families under `net.noti_me.dymit.dymit_backend_api.study_recruitment`:

- `domain`
- `application/port/in` and `application/port/in/dto`
- `application/port/out` and `application/port/out/dto`
- `application`
- `adapter/in/web`
- `adapter/out/persistence/mongo`

Use Kotlin escaped package segments exactly as `adapter.\`in\``,
`adapter.out`, `port.\`in\``, and `port.out`. Do not use `inbound`,
`outbound`, plural `adapters`, legacy global `application`, global
`controllers`, global `ports`, or alternate names.

## Architecture requirements

1. Place the aggregate in `study_recruitment.domain` and preserve its MongoDB
   collection, persisted field names, and existing behavior.
2. Place input and output port interfaces in their respective `port/in` and
   `port/out` packages. Every port owns the DTOs it exposes in that port's
   `dto` package. An adapter DTO must never be imported by a port or use case.
3. Expose exactly one `execute(...)` method per use-case interface. Convert
   web request data to an input command at the adapter boundary and return an
   input-port DTO to the web adapter. Do not expose the aggregate through the
   web or port boundary.
4. The Mongo adapter implements only the output port and maps persistence
   results to output-port DTOs. The application service maps output-port DTOs
   to input-port DTOs. Preserve cursor ordering, `isDeleted = false` filtering,
   and the existing `size + 1` pagination behavior.
5. Move `StudyRecruitmentApi`, controller, request, and response into the
   Study Recruitment module's `adapter/in/web` boundary. Preserve the exact
   endpoint path, HTTP method/status, authorization, query parameter names,
   defaults, JSON response shape, OpenAPI operation/tag metadata, and cursor
   behavior. `@LoginMember` and `MemberInfo` are already common concerns;
   retain their externally visible use and do not create a Study Recruitment
   dependency on the Member module.
6. Remove obsolete Study Recruitment sources from `adapters`, `application`,
   `controllers`, `domain`, and `ports` after imports/callers are migrated.
   Do not change unrelated modules except for necessary imports caused by this
   move.
7. Preserve or add KDoc required by project rules and keep files under 500
   lines.

## Acceptance checks

- No production Kotlin source refers to the legacy Study Recruitment package
  paths.
- `./gradlew :dymit-backend-api:compileKotlin` succeeds, then run the relevant
  existing tests or `./gradlew :dymit-backend-api:build` when practical.
- Report changed paths, verification commands/results, and any issue that
  prevents a full build in the required log.
