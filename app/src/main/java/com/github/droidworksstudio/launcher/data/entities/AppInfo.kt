package com.github.droidworksstudio.launcher.data.entities


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Every property has a default so Gson uses the no-arg constructor when restoring
// app backups, keeping defaults (e.g. globalAppOrder = -1) for fields older backups lack
@Entity(tableName = "app")
data class AppInfo(
    @PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = "id")
    var id: Int = 0,

    @field:ColumnInfo(name = "app_name")
    var appName: String = "",

    @field:ColumnInfo(name = "package_name")
    var packageName: String = "",

    @field:ColumnInfo(name = "is_favorite")
    var favorite: Boolean = false,

    @field:ColumnInfo(name = "is_hidden")
    var hidden: Boolean = false,

    @ColumnInfo(name = "is_lock")
    var lock: Boolean = false,

    @ColumnInfo(name = "user_handle")
    var userHandle: Int = 0,

    @ColumnInfo(name = "create_time")
    var createTime: String = "",

    @ColumnInfo(name = "app_order")
    var appOrder: Int = -1,

    @ColumnInfo(name = "global_app_order")
    var globalAppOrder: Int = -1,
)