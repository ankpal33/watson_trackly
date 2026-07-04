package com.watson.trackly.ui.login

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watson.trackly.repo.user.UserDataRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
data class LoginUIState(
    val userId: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val userDataRepo: UserDataRepo
) : ViewModel() {

    companion object {
        private const val VALID_USER_ID = "admin"
        private const val VALID_PASSWORD = "admin"
    }

    private val _loginUiState: MutableStateFlow<LoginUIState> = MutableStateFlow(LoginUIState())
    val loginUiState: StateFlow<LoginUIState> = _loginUiState.asStateFlow()

    fun onUserIdChange(userId: String) {
        _loginUiState.value = _loginUiState.value.copy(
            userId = userId,
            errorMessage = null
        )
    }

    fun onPasswordChange(password: String) {
        _loginUiState.value = _loginUiState.value.copy(
            password = password,
            errorMessage = null
        )
    }

    fun login() {
        viewModelScope.launch {
            _loginUiState.value = _loginUiState.value.copy(isLoading = true, errorMessage = null)

            // Check network connectivity
            if (!isNetworkAvailable()) {
                _loginUiState.value = _loginUiState.value.copy(
                    isLoading = false,
                    errorMessage = "Something went wrong"
                )
                return@launch
            }

            // Validate credentials
            val currentState = _loginUiState.value
            if (currentState.userId == VALID_USER_ID && currentState.password == VALID_PASSWORD) {
                // Successful login — persist session so it survives restarts
                userDataRepo.saveLoginSession()
                _loginUiState.value = _loginUiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    errorMessage = null
                )
            } else {
                // Failed login
                _loginUiState.value = _loginUiState.value.copy(
                    isLoading = false,
                    errorMessage = "Invalid User ID or Password"
                )
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                   capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }

    fun resetLoginState() {
        viewModelScope.launch { userDataRepo.clearLoginSession() }
        _loginUiState.value = LoginUIState()
    }
}

// Made with Bob
