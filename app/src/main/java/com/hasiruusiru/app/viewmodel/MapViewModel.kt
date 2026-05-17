package com.hasiruusiru.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasiruusiru.app.data.TreePoint
import com.hasiruusiru.app.data.TreeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapViewModel(private val repository: TreeRepository) : ViewModel() {

    val treePoints: StateFlow<List<TreePoint>> = repository.allTrees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalOxygenScore: StateFlow<Double> = treePoints.map { points ->
        points.filter { it.type == "Tree Tagger" }
            .sumOf { calculateOxygenScore(it.girth, getSpeciesFactor(it.species)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val speciesFactors = mapOf(
        "Neem" to 1.5,
        "Peepal" to 2.0,
        "Honge" to 1.2,
        "Mango" to 1.3,
        "Banyan" to 1.8
    )

    fun addTreePoint(treePoint: TreePoint) {
        viewModelScope.launch {
            repository.insert(treePoint)
        }
    }

    fun calculateOxygenScore(girth: Double, speciesFactor: Double): Double {
        return girth * speciesFactor
    }

    fun getSpeciesFactor(species: String): Double {
        return speciesFactors[species] ?: 1.0
    }
}
