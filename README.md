# Copy with inline issues for AI

![Build](https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/workflows/Build/badge.svg)
![Version](https://img.shields.io/jetbrains/plugin/v/com.github.israelkli.intellijplugincopyfilewithproblems)
![Downloads](https://img.shields.io/jetbrains/plugin/d/com.github.israelkli.intellijplugincopyfilewithproblems)

<!-- Plugin description -->
A JetBrains IDE plugin that copies your code with all errors and warnings added as comments. Paste it into ChatGPT, Claude, Gemini, Cursor, or any AI tool. The AI will see every problem your IDE sees.

## The Problem

You have broken code. Your IDE shows red and yellow markers on the lines with errors. But when you copy the code and paste it into ChatGPT or Claude, those markers are gone. The AI does not know what is wrong.

So you have to explain each error yourself: what line it is on, what the message says. This takes time. You can forget some errors. You can make typos.

## The Solution

Right-click your code and pick **Copy with inline issues**. The plugin takes every error and warning from your IDE and adds them as comments right next to the lines where they appear. Each comment uses the correct comment style for the language.

You paste the result into any AI tool. The AI sees the file name, the code, and all the error messages on the right lines. You do not need to explain anything.

### Before and After

You select this code in your IDE:
```python
def calculate(a, b):
    result = a + c
    return result
```

You paste this into ChatGPT:
```python
# File: calculator.py

def calculate(a, b):
    result = a + c
    # ERROR: Unresolved reference 'c'
    return result
```

The AI now knows the file name, the line with the error, and the error message. You did not type any of that.

## How to Use

1. **Select code** in the editor, or **right-click a file** in the project tree
2. Pick **"Copy with inline issues"** from the menu
3. **Paste** into any AI tool. All errors and warnings are already in the text

## Features

- **Ready for AI tools** — paste into ChatGPT, Claude, Gemini, Copilot, Cursor, or any LLM
- **Two ways to copy** — select code in the editor, or right-click a file in the project tree
- **Runs in the background** — detection runs off the EDT with a progress indicator; no IDE freezes on large files
- **Copy notification** — a toast confirms how many lines were copied and how many issues were inlined
- **Severity filter** — choose which severity levels to include (errors, warnings, weak warnings, info) in Preferences → Tools → Copy with Inline Issues
- **Large-selection safety** — confirmation dialog before running inspection on selections over 500 lines
- **20+ languages** — Java, Kotlin, JavaScript, TypeScript, Python, Go, Rust, C/C++, C#, PHP, Ruby, Dart, Swift, and more
- **Finds real problems** — syntax errors, missing variables, wrong types, and IDE inspection warnings
- **Correct comment style** — layered language detection picks the right comment format per file (see table below)

### Supported Languages

| Languages | Comment Format |
|-----------|---------------|
| Java, Kotlin, JS, TS, C/C++, C#, Go, Rust, PHP, Swift, Dart, Scala | `// ERROR: message` |
| Python, Ruby, Shell, YAML, TOML, Dockerfile, Makefile, Terraform, R | `# ERROR: message` |
| SQL, Lua, Haskell, Ada, VHDL | `-- ERROR: message` |
| HTML, XML, Markdown, SVG | `<!-- ERROR: message -->` |
| CSS | `/* ERROR: message */` |
| INI, Clojure, Lisp, AutoHotkey | `; ERROR: message` |

Unknown languages fall back to `//` style. File extension is used as a hint when the language ID alone is ambiguous.
<!-- Plugin description end -->

## Settings

**Preferences → Tools → Copy with Inline Issues:**

- **Severity filter** — toggle which severities are inlined (errors / warnings / weak warnings / info). Default: all on.
- **Notification level** — `BALLOON`, `STATUS_BAR`, or `NONE`. Default: `STATUS_BAR`.
- **Comment-incompatible language fallback** — what to do with languages like JSON that have no comment syntax.

## Compatibility

Works with all JetBrains IDEs:
IntelliJ IDEA, WebStorm, PyCharm, PhpStorm, GoLand, CLion, Rider, DataGrip, and more.

Supported build range: 242 (2024.2) through 272.

## Installation

**From JetBrains Marketplace:**

[![Install from Marketplace](https://img.shields.io/badge/Install-from%20Marketplace-blue?style=for-the-badge&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0Ij48cGF0aCBmaWxsPSJ3aGl0ZSIgZD0iTTEyIDJDNi40OCAyIDIgNi40OCAyIDEyczQuNDggMTAgMTAgMTAgMTAtNC40OCAxMC0xMFMxNy41MiAyIDEyIDJ6bS0xIDE0LjV2LTlsNyA0LjUtNyA0LjV6Ii8+PC9zdmc+)](https://plugins.jetbrains.com/plugin/27910-copy-with-inline-issues)

**From IDE:**
<kbd>Settings</kbd> → <kbd>Plugins</kbd> → <kbd>Marketplace</kbd> → Search **"Copy with inline issues"** → <kbd>Install</kbd>

**Manual:**
Download from [GitHub Releases](https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/releases/latest), then <kbd>Settings</kbd> → <kbd>Plugins</kbd> → <kbd>⚙️</kbd> → <kbd>Install Plugin from Disk</kbd>

## Development

```bash
./gradlew buildPlugin -x buildSearchableOptions
```

Output: `build/distributions/`

**Other commands:**
- `./gradlew runIde` — Launch sandbox IDE for testing
- `./gradlew test` — Run tests
- `./gradlew verifyPlugin` — Run JetBrains plugin verifier
- `./gradlew jar` — Quick compile (faster for dev)

---

Inspired by [Code-File-Grabber](https://plugins.jetbrains.com/plugin/21269-code-file-grabber), [CopyWithProblems](https://plugins.jetbrains.com/plugin/23051-copywithproblems), and [SLAMP](https://plugins.jetbrains.com/plugin/26544-slamp).
