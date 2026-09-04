# Demo Grails Application for Cool Request Plugin

This directory contains a sample Grails 2.5.3 application for testing the Cool Request plugin.

## Setup Instructions

### Step 1: Create a New Grails 2.5.3 Application

```bash
grails create-app demo-cool-request
cd demo-cool-request
```

### Step 2: Install the Cool Request Plugin

```bash
grails install-plugin /path/to/cool-request-grails-0.1.zip
```

### Step 3: Create Sample Controllers

Create the following controllers in `grails-app/controllers/demo/`:

#### UserController.groovy
```groovy
package demo

class UserController {

    def index() {
        render contentType: 'application/json'
        render '''{"users": [{"id": 1, "name": "John"}, {"id": 2, "name": "Jane"}]}'''
    }

    def show() {
        def id = params.id
        render contentType: 'application/json'
        render """{"id": ${id}, "name": "User ${id}"}"""
    }

    def save() {
        render contentType: 'application/json'
        render '''{"status": "created", "id": 123}'''
    }

    def update() {
        render contentType: 'application/json'
        render '''{"status": "updated"}'''
    }

    def delete() {
        render contentType: 'application/json'
        render '''{"status": "deleted"}'''
    }
}
```

#### OrderController.groovy
```groovy
package demo

class OrderController {

    def index() {
        render contentType: 'application/json'
        render '''{"orders": []}'''
    }

    def show() {
        def id = params.id
        render contentType: 'application/json'
        render """{"id": ${id}, "status": "pending"}"""
    }

    def create() {
        render contentType: 'application/json'
        render '''{"status": "order created"}'''
    }
}
```

#### ProductController.groovy
```groovy
package demo

class ProductController {

    def search(String name, Integer page, Integer limit) {
        render contentType: 'application/json'
        render '''{"products": [], "page": 1, "total": 0}'''
    }

    def details(Long productId) {
        render contentType: 'application/json'
        render """{"productId": ${productId}, "name": "Product ${productId}"}"""
    }
}
```

### Step 4: Create Command Objects

Create `grails-app/controllers/demo/UserCommand.groovy`:

```groovy
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

Create `grails-app/controllers/demo/OrderCommand.groovy`:

```groovy
package demo

class OrderCommand {

    Long userId
    List<Long> productIds
    String shippingAddress

    static constraints = {
        userId nullable: false
        productIds nullable: false, empty: false
        shippingAddress blank: false
    }
}
```

### Step 5: Create Domain Classes

Create `grails-app/domain/demo/User.groovy`:

```groovy
package demo

class User {

    String name
    String email

    static constraints = {
        name blank: false
        email email: true
    }

    String toString() {
        "${name} (${email})"
    }
}
```

Create `grails-app/domain/demo/Order.groovy`:

```groovy
package demo

class Order {

    User user
    BigDecimal total
    String status

    static constraints = {
        user nullable: false
        total nullable: false
        status inList: ['pending', 'confirmed', 'shipped', 'delivered']
    }
}
```

Create `grails-app/domain/demo/Product.groovy`:

```groovy
package demo

class Product {

    String name
    BigDecimal price
    Integer stock

    static constraints = {
        name blank: false
        price nullable: false, min: 0
        stock min: 0
    }
}
```

### Step 6: Configure URL Mappings

Edit `grails-app/conf/UrlMappings.groovy`:

```groovy
class UrlMappings {

    static mappings = {
        "/api/users"(controller: "user", action: "index")
        "/api/users/$id"(controller: "user", action: "show")
        "/api/users"(controller: "user", action: "save", method: "POST")
        "/api/users/$id"(controller: "user", action: "update", method: "PUT")
        "/api/users/$id"(controller: "user", action: "delete", method: "DELETE")

        "/api/orders"(controller: "order", action: "index")
        "/api/orders/$id"(controller: "order", action: "show")
        "/api/orders"(controller: "order", action: "create", method: "POST")

        "/api/products/search"(controller: "product", action: "search")
        "/api/products/$productId"(controller: "product", action: "details")

        "/"(view: "/index")
        "500"(view: '/error')
    }
}
```

### Step 7: Create Sample Jobs

Create `grails-app/jobs/demo/CleanupJob.groovy`:

```groovy
package demo

class CleanupJob {

    static triggers = {
        cron name: 'cleanupTrigger', cronExpression: '0 0 2 * * ?' // Daily at 2 AM
    }

    def execute() {
        println "Running cleanup job..."
        // Simulate cleanup logic
        Thread.sleep(1000)
        println "Cleanup completed!"
    }
}
```

Create `grails-app/jobs/demo/ReportJob.groovy`:

```groovy
package demo

class ReportJob {

    static triggers = {
        simple name: 'reportTrigger', startDelay: 60000, repeatInterval: 3600000 // Hourly
    }

    def execute() {
        println "Generating report..."
        // Simulate report generation
        Thread.sleep(2000)
        println "Report generated!"
    }
}
```

### Step 8: Create Sample Filter

Create `grails-app/conf/demo/ApiFilters.groovy`:

```groovy
package demo

class ApiFilters {

    def filters = {
        apiFilters(controller: '*', action: '*') {
            before = {
                log.info "Processing request: ${controllerName}.${actionName}"
                return true
            }
            after = { Map model ->
                log.info "Response sent for: ${controllerName}.${actionName}"
            }
        }
    }
}
```

### Step 9: Run the Application

```bash
grails run-app
```

### Step 10: Test with Cool Request

Open your browser and navigate to:

```
http://localhost:8080/demo-cool-request/cool-request
```

You should see:
- 3 Controllers (User, Order, Product)
- Multiple endpoints discovered from URL mappings
- 2 Jobs (CleanupJob, ReportJob)
- 3 Domain classes
- 2 Command objects

## Testing Checklist

Use Cool Request to test:

- [ ] GET /api/users - List all users
- [ ] GET /api/users/123 - Get single user
- [ ] POST /api/users - Create user (with JSON body)
- [ ] PUT /api/users/123 - Update user
- [ ] DELETE /api/users/123 - Delete user
- [ ] GET /api/orders - List orders
- [ ] GET /api/orders/456 - Get order
- [ ] POST /api/orders - Create order
- [ ] GET /api/products/search?name=test&page=1&limit=10 - Search products
- [ ] GET /api/products/789 - Get product details
- [ ] Execute CleanupJob manually
- [ ] Execute ReportJob manually
- [ ] Export OpenAPI specification
- [ ] Generate cURL commands

## Expected Discovery Results

After clicking **Refresh** in Cool Request:

- **Controllers**: 3 (User, Order, Product)
- **Endpoints**: ~10 (from URL mappings)
- **Jobs**: 2 (CleanupJob, ReportJob)
- **Domain Classes**: 3 (User, Order, Product)
- **Command Objects**: 2 (UserCommand, OrderCommand)
