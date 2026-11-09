package com.it10x.foodappgstav7_31.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.online.repository.ProductSyncRepository
import com.it10x.foodappgstav7_31.data.online.repository.ModifierSyncRepository // 👈 ADD THIS
import com.it10x.foodappgstav7_31.data.online.repository.UserSyncRepository
import com.it10x.foodappgstav7_31.data.online.repository.ProductRecipeSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_31.data.online.repository.OrderCounterSyncRepository
import com.it10x.foodappgstav7_31.data.online.repository.PosRenewalSyncRepository

class ProductSyncViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabaseProvider.get(app)

    private val repo = ProductSyncRepository(db)

    private val modifierRepo = ModifierSyncRepository(db) // 👈 ADD THIS

    private val userRepo = UserSyncRepository(db)

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing

    private val _status = MutableStateFlow<String>("")
    val status: StateFlow<String> = _status



    private val orderCounterRepo =
        OrderCounterSyncRepository(
            db,
            FirebaseFirestore.getInstance()
        )

    private val recipeRepo =
        ProductRecipeSyncRepository(db)
    fun syncAll() {
        viewModelScope.launch {
            try {
                _syncing.value = true

                _status.value = "Syncing order counter..."
                orderCounterRepo.syncLastOrderSerialNo()

                _status.value = "Syncing categories…"
                repo.syncCategories()

                _status.value = "Syncing products…"
                repo.syncProducts()

                _status.value = "Syncing modifier groups…"
                modifierRepo.syncModifierGroups()

                _status.value = "Syncing modifier items…"
                modifierRepo.syncModifierItems()

                _status.value = "Syncing product modifiers…"
                modifierRepo.syncProductModifiers()



                _status.value =
                    "Syncing product recipes..."
                recipeRepo.syncProductRecipes()

                _status.value = "Syncing users..."
                userRepo.syncUsers()

                _status.value = "Sync complete 🎉"

            } catch (e: Exception) {
                _status.value = "Sync failed: ${e.message}"
            } finally {
                _syncing.value = false
            }
        }
    }

    fun syncUsersOnly() {
        viewModelScope.launch {
            try {
                _syncing.value = true

                _status.value = "Syncing users..."
                userRepo.syncUsers()

                _status.value = "Users updated successfully 🎉"

            } catch (e: Exception) {
                _status.value = "User sync failed: ${e.message}"
            } finally {
                _syncing.value = false
            }
        }
    }

    private val posRenewalRepo =
        PosRenewalSyncRepository(
            db,
            FirebaseFirestore.getInstance()
        )


}