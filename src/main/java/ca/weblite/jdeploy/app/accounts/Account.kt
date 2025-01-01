package ca.weblite.jdeploy.app.accounts

open class Account(
    private val accountName: String,
    private val accessToken: String?,
    private val accountType: AccountType
): AccountInterface {
    override fun getAccessToken(): String? {
        return accessToken
    }

    override fun getAccountName(): String {
        return accountName
    }

    override fun getAccountType(): AccountType {
        return accountType
    }
}