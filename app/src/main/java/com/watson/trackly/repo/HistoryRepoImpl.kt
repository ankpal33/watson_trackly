package com.watson.trackly.repo

import com.watson.trackly.data.dao.QRCodeEntityDAO
import com.watson.trackly.data.entity.QRCodeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by dan on 11/01/2024
 *
 * Copyright © 2024 1010 Creative. All rights reserved.
 */

class HistoryRepoImpl(private val qrCodeEntityDAO: QRCodeEntityDAO) : HistoryRepo {
    override suspend fun getQRCodeHistory(): List<QRCodeEntity> {
        return qrCodeEntityDAO.getAllQRCodeEntity()
    }

    override fun getQRCOdeHistoryFlow(): Flow<List<QRCodeEntity>> {
        return qrCodeEntityDAO.getAllQRCodeEntityFlow()
    }

    override suspend fun insertQRCodeEntity(qrCodeEntity: QRCodeEntity): Long {
        return qrCodeEntityDAO.insertQRCodeEntity(qrCodeEntity)
    }

    override suspend fun getQRCodeEntityById(id: Int): QRCodeEntity? {
        return qrCodeEntityDAO.getQRCodeEntityById(id)
    }

    override suspend fun deleteQRCodeEntity(id: Int) {
        qrCodeEntityDAO.deleteQRCodeEntity(id)
    }

    override suspend fun deleteQRCodeEntity(rawData: String) {
        qrCodeEntityDAO.deleteQRCodeEntity(rawData)
    }
}