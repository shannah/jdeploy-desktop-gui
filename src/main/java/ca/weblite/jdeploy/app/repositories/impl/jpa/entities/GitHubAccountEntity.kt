package ca.weblite.jdeploy.app.repositories.impl.jpa.entities

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "github_accounts")
data class GitHubAccountEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "TEXT")
    val id: UUID? = null,

    @Column(name = "account_name", nullable = false)
    val accountName: String,

    @Column(name = "username", nullable = false)
    val username: String,

    @Column(name = "token", nullable = true)
    val token: String? = null,
)
