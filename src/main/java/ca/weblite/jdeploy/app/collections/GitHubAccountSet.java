package ca.weblite.jdeploy.app.collections;

public class GitHubAccountSet implements Iterable<ca.weblite.jdeploy.app.records.GitHubAccount> {

    private final java.util.Set<ca.weblite.jdeploy.app.records.GitHubAccount> gitHubAccounts = new java.util.LinkedHashSet<>();

    public GitHubAccountSet(ca.weblite.jdeploy.app.records.GitHubAccount... gitHubAccounts) {
        this.gitHubAccounts.addAll(java.util.Arrays.asList(gitHubAccounts));
    }

    public java.util.Iterator<ca.weblite.jdeploy.app.records.GitHubAccount> iterator() {
        return gitHubAccounts.iterator();
    }
}
