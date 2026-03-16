# Copy with inline issues

![Build](https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/workflows/Build/badge.svg)
![Version](https://img.shields.io/jetbrains/plugin/v/com.github.israelkli.intellijplugincopyfilewithproblems)
![Downloads](https://img.shields.io/jetbrains/plugin/d/com.github.israelkli.intellijplugincopyfilewithproblems)

## Copy Code with Errors & Warnings Inline

Never lose context when sharing code snippets or entire files. This plugin automatically includes all compilation errors, warnings, and syntax issues as inline comments.

<!-- Plugin description -->
Copy code with errors and warnings as inline comments. Perfect for sharing with AI assistants (Claude, ChatGPT, Gemini), code reviews, and bug reports.

## Key Features

- **Smart Copy** - Copy selected code or entire files with inline error/warning comments
- **Multi-Language** - Supports Java, Kotlin, JavaScript, TypeScript, Python, PHP, C/C++, C#, Go, Ruby, Rust, SQL, HTML, CSS, YAML, and more
- **AI-Ready** - Optimized for sharing with AI coding assistants for quick fixes
- **Comprehensive Detection** - Captures syntax errors, semantic issues, and inspection warnings

## How It Works

1. **Right-click in editor** → Select "Copy with inline issues" for selected text
2. **Right-click on file in project tree** → Select "Copy file with inline issues" for entire files
3. **Paste anywhere** → Get your code with all errors as language-appropriate comments

## Example Output

**Python:**
```python
# FILE: calculator.py
def calculate(a, b):
    result = a + c
    # ERROR: undefined variable 'c'
    return result
```

**Java:**
```java
// FILE: Calculator.java
public class Calculator {
    public int add(int a, int b) {
        return a + c;  // ERROR: cannot resolve symbol 'c'
    }
}
```

<!-- Plugin description end -->

## Cross-Platform Excellence

This plugin has been specifically optimized for cross-platform compatibility:

### Key Benefits:

1. **Cross-Platform Compatibility**: Works consistently across IntelliJ IDEA, WebStorm, PyCharm, and other JetBrains IDEs
2. **Language-Aware Comments**: Automatically uses the correct comment syntax for each programming language
3. **Robust Error Detection**: Multiple fallback mechanisms ensure error detection works even when IDE-specific features aren't available
4. **Better Performance**: Optimized inspection running with proper error handling and limits
5. **Comprehensive Testing**: Tests verify language-specific functionality and cross-platform compatibility

### Language-Specific Comment Support:

- **Python, Ruby, Shell, YAML**: `# ERROR: message`
- **Java, JavaScript, TypeScript, Kotlin**: `// ERROR: message`
- **SQL, Lua, Haskell**: `-- ERROR: message`
- **HTML, XML**: `<!-- ERROR: message -->`
- **CSS**: `/* ERROR: message */`

### Enhanced Error Detection:

- **PSI-based detection**: Works across all IDE environments
- **Inspection system integration**: Leverages IDE-specific inspections when available
- **Fallback mechanisms**: Ensures functionality even when some features aren't available
- **Multiple detection methods**: Combines different approaches for comprehensive error coverage

## Compatibility

This plugin is compatible with all JetBrains IDEs including:
- IntelliJ IDEA (Community & Ultimate)
- WebStorm
- PyCharm (Community & Professional)
- PhpStorm
- GoLand
- CLion
- DataGrip
- Rider
- And other JetBrains IDEs based on the IntelliJ Platform

## Installation

- Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Copy with inline issues"</kbd> >
  <kbd>Install</kbd>
  
- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/27910-copy-file-with-problems) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/27910-copy-file-with-problems/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/Israel-Kli/jetbrains-plugin-copy-with-inline-issues/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


Plugin is inspired by other plugins which have only partial functionality or are outdated, such as:
- [Code-File-Grabber](https://plugins.jetbrains.com/plugin/21269-code-file-grabber)
- [CopyWithProblems](https://plugins.jetbrains.com/plugin/23051-copywithproblems)
- [SLAMP](https://plugins.jetbrains.com/plugin/26544-slamp)

## Development

### Building the Plugin

To build the plugin ZIP file for distribution:

```bash
mkdir -p build/tmp/buildSearchableOptions && ./gradlew buildPlugin -x buildSearchableOptions
```

This creates the distributable ZIP at: `build/distributions/jetbrains-plugin-copy-with-inline-issues-1.0.5.zip`

**If you encounter build issues, try a clean build:**
```bash
./gradlew clean buildPlugin -x buildSearchableOptions
```

**Alternative build commands:**
- `./gradlew build` - Full build with tests
- `./gradlew buildPlugin` - Full plugin build (may require closing IntelliJ)
- `./gradlew jar` - Compile only, faster for development

### Testing the Plugin

To run IntelliJ IDEA with the plugin for testing:

```bash
./gradlew runIde
```

This launches a sandbox IntelliJ instance with your plugin pre-installed.

### Installation from Source

After building:
1. Install the ZIP file: <kbd>Settings</kbd> → <kbd>Plugins</kbd> → <kbd>⚙️</kbd> → <kbd>Install Plugin from Disk</kbd>
2. Select: `build/distributions/jetbrains-plugin-copy-with-inline-issues-1.0.5.zip`
3. Restart IntelliJ to ensure clean plugin loading
4. Test both actions:
   - **Editor**: Select text → right-click → "Copy with inline issues"
   - **Project Tree**: Right-click file → "Copy file with inline issues"

---