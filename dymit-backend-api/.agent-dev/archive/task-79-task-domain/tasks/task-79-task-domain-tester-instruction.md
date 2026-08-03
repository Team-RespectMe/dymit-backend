# TASK-79 Tester Instruction: Task Domain Modularization

## Mission

After the Coder completes the Task-domain migration, migrate and extend unit tests so they verify the unchanged Task behavior through the new Task module boundaries.

Read `AGENTS.md`, `.agent-dev/roles/TESTER.md`, `.agent-dev/roles/CODING_INSTRUCTION.md`, `.agent-dev/roles/PROJECT_STRUCTURE.md`, and the Coder log before editing. Do not edit production code, commit, change branches, or edit backlog/task instructions. Leave a concise English log (500 characters or fewer) in `.agent-dev/logs/`.

## Test scope

1. Move all legacy Task tests under Task-module-aligned test packages and update imports; no test may retain legacy `application.task`, `controllers.task`, `domain.task`, `ports.persistence.task`, or `adapters.persistence.mongo.task` imports.
2. Preserve existing test coverage for domain rules, service/use-case behavior, event publication, schedule synchronization/cancellation, notification preparation, assignee state, submissions, submission comments, controller routes/validation/response mapping, and Task persistence queries.
3. Add focused unit/mock/BehaviorSpec coverage for migration-sensitive boundaries:
   - controller request -> Task port input/command -> response conversion while retaining `@LoginMember` behavior;
   - Task port DTO ownership (ports/use cases do not depend on adapter DTOs);
   - Task-owned persistence port/adapters, including task/submission/assignee/comment query semantics;
   - interactions with Study Group and Study Schedule only through port DTOs;
   - current event, authorization, deletion, and `size + 1` behavior.
4. Do not write integration/E2E tests and do not change externally observable API contracts.

## Validation

Run focused Task tests first, then `../gradlew :dymit-backend-api:build`. Report exact failures and their causes without changing production code.
