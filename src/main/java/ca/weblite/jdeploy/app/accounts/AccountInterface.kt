package ca.weblite.jdeploy.app.accounts

interface AccountInterface {
    fun getAccessToken(): String?
    fun getAccountName(): String
    fun getAccountType(): AccountType
}