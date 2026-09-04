package cool.request.model

/**
 * Metadata for an action parameter
 */
class ParameterMetadata implements Serializable {
    String name
    String type
    boolean required = false
    Object example
    boolean isPathParameter = false
    boolean isQueryParameter = true
    String defaultValue
    List<String> allowedValues = []
    
    ParameterMetadata() {}
    
    ParameterMetadata(String name, String type) {
        this.name = name
        this.type = type
    }
    
    @Override
    String toString() {
        return "ParameterMetadata{name='$name', type='$type', required=$required}"
    }
}
