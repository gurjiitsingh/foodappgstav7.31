package com.it10x.foodappgstav7_31.data.pos.viewmodel.loyalty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_31.data.pos.entities.loyalty.*
import com.it10x.foodappgstav7_31.data.pos.repository.loyalty.LoyaltyRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
class LoyaltyViewModel(
    private val repository: LoyaltyRepository
) : ViewModel() {

    // ========================================================
    // CUSTOMER ACCOUNTS
    // ========================================================

    private val _accounts =
        MutableStateFlow<List<LoyaltyAccountEntity>>(emptyList())

    val accounts: StateFlow<List<LoyaltyAccountEntity>> =
        _accounts.asStateFlow()

    private val _selectedAccount =
        MutableStateFlow<LoyaltyAccountEntity?>(null)

    val selectedAccount: StateFlow<LoyaltyAccountEntity?> =
        _selectedAccount.asStateFlow()


    // ========================================================
    // LOYALTY PROGRAMS
    // ========================================================

    private val _programs =
        MutableStateFlow<List<LoyaltyProgramEntity>>(emptyList())

    val programs: StateFlow<List<LoyaltyProgramEntity>> =
        _programs.asStateFlow()


    // ========================================================
    // TRANSACTIONS
    // ========================================================

    private val _transactions =
        MutableStateFlow<List<LoyaltyTransactionEntity>>(emptyList())

    val transactions: StateFlow<List<LoyaltyTransactionEntity>> =
        _transactions.asStateFlow()


    // ========================================================
    // POINT LOTS
    // ========================================================

    private val _pointLots =
        MutableStateFlow<List<LoyaltyPointLotEntity>>(emptyList())

    val pointLots: StateFlow<List<LoyaltyPointLotEntity>> =
        _pointLots.asStateFlow()


    // ========================================================
    // REWARDS
    // ========================================================

    private val _rewards =
        MutableStateFlow<List<LoyaltyRewardEntity>>(emptyList())

    val rewards: StateFlow<List<LoyaltyRewardEntity>> =
        _rewards.asStateFlow()


    // ========================================================
    // VOUCHERS
    // ========================================================

    private val _vouchers =
        MutableStateFlow<List<LoyaltyVoucherEntity>>(emptyList())

    val vouchers: StateFlow<List<LoyaltyVoucherEntity>> =
        _vouchers.asStateFlow()


    // ========================================================
    // UI STATES
    // ========================================================

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()


    // ========================================================
    // OBSERVATION JOBS
    // ========================================================

    private var accountsJob: Job? = null
    private var programsJob: Job? = null
    private var transactionsJob: Job? = null
    private var pointLotsJob: Job? = null
    private var rewardsJob: Job? = null
    private var vouchersJob: Job? = null



    // ========================================================
// INITIALIZE LOYALTY FOR CUSTOMER
// ========================================================

    fun initializeLoyalty(
        customerId: String,
        ownerId: String,
        outletId: String
    ) {

        if (customerId.isBlank()) {
            _errorMessage.value = "Customer ID is required"
            return
        }

        if (ownerId.isBlank()) {
            _errorMessage.value = "Owner ID is required"
            return
        }

        if (outletId.isBlank()) {
            _errorMessage.value = "Outlet ID is required"
            return
        }

        viewModelScope.launch {

            _errorMessage.value = null

            try {

                // 1. Create the default programme and outlet mapping.
                repository.ensureDefaultProgram(
                    ownerId = ownerId,
                    outletId = outletId
                )
                _programs.value = repository.getActivePrograms(ownerId)
                // 2. Load customer's existing accounts.
                loadCustomerAccounts(customerId)

                // 3. Observe available programmes.
                loadPrograms(ownerId)

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                _errorMessage.value =
                    e.message ?: "Unable to initialize loyalty"

            }
        }
    }

    // ========================================================
    // LOAD CUSTOMER ACCOUNTS
    // ========================================================

    fun loadCustomerAccounts(customerId: String) {

        if (customerId.isBlank()) {
            _errorMessage.value = "Customer ID is required"
            return
        }

        accountsJob?.cancel()

        accountsJob = viewModelScope.launch {

            _errorMessage.value = null

            try {

                repository.observeCustomerAccounts(customerId)
                    .collectLatest { result ->

                        _accounts.value = result

                        // Keep selected account updated.
                        val selectedId = _selectedAccount.value?.id

                        if (selectedId != null) {
                            _selectedAccount.value =
                                result.firstOrNull { it.id == selectedId }
                        }
                    }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                _errorMessage.value =
                    e.message ?: "Unable to load customer accounts"
            }
        }
    }

    // ========================================================
    // LOAD LOYALTY PROGRAMS
    // ========================================================

    fun loadPrograms(ownerId: String) {

        if (ownerId.isBlank()) {
            _errorMessage.value = "Owner ID is required"
            return
        }

        programsJob?.cancel()

        programsJob = viewModelScope.launch {

            try {

                repository.observePrograms(ownerId)
                    .collectLatest { result ->
                        _programs.value = result
                    }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value =
                    e.message ?: "Unable to load loyalty programs"
            }
        }
    }


    // ========================================================
    // CREATE OR GET CUSTOMER ACCOUNT
    // ========================================================

    fun createOrGetAccount(
        customerId: String,
        programId: String,
        outletId: String
    ) {

        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null

            try {

                val account = repository.getOrCreateAccount(
                    customerId = customerId,
                    programId = programId,
                    outletId = outletId
                )

                _selectedAccount.value = account

                observeAccountDetails(
                    accountId = account.id,
                    programId = programId
                )

                _successMessage.value = "Loyalty account ready"

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value =
                    e.message ?: "Unable to create loyalty account"
            } finally {
                _isLoading.value = false
            }
        }
    }


    // ========================================================
    // SELECT ACCOUNT
    // ========================================================

    fun selectAccount(
        account: LoyaltyAccountEntity,
        programId: String
    ) {

        _selectedAccount.value = account

        observeAccountDetails(
            accountId = account.id,
            programId = programId
        )
    }


    // ========================================================
    // OBSERVE ACCOUNT DETAILS
    // ========================================================

    fun observeAccountDetails(
        accountId: String,
        programId: String
    ) {

        transactionsJob?.cancel()
        pointLotsJob?.cancel()
        rewardsJob?.cancel()
        vouchersJob?.cancel()

        // Transaction history
        transactionsJob = viewModelScope.launch {

            try {
                repository.observeTransactions(accountId)
                    .collectLatest {
                        _transactions.value = it
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value =
                    e.message ?: "Unable to load transactions"
            }
        }

        // Point lots
        pointLotsJob = viewModelScope.launch {

            try {
                repository.observePointLots(accountId)
                    .collectLatest {
                        _pointLots.value = it
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value =
                    e.message ?: "Unable to load point history"
            }
        }

        // Available rewards
        rewardsJob = viewModelScope.launch {

            try {
                repository.observeAvailableRewards(programId).collectLatest {
                    _rewards.value = it
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value =
                    e.message ?: "Unable to load rewards"
            }
        }

        // Active vouchers
        vouchersJob = viewModelScope.launch {

            try {
                repository.observeActiveVouchers(accountId).collectLatest {
                    _vouchers.value = it
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value =
                    e.message ?: "Unable to load vouchers"
            }
        }
    }


    // ========================================================
    // GET AVAILABLE POINTS
    // ========================================================

    suspend fun getAvailablePoints(accountId: String): Int {

        return repository.getAvailablePoints(accountId)
    }


    // ========================================================
    // EARN POINTS
    // ========================================================

    fun earnPoints(
        accountId: String,
        points: Int,
        spendMinor: Long,
        idempotencyKey: String,
        orderId: String,
        orderNo: String,
        outletId: String,
        businessDate: String,
        createdBy: String,
        description: String
    ) {

        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {

                repository.earnPoints(
                    accountId = accountId,
                    points = points,
                    spendMinor = spendMinor,
                    idempotencyKey = idempotencyKey,
                    orderId = orderId,
                    orderNo = orderNo,
                    outletId = outletId,
                    businessDate = businessDate,
                    createdBy = createdBy,
                    description = description
                )

                _successMessage.value =
                    "$points loyalty points earned successfully"

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value =
                    e.message ?: "Unable to earn loyalty points"
            } finally {
                _isLoading.value = false
            }
        }
    }


    // ========================================================
// MANUALLY ADD LOYALTY POINTS
// ========================================================

    fun addManualPoints(
        accountId: String,
        points: Int,
        outletId: String,
        createdBy: String = "STAFF",
        description: String = "Manual points added"
    ) {

        if (accountId.isBlank()) {
            _errorMessage.value = "Loyalty account is required"
            return
        }

        if (outletId.isBlank()) {
            _errorMessage.value = "Outlet ID is required"
            return
        }

        if (points <= 0) {
            _errorMessage.value = "Points must be greater than zero"
            return
        }

        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {

                repository.earnPoints(
                    accountId = accountId,
                    points = points,
                    spendMinor = 0L,
                    idempotencyKey = "MANUAL-${UUID.randomUUID()}",
                    orderId = null,
                    orderNo = null,
                    outletId = outletId,
                    businessDate = null,
                    createdBy = createdBy,
                    description = description
                )

                _successMessage.value =
                    "$points loyalty points added successfully"

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                _errorMessage.value =
                    e.message ?: "Unable to add loyalty points"

            } finally {

                _isLoading.value = false
            }
        }
    }
    // ========================================================
    // REDEEM POINTS
    // ========================================================

    fun redeemPoints(
        accountId: String,
        points: Int,
        idempotencyKey: String,
        orderId: String,
        orderNo: String,
        outletId: String,
        businessDate: String,
        createdBy: String,
        description: String
    ) {

        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {

                repository.redeemPoints(
                    accountId = accountId,
                    points = points,
                    idempotencyKey = idempotencyKey,
                    orderId = orderId,
                    orderNo = orderNo,
                    outletId = outletId,
                    businessDate = businessDate,
                    createdBy = createdBy,
                    description = description
                )

                _successMessage.value =
                    "$points loyalty points redeemed successfully"

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value =
                    e.message ?: "Unable to redeem loyalty points"
            } finally {
                _isLoading.value = false
            }
        }
    }


    // ========================================================
    // LOAD ACTIVE PROGRAMS ONCE
    // ========================================================

    fun loadActivePrograms(ownerId: String) {

        viewModelScope.launch {

            _isLoading.value = true

            try {

                _programs.value =
                    repository.getActivePrograms(ownerId)

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value =
                    e.message ?: "Unable to load active programs"
            } finally {
                _isLoading.value = false
            }
        }
    }


    // ========================================================
    // GET RECENT TRANSACTIONS
    // ========================================================

    suspend fun getRecentTransactions(
        accountId: String,
        limit: Int = 20
    ): List<LoyaltyTransactionEntity> {

        return repository.getRecentTransactions(
            accountId = accountId,
            limit = limit
        )
    }


    // ========================================================
    // GET VOUCHER BY CODE
    // ========================================================

    suspend fun getVoucherByCode(
        voucherCode: String
    ): LoyaltyVoucherEntity? {

        return repository.getVoucherByCode(voucherCode)
    }


    // ========================================================
    // CLEAR UI MESSAGES
    // ========================================================

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }


    // ========================================================
    // VIEWMODEL FACTORY
    // ========================================================

    class Factory(
        private val repository: LoyaltyRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {

            if (modelClass.isAssignableFrom(LoyaltyViewModel::class.java)) {
                return LoyaltyViewModel(repository) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}