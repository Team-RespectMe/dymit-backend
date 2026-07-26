# BaseAggregateRoot common migration — Coder Instruction

Move `src/main/kotlin/net/noti_me/dymit/dymit_backend_api/domain/BaseAggregateRoot.kt` to `src/main/kotlin/net/noti_me/dymit/dymit_backend_api/common/BaseAggregateRoot.kt` and change its package to `net.noti_me.dymit.dymit_backend_api.common`.

Update every production and test import/reference to the new common package, including `AggregateMongoEventListener` and every aggregate entity. Preserve the class implementation, annotations, generic type, constructors, visibility, and runtime behavior exactly; this is a namespace move only.

Remove the old source file. Do not leave compatibility aliases/wrappers in `domain`. Do not modify unrelated code, project configuration, BACKLOG, branches, or commits. Read Coder role instructions first. Run `../gradlew :dymit-backend-api:build`, `git diff --check`, and a repository scan proving no `net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot` import/reference or old file remains. Write a <=500-character English log in `.agent-dev/logs/CODER_<branch>_<timestamp>.md`.

## Additional required ProfileImageType migration

`domain/ProfileImageType.kt` must be removed and must **not** be moved to `common`. Each top-level consumer package/module owns and defines its own profile-image enum. This applies to all current consumers, including Member, Study Group, Board, Server Notice, Study Schedule, and Task/legacy controller-application areas where applicable. Use module-local enum names that make ownership explicit. At module boundaries, map through caller-owned port DTOs/adapters; never import another top-level module’s enum. Preserve REST JSON and Mongo values `PRESET` and `EXTERNAL`, existing validation, and runtime behavior exactly. Update all production references but do not edit tests. Verify there is no old `domain.ProfileImageType` import/reference/file and no `common.ProfileImageType`.
