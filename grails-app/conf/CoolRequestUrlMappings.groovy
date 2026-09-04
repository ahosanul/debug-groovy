class CoolRequestUrlMappings {

    static mappings = {
        "/cool-request"(controller: 'coolRequest', action: 'index')
        "/cool-request/api/controllers"(controller: 'coolRequest', action: 'apiControllers', method: 'GET')
        "/cool-request/api/controllers/$name"(controller: 'coolRequest', action: 'apiController', method: 'GET')
        "/cool-request/api/mappings"(controller: 'coolRequest', action: 'apiMappings', method: 'GET')
        "/cool-request/api/jobs"(controller: 'coolRequest', action: 'apiJobs', method: 'GET')
        "/cool-request/api/environments"(controller: 'coolRequest', action: 'apiEnvironments', method: 'GET')
        "/cool-request/api/request"(controller: 'coolRequest', action: 'apiExecuteRequest', method: 'POST')
        "/cool-request/api/action"(controller: 'coolRequest', action: 'apiInvokeAction', method: 'POST')
        "/cool-request/api/jobs/$name/run"(controller: 'coolRequest', action: 'apiRunJob', method: 'POST')
        "/cool-request/api/history"(controller: 'coolRequest', action: 'apiHistory', method: 'GET')
        "/cool-request/api/history"(controller: 'coolRequest', action: 'apiSaveHistory', method: 'POST')
        "/cool-request/api/history/$id"(controller: 'coolRequest', action: 'apiDeleteHistory', method: 'DELETE')
        "/cool-request/api/openapi"(controller: 'coolRequest', action: 'apiOpenApi', method: 'GET')
    }
}
