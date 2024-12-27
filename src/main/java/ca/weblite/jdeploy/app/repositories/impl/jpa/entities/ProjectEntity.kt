package ca.weblite.jdeploy.app.repositories.impl.jpa.entities

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "projects")
class ProjectEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "TEXT")
    var id: UUID? = null

    @Column(name = "name", nullable = false)
    var name: String

    @Column(name = "path", nullable = false)
    var path: String

    @Column(name = "last_opened", nullable = false)
    var lastOpened: Long = System.currentTimeMillis()/1000

    @ManyToOne
    @JoinColumn(name = "npm_account_id", referencedColumnName = "id", nullable = true)
    var npmAccount: NpmAccountEntity? = null

    @ManyToOne
    @JoinColumn(name = "github_account_id", referencedColumnName = "id", nullable = true)
    var gitHubAccount: GitHubAccountEntity? = null

    // No-argument constructor for Hibernate
    constructor() {
        this.name = ""
        this.path = ""
    }

    constructor(
        id: UUID? = null,
        name: String,
        path: String,
        npmAccount: NpmAccountEntity? = null,
        gitHubAccount: GitHubAccountEntity? = null,
        lastOpened: Long = System.currentTimeMillis()/1000
    ) {
        this.id = id
        this.name = name
        this.path = path
        this.lastOpened = lastOpened
        this.npmAccount = npmAccount
        this.gitHubAccount = gitHubAccount
    }

    // Optionally, you can override `toString()`, `equals()`, and `hashCode()` if you need specific implementations for them.
}
