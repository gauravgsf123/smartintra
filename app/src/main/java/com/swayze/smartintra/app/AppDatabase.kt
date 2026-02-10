package com.swayze.smartintra.app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.swayze.smartintra.ui.vehicle_load_unload.database.VehicleData
import com.swayze.smartintra.ui.vehicle_load_unload.database.VehicleDataDao
import com.swayze.smartintra.ui.vehicle_load_unload.database.VehicleListData
import com.swayze.smartintra.ui.vehicle_load_unload.database.VehicleListDataDao

@Database(entities = [VehicleData::class, VehicleListData::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vehicleDataDao(): VehicleDataDao
    abstract fun vehicleListDataDao(): VehicleListDataDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "smart_intra_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance

                }
                return instance
            }
        }
    }
}