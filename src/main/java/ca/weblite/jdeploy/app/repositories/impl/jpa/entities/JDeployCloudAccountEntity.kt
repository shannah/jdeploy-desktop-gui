package ca.weblite.jdeploy.app.repositories.impl.jpa.entities

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "jdeploy_cloud_accounts")
data class JDeployCloudAccountEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "TEXT")
    val id: UUID? = null,

    @Column(name = "account_name", nullable = false)
    val accountName: String,

    @Column(name = "server_url", nullable = false)
    val serverUrl: String = "https://cloud.jdeploy.com",

    @Column(name = "token", nullable = true)
    val token: String? = null,
) {
    // No-argument constructor for Hibernate
    constructor() : this(UUID.randomUUID(), "", "https://cloud.jdeploy.com")
}
