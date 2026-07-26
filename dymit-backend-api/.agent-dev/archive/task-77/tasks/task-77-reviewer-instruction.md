# TASK-77 Reviewer Instruction

## Role and prerequisite

Read `roles/REVIEWER.md`, `roles/CODING_INSTRUCTION.md`, `roles/PROJECT_STRUCTURE.md`, plus the Coder and Tester TASK-77 logs. Communicate in English. Do not modify code, tests, configuration, `BACKLOG.md`, branches, commits, or external infrastructure. Write a Korean review log under `.agent-dev/logs/` (maximum 500 characters).

## Dependency

Review only after Coder and Tester finish. Inspect the final working tree and give `APPROVED` or actionable, prioritized change requests.

## Required review checklist

1. Confirm exact physical/package names: `feed/adapter/in`, `feed/adapter/out`, `feed/application/port/in`, and `feed/application/port/out`; reject `inbound`/`outbound` alternatives.
2. Confirm all port-boundary DTOs belong to the owning Feed port, and no port imports an adapter DTO.
3. Confirm Feed owns Feed entities, persistence ports, services/use cases, web/event input adapters, and Mongo output adapters; old `user_feed`, `application/feed`, `controllers/user_feed`, and `ports/persistence/user_feed` source ownership has been fully removed or migrated without duplicate beans/listeners.
4. Confirm `common/event/feed` is Feed-independent, with exactly reusable personal/group event contracts; no common or producer class imports Feed entities/value objects. Check all affected producers preserve event data and Feed consumes it once.
5. Confirm Feed application/domain has neither direct foreign-domain entity imports nor `MemberInfo`; external needs go through Feed out-ports and Feed-owned DTOs. `LoginMember` resolver/principal remain shared common components, and controller converts to a command.
6. Compare REST API annotations/mappings, HTTP statuses, request/response JSON, validation, auth behavior, cursor pagination, delete/read behavior with the prior interface. No externally visible change is acceptable.
7. Confirm Mongo collection names/indexes/query behavior/data compatibility are unchanged and no migrations/config/dependency changes were introduced.
8. Review tester coverage and run/review relevant focused tests plus `../gradlew :dymit-backend-api:build`; run `git diff --check`.

Include any risks and exact remediation in the log if not approved.
