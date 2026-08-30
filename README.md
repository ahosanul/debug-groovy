# Grails Debug Assistant

An IntelliJ IDEA plugin for Grails developers that bridges the gap between standard Java/Groovy debugging and Grails-specific runtime inspection, with seamless "Cool Request" style HTTP debugging integration.

## Features

### 1. Grails Context Evaluator
A specialized Groovy evaluation console that understands Grails artifacts (Domains, Services, Controllers) during a debug session. It auto-imports common Grails packages (e.g., `grails.gorm.*`).

### 2. "Cool Request" Endpoint Extractor
When paused at a breakpoint inside a Grails Controller, provides an action to automatically extract:
- Current HTTP method
- Route and path variables
- Request parameters
- Formats them into a ready-to-use HTTP request snippet (compatible with "Cool Request" plugin or IntelliJ's native HTTP Client)

### 3. GSP & Taglib Quick-Inspect
A tool window panel that shows which GSP view or Taglib is associated with the current controller action being debugged, allowing quick navigation.

### 4. Hot-Script Execution
Write and execute ad-hoc Groovy scripts against the currently suspended debug frame's context (similar to Groovy Console Plugin but tightly bound to the debugger state).

## UI Design

The plugin features a modern UI inspired by the "Cool Request" plugin:

- **Tool Window**: Named "Grails Debug"
- **Layout**: Horizontal split pane (JBSplitter)
  - **Left Panel (30%)**: SimpleTree displaying "Grails Artifacts in Context"
    - Current Controller
    - Associated Domain Classes
    - Active Services
  - **Right Panel (70%)**: JBTabs with two tabs:
    - **Tab 1: "Evaluate"**: EditorTextField with Groovy syntax highlighting, Execute button, and read-only output console
    - **Tab 2: "Request Builder"**: Form mimicking "Cool Request" UI with Method dropdown, URL field, JSON/Params editor, and "Copy to Cool Request" button

## Requirements

- IntelliJ IDEA 2026.1.4 RC or later
- Groovy plugin installed
- Grails plugin installed (optional, for enhanced features)
- Kotlin 2.x runtime (bundled with IntelliJ)

## Installation

### From Source

1. Clone this repository
2. Open in IntelliJ IDEA
3. Run `./gradlew buildPlugin`
4. Install the plugin from `build/distributions/grails-debug-assistant-*.zip`

### Manual Installation

1. Download the plugin ZIP file
2. In IntelliJ IDEA: `Settings > Plugins > ⚙️ > Install Plugin from Disk...`
3. Select the downloaded ZIP file
4. Restart IntelliJ IDEA

## Usage

### During Debug Session

1. Start debugging your Grails application
2. Set a breakpoint in a Controller action
3. When the debugger pauses:
   - Open the "Grails Debug" tool window (bottom panel)
   - View artifacts in context (left panel)
   - Evaluate Groovy expressions (right panel, "Evaluate" tab)
   - Extract HTTP request (right panel, "Request Builder" tab)

### Extract to Cool Request

1. While paused at a breakpoint in a Controller:
   - Right-click in the debugger variables panel
   - Select "Grails Debug > Extract to Cool Request"
   - The HTTP request is copied to clipboard

### Editor Gutter Action

1. Open a Grails Controller file
2. Look for the gutter icon next to action methods
3. Click to generate and copy an HTTP request template

## Project Structure

```
src/main/kotlin/com/arafat/grails/debug/
├── GrailsDebugToolWindowFactory.kt    # Tool window factory
├── GrailsDebuggerSessionListener.kt   # Debug session listener
├── evaluator/
│   └── GrailsDebuggerEvaluator.kt     # Expression evaluation logic
├── ui/
│   ├── GrailsDebugPanel.kt            # Main UI panel
│   ├── GrailsArtifactTreeModel.kt     # Tree model for artifacts
│   └── GrailsScriptEditorProvider.kt  # Script editor provider
├── action/
│   ├── ExtractToCoolRequestAction.kt  # Extract HTTP request action
│   ├── EvaluateInContextAction.kt     # Evaluate expression action
│   ├── NavigateToGspAction.kt         # Navigate to GSP view action
│   └── ControllerGutterAction.kt      # Editor gutter action
└── util/
    ├── GrailsArtifactResolver.kt      # Artifact resolution utility
    └── GspViewLocator.kt              # GSP view location utility
```

## Building

```bash
./gradlew buildPlugin
```

The plugin will be built in `build/distributions/`.

## Development

```bash
# Run IDE with plugin
./gradlew runIde

# Verify plugin
./gradlew verifyPlugin

# Build plugin
./gradlew buildPlugin
```

## Compatibility

- **Target IDE**: IntelliJ IDEA 2026.1+
- **Groovy Version**: 2.5.3+
- **Grails Version**: 3.x, 4.x, 5.x
- **Kotlin**: 2.x

## License

Copyright 2024 Arafat Hossain. All rights reserved.

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

## Acknowledgments

- UI/UX inspiration from the "Cool Request" plugin
- Built with the IntelliJ Platform Plugin SDK
- Leveraging IntelliJ's Groovy and Grails plugin infrastructure
