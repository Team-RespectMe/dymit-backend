# TASK-75 Reviewer Instruction — Push Notification Modularization

Status: Pending (run only after Coder and Tester complete)

Read `.agent-dev/roles/REVIEWER.md`, the coder instruction, and tester results first. Do not modify source, tests, configuration, or branches.

## Review checklist

1. Confirm the complete push domain is under top-level `push_notification` with exact `adapter/in`, `adapter/out`, `application/port/in`, and `application/port/out` paths (no `inbound`/`outbound`).
2. Confirm port-signature DTO ownership: each DTO is in the owning push port's `dto` package and no port imports adapter DTOs.
3. Confirm `push_notification/application` and `domain` do not directly depend on external business-module domain/entities/services/DTOs. Member/study-group access must use push-owned outbound ports and DTOs; inbound event adapters alone may translate foreign event contracts.
4. Compare pre-refactor and post-refactor REST mappings/annotations for the admin push endpoint. Verify URL, method, authorization, validation, request/response JSON, and status remain compatible.
5. Inspect event listeners and payload construction for behavioral compatibility: all prior event types, recipients/exclusions, event names, text, image, and data keys must remain intact; no duplicate listener/service exists.
6. Confirm Firebase configuration, device-token persistence, Mongo document mappings/collections, and dependency/configuration files were not changed; no migration is required.
7. Review tester coverage for personal/group/no-token/event/admin mapping behavior. Run focused tests and a full build as appropriate, plus `git diff --check`.

Deliver either `APPROVED` or explicit, prioritized change requests with file/line evidence. Leave a Korean log at `.agent-dev/logs/REVIEWER_<branch>_<timestamp>.md` (500 characters maximum).
