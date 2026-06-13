package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FriendViewModel(private val repository: FriendRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // State for visual focus highlights
    private val _showRegisterModal = MutableStateFlow(false)
    val showRegisterModal = _showRegisterModal.asStateFlow()

    private val _selectedProfile = MutableStateFlow<FriendProfile?>(null)
    val selectedProfile = _selectedProfile.asStateFlow()

    // Money generation flows
    private val _totalEarnings = MutableStateFlow(3.50)
    val totalEarnings = _totalEarnings.asStateFlow()

    private val _earningLogs = MutableStateFlow(listOf(
        "Welcome bonus: +$3.50 credited 🪙",
        "Session started: Ticker active ⚡"
    ))
    val earningLogs = _earningLogs.asStateFlow()

    // List of profiles matching the search query
    val filteredProfiles: StateFlow<List<FriendProfile>> = combine(
        repository.allProfiles,
        _searchQuery
    ) { profiles, query ->
        if (query.isBlank()) {
            profiles
        } else {
            profiles.filter { profile ->
                profile.name.contains(query, ignoreCase = true) ||
                profile.hometown.contains(query, ignoreCase = true) ||
                profile.memoryDetail.contains(query, ignoreCase = true)
            }
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val currentUser: StateFlow<CurrentUser?> = repository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val memories: StateFlow<List<HometownMemory>> = repository.allMemories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.checkAndSeedIfEmpty()
        }

        // Keep local earnings state in sync with loaded profile
        viewModelScope.launch {
            repository.currentUser.collect { user ->
                if (user != null) {
                    _totalEarnings.value = user.earningsBalance
                }
            }
        }

        // REAL-TIME TICKER: Automatically generates money as people keep using the app
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1200)
                addEarningBonus(0.01, "Shared ad-revenue ticker", logAction = false)
            }
        }
    }

    fun addEarningBonus(amount: Double, reason: String, logAction: Boolean = false) {
        val newAmount = _totalEarnings.value + amount
        _totalEarnings.value = newAmount

        if (logAction) {
            val currentList = _earningLogs.value.toMutableList()
            val formatted = String.format(java.util.Locale.US, "+$%.2f", amount)
            currentList.add(0, "✨ $reason ($formatted)")
            if (currentList.size > 5) {
                _earningLogs.value = currentList.take(5)
            } else {
                _earningLogs.value = currentList
            }
        }

        // Persist to user if they are registered
        viewModelScope.launch {
            repository.updateCurrentUserBalance(newAmount)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun triggerSearchReward() {
        if (_searchQuery.value.isNotBlank()) {
            addEarningBonus(0.50, "Query: \"${_searchQuery.value}\"", logAction = true)
        }
    }

    fun selectProfile(profile: FriendProfile?) {
        _selectedProfile.value = profile
        if (profile != null) {
            addEarningBonus(0.75, "Profile view: ${profile.name.substringBefore(" ")}", logAction = true)
        }
    }

    fun setShowRegisterModal(show: Boolean) {
        _showRegisterModal.value = show
    }

    fun registerUser(
        name: String,
        email: String,
        hometown: String,
        searchIntent: String,
        googleMail: String? = null,
        phoneNumber: String? = null,
        faceVerified: Boolean = false,
        facePhotoUri: String? = null
    ) {
        viewModelScope.launch {
            val bonusRate = if (faceVerified) 10.00 else 5.00
            val startingBalance = _totalEarnings.value + bonusRate
            val user = CurrentUser(
                name = name,
                email = email,
                hometown = hometown,
                searchIntent = searchIntent,
                earningsBalance = startingBalance,
                googleMail = googleMail,
                phoneNumber = phoneNumber,
                faceVerified = faceVerified,
                facePhotoUri = facePhotoUri
            )
            repository.saveCurrentUser(user)
            _showRegisterModal.value = false
            _totalEarnings.value = startingBalance
            addEarningBonus(bonusRate, if (faceVerified) "Registered + Face Verification Reward" else "Registered account bonus", logAction = true)
        }
    }

    fun postMemory(text: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val memory = HometownMemory(
                authorName = user.name,
                hometownLocation = user.hometown,
                memoryText = text
            )
            repository.insertMemory(memory)
            addEarningBonus(2.50, "Memory board contribution", logAction = true)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout();
            _selectedProfile.value = null
            _totalEarnings.value = 3.50 // Reset back to empty guest baseline
            _earningLogs.value = listOf(
                "Welcome bonus: +$3.50 credited 🪙",
                "Session started: Ticker active ⚡"
            )
        }
    }
}

class FriendViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = AppDatabase.getDatabase(context)
        val repository = FriendRepository(database.friendDao())
        return FriendViewModel(repository) as T
    }
}
