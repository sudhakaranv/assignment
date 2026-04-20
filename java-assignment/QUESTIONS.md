# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**

``` 
There are three different persistence strategies used in the codebase, and as someone seeing it for the first time, this was initially confusing.

I understand why a simple CRUD approach is used for Product and why Store uses Panache’s Active Record pattern: both domains currently have limited business logic, and these approaches allow for a simpler implementation with less boilerplate.

However, from a maintainability perspective, I would personally prefer more consistency across the codebase. Using a uniform approach improves readability, makes the system easier to understand for new developers, and better prepares the code for future growth in business logic.

For that reason, I would consider aligning Product and Store with the Warehouse approach by introducing proper domain models, ports, use cases, and adapters where appropriate. This would centralize business rules, prevent logic from leaking into REST or persistence layers, and make future changes or extensions safer and easier to implement.
``` 

----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**

``` 
Using an OpenAPI‑first approach for the Warehouse API has some clear benefits. It gives everyone a clear contract up front, keeps documentation and implementation in sync, and makes the API easier to understand and use—especially for other teams or external consumers. Because the code is generated from the specification, it also helps catch mismatches or inconsistencies early on.

That said, OpenAPI isn’t free of trade‑offs. There’s some extra setup involved, and tooling limitations matter in practice. For example, in Quarkus the built‑in OpenAPI generator supports only a single specification per application module. This means that multiple APIs need to live in the same OpenAPI file, which can reduce flexibility and requires a bit more coordination when different parts of the API evolve over time.

Implementing endpoints directly in code is usually faster and feels more natural for simple or internal APIs. It avoids additional tooling complexity and makes it easier to iterate quickly. The downside is that it relies more on developer discipline to keep the API shape and documentation consistent as things change.

Given these trade‑offs, using OpenAPI for the core, contract‑driven Warehouse API makes sense, while handling smaller or more internal operations in a more pragmatic way is reasonable. Keeping related endpoints together in a single OpenAPI specification also fits well with Quarkus’s tooling and strikes a good balance between clarity, maintainability, and development speed.
```

----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
``` 
Given limited time and resources, I would prioritize tests that protect the most important business behavior. For this project, that means focusing first on unit tests for the core domain logic, especially the warehouse use cases such as creation, replacement, archiving, and fulfilment rules.

After that, I would add a small number of integration tests to make sure that the REST endpoints, use cases, and database are correctly wired together. These tests help catch configuration or transaction issues without trying to test every possible HTTP scenario.

Rather than aiming for maximum test coverage, I would focus on maintaining effective coverage by ensuring that any new or changed business logic is always accompanied by tests. This keeps the test suite valuable and manageable as the system evolves.
```