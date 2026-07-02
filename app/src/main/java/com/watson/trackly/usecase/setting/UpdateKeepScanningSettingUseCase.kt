package com.watson.trackly.usecase.setting

import com.watson.trackly.repo.user.UserDataRepo
import com.watson.trackly.usecase.base.BaseUseCase
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

/**
 * Created by dan on 10/02/2024
 *
 * Copyright © 2024 1010 Creative. All rights reserved.
 */

@ViewModelScoped
class UpdateKeepScanningSettingUseCase @Inject constructor(
    private val userDataRepo: UserDataRepo
) : BaseUseCase<Boolean, Unit>() {
    override suspend fun execute(input: Boolean) {
        userDataRepo.updateKeepScanningSetting(input)
    }
}