# Contributing to Astagfirullah

First off, thank you for considering contributing to Astagfirullah! It's people like you that make this plugin better for the Muslim developer community.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Features](#suggesting-features)
  - [Adding Translations](#adding-translations)
  - [Code Contributions](#code-contributions)
- [Development Setup](#development-setup)
- [Pull Request Process](#pull-request-process)
- [Style Guidelines](#style-guidelines)

## Code of Conduct

This project and everyone participating in it is governed by our commitment to creating a welcoming and respectful environment. Please be kind and courteous to others.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the [existing issues](https://github.com/alfayedoficial/Astagfirullah-plugin-version/issues) to avoid duplicates.

**When reporting a bug, include:**

- **Clear title** describing the issue
- **Steps to reproduce** the behavior
- **Expected behavior** vs actual behavior
- **Screenshots** if applicable
- **Environment details:**
  - IDE name and version (e.g., IntelliJ IDEA 2024.1)
  - Plugin version
  - Operating system

**Bug Report Template:**
```markdown
## Description
[Clear description of the bug]

## Steps to Reproduce
1. Go to '...'
2. Click on '...'
3. See error

## Expected Behavior
[What you expected to happen]

## Actual Behavior
[What actually happened]

## Environment
- IDE: [e.g., IntelliJ IDEA 2024.1]
- Plugin Version: [e.g., 2.0.0]
- OS: [e.g., macOS 14.0]

## Screenshots
[If applicable]
```

### Suggesting Features

Feature requests are welcome! Please provide:

- **Clear description** of the feature
- **Use case** - why is this feature needed?
- **Proposed solution** (if you have one)
- **Alternatives** you've considered

### Adding Translations

We welcome new language translations! To add a new language:

1. **Fork** the repository
2. Add phrases to `TranslatePhrases.kt`:
   ```kotlin
   private val yourLanguagePhrases = listOf(
       "Phrase 1 in your language",
       "Phrase 2 in your language",
       // ... add all 31 phrases
   )
   ```
3. Update `staticPhrasesMap`:
   ```kotlin
   "YourLanguage" to yourLanguagePhrases
   ```
4. Update `titlesMap`:
   ```kotlin
   "YourLanguage" to "Remember Allah in your language"
   ```
5. Add to `AstagfirullahSettings.SUPPORTED_LANGUAGES`
6. Add sample to `FirstRunSetupDialog.samplePhrases`
7. Submit a **Pull Request**

### Code Contributions

1. **Fork** the repository
2. **Clone** your fork
3. Create a **feature branch**
4. Make your changes
5. **Test** thoroughly
6. Submit a **Pull Request**

## Development Setup

### Prerequisites

- **JDK 17** or higher
- **Gradle 8.x** (included via wrapper)
- **IntelliJ IDEA** (recommended for development)

### Setup Steps

```bash
# 1. Clone your fork
git clone https://github.com/YOUR_USERNAME/Astagfirullah-plugin-version.git
cd Astagfirullah-plugin-version

# 2. Open in IntelliJ IDEA
# File → Open → Select the project directory

# 3. Build the project
./gradlew build

# 4. Run in sandbox IDE
./gradlew runIde
```

### Project Structure

```
src/main/
├── kotlin/com/alfayedoficial/astagfirullah/
│   ├── core/           # Constants, utilities
│   ├── data/           # API, cache, models, sync
│   ├── *.kt            # Main plugin classes
└── resources/
    ├── META-INF/       # plugin.xml
    ├── icons/          # Plugin icons
    └── raw/            # Audio files
```

## Pull Request Process

### Before Submitting

1. **Update** your fork with the latest `main`:
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Test** your changes:
   ```bash
   ./gradlew build
   ./gradlew runIde  # Manual testing
   ```

3. **Ensure** no compilation errors or warnings

### PR Guidelines

1. **Create a descriptive title**:
   - `feat: Add Bengali language support`
   - `fix: Resolve rating prompt showing repeatedly`
   - `docs: Update README with build instructions`

2. **Fill in the PR template**:
   ```markdown
   ## Description
   [What does this PR do?]

   ## Type of Change
   - [ ] Bug fix
   - [ ] New feature
   - [ ] Documentation
   - [ ] Refactoring

   ## Related Issue
   Fixes #[issue number]

   ## Testing
   [How did you test this?]

   ## Screenshots
   [If applicable]
   ```

3. **Keep PRs focused** - one feature/fix per PR

4. **Respond to review feedback** promptly

### After Submitting

- Maintainers will review your PR
- Address any requested changes
- Once approved, your PR will be merged

## Style Guidelines

### Kotlin Code Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use **4 spaces** for indentation (no tabs)
- Maximum line length: **120 characters**
- Use **meaningful names** for variables and functions

### Code Examples

**Good:**
```kotlin
/**
 * Returns phrases for the specified language.
 * @param language The language code
 * @return List of phrases
 */
fun getPhrasesForLanguage(language: String): List<String> {
    return phrasesMap[language] ?: defaultPhrases
}
```

**Avoid:**
```kotlin
// Bad: No documentation, unclear naming
fun getPhrases(l: String): List<String> {
    return pm[l] ?: dp
}
```

### Commit Messages

Use conventional commit format:

```
type(scope): description

[optional body]

[optional footer]
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Formatting (no code change)
- `refactor`: Code restructuring
- `test`: Adding tests
- `chore`: Maintenance

**Examples:**
```
feat(translations): add Turkish language support

fix(audio): resolve sound not playing on Windows

docs(readme): add contribution guidelines
```

### Documentation

- Add **KDoc comments** for public classes and functions
- Update **README.md** if adding new features
- Update **CHANGELOG.md** for notable changes

## Questions?

Feel free to:
- Open an [issue](https://github.com/alfayedoficial/Astagfirullah-plugin-version/issues) for questions
- Contact the maintainer on [LinkedIn](https://www.linkedin.com/in/alfayedoficial)

---

**JazakAllahu Khairan** (May Allah reward you with goodness) for contributing!
