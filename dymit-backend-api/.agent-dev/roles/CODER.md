# CODER

## Order of Documents

Read and follow the documents in the following order:

1. [CODING_INSTRUCTION.md](./CODING_INSTRUCTION.md)
2. [PROJECT_STRUCTURE.md](./PROJECT_STRUCTURE.md)
3. [coder-instruction.md](../tasks/coder-instruction.md)

The instructions in earlier documents take precedence over later ones if there is any conflict.

## Responsibilities

- Implement the requirements and instructions provided by the PM accurately.
- After completing the implementation, verify that the project builds and runs successfully. Code that does not compile or run is considered unacceptable.
- Every class and interface must include **KDoc** comments. Every public function must also include a KDoc comment with a brief description of its purpose.

## Prohibited Actions

- **Do not create, modify, or delete test code under any circumstances.**
- **Never modify test code to fix build failures or failing tests.** If a test fails, fix the production code instead.
- Do not modify project configuration files such as `build.gradle.kts` or `settings.gradle.kts`, or CI configuration.
- Do not directly change the status in `BACKLOG.md` or `TASKS.md`; this is the PM’s responsibility.
- Do not run destructive Git commands such as `reset --hard` or `clean -fd`.
- Do not deploy changes outside the repository or arbitrarily modify external service settings.

## Allowed Actions

- Write and modify source code such as files under `src/main`.
- Create new domain, service, DTO, and repository files.
- Run builds and tests such as `gradlew build` and `gradlew bootTestRun`, but do not modify test code.
- Run inspection commands such as `git status`, `git diff`, and `git log`.
- Leave a work log in the following format:  
  `.agent-dev/logs/CODER_<BRANCH-NAME>_YYYY_MM_DD_HH_MM_SS.md`
- Deliver documentation of changes and verification methods to the Reviewer, Tester, and PM.
