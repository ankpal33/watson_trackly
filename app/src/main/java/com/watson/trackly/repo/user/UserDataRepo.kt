package com.watson.trackly.repo.user

import com.watson.trackly.data.entity.UserSettingData
import kotlinx.coroutines.flow.Flow

/**
 * Created by dan on 20/01/2024
 *
 * Copyright © 2024 1010 Creative. All rights reserved.
 */

interface UserDataRepo {
    val userSettingData: Flow<UserSettingData>

    suspend fun updateSoundSetting(isEnableSound: Boolean)

    suspend fun isEnableSound(): Boolean

    suspend fun updateVibrateSetting(isEnableVibrate: Boolean)

    suspend fun isEnableVibrate(): Boolean

    suspend fun updatePremiumSetting(isPremium: Boolean)
    suspend fun isPremium(): Boolean
    suspend fun updateKeepScanningSetting(isKeepScanning: Boolean)
    suspend fun isKeepScanning(): Boolean

    /** Persist login session with current timestamp. */
    suspend fun saveLoginSession()

    /** Clear the persisted login session (on logout). */
    suspend fun clearLoginSession()

    /**
     * Returns true if a login session exists and was saved less than [sessionDurationMs] ago.
     * Default: 1 hour (3 600 000 ms).
     */
    suspend fun isSessionValid(sessionDurationMs: Long = 60L * 60L * 1000L): Boolean
}