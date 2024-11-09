package ca.weblite.jdeploy.app.repositories.impl.jpa.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.ManyToAny;

import java.util.UUID;

@Entity
@Table(name = "projects")
data class ProjectEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "TEXT")
    val id: UUID? = null,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "path", nullable = false)
    val path: String,

    @ManyToAny
    @JoinColumn(name = "npm_account_id", referencedColumnName = "id", nullable = true)
    val npmAccount: NpmAccountEntity? = null,

    @ManyToAny
    @JoinColumn(name = "npm_account_id", referencedColumnName = "id", nullable = true)
    val gitHubAccount: GitHubAccountEntity? = null
)
