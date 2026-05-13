# AGENTS.MD
---
## Project Overview
Dymit, The study group management application backend project.

## Global Commands

### Build
```bash
gradlew build
```
### Run Test
```bash
gradlew bootTestRun  
```
### Build Docker Image
```bash
gradlew buildDockerImage
```
- Push Docker Image
```bash
gradlew pushDockerImage
```

## Roles

- Each agent must always include the `roles/<RULE>.md` file that matches its role as defined under the project.
- CODER and TESTER required to read roles/CODE.md
- Each agent must leave a log of its work results in the following format so that other agents can sufficiently understand what was done:  
  `.agent-logs/<RULE>_<BRANCHNAME>_YYYY_MM_DD_HH_MM_SS.md`
- Each agent should keep its log as concise as possible. The entire file must be no more than 500 characters.
- Each agent may only perform actions listed under **Allowed Actions** in its own role `.md` file.

### PM

The PM analyzes the `BACKLOGS.md` file, creates a list of required tasks, and assigns work to the coder, tester, and reviewer based on that list.

The PM is responsible for running sub-agents. Based on the outputs from the sub-agents, the PM determines whether the work should continue.

### Coder

The Coder directly implements the tasks written by the PM.

### Tester

The Tester writes test code directly to verify whether the logic written by the Coder correctly implements all domain rules, based on the tasks written by the PM.

### Reviewer

The Reviewer verifies the Coder Output and Tester Output for the tasks implemented by the Coder, and determines whether there are any issues with merging into the main branch.

## Global Git Rules
- Agents are not allowed to create, switch, delete, merge, or push branches.
- Agents are not allowed to commit changes.
- Agents may only work on the current branch.
- Agents may use `git diff` and `git status` for inspection purposes.
- All sub-agents — Coder, Tester, and Reviewer — must work only based on the current branch.

## Global Code Style
- Follow the Kotlin Coding Conventions.
- If a function has three or more parameters, or if the function name becomes long enough to reduce readability, write it in the following format:
```kotlin
fun functionName(
    param1: Type1,
    param2: Type2,
    param3: Type3
): ReturnType {
    // function body
}
```
- Need a space after 'if' and 'for, while' keywords. and begin, end of parenthesis.
- Add one blank line after a class declaration.
- Comments for documentation generation must be written in KDoc style.
- For documentation generation, specify field and constructor information above class definitions.
- For documentation generation, specify behavior, parameters, and return type above methods.
- For documentation generation, also describe what each interface does above the interface definition, and specify behavior, parameters, and return type above interface methods.
- The maximum size of a single file is limited to 500 lines. Split the file if it exceeds this limit.

## GLOBAL RULES
- Files intended to be read by humans must be written in Korean.
- Communication between agents must be written in English.