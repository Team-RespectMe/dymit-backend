# TASKS
---
## BACKLOG TASK 1: File API Requirement Change

### PM Summary
- Goal: extend the file upload domain so JPEG/JPG uploads store and expose thumbnail metadata, validate supported types by magic number, and keep S3 thumbnail objects under `/dymit/thumbnails/`.
- Current code state:
  - `UserFile` does not store thumbnail metadata.
  - `UploadFileUseCaseImpl` accepts any multipart payload and derives storage path only from filename.
  - `FileDto` / `FileUploadResponse` expose only original file access data.
  - No dedicated file detail endpoint was found; current interpretation is that every file-information response built from `FileDto` must expose thumbnail data when the file is an image.

### Scope
- In scope:
  - File domain metadata for thumbnail storage
  - Upload-time file signature validation for JPEG/JPG/PDF
  - Thumbnail generation for image uploads
  - S3 upload flow changes required to store thumbnails under `/dymit/thumbnails/`
  - File DTO / API response changes so image files return thumbnail information
  - Unit tests for the above
- Out of scope:
  - Study schedule attachments from BACKLOG TASK 2
  - New public API endpoints beyond what TASK 1 strictly requires
  - Non-unit integration or end-to-end test suites

### Assumptions And Open Questions
- Assumption: PDF uploads remain supported without a thumbnail unless existing code already has a document-preview policy elsewhere.
- Assumption: `FileDto` is the canonical file-info payload for TASK 1 because no separate file detail API was found in the repository.
- Risk: `build.gradle.kts` does not currently declare a thumbnail library. Role rules prohibit sub-agents from changing project settings. If implementation cannot satisfy the requirement with currently available dependencies, the Coder must stop and report the blocker instead of editing `build.gradle.kts`.
- PM fallback decision: do not edit `build.gradle.kts` for TASK 1. If no thumbnail library is already available, complete the functional requirement with currently available JDK/image facilities and document the deviation explicitly for Reviewer follow-up.

### Dependency Order
1. `CODER-T1` must finish first.
2. `TESTER-T1` starts after coder changes are available.
3. `REVIEWER-T1` starts after coder and tester outputs are available.

### CODER-T1
- Owner: Coder
- Status: in_progress
- Objective: implement BACKLOG TASK 1 in production code only.
- Required references:
  - `AGENTS.md`
  - `agent-roles/Coder.md`
  - `BACKLOG.md`
  - This `TASKS.md`
- Target areas:
  - `src/main/kotlin/net/noti_me/dymit/dymit_backend_api/domain/file/**`
  - `src/main/kotlin/net/noti_me/dymit/dymit_backend_api/application/file/**`
  - `src/main/kotlin/net/noti_me/dymit/dymit_backend_api/controllers/files/**`
  - `src/main/kotlin/net/noti_me/dymit/dymit_backend_api/adapters/persistence/mongo/file/**`
  - Supporting ports if required
- Deliverables:
  - Persist thumbnail metadata on `UserFile` without breaking existing file status flows.
  - Validate upload content by magic number and reject unsupported payloads with a domain-appropriate client error.
  - Support only JPEG/JPG/PDF uploads.
  - Generate and upload a thumbnail for JPEG/JPG uploads.
  - Store thumbnail objects under `/dymit/thumbnails/` in S3.
  - Expose thumbnail information in file-info responses when the file is an image.
  - Keep controller responsibilities limited to routing and request/response mapping.
  - Write a detailed execution log to `.agent-logs/CODER_feat_file-api_<timestamp>.md`.
- Acceptance criteria:
  - Image upload success returns original file metadata and non-null thumbnail metadata.
  - PDF upload success returns original file metadata and no misleading thumbnail data.
  - Unsupported or forged file payloads are rejected based on file signature, not only extension or content type.
  - Thumbnail upload failure does not leave the file in an inconsistent success state.
  - Existing DTO / use case call chains that return `FileDto` remain coherent after the contract change.
  - Build and existing tests are executed, and failures are documented if they are unrelated or blocked.

### TESTER-T1
- Owner: Tester
- Status: in_progress
- Objective: add unit tests for BACKLOG TASK 1 after coder output lands.
- Required references:
  - `AGENTS.md`
  - `agent-roles/Tester.md`
  - `BACKLOG.md`
  - This `TASKS.md`
  - Coder log in `.agent-logs/`
- Target areas:
  - `src/test/kotlin/net/noti_me/dymit/dymit_backend_api/units/application/file/**`
  - `src/test/kotlin/net/noti_me/dymit/dymit_backend_api/units/controllers/files/**`
  - `src/test/kotlin/net/noti_me/dymit/dymit_backend_api/supports/**` only if reusable helpers are necessary
- Deliverables:
  - Unit tests that verify signature validation, thumbnail branching, and response mapping.
  - A test log at `.agent-logs/TESTER_feat_file-api_<timestamp>.md` with executed commands and results.
- Minimum test scenarios:
  - JPEG upload stores thumbnail metadata and returns thumbnail fields.
  - PDF upload succeeds without thumbnail metadata.
  - Unsupported magic number is rejected.
  - Upload failure paths set the persisted status to `FAILED`.
  - Controller-layer response mapping includes thumbnail fields correctly.

### REVIEWER-T1
- Owner: Reviewer
- Status: pending
- Objective: review coder and tester outputs for merge readiness after both complete.
- Required references:
  - `AGENTS.md`
  - `agent-roles/Reviewer.md`
  - `BACKLOG.md`
  - This `TASKS.md`
  - Latest coder/tester logs in `.agent-logs/`
- Deliverables:
  - Review findings prioritized by severity.
  - Explicit statement on whether BACKLOG TASK 1 is safe to mark complete.
  - Review log at `.agent-logs/REVIEWER_feat_file-api_<timestamp>.md`.
- Review checklist:
  - Requirement coverage for thumbnail metadata, thumbnail S3 path, and magic number validation
  - Correct handling of JPEG/JPG versus PDF behavior
  - Contract compatibility of `FileDto` and controller responses
  - Failure-state consistency and persistence updates
  - Adequacy of unit-test coverage
