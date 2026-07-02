package com.watson.trackly.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.watson.trackly.data.dao.QRCodeEntityDAO
import com.watson.trackly.data.entity.QRCodeEntity

/**
 * Created by dan on 10/01/2024
 *
 * Copyright © 2024 1010 Creative. All rights reserved.
 */

@Database(entities = [QRCodeEntity::class], version = 1, exportSchema = false)
abstract class QRAppDatabase : RoomDatabase() {

    abstract fun qrCodeEntityDAO(): QRCodeEntityDAO

    companion object {
        private const val DB_NAME = "qr_app_db.sqlite"

        @Volatile
        private var INSTANCE: QRAppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = INSTANCE ?: synchronized(LOCK) {
            INSTANCE ?: buildDatabase(context).also {
                INSTANCE = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            QRAppDatabase::class.java,
            DB_NAME
        ).build()
    }
}