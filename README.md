# Cool Request for Grails

A developer-focused API inspection, testing, debugging, and automation tool for **Grails 2.5.3 applications**.

## Overview

Cool Request for Grails is a reusable plugin that provides a browser-based developer UI for inspecting and testing your Grails application's APIs. It automatically discovers controllers, actions, URL mappings, and more, making it easy to test APIs without manually creating Postman collections or writing test code.

## Features

### Automatic Discovery
- **Controllers & Actions**: Automatically discovers all Grails controllers and their actions
- **URL Mappings**: Inspects and displays all URL mappings with HTTP methods
- **Parameters**: Extracts action method signatures including parameter names and types
- **Command Objects**: Detects and displays Grails command objects
- **Domain Classes**: Shows domain class metadata for reference
- **Jobs**: Discovers Grails/Quartz scheduled jobs

### API Testing
- **HTTP Methods**: Support for GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
- **Request Editor**: Full-featured editor for URLs, parameters, headers, and body
- **Response Viewer**: Intelligent response formatting (JSON, XML, HTML, text, binary)
- **Response Metadata**: Status codes, execution time, response size, headers

### Developer Tools
- **cURL Export**: Generate cURL commands from any request
- **OpenAPI Export**: Generate OpenAPI 3.0 specifications from discovered endpoints
- **Request History**: Store and replay recent requests
- **Saved Requests**: Organize frequently used requests into collections
- **Environment Variables**: Manage variables across different environments
- **Pre/Post Scripts**: Execute Groovy scripts before/after requests

### Security
- **Environment Protection**: Disabled by default in production
- **Access Control**: Optional authentication and IP allowlisting
- **Sensitive Data**: Does not log passwords, tokens, or API keys by default

## Installation

### Step 1: Build the Plugin

```bash
cd cool-request-grails
grails package-plugin
```

This creates a plugin ZIP file in the `target/` directory.

### Step 2: Install into Your Grails Application

In your Grails 2.5.3 application directory:

```bash
grails install-plugin /path/to/cool-request-grails-0.1.zip
```

Or add to `BuildConfig.groovy`:

```groovy
grails.project.dependency.resolution = {
    plugins {
        compile ':cool-request:0.1'
    }
}
```

### Step 3: Run Your Application

```bash
grails run-app
```

### Step 4: Access Cool Request

Open your browser and navigate to:

```
http://localhost:8080/your-app/cool-request
```

## Configuration

Add the following to `Config.groovy`:

```groovy
environments {
    development {
        coolRequest {
            enabled = true
            path = '/cool-request'
            allowProduction = false
            enableJobExecution = true
            enableScripts = true
            enableDirectInvocation = true
            enableBeanInspection = false
            maxHistory = 100
            responseMaxSize = 10 * 1024 * 1024 // 10MB
        }
    }
    
    test {
        coolRequest {
            enabled = true
            allowProduction = false
        }
    }
    
    production {
        coolRequest {
            enabled = false  // DISABLED by default!
            allowProduction = false
        }
    }
}
```

### Configuration Options

| Option | Default | Description |
|--------|---------|-------------|
| `enabled` | `true` (dev), `false` (prod) | Enable/disable the plugin |
| `path` | `/cool-request` | URL path to access the UI |
| `allowProduction` | `false` | Allow running in production environment |
| `enableJobExecution` | `true` | Allow manual job execution |
| `enableScripts` | `true` | Allow pre/post request scripts |
| `enableDirectInvocation` | `true` | Allow direct controller action invocation |
| `enableBeanInspection` | `false` | Allow Spring bean inspection |
| `maxHistory` | `100` | Maximum number of history entries |
| `responseMaxSize` | `10MB` | Maximum response size to display |

## Usage

### Browsing Controllers

1. Open `/cool-request` in your browser
2. The left sidebar shows all discovered controllers
3. Expand a controller to see its actions
4. Click an action to load it in the request editor

### Testing an API

1. Select an endpoint from the controller tree
2. Modify parameters, headers, or body as needed
3. Click **Send Request** (or press Ctrl+Enter)
4. View the response in the bottom panel

### Using Environment Variables

1. Select an environment from the dropdown (Development, Test, etc.)
2. Use variables in URLs like `{{baseUrl}}/api/users/{{userId}}`
3. Define variables in the environment settings

### Exporting to cURL

1. After configuring a request, click **Copy as cURL**
2. Paste into your terminal to reproduce the request

### Generating OpenAPI Specification

1. Click **Export OpenAPI** in the header
2. Download the generated `openapi.json` file
3. Import into Swagger UI, Postman, or other tools

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Ctrl+Enter` | Send request |
| `Ctrl+S` | Save request |
| `Ctrl+K` | Focus search |
| `Ctrl+/` | Format JSON |

## Security Considerations

⚠️ **IMPORTANT**: This plugin exposes powerful debugging capabilities:

1. **Never enable in production** without explicit security measures
2. The plugin can execute arbitrary controller actions
3. Job execution can trigger business logic
4. Script execution allows running Groovy code

### Recommended Security Settings

```groovy
// Config.groovy
coolRequest {
    // Disable in production
    enabled = (grails.util.Environment.current != grails.util.Environment.PRODUCTION)
    
    // Or use IP allowlist
    allowedIPs = ['127.0.0.1', '192.168.1.0/24']
    
    // Require authentication token
    authToken = 'your-secure-random-token'
}
```

## Troubleshooting

### Plugin Not Loading

Ensure you're running Grails 2.5.3:

```bash
grails --version
```

### No Controllers Discovered

- Make sure your application is fully compiled
- Click the **Refresh** button in the UI
- Check that controllers follow Grails naming conventions (*Controller suffix)

### Request Execution Fails

- Verify the URL is correct
- Check for authentication requirements
- Review application logs for errors

### CSS/JS Not Loading

Clear browser cache or force refresh (Ctrl+F5).

## Architecture

```
cool-request-grails/
├── grails-app/
│   ├── controllers/
│   │   └── CoolRequestController.groovy
│   ├── services/
│   │   ├── ControllerDiscoveryService.groovy
│   │   ├── UrlMappingDiscoveryService.groovy
│   │   ├── ParameterDiscoveryService.groovy
│   │   ├── RequestExecutionService.groovy
│   │   └── ...
│   └── views/
│       └── coolRequest/
│           └── index.gsp
├── src/groovy/cool/request/
│   ├── discovery/
│   ├── model/
│   ├── export/
│   └── util/
├── web-app/
│   ├── js/cool-request/
│   │   └── app.js
│   └── css/cool-request/
│       └── styles.css
└── test/
    └── unit/cool/request/
```

## Compatibility

- **Grails**: 2.5.3
- **Java**: 7 or 8
- **Groovy**: 2.4.x (bundled with Grails 2.5.3)
- **Browsers**: Modern browsers (Chrome, Firefox, Safari, Edge)

## License

MIT License - See LICENSE file for details.

## Contributing

Contributions are welcome! Please ensure compatibility with Grails 2.5.3.

## Support

For issues and feature requests, please open a GitHub issue.
