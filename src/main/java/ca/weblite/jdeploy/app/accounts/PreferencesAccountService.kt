package ca.weblite.jdeploy.app.accounts

import ca.weblite.jdeploy.app.secure.PasswordServiceInterface
import java.util.concurrent.CompletableFuture
import java.util.Collections
import java.util.prefs.BackingStoreException
import java.util.prefs.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesAccountService @Inject constructor(
    private val passwordService: PasswordServiceInterface
) : AccountServiceInterface {

    companion object {
        private val PREFS: Preferences =
            Preferences.userNodeForPackage(PreferencesAccountService::class.java).node("accounts")
    }

    override fun findAll(): CompletableFuture<List<AccountInterface>> {
        return CompletableFuture.supplyAsync {
            val accounts = mutableListOf<AccountInterface>()
            try {
                val keys = PREFS.keys()
                for (key in keys) {
                    val accountInfo = PREFS.get(key, null)
                    if (accountInfo != null) {
                        val parts = accountInfo.split("|")
                        if (parts.size == 2) {
                            val accountType = AccountType.valueOf(parts[0])
                            val accountName = parts[1]
                            accounts.add(Account(accountName, null, accountType))
                        }
                    }
                }
            } catch (e: BackingStoreException) {
                e.printStackTrace()
            }
            Collections.unmodifiableList(accounts)
        }
    }

    override fun save(account: AccountInterface): CompletableFuture<AccountInterface> {
        return CompletableFuture.supplyAsync {
            val accountKey = generateAccountKey(account)
            val accountInfo = "${account.getAccountType()}|${account.getAccountName()}"
            PREFS.put(accountKey, accountInfo)

            account.getAccessToken()?.let { token ->
                passwordService.setPassword(accountKey, token.toCharArray()).join()
            }

            account
        }
    }

    override fun delete(account: AccountInterface): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val accountKey = generateAccountKey(account)
            PREFS.remove(accountKey)
            passwordService.removePassword(accountKey).join()
        }
    }

    override fun loadToken(account: AccountInterface): CompletableFuture<AccountInterface> {
        return CompletableFuture.supplyAsync {
            val accountKey = generateAccountKey(account)
            val tokenFuture = passwordService.getPassword(accountKey, "Load token for ${account.getAccountType()} account")

            tokenFuture.thenApply { token ->
                Account(account.getAccountName(), String(token), account.getAccountType())
            }.join()
        }
    }

    private fun generateAccountKey(account: AccountInterface): String {
        return "${account.getAccountType()}_${account.getAccountName()}"
    }
}
