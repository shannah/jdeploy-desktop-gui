# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java/Kotlin desktop GUI application for jDeploy, built with Swing and using Maven as the build system. The application serves as a desktop companion for managing jDeploy projects, allowing users to create, import, and manage Java desktop applications that can be deployed as native bundles.

## Build and Development Commands

### Building the Application
```bash
# Clean and compile the project
./mvnw clean compile

# Build the JAR file
./mvnw clean package

# Run the application in development mode
./mvnw exec:java
```

### Running Tests
```bash
# Run all tests
./mvnw test

# Run tests with surefire plugin
./mvnw surefire:test
```

### Dependencies Management
```bash
# Install/update dependencies
./mvnw dependency:resolve

# Copy dependencies to target/libs
./mvnw dependency:copy-dependencies
```

### jDeploy Version Configuration

The project uses a `.env` file to configure the jDeploy version used during CI/CD builds:

- **Configuration File**: `.env` in the project root
- **Variable**: `JDEPLOY_VERSION` (defaults to 'latest' if not specified)
- **Usage**: The GitHub workflow reads this version and passes it to `install-signed-installer-templates.sh`

**Development Workflow**:
1. For development on `dev` branch: Set `JDEPLOY_VERSION=4.1.0-SNAPSHOT` (or specific dev version)
2. Before merging to `master`: Change to stable version like `JDEPLOY_VERSION=4.0.26`
3. For latest stable: Use `JDEPLOY_VERSION=latest`

## Architecture Overview

### Technology Stack
- **Languages**: Java 17 + Kotlin 2.0.21 (mixed codebase)
- **UI Framework**: Swing with FlatLaf Look & Feel
- **UI DSL**: KtSwing for Kotlin-based UI components
- **Database**: SQLite with Hibernate ORM (JPA)
- **Database Migration**: Flyway
- **Dependency Injection**: Feather DI framework
- **Build System**: Maven 
- **Security**: Java Keyring for password management

### Main Application Entry Point
- **Main Class**: `com.github.shannah.jdeploydesktopgui.JdeployDesktopGui`
- Initializes DI container, sets up database, shows splash screen, and launches main menu

### Key Architectural Patterns

#### Dependency Injection Structure
- **DI Framework**: Uses Feather DI with custom modules
- **Main Module**: `JDeployDesktopGuiModule` coordinates all dependencies  
- **JPA Module**: `JdeployJpaModule` handles database-related dependencies
- **GUI Module**: `JdeployGuiModule` manages UI-related services

#### Repository Pattern
- **Interfaces**: All repositories implement interfaces (e.g., `ProjectRepositoryInterface`)
- **JPA Implementation**: Located in `repositories.impl.jpa` package
- **Entity Mapping**: JPA entities in `repositories.impl.jpa.entities`

#### MVC Architecture
- **Controllers**: Handle business logic (in `controllers` package)
- **Forms**: UI components using Kotlin DSL (in `forms` package)  
- **Services**: Business services layer (in `services` package)

### Database Schema
- **Database**: SQLite stored in `${user.home}/.jdeploy/jdeploy-gui.db`
- **Migrations**: Located in `src/main/resources/db/migration/`
- **Entities**: GitHub accounts, NPM accounts, and Projects

### Key Components

#### Account Management
- Supports GitHub and NPM account types
- Secure password storage via Java Keyring
- Account services: `GitHubAccountService`, `NpmAccountService`

#### Project Management  
- Project entity with templates and validation
- Project import/export functionality
- Project templates loaded from XML sources

#### UI Components
- Mixed Java Swing and Kotlin KtSwing DSL
- Form files (`.form`) for IntelliJ IDEA GUI designer
- Custom components like `SearchTextField`, `TagLabel`
- Responsive design with custom layouts

## Code Conventions

### Language Usage
- **Java**: Legacy controllers, services, and infrastructure
- **Kotlin**: New UI forms, data classes, and modern components
- **Mixed Files**: Some classes have both Java and Kotlin implementations

### Package Structure
- `ca.weblite.jdeploy.app.*` - Main application code
- `com.github.shannah.jdeploydesktopgui` - Entry point package
- Organized by layer: controllers, forms, repositories, services, etc.

### Styling and UI
- Uses KtSwing DSL with `Stylesheet` for component styling
- Material Design icons via `org.kordamp.ikonli`
- Platform-specific UI adjustments (Mac vs Windows/Linux)

### Testing
- JUnit 5 for unit tests
- Integration tests in `tests` package
- H2 database for test environments
- Test-specific DI modules and configurations

## Development Notes

### Database Development
- Hibernate auto-DDL is disabled (`hibernate.hbm2ddl.auto=none`)
- Use Flyway migrations for schema changes
- Entity scanning limited to `ca.weblite.jdeploy.app.repositories.impl.jpa.entities`

### UI Development
- Form files require IntelliJ IDEA for visual editing
- Use KtSwing DSL for new Kotlin forms
- Apply platform-specific styling considerations

### Debugging
- Debug configuration available via exec-maven-plugin on port 5005
- Hibernate SQL logging enabled in persistence.xml