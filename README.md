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
- **15+ languages** — Java, Kotlin, JavaScript, TypeScript, Python, Go, Rust, C/C++, PHP, and more
- **Finds real problems** — syntax errors, missing variables, wrong types, and inspection warnings
- **Correct comment style** — each language gets its own comment format (see table below)

### Supported Languages

| Languages | Comment Format |
|-----------|---------------|
| Java, Kotlin, JS, TS, C/C++, C#, Go, Rust, PHP | `// ERROR: message` |
| Python, Ruby, Shell, YAML, Dockerfile | `# ERROR: message` |
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
