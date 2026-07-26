# TASK-75 Coder Instruction — Push Notification Modularization

Status: Pending (do not start until PM explicitly delegates)

Read `.agent-dev/roles/CODER.md`, `CODING_INSTRUCTION.md`, and `PROJECT_STRUCTURE.md` first.

## Objective

Refactor the existing push-notification implementation into the top-level `push_notification` logical module without changing any existing REST contract or notification behavior.

## Required physical package layout

Place push-owned code only under the following module root and use these exact directory names:

`push_notification/domain`
`push_notification/application/port/in`
`push_notification/application/port/out`
`push_notification/application`
`push_notification/adapter/in`
`push_notification/adapter/out`

Use `adapter/in` and `adapter/out`, and `application/port/in` and `application/port/out` exactly. Do not rename these paths to `inbound`, `outbound`, or another synonym. In Kotlin package/import syntax, escape only the keyword segment: `adapter.\`in\`` and `port.\`in\``.

## Scope

1. Move `domain/push`, `application/push_notification`, the Firebase implementation, and the admin push-send responsibility into `push_notification`.
2. Preserve the existing admin push REST endpoint, HTTP method/status, URL, request JSON, validation, authorization, and response exactly. It may be relocated to a push inbound web adapter, but no client-visible interface may change.
3. The module owns message models, event-to-message translation, use cases, ports, and Firebase adapter. It must only dispatch notifications; it must not acquire unrelated business responsibilities.
4. Preserve every currently handled event and its exact recipient selection, exclusion behavior, titles, bodies, event names, data keys/values, images, and Firebase multicast behavior. Event publishers must continue to work.
5. `push_notification/domain` and `push_notification/application` must not import another business module's domain/entity/service/DTO. Define push-owned port DTOs below the owning port's `dto` package. A port must never import an adapter DTO.
6. Replace direct member and study-group data access with push-owned outbound ports and DTOs. Their adapters may read the existing database directly, but must map results into push-owned DTOs before crossing the port boundary. Do not introduce REST/service-to-service communication or dependencies.
7. Foreign event contracts may be consumed only at an inbound event adapter boundary and mapped immediately to push-owned commands/DTOs; application/domain code must remain independent of the foreign module contract. Preserve Spring event dispatch semantics.
8. `@LoginMember` and its resolver are already common infrastructure. Keep their public behavior and all existing endpoints unchanged. Do not move them into the push module. Where a push command/model currently relies on a foreign object through `from(...)`, convert it to an `of(...)` factory taking the required scalar values; do not make it depend on a foreign package type.
9. Preserve existing Firebase configuration, device-token persistence, collection/document mappings, and runtime wiring. Do not add dependencies, alter Gradle/configuration, change Mongo data, or introduce a migration.
10. Remove obsolete legacy push source files only after all imports and component scanning point to the module. Do not retain duplicate Spring listeners/services.

## Architectural guardrails

- Each new use-case interface has exactly one `execute(...)` method and a Command object at the application boundary.
- REST request/response DTOs belong to the inbound web port's `dto` package; outbound data DTOs belong to the corresponding outbound port's `dto` package.
- Adapter DTOs may map HTTP/Firebase/DB representation but cannot be imported by a port.
- Keep files at or below roughly 500 lines; split the legacy listener by event family if required.
- Maintain KDoc required by the role documents.
- Do not edit any test or project configuration file, `BACKLOG.md`, or branch state.

## Acceptance criteria and verification

- No production reference remains to legacy `application.push_notification` or `domain.push` packages.
- The required exact package paths exist and no `inbound`/`outbound` naming is introduced for this module.
- Application/domain have no direct member, study-group, study-schedule, board, task, or reminder module dependency. Cross-module data retrieval occurs through push-owned outbound ports/DTOs.
- Existing admin push REST contract and all event-driven notifications remain behaviorally compatible.
- `./gradlew :dymit-backend-api:compileKotlin` and relevant existing tests pass; report any pre-existing unrelated failure precisely.
- Run `git diff --check`.
- Leave a Korean log under `.agent-dev/logs/CODER_<branch>_<timestamp>.md` (500 characters maximum) stating changed scope and verification.
