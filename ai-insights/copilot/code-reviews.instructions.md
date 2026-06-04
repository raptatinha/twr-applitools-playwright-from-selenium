---
description: "Use when writing or reviewing TypeScript/Playwright code, performing code reviews, or checking code quality standards."
applyTo: "**/*.ts"
author: Renata Andrade
---
# Development & Code Review Standards

## Language & Framework
- TypeScript
- Playwright

## Code Quality Rules

### Naming Conventions
- `lowerCamelCase` for: variable, parameter, function, method, property, module alias
- `CONSTANT_CASE` for: global constants and enum members
- `UpperCamelCase` for: Class, Interface, Type, Enum, Decorator

### Complexity & Size Limits
- Cyclomatic complexity ≤ 10
- Number of methods per file ≤ 20
- File length: ideally < 100 lines, must not exceed 200 lines
- Methods should not have more than 4 parameters (use an object if more are needed)

### Code Hygiene
- No magic numbers — use named constants with meaningful values
- No unused variables
- No misspelled words
- Sentences in comments/messages must end with a period

### Patterns
- Use the AAA (Arrange, Act, Assert) pattern in tests
- Tests should use tags with the feature at the beginning of the test name
- Use DDT (Data Driven Testing) strategy for data management

## Code Review Prompts

### Junior/Mid Level — Run Before Pushing

```
Check if code below meets the listed rules:
###LANGUAGE AND FRAMEWORK###
- TypeScript
- Playwright
###RULES###
- Cyclomatic Complexity <= 10
- lowerCamelCase for: variable, parameter, function, method, property, module alias
- CONSTANT_CASE for: global constants and enum members
- Class names should follow UpperCamelCase. Same for Interface, Type, Enum, Decorator
- Use the AAA (arrange, act, assert) pattern
- Test should use tags with the feature (beginning of the test name)
- Number of methods <= 20
- File should not have more than 200 lines of code, ideally less than 100
- Identify any magic numbers
- Methods should not have more than 4 params. If so, params should be transformed into object
- Variables not being used
- Misspelled words in English
- Sentences missing period at the end
###RESPONSE FORMAT###
First give the conclusion of the review, then the code with changes applied.
```

### Senior/Principal/Staff Level — Run Before Pushing

```
Check if code below meets the listed rules:
###LANGUAGE AND FRAMEWORK###
- TypeScript
- Playwright
###RULES###
- Best Design Patterns and Principles (SOLID, DRY, KISS)
- Code Complexity (cyclomatic ≤ 10, cognitive complexity, time complexity)
###RESPONSE FORMAT###
First give the conclusion of the review, then the code with changes applied.
```

## Tools
- GitHub Copilot for VSCode (for coding assistance)
- Replace sensitive data before running any review prompts
