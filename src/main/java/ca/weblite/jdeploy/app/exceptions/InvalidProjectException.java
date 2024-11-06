package ca.weblite.jdeploy.app.exceptions;

public class InvalidProjectException extends Exception {
    private String projectPath;

    public InvalidProjectException(String projectPath) {
        super("Project at path " + projectPath + " is invalid");
        this.projectPath = projectPath;
    }

    public String getProjectPath() {
        return projectPath;
    }
}
