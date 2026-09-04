package cool.request.model

/**
 * Request execution result
 */
class RequestResult implements Serializable {
    int status
    String statusText
    long executionTimeMs
    Map<String, List<String>> headers = [:]
    String body
    String contentType
    long contentLength
    List<String> cookies = []
    boolean success = false
    String errorMessage
    String errorType
    
    RequestResult() {}
    
    boolean isJson() {
        return contentType?.contains('application/json')
    }
    
    boolean isXml() {
        return contentType?.contains('application/xml') || contentType?.contains('text/xml')
    }
    
    boolean isHtml() {
        return contentType?.contains('text/html')
    }
    
    boolean isImage() {
        return contentType?.startsWith('image/')
    }
    
    @Override
    String toString() {
        return "RequestResult{status=$status, time=${executionTimeMs}ms, success=$success}"
    }
}
