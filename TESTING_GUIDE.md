# Cool Request for Grails - Testing Guide

## Overview

This document provides comprehensive testing instructions for the Cool Request for Grails plugin.

## Test Suite Summary

### Unit Tests (5 test classes)

Located in `/workspace/test/unit/cool/request/`:

1. **ControllerDiscoveryServiceTests.groovy**
   - Tests controller discovery functionality
   - Validates controller name extraction
   - Verifies action metadata extraction

2. **UrlMappingDiscoveryServiceTests.groovy**
   - Tests URL mapping discovery
   - Validates path variable extraction
   - Tests static vs dynamic path handling

3. **ParameterDiscoveryServiceTests.groovy**
   - Tests primitive type detection
   - Validates example value generation
   - Tests various parameter types

4. **CurlExporterTests.groovy**
   - Tests cURL command generation
   - Validates header inclusion
   - Tests URL escaping

5. **OpenApiExporterTests.groovy**
   - Tests OpenAPI spec generation
   - Validates endpoint addition
   - Tests different HTTP methods

### Integration Tests (4 test classes)

Located in `/workspace/test/integration/cool/request/`:

1. **DiscoveryIntegrationTests.groovy**
   - End-to-end controller discovery
   - URL mapping integration
   - Export service integration

2. **RequestExecutionIntegrationTests.groovy**
   - HTTP request execution
   - Header preservation
   - Error handling

3. **JobDiscoveryIntegrationTests.groovy**
   - Job discovery from Grails application
   - Manual vs scheduled job detection
   - Job metadata validation

4. **ScriptExecutionIntegrationTests.groovy**
   - Pre-request script execution
   - Post-request script execution
   - Security boundary testing

## Running Tests

### Prerequisites

1. Grails 2.5.3 installed and configured
2. Java 7 or 8 (compatible with Grails 2.5.3)
3. Plugin installed in a Grails application

### Running Unit Tests

```bash
cd /path/to/grails-app-with-plugin
grails test-app unit:cool.request
```

Or run specific test:

```bash
grails test-app unit:cool.request.ControllerDiscoveryServiceTests
```

### Running Integration Tests

```bash
grails test-app integration:cool.request
```

Or run specific test:

```bash
grails test-app integration:cool.request.DiscoveryIntegrationTests
```

### Running All Tests

```bash
grails test-app
```

## Manual Testing Checklist

### Discovery Features

- [ ] Open `/cool-request` in browser
- [ ] Verify controllers are listed in sidebar
- [ ] Expand controller nodes to see actions
- [ ] Verify URL mappings are discovered
- [ ] Check parameter types are correctly identified
- [ ] Click refresh button to re-scan application

### Request Execution

- [ ] Select an endpoint from the tree
- [ ] Verify HTTP method is pre-selected
- [ ] Enter path parameters
- [ ] Enter query parameters
- [ ] Add custom headers
- [ ] Enter JSON body for POST/PUT requests
- [ ] Click "Send Request"
- [ ] Verify response status code
- [ ] Verify response body is displayed
- [ ] Check response time is shown

### Developer Tools

- [ ] Click "Copy as cURL" - verify command is correct
- [ ] Click "Export OpenAPI" - verify JSON is valid
- [ ] Save a request to history
- [ ] Re-run a request from history
- [ ] Test environment variable substitution
- [ ] Execute pre-request script
- [ ] Execute post-request script

### UI/UX

- [ ] Search for controllers/actions
- [ ] Use keyboard shortcuts (Ctrl+Enter, Ctrl+K)
- [ ] Toggle dark/light theme
- [ ] Resize panels
- [ ] Test on different screen sizes

## Creating a Demo Application

To fully test the plugin, create a sample Grails 2.5.3 application:

### Step 1: Create Grails App

```bash
grails create-app cool-request-demo
cd cool-request-demo
```

### Step 2: Install Plugin

Copy plugin files to `plugins/cool-request-grails/` and add to `BuildConfig.groovy`:

```groovy
grails.project.plugins.local = ["plugins/cool-request-grails"]
```

### Step 3: Create Test Controllers

```groovy
// grails-app/controllers/demo/UserController.groovy
package demo

class UserController {

    def index() {
        render contentType: 'application/json'
        render '''{"users": [{"id": 1, "name": "John"}]}'''
    }

    def show(Long id) {
        render contentType: 'application/json'
        render """{"id": $id, "name": "User $id"}"""
    }

    def save() {
        render contentType: 'application/json'
        render '''{"status": "created", "id": 123}'''
    }

    def update(Long id) {
        render contentType: 'application/json'
        render """{"id": $id, "status": "updated"}"""
    }

    def delete(Long id) {
        render contentType: 'application/json'
        render """{"id": $id, "status": "deleted"}"""
    }
}
```

### Step 4: Create URL Mappings

```groovy
// grails-app/conf/UrlMappings.groovy
class UrlMappings {

    static mappings = {
        "/api/users"(controller: "user", action: "index", method: "GET")
        "/api/users/$id"(controller: "user", action: "show", method: "GET")
        "/api/users"(controller: "user", action: "save", method: "POST")
        "/api/users/$id"(controller: "user", action: "update", method: "PUT")
        "/api/users/$id"(controller: "user", action: "delete", method: "DELETE")

        "/cool-request"(controller: "coolRequest", action: "index")
        "/cool-request/api/$action?"(controller: "coolRequest")
    }
}
```

### Step 5: Create Command Object

```groovy
// grails-app/commands/demo/UserCommand.groovy
package demo

class UserCommand {

    String name
    String email
    Integer age

    static constraints = {
        name blank: false
        email email: true
        age min: 0
    }
}
```

### Step 6: Run and Test

```bash
grails run-app
```

Open browser to `http://localhost:8080/cool-request`

## Expected Results

### Controller Discovery

Should find:
- UserController with 5 actions
- CoolRequestController with API endpoints

### URL Mapping Discovery

Should find:
- GET /api/users
- GET /api/users/{id}
- POST /api/users
- PUT /api/users/{id}
- DELETE /api/users/{id}

### Request Execution

All HTTP methods should work:
- GET returns user data
- POST creates new user
- PUT updates user
- DELETE removes user

## Troubleshooting

### Tests Not Running

Ensure test classes follow naming convention: `*Tests.groovy`

### Discovery Returns Empty Results

- Verify controllers are in `grails-app/controllers/`
- Check URL mappings in `UrlMappings.groovy`
- Restart application after adding new controllers

### Request Execution Fails

- Check CORS settings if testing cross-origin
- Verify authentication isn't blocking requests
- Check application logs for errors

### UI Not Loading

- Verify static resources are accessible
- Check browser console for JavaScript errors
- Ensure GSP views are compiled

## Performance Testing

Test with large applications:
- 50+ controllers
- 200+ endpoints
- Complex URL mappings
- Multiple job classes

Measure:
- Initial load time (< 3 seconds expected)
- Refresh time (< 1 second expected)
- Request execution time (varies by endpoint)

## Security Testing

Verify:
- Plugin is disabled in production by default
- Authentication can be enabled
- Sensitive data is not logged
- Script execution has proper boundaries
- Bean inspection is opt-in only

## Reporting Issues

When reporting bugs, include:
1. Grails version
2. Java version
3. Plugin version
4. Steps to reproduce
5. Expected vs actual behavior
6. Relevant log output
