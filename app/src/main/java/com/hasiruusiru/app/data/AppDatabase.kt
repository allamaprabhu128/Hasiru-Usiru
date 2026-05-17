package com.hasiruusiru.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow



@Dao
interface TreeDao {
    @Query("SELECT * FROM tree_points ORDER BY timestamp DESC")
    fun getAllTrees(): Flow<List<TreePoint>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTree(treePoint: TreePoint)

    @Delete
    suspend fun deleteTree(treePoint: TreePoint)
}

@Database(entities = [TreePoint::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun treeDao(): TreeDao
}
