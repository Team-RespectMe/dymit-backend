# TASK-79 — Coder Instruction

## Objective

Refactor the Admin and Reminder domains into top-level logical modular-monolith packages. Preserve all REST contracts, Quartz identities/schedules, and runtime behavior. Do not add dependencies.

## Mandatory package layout

Use these exact names. Kotlin package declarations/imports must escape keyword segments with backticks: `adapter.\`in\`` and `application.port.\`in\``. Never use `inbound`, `outbound`, or substitute names.

```text
admin/
  domain/
  application/
    port/in/web/dto/
    port/out/member/dto/
    port/out/push_notification/dto/
    usecase/
  adapter/in/web/
  adapter/out/member/
  adapter/out/push_notification/
reminder/
  domain/
  application/
    port/out/study_group/dto/
    port/out/study_schedule/dto/
    usecase/
  adapter/out/study_group/
  adapter/out/study_schedule/
```

Only create folders that have a concrete class. Do not leave compatibility wrappers in legacy packages.

## Scope

1. Move all Admin-related source from `application/admin/**` and the Admin push REST/use-case/service currently under `push_notification/**` into `admin/**`.
2. Move all Reminder jobs/events from `application/reminder/**` into `reminder/**`.
3. Update `QuartzConfig` only to point to the new Reminder job classes. Keep bean names, Quartz job identities, cron expressions, time zone, and trigger behavior unchanged.
4. Move or create every use-case contract under the owning module’s `application/usecase`; every use case has exactly one `execute(...)` function. Adapt the existing daily-status contract to that rule without changing its observable REST behavior (there is currently no daily-status REST endpoint).
5. Keep `@LoginMember` and `MemberInfo` in `common`. If any common authentication conversion still accepts a module-specific class, replace it with a direct-value `of(...)` factory; do not add module dependencies to common.

## Boundary and DTO rules (non-negotiable)

- A module must not import another domain module’s domain, application, adapter, or persistence type.
- Define output ports in the *calling* module. Required names must be module-prefixed to prevent future collisions: `AdminMemberStatusPort`, `AdminPushNotificationPort`, `ReminderStudyGroupPort`, and `ReminderStudySchedulePort` (or equally explicit module-prefixed names if a split is truly needed).
- Each cross-domain port defines its own DTOs in that port’s `dto` package. Never return or accept `Member`, `DailyMemberStatus`, Study Group DTOs, Study Schedule DTOs, Push Notification commands, or adapter DTOs from another module.
- The corresponding `adapter/out/<target>` may query Mongo/persistence directly and maps persistence/domain objects to the caller-owned port DTOs. It must not expose foreign objects through the port.
- REST request/response DTOs and command/result DTOs that cross an Admin input port belong in the owning Admin `application/port/\`in\`/web/dto` package. No adapter-owned DTO may be imported by a port, and do not place boundary DTOs in `application/usecase/dto`.
- Preserve the existing `POST /api/v1/admin/push-notifications`, response status 201, request JSON shape, SpringDoc metadata, and authentication annotation. Preserve the push payload fields/title/event name.
- Reminder events must retain their exact push/feed payload semantics, timestamps/window calculations, batching, de-duplication, and Quartz behavior, while using Reminder-owned output ports and DTOs.

## Migration completeness

- Relocate all related production source and update every reference; delete the old `application/admin`, `application/reminder`, and Admin-specific `push_notification` classes after migration.
- Do not move general personal push-notification functionality out of `push_notification`.
- Migrate relevant unit tests only through the Tester role; do not edit `src/test`.
- Check for stale legacy package imports and forbidden package names.

## Verification and handoff

Run `../gradlew :dymit-backend-api:build` and report the result. Also run a source scan proving no legacy Admin/Reminder production classes or forbidden `inbound`/`outbound` packages remain, and `git diff --check`.

Read `.agent-dev/roles/CODER.md`, `CODING_INSTRUCTION.md`, and `PROJECT_STRUCTURE.md` first. Do not change tests, configuration (except the required import/class references in `QuartzConfig`), `BACKLOG.md`, branches, commits, or dependencies. Write a concise (<=500 chars) English log to `.agent-dev/logs/CODER_<branch>_<timestamp>.md`.
