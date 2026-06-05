package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AuctionDao
import com.example.data.entity.AuctionItemEntity
import com.example.data.entity.BidEntity
import com.example.data.entity.UserEntity
import com.example.data.entity.WinnerEntity

@Database(
    entities = [
        UserEntity::class,
        AuctionItemEntity::class,
        BidEntity::class,
        WinnerEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun auctionDao(): AuctionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "auctronix_live_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
