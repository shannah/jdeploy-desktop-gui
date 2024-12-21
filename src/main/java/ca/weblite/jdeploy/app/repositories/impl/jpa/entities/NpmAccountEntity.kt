package ca.weblite.jdeploy.app.repositories.impl.jpa.entities

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "npm_accounts")
class NpmAccountEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "TEXT")
    val id: UUID? = null,

    @Column(name = "account_name", nullable = false)
    val accountName: String,

    @Column(name = "username", nullable = true)
    val username: String? = null,

    @Column(name = "password", nullable = true)
    val password: String? = null
)
