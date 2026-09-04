package cool.request.model

/**
 * Metadata for a Grails domain class
 */
class DomainMetadata implements Serializable {
    String name
    String className
    String packageName
    List<PropertyMetadata> properties = []
    Map<String, Object> constraints = [:]
    boolean hasId = true
    String idType = "Long"
    
    DomainMetadata() {}
    
    DomainMetadata(String name, String className) {
        this.name = name
        this.className = className
    }
    
    @Override
    String toString() {
        return "DomainMetadata{name='$name', className='$className', properties=${properties?.size() ?: 0}}"
    }
}

/**
 * Metadata for a domain property
 */
class PropertyMetadata implements Serializable {
    String name
    String type
    boolean required = false
    boolean unique = false
    String constraint
    Object defaultValue
    
    PropertyMetadata() {}
    
    PropertyMetadata(String name, String type) {
        this.name = name
        this.type = type
    }
}
