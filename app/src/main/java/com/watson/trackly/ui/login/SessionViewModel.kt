package com.watson.trackly.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watson.trackly.repo.user.UserDataRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Lightweight ViewModel that exposes session helpers to the nav graph.
 * Hilt injects [UserDataRepo] here so QRApp can call session methods
 * without needing direct repo access.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    val repo: UserDataRepo
) : ViewModel() {

    fun clearSession() {
        viewModelScope.launch { repo.clearLoginSession() }
    }
}

// Made with Bob
