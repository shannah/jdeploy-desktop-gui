---
bootstrap: true
generated_at: 2026-05-08T13:00:00-07:00
---

# REASONS Canvas: Publishing Accounts (GitHub and NPM)

## R · Requirements
- The application stores credentials for two account types — `GITHUB` and `NPM` — used
  when publishing or creating projects.
- The user-facing flow is the **Account Chooser**: a modal dialog opened by feature
  controllers (e.g. the new-project wizard) that:
  - Lists existing accounts of the requested type.
  - Lets the user **Add**, **Edit**, or **Delete** an account.
  - Confirms deletes via a Yes/No dialog.
  - Returns the selected account (with token loaded) as a `CompletableFuture<AccountInterface>`.
- The **Edit Account** dialog has `name` and `token` (password) fields. The Save button
  is enabled only when both are non-empty. Save failures show a `JOptionPane` error
  message; success calls the `afterSave` callback and disposes the dialog.
- Account persistence (the working, GUI-visible path) goes through
  **`PreferencesAccountService`**: account names are stored as
  `<TYPE>|<NAME>` under a per-class Preferences node, keyed by `<TYPE>_<NAME>`. Tokens
  are stored separately via `JavaKeyringPasswordService` in the OS keyring (service
  name `"com.jdeploy"`), with a Preferences-based fallback if no keyring backend is
  available.
- Definition of Done as it stands today: covered by `GitHubAccountServiceTest.kt` and
  `NpmAccountServiceTest.kt` (which exercise the JPA path); UI side is manual.

## E · Entities

- **`AccountType`** (`src/main/java/ca/weblite/jdeploy/app/accounts/AccountType.kt`):
  enum `GITHUB`, `NPM`.

- **`AccountInterface`** (`src/main/java/ca/weblite/jdeploy/app/accounts/AccountInterface.kt`):
  `getAccountName(): String`, `getAccessToken(): String?`, `getAccountType(): AccountType`.

- **`Account`** (`src/main/java/ca/weblite/jdeploy/app/accounts/Account.kt:3-19`):
  immutable open class implementing `AccountInterface`; constructor takes
  `accountName`, `accessToken`, `accountType`.

- **`GitHubAccountEntity`**
  (`src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/entities/GitHubAccountEntity.kt:6-26`)
  — table `github_accounts`. Fields: `id` (UUID PK, TEXT, generated), `accountName`
  NOT NULL, `username` NOT NULL, `token` nullable. Hibernate no-arg constructor seeds
  `id`/`accountName`/`username` defaults (line 25).

- **`NpmAccountEntity`** — table `npm_accounts`. Fields: `id` (UUID PK), `accountName`
  NOT NULL, `username` nullable, `password` nullable. (Symmetric to GitHub except
  `username` is nullable here.)

- **`GitHubAccount`** / **`NpmAccount`** records — domain DTOs returned by services.

- **Persistence schema invariants** — accounts share PKs across the project FK; see
  `db/migration/V1__Create_github_accounts.sql:3-8` and
  `db/migration/V2__Create_npm_accounts.sql:3-8`.

## A · Approach
- Two parallel persistence paths exist — this is intentional but not yet unified:
  1. **Preferences + Keyring** path (`PreferencesAccountService` +
     `JavaKeyringPasswordService`) — async (`CompletableFuture`), the one wired into
     the GUI via `JdeployGuiModule.java:58-61`.
  2. **JPA** path (`GitHubAccountService`/`NpmAccountService` +
     `JpaGitHubAccountRepository`/`JpaNpmAccountRepository`) — synchronous, used when
     a project's FKs need a persisted record and exercised by the integration tests.
- The chooser/edit dialogs are pure UI; all persistence flows through the
  `AccountServiceInterface` they receive.
- Token storage is split from account metadata so tokens can use the OS keyring
  (`JavaKeyringPasswordService`). When the keyring isn't available
  (`Throwable` from `Keyring.create()`), the service silently falls back to
  `Preferences.userRoot().node("com.jdeploy")`.

## S · Structure

UI / controllers:
- `src/main/java/ca/weblite/jdeploy/app/controllers/AccountChooserController.java`
- `src/main/java/ca/weblite/jdeploy/app/controllers/EditAccountController.java`
- `src/main/java/ca/weblite/jdeploy/app/forms/AccountChooserDialog.java`
- `src/main/java/ca/weblite/jdeploy/app/forms/EditAccountDialog.java`
- `src/main/java/ca/weblite/jdeploy/app/forms/EditAccountPanel.java`

Domain / services:
- `src/main/java/ca/weblite/jdeploy/app/accounts/Account.kt`
- `src/main/java/ca/weblite/jdeploy/app/accounts/AccountInterface.kt`
- `src/main/java/ca/weblite/jdeploy/app/accounts/AccountType.kt`
- `src/main/java/ca/weblite/jdeploy/app/accounts/AccountServiceInterface.kt`
- `src/main/java/ca/weblite/jdeploy/app/accounts/PreferencesAccountService.kt`
- `src/main/java/ca/weblite/jdeploy/app/services/GitHubAccountService.kt`
- `src/main/java/ca/weblite/jdeploy/app/services/NpmAccountService.kt`

Secure storage:
- `src/main/java/ca/weblite/jdeploy/app/secure/PasswordServiceInterface.java`
- `src/main/java/ca/weblite/jdeploy/app/secure/JavaKeyringPasswordService.java`

JPA persistence:
- `src/main/java/ca/weblite/jdeploy/app/repositories/GitHubAccountRepositoryInterface.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/NpmAccountRepositoryInterface.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/repositories/JpaGitHubAccountRepository.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/repositories/JpaNpmAccountRepository.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/entities/GitHubAccountEntity.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/entities/NpmAccountEntity.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/factories/GitHubAccountFactory.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/factories/GitHubAccountEntityFactory.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/factories/NpmAccountFactory.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/factories/NpmAccountEntityFactory.kt`
- `src/main/resources/db/migration/V1__Create_github_accounts.sql`
- `src/main/resources/db/migration/V2__Create_npm_accounts.sql`

## O · Operations

### 1. Account Domain Type — `Account` / `AccountInterface` / `AccountType`
File: `src/main/java/ca/weblite/jdeploy/app/accounts/Account.kt`

1. Responsibility: immutable triple of `(name, token?, type)` exposed as
   `AccountInterface`.
2. Methods:
   - `getAccountName(): String`, `getAccessToken(): String?`, `getAccountType(): AccountType`
     — direct getters returning constructor params (lines 8-18).

### 2. GitHub Entity — `GitHubAccountEntity`
File: `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/entities/GitHubAccountEntity.kt`

1. Responsibility: JPA mapping for `github_accounts`.
2. Fields:
   - `id: UUID?` — PK, `@GeneratedValue(strategy = GenerationType.UUID)`,
     `columnDefinition = "TEXT"`, `updatable = false`, `nullable = false` (lines 10-13).
   - `accountName: String` — non-null (lines 15-16).
   - `username: String` — non-null (lines 18-19).
   - `token: String?` — nullable (lines 21-22).
3. Constraints:
   - The Hibernate-required no-arg constructor seeds with `UUID.randomUUID()` and
     empty strings (line 25). Only invoked by Hibernate; do not call directly.

### 3. JPA GitHub Repository — `JpaGitHubAccountRepository`
File: `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/repositories/JpaGitHubAccountRepository.kt`

1. Responsibility: synchronous CRUD for GitHub accounts via JPA.
2. Methods:
   - `findOneById(id: UUID): GitHubAccount` (line 19-29)
     - Logic: run `executeInTransaction { em.createQuery(...).singleResult }` (lines
       20-23); if null, throw `NotFoundException`; map to record via
       `GitHubAccountFactory.createOne(entity)`.
   - `findOneByIdOrNull(id: UUID): GitHubAccount?` (line 31-37) — wrap and
     translate `NotFoundException` to null.
   - `saveOne(gitHubAccount: GitHubAccount): GitHubAccount` (line 39-45)
     - Logic: convert to entity via
       `GitHubAccountEntityFactory.extractOrCreate(gitHubAccount)`; `em.persist(entity)`;
       map back to record via `GitHubAccountFactory.createOne(entity)`.

### 4. GitHub Account Service — `GitHubAccountService`
File: `src/main/java/ca/weblite/jdeploy/app/services/GitHubAccountService.kt`

1. Responsibility: Singleton-scoped service that delegates to the repository.
2. Methods:
   - `findOneById(uuid: UUID): GitHubAccount` (line 14-17) — throws `NotFoundException`.
   - `saveOne(gitHubAccount: GitHubAccount): GitHubAccount` (line 19-21).

### 5. NPM Persistence (parallel structure)
Files:
- `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/entities/NpmAccountEntity.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/repositories/JpaNpmAccountRepository.kt`
- `src/main/java/ca/weblite/jdeploy/app/services/NpmAccountService.kt`

Responsibility and method shape mirror the GitHub side. NPM differs in
that `username` is nullable but `accountName` is non-null, and the credential is
stored under `password` rather than `token`.

### 6. Account Service Interface — `AccountServiceInterface` (Preferences path)
File: `src/main/java/ca/weblite/jdeploy/app/accounts/AccountServiceInterface.kt`

1. Responsibility: async CRUD contract used by the GUI.
2. Methods (signatures inferred from `PreferencesAccountService`):
   - `findAll(): CompletableFuture<List<AccountInterface>>`
   - `save(account): CompletableFuture<AccountInterface>`
   - `delete(account): CompletableFuture<Void>`
   - `loadToken(account): CompletableFuture<AccountInterface>`

### 7. Preferences Account Service — `PreferencesAccountService`
File: `src/main/java/ca/weblite/jdeploy/app/accounts/PreferencesAccountService.kt`

1. Responsibility: store account metadata in
   `Preferences.userNodeForPackage(PreferencesAccountService).node("accounts")` and
   tokens in `PasswordServiceInterface`. Singleton.
2. Companion:
   - `PREFS: Preferences` (lines 17-19) — the `accounts` sub-node.
3. Methods:
   - `findAll()` (line 21-42) — `supplyAsync` enumerates `PREFS.keys()`; values are
     `"<TYPE>|<NAME>"` strings. Skips malformed entries silently. Returns an
     unmodifiable list.
   - `save(account)` (line 44-56) — `supplyAsync`: write
     `"<TYPE>|<NAME>"` to `PREFS` keyed by `<TYPE>_<NAME>` (`generateAccountKey`,
     line 77-79). If `accessToken` is non-null, call
     `passwordService.setPassword(accountKey, token.toCharArray()).join()`.
   - `delete(account)` (line 58-64) — remove from `PREFS` and call
     `passwordService.removePassword(accountKey).join()`.
   - `loadToken(account)` (line 66-75) — `supplyAsync`:
     `passwordService.getPassword(accountKey, prompt).thenApply { Account(name, token, type) }.join()`.
   - `generateAccountKey(account): String` (line 77-79) — `"<TYPE>_<NAME>"`.
4. Constraints:
   - Account-name keys must not contain `|` (would break the parse on line 29).

### 8. Password Service — `JavaKeyringPasswordService`
File: `src/main/java/ca/weblite/jdeploy/app/secure/JavaKeyringPasswordService.java`

1. Responsibility: async wrapper around `com.github.javakeyring.Keyring` with a
   `Preferences.userRoot().node("com.jdeploy")` fallback.
2. Constants:
   - `SERVICE_NAME = "com.jdeploy"` (line 14).
3. Fields:
   - `fallbackPrefs: Preferences` (line 17) — `Preferences.userRoot().node(SERVICE_NAME)`.
4. Methods:
   - `getPassword(name, prompt): CompletableFuture<char[]>` (line 20-35)
     - Logic: `supplyAsync`: try `Keyring.create().getPassword(SERVICE_NAME, name)`;
       on success return its char array; on `Throwable` fall through to read from
       `fallbackPrefs`. Return null if neither has it.
   - `setPassword(name, password): CompletableFuture<Void>` (line 38-63)
     - Logic: `runAsync`: try keyring (delete on null/empty, else set);
       on success, also remove the fallback. On `Throwable`, fall back to writing
       to `fallbackPrefs`.
   - `removePassword(name): CompletableFuture<Void>` (line 66-76)
     - Logic: `runAsync`: try `keyring.deletePassword(...)`; ignore failures; always
       remove from fallback.

### 9. Account Chooser Controller — `AccountChooserController`
File: `src/main/java/ca/weblite/jdeploy/app/controllers/AccountChooserController.java`

1. Responsibility: present an `AccountChooserDialog` for a single `AccountType`,
   wire add/edit/delete events, and return the chosen account with its token loaded.
2. Fields:
   - `EDT_EXECUTOR: Executor = new SwingExecutor()` (line 19).
   - `accountService: AccountServiceInterface` (line 21).
   - `parentFrame: Frame`, `accountType: AccountType`, `selectedAccount: AccountInterface` (lines 23-27).
3. Methods:
   - `show(): CompletableFuture<AccountInterface>` (line 46-48)
     - Logic: `accountService.findAll().thenComposeAsync(this::showDialog, EDT_EXECUTOR)`.
   - `showDialog(accounts): CompletableFuture<AccountInterface>` (line 50-134)
     - Logic:
       1. Filter accounts to `accountType` (lines 51-53).
       2. Build `AccountChooserDialog`; set title label to `"npm"` or `"GitHub"`
          (lines 54-62).
       3. Wire **Add Account** button → open `EditAccountController` with a blank
          `Account("", null, accountType)`; on `afterSave`, set `selectedAccount` and
          dispose dialog (lines 64-80).
       4. Listen for chooser events: on `DeleteAccountEvent`, prompt
          `JOptionPane.YES_NO_OPTION`; if confirmed, call
          `accountService.delete(account)` and `event.commit()` on EDT (lines 82-102).
       5. On `EditAccountEvent`, open `EditAccountController` with the existing
          account; on `afterSave`, call `event.commit()` (lines 104-118).
       6. Block on `dialog.showDialog()` (modal) (line 120). If the user picked an
          account, set `selectedAccount`.
       7. If `selectedAccount != null`, return
          `accountService.loadToken(selectedAccount).thenApplyAsync(...)` (lines
          124-132). Otherwise return `CompletableFuture.completedFuture(null)`.

### 10. Edit Account Controller — `EditAccountController` (abstract)
File: `src/main/java/ca/weblite/jdeploy/app/controllers/EditAccountController.java`

1. Responsibility: drive the `EditAccountDialog` for both add and edit flows. Subclasses
   provide an `afterSave(account)` callback.
2. Fields:
   - `dialog: EditAccountDialog`, `parentFrame: Window`, `accountService`,
     `account: AccountInterface`, `newAccount: AccountInterface` (lines 17-25).
3. Methods:
   - `show(): void` (line 43-46) — make the dialog visible.
   - `afterSave(account): void` — abstract (line 48).
   - `getAccount(): AccountInterface` (line 50-58)
     - Logic: build new `Account(nameField.text, tokenField empty? null : new String(tokenField.password),
       account.getAccountType())`.
   - `isAccountValid(): boolean` (line 64-66) — both fields non-empty.
   - `setupSaveButton()` (line 72-91) — Save click: call
     `accountService.save(newAccount).thenAcceptAsync(result -> { afterSave(newAccount);
     dialog.dispose(); parentFrame.requestFocus() }, EDT)`. On exception, show
     `JOptionPane.ERROR_MESSAGE` `"Failed to save account: <msg>"` on the EDT.
   - `setupCancelButton()` (line 93-99) — clear `newAccount`, dispose, refocus parent.

## N · Norms
- All Account UI work returns to the EDT through `SwingExecutor` (a custom `Executor`
  used as the `CompletableFuture` callback executor).
- The keyring service name is `"com.jdeploy"`. Don't change it unless you intentionally
  want existing users to lose their stored tokens (the keyring lookup is keyed on
  `(service, name)`).
- Account keys in `PreferencesAccountService` are case-sensitive and join `<TYPE>_<NAME>`
  with an underscore; no escaping. Names containing underscores are technically
  ambiguous but currently safe because the same key is also used for storage and
  lookup, never reverse-parsed.

## S · Safeguards
- Save is gated on `isAccountValid()` — both fields must be non-empty
  (`EditAccountController.java:64-66`).
- Delete is gated by an explicit Yes/No `JOptionPane`
  (`AccountChooserController.java:86-95`).
- Keyring failures fall back to `Preferences` rather than crashing the UI
  (`JavaKeyringPasswordService.java:28-30, 52-54, 71-73`). This is by design — Linux
  systems without a Secret Service backend would otherwise break account storage.
- Save failure surfaces a `JOptionPane.ERROR_MESSAGE` rather than silently dropping
  the click (`EditAccountController.java:79-89`).
- Token loading happens lazily (only when an account is *selected*, not when the list
  is shown) so a chooser open doesn't unlock the keyring
  (`AccountChooserController.java:124-132`).
