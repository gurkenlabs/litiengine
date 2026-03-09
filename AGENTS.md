# AGENTS.md - LITIENGINE Developer Guide

This file provides guidance for AI agents working on the LITIENGINE codebase.

## Project Overview

LITIENGINE is a free and open-source Java 2D game engine. It uses Gradle as the build system and JUnit 5 for testing.

- **Language**: Java 17+
- **Build System**: Gradle (wrapper: `./gradlew`)
- **Test Framework**: JUnit 5 (Jupiter)
- **Code Style**: Spotless with custom Eclipse formatter

---

## Build Commands

### Full Build (without tests)
```bash
./gradlew build -x test -PskipSpotless
```

### Run All Tests
```bash
./gradlew test
```

### Run Tests with Coverage Report
```bash
./gradlew jacocoTestReport
```

### Run a Single Test Class
```bash
./gradlew :litiengine:test --tests "de.gurkenlabs.litiengine.util.MathUtilitiesTests"
```

### Run a Single Test Method
```bash
./gradlew :litiengine:test --tests "de.gurkenlabs.litiengine.util.MathUtilitiesTests.testRound"
```

### Run Tests in a Specific Module
```bash
./gradlew :litiengine:test        # Main engine
./gradlew :shared:test            # Shared utilities
./gradlew :utiliti:test           # Map editor
```

### Code Style: Check
```bash
./gradlew spotlessCheck
```

### Code Style: Apply Auto-fix
```bash
./gradlew spotlessApply
```

### Static Analysis (SonarQube)
```bash
./gradlew sonar -PskipSpotless
```

### Clean Build
```bash
./gradlew clean build
```

---

## Code Style Guidelines

### General Principles

- Use the **Gurkenlabs Java Code Style** (see `config/gurkenlabs.eclipseformat.xml`)
- Run `spotlessApply` before committing
- All public methods must have Javadoc comments
- Write unit tests for new features

### Formatting Rules

- **Indentation**: 2 spaces (not tabs)
- **Line length**: Max 120 characters for code, 80 for comments
- **Braces**: End-of-line style (K&R)
- **Imports**: Sorted alphabetically, grouped by package
- **Blank lines**: One blank line between import groups, two between class members

### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `Game`, `EntityTests` |
| Interfaces | PascalCase with 'I' prefix | `ICollisionEntity` |
| Methods | camelCase | `getX()`, `initGame()` |
| Variables | camelCase | `gameLoop`, `entityList` |
| Constants | UPPER_SNAKE_CASE | `EXIT_GAME_CLOSED` |
| Test Classes | `ClassNameTests` | `MathUtilitiesTests` |
| Test Methods | `testMethodName` | `testRound()`, `testIntClamp()` |

### Package Structure

```
de.gurkenlabs.litiengine
  |-- annotations/    # Custom annotations
  |-- attributes/    # Attribute system (health, mana, etc.)
  |-- configuration/ # Game configuration classes
  |-- environment/   # Map, entities, tilemap
  |-- graphics/      # Rendering, animations, particles
  |-- gui/           # UI components
  |-- input/         # Keyboard, mouse, gamepad
  |-- physics/       # Collision, movement
  |-- resources/     # Fonts, images, sounds
  |-- sound/         # Audio playback
  |-- util/          # Utility classes
  |-- abilities/     # Skill/ability system
  |-- networking/   # Multiplayer support
```

### Import Order

1. `java.*` packages
2. `javax.*` packages
3. Third-party libraries
4. `de.gurkenlabs.litiengine` project imports
5. Static imports

### Error Handling

- Use unchecked exceptions (RuntimeException subclasses) for programming errors
- Use checked exceptions for recoverable conditions that callers should handle
- Always log errors with appropriate level before throwing
- Never expose raw exceptions to end users

### Type Usage

- **Prefer interfaces** over concrete classes for method parameters (`List<T>` not `ArrayList<T>`)
- **Use primitives** for performance-critical numeric types
- **Avoid raw types** - always use generics (`List<String>` not `List`)
- **Use Optional** for nullable return values

### Logging

- Use `java.util.logging.Logger`
- Log at appropriate levels: `FINEST` (debug), `INFO` (info), `WARNING` (warn), `SEVERE` (error)
- Never log sensitive information (passwords, keys)

---

## Testing Guidelines

### Test Naming

- Test class: `{ClassName}Tests` (e.g., `EntityTests`)
- Test method: `test{methodName}_{scenario}` (e.g., `testGetX_returnsCorrectValue`)

### Test Structure

```java
package de.gurkenlabs.litiengine.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class MathUtilitiesTests {

  @Test
  void testRound() {
    // Arrange
    float value = 4.3f;
    
    // Act
    float result = MathUtilities.round(value, 0);
    
    // Assert
    assertEquals(4, result, 0.0001);
  }
}
```

### Test Patterns

- Use `@ParameterizedTest` with `@CsvSource` for multiple inputs
- Use `@Nested` for organizing related tests
- Use `assertThrows()` for exception testing
- Use `assertAll()` for grouping related assertions

### Test Dependencies

```groovy
testImplementation project(":shared")
testImplementation libs.bundles.junit
testImplementation libs.bundles.mockito
```

---

## IDE Setup

### Eclipse
1. Import project as Gradle project
2. Window → Preferences → Java → Code Style → Formatter
3. Import `config/gurkenlabs.eclipseformat.xml`

### IntelliJ / VS Code
1. Import project as Gradle project
2. File → Settings → Editor → Code Style → Java
3. Import `config/gurkenlabs.eclipseformat.xml` (or use Spotless plugin)

---

## Common Gradle Tasks

| Task | Description |
|------|-------------|
| `./gradlew build` | Full build with tests |
| `./gradlew test` | Run tests only |
| `./gradlew jar` | Create JAR file |
| `./gradlew javadoc` | Generate Javadoc |
| `./gradlew dependencies` | Show dependency tree |
| `./gradlew projects` | List subprojects |

---

## Module Structure

| Module | Description |
|--------|-------------|
| `litiengine` | Core game engine |
| `shared` | Shared test utilities |
| `utiliti` | Map editor (utiLITI) |

---

## CI/CD

GitHub Actions runs:
1. `./gradlew build -x test -PskipSpotless` - Build without tests
2. `./gradlew jacocoTestReport` - Test execution with coverage
3. `./gradlew sonar` - Static analysis
4. `./gradlew spotlessCheck` - Code style validation

---

## Additional Resources

- [Contribution Guide](CONTRIBUTING.md)
- [LITIENGINE Docs](https://litiengine.com/docs/)
- [LITIENGINE Forum](https://forum.litiengine.com/)
