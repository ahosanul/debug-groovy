package cool.request.intellij.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Model representing a Grails controller with its actions
 */
public class ControllerMetadata {
    private String name;
    private String className;
    private String packageName;
    private List<ActionMetadata> actions;

    public ControllerMetadata() {}

    public ControllerMetadata(String name, String className, String packageName, List<ActionMetadata> actions) {
        this.name = name;
        this.className = className;
        this.packageName = packageName;
        this.actions = actions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<ActionMetadata> getActions() {
        return actions;
    }

    public void setActions(List<ActionMetadata> actions) {
        this.actions = actions;
    }
}
