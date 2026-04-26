# Changelog

All notable changes to the "Copy with inline issues" plugin will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.4/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.2.1] - 2026-04-26

### Fixed

- **Crash on settings load** — Fixed `IllegalStateException: Cannot resolve $PRODUCT_WORKSPACE_FILE$` thrown when the plugin tried to access settings from an application-level service. Settings are now stored in a dedicated `copyWithInlineIssues.xml` file under the IDE config directory.
- **Reintroduced deprecated API** — Restored `ApplicationManager.getApplication().runReadAction(Computable)` in both copy actions, replacing the deprecated `ReadAction.compute(ThrowableComputable)` that was accidentally reintroduced in 1.2.0 (originally fixed in 1.1.2).
- **Higher idle resource usage** — Programmatic `LocalInspectionTool` execution is now skipped when the IDE Daemon has already produced highlights for the requested range. This avoids re-running up to five inspections on every copy when the cached results are already available, restoring v1.1.x-level resource usage on already-analyzed files.

## [1.2.0] - 2026-04-26

### Added

- **Settings page** — New "Copy with Inline Issues" settings in Preferences → Tools. Configure which severity levels to include (errors, warnings, weak warnings, info), comment-incompatible language fallback behavior, and notification preference.
- **Background task with progress indicator** — Problem detection now runs off the EDT in a background thread with a status-bar progress indicator. The IDE stays responsive even for large files.
- **Per-line dedup** — Issues are now deduplicated within each line rather than globally, so the same error type on different lines is correctly reported for each occurrence.

### Changed

- **Faster performance** — Problem detection now runs ONCE per file and buckets results by line number, instead of re-running all three detection methods for every line. This gives a ~Nx speedup for N-line files.
- **Detection runs in background** — Both "Copy with inline issues" (editor selection) and "Copy file with inline issues" (project tree) actions now queue detection in a cancellable background task with progress indication.

### Fixed

- **EDT freeze eliminated** — Previously, large file selections could freeze the IDE. Now the detection runs asynchronously with a status-bar progress indicator.

## [1.1.3]

### Changed

- **Improved README** - Rewrote plugin description and README in simple, clear English for better readability; removed duplicate section, fixed Before/After example to match actual plugin output, added PHP and Dockerfile to the supported languages table

## [1.1.2]

### Fixed

- **Deprecated API fix** - Replaced deprecated `ReadAction.compute(ThrowableComputable)` with `ApplicationManager.getApplication().runReadAction(Computable)` in `ProblemDetectionService.runInspectionsOnRange()` to resolve plugin verifier compatibility warning across all supported IDE builds (242–272)

## [1.1.1]

### Fixed

- **Deprecated API migration** - Replaced deprecated `runReadAction(Function0)` Kotlin extension with `ReadAction.compute(ThrowableComputable)` in `ProblemDetectionService.runInspectionsOnRange()` to resolve plugin verifier compatibility warning with IntelliJ Platform 261+

## [1.1.0]

### Changed

- **Renamed to "Copy with inline issues for AI"** - Better discoverability in the marketplace for users looking for AI-powered coding tools
- **Improved plugin description** - Clear before/after code example, step-by-step usage guide, and supported languages table
- **Updated README** - Redesigned GitHub page with better onboarding, fixed incorrect marketplace links, added demo GIF placeholder

## [1.0.9]

### Fixed

- **Deprecated API fix** - Replaced deprecated `HighlightSeverity.INFORMATION` with `HighlightSeverity.TEXT_ATTRIBUTES` to resolve the remaining deprecated API warning with IntelliJ Platform 261+

## [1.0.8]

### Fixed

- **Deprecated API migration** - Replaced deprecated `ReadAction.compute(ThrowableComputable)` with `runReadAction` to resolve compatibility warning with IntelliJ Platform 261+
- **Change notes not displayed** - Fixed build configuration that caused "What's New" section to always be empty in IDE plugin settings
- **Cleaned up plugin description** - Removed emoji icons from plugin description, changelog, and README for a cleaner presentation

## [1.0.7] - 2025-12-15

### Changed

- **Extended IDE compatibility** - Updated compatibility range to support IDE builds up to 272.* (through 2027.2)
- **2025.3 Support** - Now compatible with WebStorm 2025.3, IntelliJ IDEA 2025.3, and all other JetBrains IDEs version 2025.3
- **Improved plugin description** - Simplified and clarified the plugin description for better readability

### Technical

- Updated `pluginUntilBuild` from 252.* to 272.* in gradle.properties
- Streamlined README.md and plugin.xml descriptions for better user experience
- Ensures compatibility with IDE versions 2024.2 through 2027.2

## [1.0.6] - 2025-01-19

### Fixed

- **Deprecated API updates** - Replaced deprecated `AnActionEvent.getRequiredData()` calls with modern `getData()` API
- **Enhanced null safety** - Added explicit null checks for improved error handling and compatibility
- **Future-proofing** - Eliminated "scheduled for removal" API warnings in IntelliJ Platform 252+

### Technical

- Updated `CopyWithInlineIssues.kt` to use `getData()` with null safety
- Updated `CopyFileWithInlineIssues.kt` to use `getData()` with null safety
- Maintained identical functionality while improving API compatibility

## [1.0.5] - 2025-01-18

### Changed

- **Complete name consistency** - Updated all references from "Copy File with Problems" to "Copy with inline issues" across the entire project
- **Fixed marketplace display** - Plugin now correctly shows as "Copy with inline issues" in JetBrains Marketplace
- **Updated documentation** - All installation instructions and references use the new name consistently
- **Code cleanup** - Consistent naming throughout codebase, configuration files, and action classes

## [1.0.4] - 2025-01-18

### Changed

- **Plugin name** updated from "Copy File with Problems" to "Copy with inline issues"
- **Plugin description** updated to reflect new branding
- **Version bumped** to 1.0.4 across all configuration files
- **Documentation** updated to reflect new plugin name

### Technical

- Added CLAUDE.md for development guidance and Claude Code integration
- Updated GitHub Actions workflow with proper token usage
- **Automatic deployment** - Added automated plugin publishing to JetBrains Marketplace
- Improved build documentation and distribution paths

## [1.0.0] - 2025-01-13

### Added

- **Copy With Problems** action for selected text in the editor
- **Copy File With Inline Issues** action for entire files in a project tree
- Comprehensive error detection system with multiple detection methods:
  - PSI syntax error detection
  - Editor markup highlight analysis
  - IntelliJ inspection system integration
  - PsiReference resolution for unresolved symbols
- **Java-specific validations:**
  - Split identifier detection (e.g., "cacheM anager" -> "cacheManager")
  - Method declaration validation
  - Annotation placement verification
- **YAML-specific validations:**
  - Block mapping error detection ("Invalid child element in a block mapping")
  - Indentation validation
  - Tab usage detection
  - Special character warnings
- **Smart file headers:**
  - Selected text: Shows filename only
  - Full file: Shows a relative path from the project root
- **Context menu integration:**
  - Editor popup menu for selected text
  - Project view popup menu for files
- **Multi-language support** for all file types
- Custom icons for both actions

### Technical Features

- Multiple error detection strategies for comprehensive coverage
- Relative path calculation from the project root
- Distinct severity levels (ERROR, WARNING, INFO)
- Pattern-based validation for common syntax issues
- Integration with IntelliJ's inspection profile system

### Use Cases

- Code reviews with complete error context
- Bug reporting with precise error information
- Team collaboration without losing IDE context
- Documentation of known issues
- Stack Overflow posts with comprehensive context

### Requirements

- IntelliJ IDEA 2024.2+ (Build 242+)
- Compatible with all IntelliJ-based IDEs

[Unreleased]: https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/compare/v1.2.0...HEAD
[1.2.0]: https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/compare/v1.1.3...v1.2.0
[1.1.3]: https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/compare/v1.1.2...v1.1.3
[1.1.2]: https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/compare/v1.1.1...v1.1.2
[1.1.1]: https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/compare/v1.1.0...v1.1.1
[1.1.0]: https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/compare/v1.0.9...v1.1.0
[1.0.9]: https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/compare/v1.0.8...v1.0.9
[1.0.8]: https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/compare/v1.0.7...v1.0.8
[1.0.7]: https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/compare/v1.0.6...v1.0.7
[1.0.6]: https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/compare/v1.0.5...v1.0.6
[1.0.5]: https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/compare/v1.0.4...v1.0.5
[1.0.4]: https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/compare/v1.0.0...v1.0.4
[1.0.0]: https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/commits/v1.0.0
