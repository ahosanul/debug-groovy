# Cool Request for Grails - IntelliJ Plugin

An IntelliJ IDEA plugin that provides a developer tool window and gutter icons for testing Grails 2.5.3 controllers and endpoints directly from the IDE, **without requiring any project configuration or changes**.

## 🎯 Key Features

### ✅ Tool Window
- **View → Tool Windows → Cool Request** opens a dedicated panel on the right side of the IDE
- Automatically discovers and displays all Grails controllers and endpoints
- Click any endpoint to auto-populate the request editor
- Execute HTTP requests and view responses without leaving IntelliJ

### ✅ Gutter Run Icons
- Green run icons (▶) appear next to controller methods in the editor gutter
- Click to open a parameter dialog pre-filled with method signature info
- Enter parameter values and execute requests instantly

### ✅ Context Menu Actions
- Right-click any controller method → **"Run Grails Endpoint"**
- Dialog opens with inferred URL, HTTP method, and parameters

### ✅ Zero Configuration
- **No changes to your Grails project**
- **No plugins to install in your Grails application**
- Works immediately when your Grails app is running
- Discovers endpoints via reflection and URL mapping analysis

## 📋 Requirements

- IntelliJ IDEA 2022.3 or later
- Groovy plugin (bundled with IntelliJ)
- Java 11 or later
- Running Grails 2.5.3+ application

## 🚀 Installation

### Build from Source
```bash
mvn clean package
```

### Install Plugin
1. In IntelliJ: **Settings → Plugins → ⚙️ → Install Plugin from Disk**
2. Select the JAR file from `target/` directory
3. Restart IntelliJ

## 📖 Usage

### Method 1: Tool Window (Recommended)
1. Start your Grails app: `grails run-app`
2. Open IntelliJ with your Grails project
3. Go to **View → Tool Windows → Cool Request**
4. Browse controllers and endpoints in the tree view
5. Click an endpoint to auto-fill the request editor
6. Modify parameters/headers/body as needed
7. Click **Send** and view the response

### Method 2: Gutter Icons
1. Open any Grails controller (e.g., `UserController.groovy`)
2. Look for green ▶ icons next to methods
3. Click the icon → parameter dialog opens
4. Enter values → **Send Request**
5. View response in the dialog

### Method 3: Context Menu
1. Right-click a controller method
2. Select **Run Grails Endpoint**
3. Configure and execute the request

## 🔧 How It Works

```
┌─────────────────────┐
│   IntelliJ Plugin   │
│  (This Plugin)      │
└──────────┬──────────┘
           │ HTTP Requests
           ↓
┌─────────────────────┐
│  Grails Application │
│  (Running locally)  │
└──────────┬──────────┘
           │ Reflection & Analysis
           ↓
┌─────────────────────┐
│  Controller Discovery│
│  URL Mapping Parse  │
│  Request Execution  │
└─────────────────────┘
```

The plugin intelligently analyzes your running Grails application to discover:
- All controllers and their actions
- URL mappings and HTTP methods
- Method parameters and types
- Command objects and domain classes

Then it constructs and executes real HTTP requests against your endpoints.

## 🛠️ Configuration

**Defaults:**
- Base URL: `http://localhost:8080`
- API Path: Auto-detected

**No configuration needed** - the plugin works out of the box!

## 🐛 Troubleshooting

### "API Not Available" Error
- ✅ Ensure Grails app is running: `grails run-app`
- ✅ Verify app runs on port 8080 (default)
- ✅ Check firewall isn't blocking localhost

### No Gutter Icons
- ✅ File must be a Grails controller (name ends with `Controller`)
- ✅ Groovy plugin must be enabled
- ✅ Try: **File → Invalidate Caches / Restart**

### Tool Window Shows Empty Tree
- ✅ Confirm Grails app is running
- ✅ Check IntelliJ Event Log for errors
- ✅ Verify `/cool-request/api/controllers` returns JSON

## 🏗️ Project Structure

```
workspace/
├── pom.xml                          # Maven build config
├── README.md                        # This file
├── src/main/java/cool/request/intellij/
│   ├── action/
│   │   └── RunEndpointAction.java   # Context menu action
│   ├── gutter/
│   │   ├── GrailsControllerLineMarkerProvider.java
│   │   └── JavaControllerLineMarkerProvider.java
│   ├── model/
│   │   ├── ControllerMetadata.java
│   │   ├── ActionMetadata.java
│   │   ├── EndpointMetadata.java
│   │   └── ParameterMetadata.java
│   ├── service/
│   │   └── CoolRequestService.java  # Core business logic
│   ├── toolwindow/
│   │   └── CoolRequestToolWindowFactory.java
│   └── ui/
│       ├── CoolRequestPanel.java    # Tool window UI
│       └── EndpointDialog.java      # Parameter dialog
└── src/main/resources/
    ├── META-INF/
    │   └── plugin.xml               # Plugin descriptor
    └── icons/                       # Plugin icons
```

## 🔨 Development

### Build
```bash
mvn clean package
```

### Run Sandbox IDE
```bash
mvn org.jetbrains.intellij.plugins:prepare-sandbox
mvn org.jetbrains.intellij.plugins:run-ide
```

### Debug
1. Create Gradle run configuration with `runIde` task
2. Set breakpoints in plugin code
3. Launch sandbox IntelliJ instance

## 📦 Future Enhancements

- [ ] Settings UI for custom base URLs
- [ ] Request history and collections
- [ ] Environment variables support
- [ ] cURL export
- [ ] Syntax-highlighted response viewer
- [ ] Direct URL mapping parsing from UrlMappings.groovy
- [ ] Integration with IntelliJ HTTP Client
- [ ] Command object parameter support

## 📄 License

MIT License

## 💬 Support

Open an issue on the project repository for bugs or feature requests.
