package cool.request.model

/**
 * Request execution context
 */
class RequestContext implements Serializable {
    String method = "GET"
    String url
    String path
    Map<String, String> pathParams = [:]
    Map<String, String> queryParams = [:]
    Map<String, String> headers = [:]
    Map<String, String> cookies = [:]
    String body
    String contentType = "application/json"
    String environment = "Development"
    boolean useDirectInvocation = false
    String preRequestScript
    String postRequestScript
    
    // For direct invocation
    String controllerName
    String actionName
    
    RequestContext() {}
    
    String getFullUrl() {
        StringBuilder sb = new StringBuilder(url ?: "")
        
        if (queryParams) {
            def queryString = queryParams.collect { key, value ->
                "${URLEncoder.encode(key, 'UTF-8')}=${URLEncoder.encode(value ?: '', 'UTF-8')}"
            }.join('&')
            
            if (queryString) {
                sb.append(sb.toString().contains('?') ? '&' : '?').append(queryString)
            }
        }
        
        return sb.toString()
    }
    
    @Override
    String toString() {
        return "RequestContext{method='$method', url='$url', controller='$controllerName', action='$actionName'}"
    }
}
