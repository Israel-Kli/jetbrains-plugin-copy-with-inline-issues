# Copy with inline issues for AI

![Build](https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/workflows/Build/badge.svg)
![Version](https://img.shields.io/jetbrains/plugin/v/com.github.israelkli.intellijplugincopyfilewithproblems)
![Downloads](https://img.shields.io/jetbrains/plugin/d/com.github.israelkli.intellijplugincopyfilewithproblems)

<!-- Plugin description -->
Copy code with errors and warnings as inline comments. Perfect for sharing with AI assistants (ChatGPT, Claude, Gemini, Cursor), code reviews, and bug reports.

## The Problem

When you paste broken code into ChatGPT or Claude, the AI can't see your IDE's red squiggly lines. You end up manually typing out error messages — wasting time and introducing mistakes.

## The Problem

When you paste broken code into ChatGPT or Claude, the AI can't see your IDE's red squiggly lines. You end up manually typing out error messages — wasting time and introducing mistakes.

## The Solution

Right-click → **Copy with inline issues** → paste into any AI tool. Errors appear as comments in the correct syntax for your language.

### Before vs After

**You copy this:**
```python
def calculate(a, b):
    result = a + c
    return result
```

**You paste this:**
```python
# FILE: calculator.py
def calculate(a, b):
    result = a + c
    # ERROR: undefined variable 'c'
    return result
```

## How it works

1. **Select code** in the editor, or **right-click a file** in the project tree
2. Choose **"Copy with inline issues"** from the context menu
3. **Paste** into ChatGPT, Claude, Gemini, Cursor, or any AI tool

## Features

- **AI-Ready Output** — Paste directly into ChatGPT, Claude, Gemini, Copilot, Cursor, or any LLM for instant debugging help
- **Copy Selection or Entire File** — Works from the editor (selection) and project tree (full file)
- **Multi-Language** — Java, Kotlin, JavaScript, TypeScript, Python, PHP, C/C++, C#, Go, Ruby, Rust, SQL, HTML, CSS, YAML, and more
- **Comprehensive Detection** — Captures syntax errors, semantic issues, and inspection warnings
- **Language-Aware Comments** — Automatically uses the correct comment syntax for each language

### Supported Languages

| Languages | Comment Format |
|-----------|---------------|
| Java, Kotlin, JS, TS, C/C++, C#, Go, Rust | `// ERROR: message` |
| Python, Ruby, Shell, YAML | `# ERROR: message` |
| SQL, Lua, Haskell | `-- ERROR: message` |
| HTML, XML | `<!-- ERROR: message -->` |
| CSS | `/* ERROR: message */` |
<!-- Plugin description end -->

## Compatibility

Works with all JetBrains IDEs:
IntelliJ IDEA, WebStorm, PyCharm, PhpStorm, GoLand, CLion, Rider, DataGrip, and more.

## Installation

**From JetBrains Marketplace:**

[![Install from Marketplace](https://img.shields.io/badge/Install-from%20Marketplace-blue?style=for-the-badge&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0Ij48cGF0aCBmaWxsPSJ3aGl0ZSIgZD0iTTEyIDJDNi40OCAyIDIgNi40OCAyIDEyczQuNDggMTAgMTAgMTAgMTAtNC40OCAxMC0xMFMxNy41MiAyIDEyIDJ6bS0xIDE0LjV2LTlsNyA0LjUtNyA0LjV6Ii8+PC9zdmc+)](https://plugins.jetbrains.com/plugin/27910-copy-with-inline-issues)

**From IDE:**
<kbd>Settings</kbd> → <kbd>Plugins</kbd> → <kbd>Marketplace</kbd> → Search **"Copy with inline issues"** → <kbd>Install</kbd>

**Manual:**
Download from [GitHub Releases](https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/releases/latest), then <kbd>Settings</kbd> → <kbd>Plugins</kbd> → <kbd>⚙️</kbd> → <kbd>Install Plugin from Disk</kbd>

## Development

### Building the Plugin

```bash
./gradlew buildPlugin -x buildSearchableOptions
```

Output: `build/distributions/`

**Other commands:**
- `./gradlew runIde` — Launch sandbox IDE for testing
- `./gradlew test` — Run tests
- `./gradlew jar` — Quick compile (faster for dev)

---

Inspired by [Code-File-Grabber](https://plugins.jetbrains.com/plugin/21269-code-file-grabber), [CopyWithProblems](https://plugins.jetbrains.com/plugin/23051-copywithproblems), and [SLAMP](https://plugins.jetbrains.com/plugin/26544-slamp).
