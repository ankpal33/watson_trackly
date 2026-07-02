package com.watson.trackly

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Created by dan on 11/01/2024
 *
 * Copyright © 2024 1010 Creative. All rights reserved.
 */

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        setupStrictMode()
    }

    private fun setupStrictMode() {
//        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
//            .detectAll()
//            .penaltyLog()
//            .penaltyDialog()
//            .build())
    }
}