package com.hasiruusiru.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tree_points")
data class TreePoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val species: String,
    val girth: Double,
    val healthStatus: String,
    val type: String, // "Tree Tagger" or "Empty Pit"
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
