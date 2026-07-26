# TASK-75 Tester Instruction — Push Notification Modularization

Status: Pending (run only after the Coder reports completion)

Read `.agent-dev/roles/TESTER.md` and the coder instruction first.

## Objective

Add deterministic unit tests for the refactored `push_notification` module. Use Kotest `BehaviorSpec` and mocks/test doubles only. Do not modify production code or configuration.

## Required coverage

1. Personal dispatch: the personal-message use case loads the intended member through the push-owned member port, filters inactive device tokens, and sends Firebase payload data including `eventName`.
2. Group dispatch: it loads group members through the push-owned port, honors excluded members, deduplicates active device tokens, and retains the original payload data.
3. No active token behavior: no Firebase send is attempted and the call completes safely.
4. Inbound event mapping: representative personal, group, broadcast, and study-schedule event adapters preserve recipient IDs, event name, title/body, image, and payload keys. Mock dependencies; do not use Spring context or a database.
5. Admin push input mapping/validation: verify that the existing endpoint's request fields map to the same command/message semantics and that validation/output shape remains unchanged, using controller-level unit tests only.
6. Structural tests where practical: assert that public port DTOs are push-owned and no legacy `application.push_notification` / `domain.push` import remains.

## Guardrails

- Tests only under `src/test/.../units/push_notification` (and reusable helpers only under `src/test/.../supports`).
- Mock every collaborator; do not create integration or end-to-end tests, Spring contexts, Firebase calls, or Mongo access.
- Do not rewrite unrelated existing tests simply to make the build pass. Report any legacy failure to the PM.
- Preserve existing tests unless their import must be changed solely because the production class moved.

## Verification and handoff

Run the focused push-notification unit tests and `./gradlew :dymit-backend-api:build` if feasible. Report commands/results and leave a Korean log under `.agent-dev/logs/TESTER_<branch>_<timestamp>.md` (500 characters maximum).
