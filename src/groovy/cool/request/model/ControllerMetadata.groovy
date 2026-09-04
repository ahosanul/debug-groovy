package cool.request.model

/**
 * Metadata for a Grails controller
 */
class ControllerMetadata implements Serializable {
    String name
    String className
    String packageName
    List<ActionMetadata> actions = []
    
    ControllerMetadata() {}
    
    ControllerMetadata(String name, String className) {
        this.name = name
        this.className = className
    }
    
    @Override
    String toString() {
        return "ControllerMetadata{name='$name', className='$className', actions=${actions?.size() ?: 0}}"
    }
}
