package ca.weblite.jdeploy.app.accounts

import java.util.concurrent.CompletableFuture

interface AccountServiceInterface {
    fun findAll(): CompletableFuture<List<AccountInterface>>
    fun save(account: AccountInterface): CompletableFuture<AccountInterface>
    fun delete(account: AccountInterface): CompletableFuture<Void>
    fun loadToken(account: AccountInterface): CompletableFuture<AccountInterface>
}