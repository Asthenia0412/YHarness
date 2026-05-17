
package com.yancy.yharness.context;

import java.util.ArrayList;
import java.util.List;

public class ToolDefinition {
    
    private String name;
    private String description;
    private List<ToolParameter> parameters = new ArrayList<>();
    private String returnType = "string";

    public ToolDefinition() {
    }

    public ToolDefinition(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ToolParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<ToolParameter> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(ToolParameter parameter) {
        this.parameters.add(parameter);
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
}
