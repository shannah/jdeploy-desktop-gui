package ca.weblite.jdeploy.app.repositories.impl.jpa.tests

import ca.weblite.jdeploy.app.repositories.impl.jpa.entities.GitHubAccountEntity
import ca.weblite.jdeploy.app.repositories.impl.jpa.entities.NpmAccountEntity
import ca.weblite.jdeploy.app.repositories.impl.jpa.entities.ProjectEntity
import ca.weblite.jdeploy.app.tests.BaseIntegrationTest
import org.junit.jupiter.api.*
import jakarta.persistence.*
import org.flywaydb.core.Flyway

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JpaIntegrationTest: BaseIntegrationTest() {

    @Test
    fun testGitHubAccountEntity() {
        val em = emf.createEntityManager()
        em.transaction.begin()

        // Create and persist a GitHubAccountEntity
        val gitHubAccount = GitHubAccountEntity(
            accountName = "My GitHub Account",
            username = "myusername",
            token = "ghp_sometoken"
        )
        em.persist(gitHubAccount)

        em.transaction.commit()

        // Verify it was saved
        val saved = em.find(GitHubAccountEntity::class.java, gitHubAccount.id)
        Assertions.assertNotNull(saved)
        Assertions.assertEquals("My GitHub Account", saved.accountName)
        Assertions.assertEquals("myusername", saved.username)
        Assertions.assertEquals("ghp_sometoken", saved.token)

        em.close()
    }

    @Test
    fun testNpmAccountEntity() {
        val em = emf.createEntityManager()
        em.transaction.begin()

        // Create and persist an NpmAccountEntity
        val npmAccount = NpmAccountEntity(
            accountName = "My NPM Account",
            username = "npmuser",
            password = "secret"
        )
        em.persist(npmAccount)

        em.transaction.commit()

        // Verify it was saved
        val saved = em.find(NpmAccountEntity::class.java, npmAccount.id)
        Assertions.assertNotNull(saved)
        Assertions.assertEquals("My NPM Account", saved.accountName)
        Assertions.assertEquals("npmuser", saved.username)
        Assertions.assertEquals("secret", saved.password)

        em.close()
    }

    @Test
    fun testProjectEntity() {
        val em = emf.createEntityManager()
        em.transaction.begin()

        // First, create related accounts
        val npmAccount = NpmAccountEntity(
            accountName = "NPM Account for Project",
            username = "npmproject",
            password = "pw"
        )
        val gitHubAccount = GitHubAccountEntity(
            accountName = "GitHub Account for Project",
            username = "ghuser",
            token = "ghp_projecttoken"
        )
        em.persist(npmAccount)
        em.persist(gitHubAccount)

        // Create ProjectEntity referencing them
        val project = ProjectEntity(
            name = "MyProject",
            path = "/path/to/myproject",
            npmAccount = npmAccount,
            gitHubAccount = gitHubAccount
        )
        em.persist(project)

        em.transaction.commit()

        // Verify it was saved and relationships are correct
        val saved = em.find(ProjectEntity::class.java, project.id)
        Assertions.assertNotNull(saved)
        Assertions.assertEquals("MyProject", saved.name)
        Assertions.assertEquals("/path/to/myproject", saved.path)
        Assertions.assertNotNull(saved.npmAccount)
        Assertions.assertNotNull(saved.gitHubAccount)

        // Additional checks if needed:
        Assertions.assertEquals("NPM Account for Project", saved.npmAccount?.accountName)
        Assertions.assertEquals("GitHub Account for Project", saved.gitHubAccount?.accountName)

        em.close()
    }
}
