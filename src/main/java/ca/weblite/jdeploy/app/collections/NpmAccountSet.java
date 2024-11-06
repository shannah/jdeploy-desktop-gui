package ca.weblite.jdeploy.app.collections;

public class NpmAccountSet implements Iterable<ca.weblite.jdeploy.app.records.NpmAccount> {

    private final java.util.Set<ca.weblite.jdeploy.app.records.NpmAccount> npmAccounts = new java.util.LinkedHashSet<>();

    public NpmAccountSet(ca.weblite.jdeploy.app.records.NpmAccount... npmAccounts) {
        this.npmAccounts.addAll(java.util.Arrays.asList(npmAccounts));
    }

    public java.util.Iterator<ca.weblite.jdeploy.app.records.NpmAccount> iterator() {
        return npmAccounts.iterator();
    }
}
