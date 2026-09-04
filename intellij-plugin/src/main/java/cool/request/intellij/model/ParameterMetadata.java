package cool.request.intellij.model;

/**
 * Model representing an action parameter
 */
public class ParameterMetadata {
    private String name;
    private String type;
    private boolean required;
    private Object example;
    private String source; // path, query, body

    public ParameterMetadata() {}

    public ParameterMetadata(String name, String type) {
        this.name = name;
        this.type = type;
        this.required = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Object getExample() {
        return example;
    }

    public void setExample(Object example) {
        this.example = example;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
