# Project Structure

## Overview

This project follows the **Hexagonal Architecture**.

It also adopts a **Modular Monolith** architecture. Keep module boundaries clear and avoid unnecessary dependencies between modules.

## Structure

- dymit-backend: Project Root
    - dymit-backend-api: API Source Code
        - `<domain>`: Packages are organized by domain.
            - application: Contains business logic and port definitions.
                - port
                    - in
                        - web
                            - dto
                              - CreateExampleRequest
                              - ExampleResponse
                            - ExampleApi: Defines REST controller interfaces and documents them using SpringDoc annotations.
                    - out
                        - persistence
                            - SaveExamplePort: Port for saving entities.
                            - LoadExamplePort: Port for loading entities.
                - usecase: Defines business use case interfaces only.
                    - GetExampleUseCase
                    - CreateExampleUseCase
                - GetMemberService: Implementation of GetMemberUseCase.
                - CreateMemberService: Implementation of CreateMemberUseCase.
            - domain
                - Example: Domain entity. Domain and persistence entities are currently not separated. Entity design depends on the persistence technology used by the domain.
            - adapter
                - in
                    - web
                        - ExampleRestController: Implementation of ExampleApi.
                - out
                    - persistence
                        - MongoSaveMemberAdapter
                        - MongoLoadMemberAdapter

## Guide

- Every UseCase must expose **exactly one** function named `execute(...)`. One UseCase must represent one business responsibility.
- Always define a **Command** object when invoking a UseCase. Convert incoming request objects into Command objects before passing them to the application layer.
- Transfer data between layers using dedicated DTOs instead of exposing domain or persistence objects directly. Example flow:
    1. The REST adapter receives a `CreateMemberRequest`.
    2. Convert it to a `CreateMemberCommand`.
    3. The `CreateMemberUseCase` implementation persists a `MemberEntity` using the repository.
    4. Convert the `MemberEntity` into a `MemberDto`.
    5. The REST adapter converts the `MemberDto` into a `MemberResponse` and returns it.
- Request and response models must follow the naming convention `*Request` and `*Response`.
- Objects passed to the application layer should use names such as `*Command` and `*Dto`.
- **Do not reference classes from other domain modules directly.** If necessary, create separate classes within the current module, even if they contain duplicated fields. This helps preserve module boundaries.
- Do not introduce design patterns that add unnecessary complexity.
- Keep each source file around **500 lines or fewer**. Split large files when appropriate.
- All `*Response` classes should inherit from `BaseResponse` by default.