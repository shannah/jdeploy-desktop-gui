package ca.weblite.jdeploy.app.forms

import javax.swing.*

interface NewProjectFormInterface {
    val displayName: JTextField
    val groupId: JTextField
    val artifactId: JTextField
    val projectLocation: JTextField
    val projectTemplate: JComboBox<String>
    val selectProjectLocationButton: JButton
    val refreshTemplatesButton: JButton
    val npmProjectName: JTextField
    val githubRepositoryUrl: JTextField
    val githubReleasesRepositoryUrl: JTextField
    val createGithubReleasesRepositoryCheckBox: JCheckBox
    val createGithubRepositoryUrlCheckBox: JCheckBox
    val npmRadioButton: JRadioButton
    val gitHubReleasesRadioButton: JRadioButton
    val createProjectButton: JButton
}