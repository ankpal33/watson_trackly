package com.watson.trackly.di.module

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.watson.trackly.data.dao.QRCodeEntityDAO
import com.watson.trackly.repo.HistoryRepo
import com.watson.trackly.repo.HistoryRepoImpl
import com.watson.trackly.repo.user.UserDataRepo
import com.watson.trackly.repo.user.UserDataRepoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {

    @Singleton
    @Provides
    fun provideHistoryRepo(qrCodeEntityDAO: QRCodeEntityDAO): HistoryRepo =
        HistoryRepoImpl(qrCodeEntityDAO)

    @Singleton
    @Provides
    fun provideUserDataRepo(userSettingPref: DataStore<Preferences>): UserDataRepo =
        UserDataRepoImpl(userSettingPref)
}