package com.hasiruusiru.app.data

import kotlinx.coroutines.flow.Flow

class TreeRepository(private val treeDao: TreeDao) {
    val allTrees: Flow<List<TreePoint>> = treeDao.getAllTrees()

    suspend fun insert(treePoint: TreePoint) {
        treeDao.insertTree(treePoint)
        // Here you would also sync to Firebase Firestore
    }
}
