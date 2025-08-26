package com.github.droidworksstudio.launcher.di

import android.content.Context
import android.content.pm.PackageManager
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.droidworksstudio.launcher.data.AppDatabase
import com.github.droidworksstudio.launcher.data.dao.AppInfoDAO
import com.github.droidworksstudio.launcher.helper.AppHelper
import com.github.droidworksstudio.launcher.helper.BottomDialogHelper
import com.github.droidworksstudio.launcher.helper.PreferenceHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE app ADD COLUMN global_app_order INTEGER NOT NULL DEFAULT -1")
        }
    }

    @Provides
    @Singleton
    fun provideLocalDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
        .addMigrations(MIGRATION_1_2)
        .build()

    @Provides
    @Singleton
    fun provideAppDao(appDatabase: AppDatabase): AppInfoDAO = appDatabase.appDao()

    @Provides
    fun providePackageManager(@ApplicationContext context: Context): PackageManager {
        return context.packageManager
    }

    @Provides
    @Singleton
    fun providePreferenceHelper(@ApplicationContext context: Context): PreferenceHelper {
        return PreferenceHelper(context)
    }

    @Provides
    fun provideAppHelper(): AppHelper {
        return AppHelper()
    }

    @Provides
    fun provideBottomDialogHelper(): BottomDialogHelper {
        return BottomDialogHelper()
    }
}
