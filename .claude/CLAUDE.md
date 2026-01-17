# AI Rules for {{project-name}}

{{project-description}}

---

## COLLABORATIVE WORKFLOW

**IMPORTANT: This section defines the default working methodology. Follow these guidelines unless explicitly overridden
by user command.**

### Iterative Development Process

When working on tasks, use an **iterative, educational approach** that balances task completion with learning:

1. **Task Planning & Tracking**
   - ALWAYS use `TodoWrite` tool to break complex tasks into manageable steps
   - Track progress in real-time, marking todos as `in_progress` and `completed` immediately
   - For multi-step tasks (3+ steps), create a todo list BEFORE starting work
   - Update todos as you work - don't batch completions

2. **Active Learning & User Involvement**
   - For key design decisions or meaningful code sections (2-10 lines), request user contribution using the "Learn by
     Doing" pattern
   - Add `TODO(human)` markers in code where user input is needed
   - Provide context, guidance, and trade-offs for each learning opportunity
   - Don't request contributions for trivial/boilerplate code

3. **Educational Insights**
   - Before and after significant code changes, provide brief insights using the format:
     ```
     ★ Insight ─────────────────────────────────────
     [2-3 key educational points specific to the codebase]
     ─────────────────────────────────────────────────
     ```
   - Focus on interesting, codebase-specific insights rather than general programming concepts
   - Explain architectural decisions, design patterns, and "why" not just "what"

4. **Incremental Implementation**
   - Implement changes in small, testable increments
   - Run tests frequently to catch issues early
   - Fix issues immediately when tests fail - explain what went wrong and why
   - Don't move to the next step until current step works

5. **Helper Methods & Reusability**
   - Create reusable helper methods for repeated patterns
   - Document helpers with clear JavaDoc
   - Refactor duplication into shared utilities

6. **Problem-Solving Approach**
   - When errors occur, explain the root cause clearly
   - Provide multiple solution options when appropriate
   - Show both quick fixes and proper long-term solutions
   - Use error messages as teaching moments

7. **Quality Assurance**
   - Verify changes compile before moving to tests
   - Run relevant test suites after each major change
   - Ensure all tests pass before marking tasks complete
   - Clean up code (remove unused imports, TODOs, etc.) before finishing

### Communication Style

- Be **collaborative and encouraging** - frame contributions as valuable design input
- **Explain trade-offs** when multiple approaches exist
- **Ask clarifying questions** rather than making assumptions
- **Provide concise summaries** after completing tasks
- **Use file:line references** when discussing specific code locations

### Override Protocol

These guidelines can be overridden by explicit user commands such as:

- "Just implement this directly without my input"
- "Skip the learning parts and complete the task"
- "Don't use todos for this"
- "Explain less, just fix it"

---

## BACKEND

### Guidelines for JAVA

#### SPRING_BOOT

- Use Spring Boot for simplified configuration and rapid development with sensible defaults
- Prefer constructor-based dependency injection over `@Autowired`
- Avoid hardcoding values that may change externally, use configuration parameters instead
- For complex logic, use Spring profiles and configuration parameters to control which beans are injected instead of hardcoded conditionals
- If a well-known library simplifies the solution, suggest using it instead of generating a custom implementation
- Use DTOs as immutable `record` types
- Use Bean Validation annotations (e.g., `@Size`, `@Email`, etc.) instead of manual validation logic
- Use `@Valid` on request parameters annotated with `@RequestBody`
- Use custom exceptions for business-related scenarios
- Centralize exception handling with `@ControllerAdvice` and return a consistent error DTO: `{{error_dto}}`
- REST controllers should handle only routing and I/O mapping, not business logic
- Use SLF4J for logging instead of `System.out.println`
- Prefer using lambdas and streams over imperative loops and conditionals where appropriate
- Use `Optional` to avoid `NullPointerException`

#### SPRING_DATA_JPA

- Define repositories as interfaces extending `JpaRepository` or `CrudRepository`
- Never expose JPA entities in API responses – always map them to DTOs
- Use `@Transactional` at the service layer for state-changing methods, and keep transactions as short as possible
- Use `@Transactional(readOnly = true)` for read-only operations
- Use `@EntityGraph` or fetch joins to avoid the N+1 select problem
- Use `@Query` for complex queries
- Use projections (DTOs) in multi-join queries with `@Query`
- Use Specifications for dynamic filtering
- Use pagination when working with large datasets
- Use `@Version` for optimistic locking in concurrent updates
- Avoid `CascadeType.REMOVE` on large entity relationships
- Use HikariCP for efficient connection pooling

#### LOMBOK

- Use Lombok where it clearly simplifies the code
- Use constructor injection with `@RequiredArgsConstructor`
- Prefer Java `record` over Lombok’s `@Value` when applicable
- Avoid using `@Data` in non-DTO classes, instead, use specific annotations like `@Getter`, `@Setter`, and `@ToString`
- Apply Lombok annotations to fields rather than the class if only some fields require them
- Use Lombok’s `@Slf4j` to generate loggers

