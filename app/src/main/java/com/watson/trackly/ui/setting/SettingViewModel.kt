package com.watson.trackly.ui.setting

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watson.trackly.R
import com.watson.trackly.repo.user.UserDataRepo
import com.watson.trackly.ui.map.LocationStatus
import com.watson.trackly.usecase.setting.GetAppSettingFlowUseCase
import com.watson.trackly.usecase.setting.UpdateKeepScanningSettingUseCase
import com.watson.trackly.usecase.setting.UpdatePremiumSettingUseCase
import com.watson.trackly.usecase.setting.UpdateSoundSettingUseCase
import com.watson.trackly.usecase.setting.UpdateVibrateSettingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by dan on 19/01/2024
 *
 * Copyright © 2024 1010 Creative. All rights reserved.
 */

enum class SettingNavigation {
    ABOUT_US,
    RATE_US,
    MANAGE_SUBSCRIPTION,
    PURCHASE_PREMIUM
}

data class LocationItem(
    val name: String,
    val barcode: String,
    val status: LocationStatus = LocationStatus.DEFAULT
)

@HiltViewModel
class SettingViewModel @Inject constructor(
    getAppSettingUseCase: GetAppSettingFlowUseCase,
    private val updateSoundSettingUseCase: UpdateSoundSettingUseCase,
    private val updateVibrateSettingUseCase: UpdateVibrateSettingUseCase,
    private val updateKeepScanningSettingUseCase: UpdateKeepScanningSettingUseCase,
    private val updatePremiumSettingUseCase: UpdatePremiumSettingUseCase,
    private val userDataRepo: UserDataRepo,
    @ApplicationContext private val applicationContext: Context
) : ViewModel() {

    val listSettingUIState: StateFlow<ListSettingUIState> =
        getAppSettingUseCase.execute(Unit).stateIn(viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ListSettingUIState(emptyList()))

    private val _toastSharedFlow: MutableSharedFlow<String> = MutableSharedFlow(extraBufferCapacity = 1)
    val toastSharedFlow: SharedFlow<String> = _toastSharedFlow

    private val _navigationSharedFlow: MutableSharedFlow<SettingNavigation> = MutableSharedFlow(extraBufferCapacity = 1)
    val navigationSharedFlow: SharedFlow<SettingNavigation> = _navigationSharedFlow


    fun handleSetting(settingItem: SettingItemUIState) {
        when (settingItem) {
            is SettingItemUIState.SwitchUIState -> {
                when (settingItem.id) {
                    SettingId.SOUND.value -> {
                        viewModelScope.launch {
                            updateSoundSettingUseCase.execute(!settingItem.isEnable)
                        }
                    }

                    SettingId.VIBRATE.value -> {
                        viewModelScope.launch {
                            updateVibrateSettingUseCase.execute(!settingItem.isEnable)
                        }
                    }

                    SettingId.KEEP_SCANNING.value -> {
                        viewModelScope.launch {
                            if (userDataRepo.isPremium()) {
                                updateKeepScanningSettingUseCase.execute(!settingItem.isEnable)
                            } else {
                                _toastSharedFlow.emit(applicationContext.getString(R.string.this_feature_is_only_available_for_premium_user))
                            }
                        }
                    }

                    SettingId.PREMIUM.value -> {
                        viewModelScope.launch {
                            updatePremiumSettingUseCase.execute(!settingItem.isEnable)
                        }
                    }
                }
            }

            is SettingItemUIState.TextUIState -> {
                when (settingItem.id) {
                    SettingId.ABOUT_US.value -> {
                        _toastSharedFlow.tryEmit(settingItem.title)
                        _navigationSharedFlow.tryEmit(SettingNavigation.ABOUT_US)
                    }

                    SettingId.RATE_US.value -> {
                        _toastSharedFlow.tryEmit(settingItem.title)
                        _navigationSharedFlow.tryEmit(SettingNavigation.RATE_US)
                    }

                    SettingId.MANAGE_SUBSCRIPTION.value -> {
                        viewModelScope.launch {
                            _toastSharedFlow.emit(settingItem.title)
                            _navigationSharedFlow.emit(
                                if (userDataRepo.isPremium())
                                    SettingNavigation.MANAGE_SUBSCRIPTION
                                else
                                    SettingNavigation.PURCHASE_PREMIUM
                            )
                        }
                    }
                }
            }

            else -> {}
        }
    }
}

@Stable
data class ListSettingUIState(val data: List<SettingItemUIState>)

@Stable
sealed class SettingItemUIState(open val id: Int) {

    @Stable
    data class SettingHeaderUIState(
        override val id: Int,
        val title: String,
        val description: String = "",
    ) : SettingItemUIState(id)

    @Stable
    data class TextUIState(
        override val id: Int,
        val title: String,
        val description: String = "",
        @DrawableRes val iconRes: Int,
    ) : SettingItemUIState(id)

    @Stable
    data class SwitchUIState(
        override val id: Int,
        val title: String,
        val isEnable: Boolean = false,
    ) : SettingItemUIState(id)

    @Stable
    data class DividerUIState(
        override val id: Int,
    ) : SettingItemUIState(id)
}

enum class SettingId(val value: Int) {
    SOUND(1),
    VIBRATE(2),
    ABOUT_US(3),
    RATE_US(4),
    MANAGE_SUBSCRIPTION(5),
    KEEP_SCANNING(6),
    PREMIUM(7)
}