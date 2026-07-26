# TASK-77 Coder Instruction

## Role and prerequisite

Read `roles/CODER.md`, `roles/CODING_INSTRUCTION.md`, and `roles/PROJECT_STRUCTURE.md` completely before implementation. Communicate in English and create a Korean log under `.agent-dev/logs/` (maximum 500 characters). Do not modify tests, configuration, `BACKLOG.md`, branches, commits, or external infrastructure.

## Objective

Refactor the legacy user-feed implementation into the top-level `feed` modular-monolith package while preserving every existing feed REST API contract and behavior. Move the two reusable feed-event contracts to the common module. The Feed module must consume those events and create feeds. Do not add dependencies or DB migrations.

## Mandatory package and ownership rules

Use these exact physical directories and Kotlin packages (Kotlin keyword segments must use backticks; do not invent `inbound` or `outbound`):

```
feed/domain/**
feed/application/port/in/web/dto/**
feed/application/port/out/persistence/dto/**
feed/application/**
feed/adapter/in/event/**
feed/adapter/in/web/**
feed/adapter/out/persistence/**
common/event/feed/**
common/annotation/**
common/security/**
```

`feed.application.port.\`in\`` owns use-case interfaces and their command/result DTOs. `feed.application.port.\`out\`` owns persistence-port interfaces and any DTOs crossing that boundary. An adapter must never define a DTO imported by a port. Do not import another domain module's entity into `feed/domain` or `feed/application`; introduce a Feed-owned out-port and DTO whenever Feed needs external-domain data. The Mongo adapter may use the existing database directly, but maps it to the Feed-owned port DTO/entity boundary.

## Required implementation work

1. Inventory and relocate all legacy feed assets: `domain/user_feed`, `application/feed`, user-feed controller/API/DTO/VO, `ports/persistence/user_feed`, and Mongo user-feed adapters into the exact Feed module layers above. Remove obsolete legacy feed files after imports are migrated. Keep the persistence collection names, indexes, query semantics, and data shape unchanged.
2. Preserve the REST surface exactly: `/api/v1/user-feeds`, HTTP methods/statuses, paths, parameter names/types/defaults, request/response JSON, validation, authentication semantics, Swagger annotations, pagination/cursor behavior, delete authorization, and read-marking behavior. The web adapter converts `@LoginMember MemberInfo` into Feed-owned command DTOs before calling an in-port; Feed application/domain code must not depend on `MemberInfo`.
3. Keep `LoginMember`, its resolver, and authentication principal in `common`. If any existing `from(MemberInfo)`-style conversion binds a reusable/common type to a feature package, replace only that coupling with an `of(memberId, nickname, roles)`-style direct-data conversion, preserving behavior. Do not move these shared classes into Feed.
4. Replace common event contracts that directly expose `UserFeed`/`GroupFeed` or Feed-owned value objects. Define exactly the reusable personal-feed and group-feed event contracts/data under `common/event/feed`, carrying only primitive/common event data necessary to express recipient(s), content, metadata, associated-resource identifiers, and exclusions. No common event class may import `feed/**`. Update all event producers (including task, study-group, study-schedule, and any other discovered producer) to publish the common contracts without importing Feed classes.
5. Add Feed event input adapters under `feed/adapter/in/event`. They listen to the two common event types and map event data to Feed domain/application commands. Preserve current asynchronous listener behavior, event names, message composition/order, icon/resource values, individual/group recipient behavior, excluded-member filtering, and broadcast behavior. Do not leave duplicate listeners that create two feeds for one publication.
6. Migrate special existing listeners (study-group and study-schedule events) into Feed input adapters or convert them through the common personal/group contracts as necessary. Feed must remain the sole owner of actual `UserFeed`/`GroupFeed` creation.
7. Maintain the existing current API and Mongo behavior. Do not change Gradle files, resource/configuration files, collection names, schemas, or add migrations. No direct legacy package imports may remain outside tests after migration.

## Acceptance checks

- Exact `adapter/in`, `adapter/out`, `application/port/in`, and `application/port/out` paths exist; no `inbound`/`outbound` paths exist.
- Port DTO ownership is respected and no adapter DTO is imported by a port.
- `common/event/feed` has no Feed imports; non-Feed event producers do not import Feed classes.
- Feed application/domain does not import foreign-domain entities or `MemberInfo`.
- Legacy user-feed source paths are gone, with no duplicate event listeners.
- Existing REST contract and persistence behavior are unchanged.
- Run focused relevant tests plus `../gradlew :dymit-backend-api:build`; report commands/results and known risks in the log.
