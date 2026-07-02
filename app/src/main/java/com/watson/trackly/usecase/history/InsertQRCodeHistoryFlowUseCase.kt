package com.watson.trackly.usecase.history

import com.watson.trackly.data.entity.QRCodeEntity
import com.watson.trackly.repo.HistoryRepo
import com.watson.trackly.repo.user.UserDataRepo
import com.watson.trackly.usecase.base.BaseFlowUseCase
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by dan on 14/01/2024
 *
 * Copyright © 2024 1010 Creative. All rights reserved.
 */

@ViewModelScoped
class InsertQRCodeHistoryFlowUseCase @Inject
 constructor(
    private val historyRepo: HistoryRepo,
    private val userDataRepo: UserDataRepo
 ) : BaseFlowUseCase<QRCodeEntity, Long>() {
    override fun execute(input: QRCodeEntity): Flow<Long> {
        return flow {
            if (userDataRepo.isPremium()) {
                input.rawData?.let { historyRepo.deleteQRCodeEntity(it) }
            }
            emit(historyRepo.insertQRCodeEntity(input))
        }
    }
}