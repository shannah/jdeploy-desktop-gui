package ca.weblite.jdeploy.app.exceptions;

public class InvalidProjectException extends Exception {
    private String projectPath;
    private String reason;

    public InvalidProjectException(String projectPath, String reason) {
        super("Project at path " + projectPath + " is invalid.  Reason: " + reason);
        this.projectPath = projectPath;
        this.reason = reason;
    }

    public InvalidProjectException(String projectPath, String reason, Throwable cause) {
        super("Project at path " + projectPath + " is invalid", cause);
        this.projectPath = projectPath;
        this.reason = reason;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getReason() {
        return reason;
    }
}
