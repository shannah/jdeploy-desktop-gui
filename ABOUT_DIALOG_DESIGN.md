## About Dialog UI Layout

The AboutDialog creates a modal dialog with the following layout:

```
┌─────────────────────────────────────┐
│             About jDeploy           │ ← Window title
├─────────────────────────────────────┤
│                                     │
│               [ICON]                │ ← jDeploy icon (64x64)
│               jDeploy               │ ← Title in large bold font (24pt)
│                                     │
│           Version: 1.0.0            │ ← Version or commit info (14pt)
│                                     │
│                                     │
│               [OK]                  │ ← OK button (centered)
│                                     │
└─────────────────────────────────────┘
```

### Layout Details:
- **Window**: Modal dialog, non-resizable, minimum 300x200px
- **Icon**: 64x64 scaled version of `/ca/weblite/jdeploy/app/assets/icon.png`
- **Title**: "jDeploy" in 24pt bold SansSerif font
- **Version text**: Dynamic based on system properties:
  - Normal version: "Version: 1.0.0" 
  - Dev version (0.0.0*): "Commit: abc123def456"
  - Missing props: "Version: Unknown" or "Commit: Unknown"
- **OK Button**: Default button, responds to Enter and Escape keys
- **Padding**: 20px border around content, 10px spacing between sections

### System Properties Read:
- `jdeploy.app.version` - The application version
- `jdeploy.commitHash` - The git commit hash

### Version Display Logic:
```java
if (version.startsWith("0.0.0")) {
    return "Commit: " + commitHash;
} else {
    return "Version: " + version;
}
```

The dialog integrates seamlessly with the existing application through a new "Help" menu in the main window menu bar, accessible via "Help" → "About jDeploy".