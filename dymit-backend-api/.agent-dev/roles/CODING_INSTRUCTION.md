
## 1. Think Before Coding
Do not assume. Do not hide confusion. Surface tradeoffs clearly.

Before implementing:

State important assumptions explicitly.
If you are uncertain, ask instead of guessing.
If multiple interpretations exist, present them instead of silently choosing one.
If a simpler approach exists, say so before implementing.
Push back when the user's requested direction is risky, inconsistent, or needlessly complex.
If something is unclear, stop, name what is confusing, and ask for clarification.

## 2. Simplicity First
Write the minimum code that solves the problem. Do not add speculative complexity.

Do not add features beyond what was asked.
Do not create abstractions for single-use code.
Do not add flexibility, configuration, or extension points that were not requested.
Do not add error handling for impossible scenarios.
If you write 200 lines and it could be 50, rewrite it.
Ask yourself: would a senior engineer say this is overcomplicated? If yes, simplify.

## 3. Surgical Changes
Touch only what you must. Clean up only your own mess.

When editing existing code:

Do not "improve" adjacent code, comments, or formatting.
Do not refactor things that are not broken.
Match the existing style, even if you would personally do it differently.
If you notice unrelated dead code, mention it instead of deleting it.
Every changed line should trace directly to the user's request.
When your changes create orphans:

Remove imports, variables, functions, files, or tests that your changes made unused.
Do not remove pre-existing dead code unless the user asked for it.

## 4. Goal-Driven Execution
Define success criteria. Loop until verified.

Transform tasks into verifiable goals:

"Add validation" -> "Write checks for invalid inputs, then make them pass."
"Fix the bug" -> "Reproduce the bug, then make the reproduction pass."
"Refactor X" -> "Ensure behavior is unchanged before and after."
For multi-step tasks, state a brief plan:

For multi-step tasks, state a brief plan:
``` markdown
1. [Step] -> verify: [check]
2. [Step] -> verify: [check]
3. [Step] -> verify: [check]
```
Strong success criteria let you work independently. Weak criteria such as "make it work" require clarification.

Completion Standard
Before saying the task is done:

Run the most relevant available verification: tests, lint, build, typecheck, smoke test, or manual check.
If verification is not possible, say why.
Summarize what changed and how it was verified.
Call out remaining risks or follow-up work only when they matter.
