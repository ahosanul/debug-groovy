# Quick Start Guide - Cool Request for Grails

## Installation in Your Grails 2.5.3 Application

### Option 1: Install from Source

1. **Clone or copy the plugin** to your project's `plugins` directory:
   ```bash
   cd /path/to/your/grails-app/plugins
   git clone https://github.com/arafat/cool-request-grails.git
   ```

2. **Add to BuildConfig.groovy**:
   ```groovy
   grails.project.work.dir = 'target/work'
   
   grails.project.dependency.resolution = {
       inherits("legacy-all")
       
       repositories {
           mavenCentral()
           grailsPlugins()
           grailsHome()
       }
       
       plugins {
           build ":tomcat:7.0.55"
           
           // Add Cool Request plugin
           compile ":cool-request:0.1.0"
           
           // Or reference local plugin
           // compile (plugin: 'cool-request', version: '0.1.0')
       }
   }
   ```

3. **Install the plugin**:
   ```bash
   grails clean
   grails compile
   grails install-plugin /path/to/cool-request-grails
   ```

### Option 2: Direct File Copy

Copy these directories to your Grails application:
```bash
# Copy plugin files
cp -r cool-request-grails/grails-app/* your-app/grails-app/
cp -r cool-request-grails/web-app/* your-app/web-app/
cp -r cool-request-grails/src/groovy/* your-app/src/groovy/
```

## Configuration

Add to `grails-app/conf/Config.groovy`:

```groovy
environments {
    development {
        coolRequest {
            enabled = true
            path = "/cool-request"
            allowProduction = false
            enableJobExecution = true
            enableScripts = true
            enableDirectInvocation = true
            enableBeanInspection = false
            maxHistory = 100
            responseMaxSize = 10 * 1024 * 1024
        }
    }
    
    test {
        coolRequest {
            enabled = true
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

## Running

1. **Start your Grails application**:
   ```bash
   grails clean
   grails compile
   grails run-app
   ```

2. **Open your browser** and navigate to:
   ```
   http://localhost:8080/your-app-name/cool-request
   ```

## What You'll See

Upon opening `/cool-request`, you should see:

- **Header**: App name, environment selector, refresh button
- **Left Sidebar**: List of all discovered controllers and actions
- **Center Panel**: Request editor (HTTP method, URL, parameters, headers, body)
- **Bottom Panel**: Response viewer (status, time, size, formatted body)

## Testing the Plugin

### Verify Controller Discovery

The plugin should automatically discover all controllers in your application. Check the left sidebar for your controllers like:
- UserController
- OrderController
- ProductController

### Test an Endpoint

1. Click on a controller action in the sidebar
2. The request editor will auto-populate with the endpoint details
3. Modify parameters as needed
4. Press **Ctrl+Enter** or click **Send Request**
5. View the response in the bottom panel

### Export Options

- **cURL**: Click "Copy as cURL" to get a curl command
- **OpenAPI**: Click "Export OpenAPI" to download API specification

## Troubleshooting

### Plugin Not Loading

Check `grails-app/conf/BuildConfig.groovy` includes the plugin:
```groovy
plugins {
    compile ":cool-request:0.1.0"
}
```

### Controllers Not Showing

1. Ensure controllers follow naming convention: `*Controller.groovy`
2. Check they are in `grails-app/controllers/`
3. Try clicking "Refresh Metadata" button

### 403 Forbidden Error

The plugin may be disabled for your environment. Check `Config.groovy`:
```groovy
coolRequest.enabled = true
```

### Production Warning

If you see a production warning, this is intentional. The plugin is designed for development use only.

## Security Notes

⚠️ **IMPORTANT**: This plugin exposes powerful debugging capabilities:

- Disabled in production by default
- Can execute arbitrary controller actions
- Supports Groovy script execution
- Can manually trigger scheduled jobs

**Never enable this plugin in production** unless you fully understand the security implications.

## Next Steps

1. Explore discovered controllers and endpoints
2. Test your APIs with different HTTP methods
3. Save frequently used requests
4. Export OpenAPI spec for documentation
5. Use cURL export for automation scripts

## Support

For issues or questions:
- GitHub Issues: https://github.com/arafat/cool-request-grails/issues
- Documentation: See README.md
