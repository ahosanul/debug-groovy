package cool.request.model

/**
 * Metadata for a controller action
 */
class ActionMetadata implements Serializable {
    String name
    String methodName
    List<ParameterMetadata> parameters = []
    List<EndpointMetadata> endpoints = []
    Set<String> httpMethods = [] as Set
    String returnType
    boolean isRestful = false
    
    ActionMetadata() {}
    
    ActionMetadata(String name, String methodName) {
        this.name = name
        this.methodName = methodName
    }
    
    void addHttpMethod(String method) {
        if (method) {
            httpMethods << method.toUpperCase()
        }
    }
    
    @Override
    String toString() {
        return "ActionMetadata{name='$name', methodName='$methodName', parameters=${parameters?.size() ?: 0}, endpoints=${endpoints?.size() ?: 0}}"
    }
}
