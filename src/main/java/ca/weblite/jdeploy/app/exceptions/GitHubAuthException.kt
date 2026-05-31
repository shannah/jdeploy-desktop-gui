package ca.weblite.jdeploy.app.exceptions

/**
 * Raised when GitHub credentials are missing, invalid or expired so the create
 * project flow can prompt the user to (re-)authenticate instead of surfacing a
 * raw error.
 */
class GitHubAuthException(message: String, cause: Throwable? = null) : Exception(message, cause)
