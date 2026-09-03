# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Build and Test
- **Build plugin**: `./gradlew buildPlugin` (creates distributable ZIP)
- **Clean build**: `./gradlew clean buildPlugin`
- **Run tests**: `./gradlew test`
- **Verify plugin**: `./gradlew verifyPlugin` (runs the JetBrains Plugin Verifier)
- **Run test IDE**: `./gradlew runIde` (launches IntelliJ with plugin for testing)
- **Build and run**: `./gradlew build` (full build with tests)
- **Quick compile**: `./gradlew jar` (faster for development)

Do not pass `-x buildSearchableOptions`: `prepareJarSearchableOptions` then fails on a
missing input directory. CI runs plain `buildPlugin`.

### Distribution
- Plugin ZIP location: `build/distributions/jetbrains-plugin-copy-with-inline-issues-<pluginVersion>.zip`
- Install manually: Settings → Plugins → ⚙️ → Install Plugin from Disk

## Architecture

This is an IntelliJ Platform plugin that adds context menu actions to copy code with inline error/warning comments.

### Key Components
- **Actions**: `CopyWithInlineIssues` (editor) and `CopyFileWithInlineIssues` (project tree)
- **Base Class**: `BaseFileAction` - handles file content building, comment formatting, and clipboard operations
- **Service**: `ProblemDetectionService` - detects errors/warnings using multiple methods:
  - IDE highlights via `DocumentMarkupModel`
  - Programmatic inspections via `LocalInspectionTool`
  - PSI error elements via `PsiErrorElement`

### Language Support
The plugin uses language-specific comment formats:
- Java/Kotlin/JavaScript/TypeScript: `// ERROR: message`
- Python/Ruby/Shell/YAML: `# ERROR: message`
- SQL/Lua/Haskell: `-- ERROR: message`
- HTML/XML: `<!-- ERROR: message -->`
- CSS: `/* ERROR: message */`

### Cross-IDE Compatibility
- Primary target: IntelliJ IDEA Community (IC) 2024.2.5
- Supports: WebStorm, PyCharm, PhpStorm, etc.
- No language-plugin dependencies: `plugin.xml` depends only on `com.intellij.modules.platform` and `com.intellij.modules.lang`
- Comment style resolves by language ID, then file name, then extension, so it stays correct in IDEs that do not bundle a given language plugin

### Test Strategy
- Uses `BasePlatformTestCase` for unit tests
- Tests both valid and invalid code scenarios
- Verifies language-specific comment formatting
- Tests located in `src/test/kotlin/`

## Plugin Configuration
- **ID**: `com.github.israelkli.intellijplugincopyfilewithproblems`
- **Version**: Managed in `gradle.properties`
- **Plugin XML**: `src/main/resources/META-INF/plugin.xml`
- **Icon**: `src/main/resources/icons/copyWithProblems.svg`

## Gradle Properties
- **Platform**: IntelliJ Community (IC) 2024.2.5
- **Build range**: 242 to 272.*
- **Kotlin**: JVM toolchain 21
- **Gradle**: 8.13