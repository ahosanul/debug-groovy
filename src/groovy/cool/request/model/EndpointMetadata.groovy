package cool.request.model

/**
 * Metadata for a URL mapping endpoint
 */
class EndpointMetadata implements Serializable {
    String path
    String httpMethod
    String controller
    String action
    List<ParameterMetadata> parameters = []
    String mappingName
    boolean isRestful = false
    Map<String, Object> constraints = [:]
    
    EndpointMetadata() {}
    
    EndpointMetadata(String path, String httpMethod, String controller, String action) {
        this.path = path
        this.httpMethod = httpMethod
        this.controller = controller
        this.action = action
    }
    
    @Override
    String toString() {
        return "EndpointMetadata{path='$path', method='$httpMethod', controller='$controller', action='$action'}"
    }
}
