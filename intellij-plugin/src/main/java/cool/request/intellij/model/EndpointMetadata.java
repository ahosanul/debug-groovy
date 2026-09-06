package cool.request.intellij.model;

import java.util.List;

/**
 * Model representing an endpoint (URL mapping)
 */
public class EndpointMetadata {
    private String path;
    private String httpMethod;
    private String controller;
    private String action;
    private List<ParameterMetadata> parameters;

    public EndpointMetadata() {}

    public EndpointMetadata(String path, String httpMethod, String controller, String action) {
        this.path = path;
        this.httpMethod = httpMethod;
        this.controller = controller;
        this.action = action;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<ParameterMetadata> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterMetadata> parameters) {
        this.parameters = parameters;
    }
}
