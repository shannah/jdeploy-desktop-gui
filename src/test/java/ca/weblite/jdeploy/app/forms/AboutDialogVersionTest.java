package ca.weblite.jdeploy.app.forms;

/**
 * Unit test for AboutDialog version logic without GUI
 */
public class AboutDialogVersionTest {
    public static void main(String[] args) {
        System.out.println("Testing AboutDialog version logic...");
        
        // Test 1: Version 1.0.0 (should show version)
        System.setProperty("jdeploy.app.version", "1.0.0");
        System.setProperty("jdeploy.commitHash", "abc123def456");
        String result1 = getVersionText();
        System.out.println("Test 1 - Version 1.0.0: " + result1);
        assert result1.equals("Version: 1.0.0") : "Expected 'Version: 1.0.0', got: " + result1;
        
        // Test 2: Version 0.0.0 (should show commit hash)
        System.setProperty("jdeploy.app.version", "0.0.0-SNAPSHOT");
        System.setProperty("jdeploy.commitHash", "commit123456789");
        String result2 = getVersionText();
        System.out.println("Test 2 - Version 0.0.0: " + result2);
        assert result2.equals("Commit: commit123456789") : "Expected 'Commit: commit123456789', got: " + result2;
        
        // Test 3: No system properties (should show defaults)
        System.clearProperty("jdeploy.app.version");
        System.clearProperty("jdeploy.commitHash");
        String result3 = getVersionText();
        System.out.println("Test 3 - No properties: " + result3);
        assert result3.equals("Version: Unknown") : "Expected 'Version: Unknown', got: " + result3;
        
        // Test 4: Version 0.0.0 but no commit hash
        System.setProperty("jdeploy.app.version", "0.0.0");
        System.clearProperty("jdeploy.commitHash");
        String result4 = getVersionText();
        System.out.println("Test 4 - Version 0.0.0, no commit: " + result4);
        assert result4.equals("Commit: Unknown") : "Expected 'Commit: Unknown', got: " + result4;
        
        System.out.println("All tests passed!");
    }
    
    // Copy of the logic from AboutDialog for testing
    private static String getVersionText() {
        String DEFAULT_VERSION = "Unknown";
        String DEFAULT_COMMIT = "Unknown";
        
        String version = System.getProperty("jdeploy.app.version", DEFAULT_VERSION);
        String commitHash = System.getProperty("jdeploy.commitHash", DEFAULT_COMMIT);
        
        // If version starts with "0.0.0", show commit hash instead
        if (version.startsWith("0.0.0")) {
            return "Commit: " + commitHash;
        } else {
            return "Version: " + version;
        }
    }
}