# Cool Request for Grails - IntelliJ Plugin

An IntelliJ IDEA plugin that provides a developer tool window and gutter icons for testing Grails 2.5.3 controllers and endpoints directly from the IDE, without requiring any project configuration.

## Features

### Tool Window
- **View → Tool Windows → Cool Request** opens a dedicated panel on the right side of the IDE
- Displays all discovered Grails controllers and their endpoints in a tree view
- Click on any endpoint to automatically populate the request editor
- Execute HTTP requests and view responses without leaving the IDE

### Gutter Run Icons
- Green run icons appear next to controller methods in the editor gutter
- Click the icon to open a parameter dialog pre-filled with method signature information
- Enter parameter values and execute requests directly

### Context Menu Action
- Right-click on any controller method and select "Run Grails Endpoint"
- Opens the endpoint dialog with inferred URL and parameters

### No Configuration Required
- Zero changes needed to your Grails project
- No plugins to install in your Grails application (optional companion plugin enhances functionality)
- Works out of the box when your Grails app is running

## Requirements

- IntelliJ IDEA 2022.3 or later
- Groovy plugin installed (bundled with IntelliJ)
- Java 11 or later
- Running Grails 2.5.3+ application (for full functionality)

## Installation

### From JAR (Development)
1. Build the plugin: `mvn clean package`
2. Go to **Settings → Plugins → ⚙️ → Install Plugin from Disk**
3. Select the generated JAR file from `target/` directory
4. Restart IntelliJ

### From JetBrains Marketplace (Future)
Once published, install directly from **Settings → Plugins → Marketplace**

## Usage

### Using the Tool Window

1. Start your Grails application: `grails run-app`
2. Open IntelliJ and load your Grails project
3. Go to **View → Tool Windows → Cool Request** (or click the CR icon on the right sidebar)
4. The tool window will attempt to connect to your running Grails app at `http://localhost:8080/cool-request/api`
5. Browse controllers and endpoints in the tree view
6. Click an endpoint to auto-populate the request editor
7. Modify parameters/headers/body as needed
8. Click **Send** to execute the request
9. View the response in the response panel

### Using Gutter Icons

1. Open any Grails controller file (e.g., `UserController.groovy`)
2. Look for green run icons (▶) next to controller methods
3. Click the icon to open the endpoint dialog
4. The dialog pre-fills:
   - HTTP method (inferred from action)
   - URL (inferred from controller/method name)
   - Parameters (from method signature)
5. Enter parameter values
6. Click **Send Request** to execute
7. View response in the dialog

### Using Context Menu

1. Right-click on a controller method in the editor
2. Select **Run Grails Endpoint** from the context menu
3. Use the dialog to configure and execute the request

## Architecture

```
intellij-plugin/
├── src/main/java/cool/request/intellij/
│   ├── action/           # Context menu and gutter actions
│   │   └── RunEndpointAction.java
│   ├── gutter/           # Line marker providers
│   │   ├── GrailsControllerLineMarkerProvider.java
│   │   └── JavaControllerLineMarkerProvider.java
│   ├── model/            # Data models
│   │   ├── ControllerMetadata.java
│   │   ├── ActionMetadata.java
│   │   ├── EndpointMetadata.java
│   │   └── ParameterMetadata.java
│   ├── service/          # Business logic
│   │   └── CoolRequestService.java
│   ├── toolwindow/       # Tool window factory
│   │   └── CoolRequestToolWindowFactory.java
│   └── ui/               # UI components
│       ├── CoolRequestPanel.java
│       └── EndpointDialog.java
├── src/main/resources/
│   ├── META-INF/
│   │   └── plugin.xml    # Plugin descriptor
│   └── icons/            # Plugin icons
└── pom.xml               # Maven build configuration
```

## How It Works

The IntelliJ plugin communicates with the **Cool Request for Grails** backend plugin running in your Grails application:

```
IntelliJ Plugin
      ↓ HTTP
Grails App + Cool Request Plugin
      ↓
Controller Discovery
URL Mapping Analysis
Request Execution
```

### Communication Flow

1. **Discovery**: Plugin fetches controller metadata from `/cool-request/api/controllers`
2. **Display**: Tree view shows controllers → actions → endpoints
3. **Execution**: HTTP requests sent to your Grails app's actual endpoints
4. **Response**: Results displayed in the tool window or dialog

## Configuration

The plugin uses these defaults:
- Base URL: `http://localhost:8080`
- API Path: `/cool-request/api`

To change the base URL (e.g., different port):
1. Go to **File → Settings → Tools → Cool Request** (future feature)
2. Or modify in code: `CoolRequestService.setBaseUrl()`

## Companion Grails Plugin

For full functionality, install the **Cool Request for Grails** plugin in your Grails application:

```groovy
// grails-app/conf/BuildConfig.groovy
grails.project.dependency.resolution = {
    plugins {
        compile ":cool-request:1.0.0"
    }
}
```

This provides:
- Controller discovery API
- URL mapping inspection
- Request execution endpoint
- Metadata export

## Troubleshooting

### "Cool Request API Not Available"
- Ensure your Grails application is running: `grails run-app`
- Verify the Cool Request plugin is installed in your Grails app
- Check that the app is running on port 8080 (or update base URL)
- Confirm no firewall is blocking localhost connections

### No Gutter Icons Appearing
- Make sure you're editing a Grails controller class (name ends with "Controller")
- Ensure the Groovy plugin is enabled in IntelliJ
- Try invalidating caches: **File → Invalidate Caches / Restart**

### Tool Window Not Loading Controllers
- Check IntelliJ's Event Log for error messages
- Verify your Grails app has the Cool Request backend plugin installed
- Ensure the `/cool-request/api/controllers` endpoint returns valid JSON

## Development

### Building

```bash
cd intellij-plugin
mvn clean package
```

### Running Tests

```bash
mvn test
```

### Debugging

1. Create a Gradle run configuration in IntelliJ
2. Use the `runIde` task to launch a sandbox IntelliJ instance
3. Your plugin will be automatically installed in the sandbox

## Future Enhancements

- [ ] Configuration UI for base URL and other settings
- [ ] Request history within IntelliJ
- [ ] Saved requests/collections
- [ ] Environment variables support
- [ ] cURL export from IntelliJ
- [ ] Response body viewer with syntax highlighting
- [ ] Automatic URL mapping detection from Grails UrlMappings.groovy
- [ ] Integration with IntelliJ's HTTP Client
- [ ] Support for Grails command objects in parameter dialogs

## License

MIT License - See LICENSE file for details

## Support

For issues or feature requests, please open an issue on the project repository.
