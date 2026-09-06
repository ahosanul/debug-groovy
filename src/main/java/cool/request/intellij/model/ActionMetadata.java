package cool.request.intellij.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Model representing a controller action with parameters and endpoints
 */
public class ActionMetadata {
    private String name;
    private String methodName;
    private List<ParameterMetadata> parameters;
    private List<EndpointMetadata> endpoints;
    private String returnType;
    private List<String> httpMethods;

    public ActionMetadata() {}

    public ActionMetadata(String name, String methodName, List<ParameterMetadata> parameters) {
        this.name = name;
        this.methodName = methodName;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<ParameterMetadata> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterMetadata> parameters) {
        this.parameters = parameters;
    }

    public List<EndpointMetadata> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<EndpointMetadata> endpoints) {
        this.endpoints = endpoints;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<String> getHttpMethods() {
        return httpMethods;
    }

    public void setHttpMethods(List<String> httpMethods) {
        this.httpMethods = httpMethods;
    }
}
