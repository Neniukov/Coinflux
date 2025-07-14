package com.neniukov.tradebot.ui.screen.statistics

import com.neniukov.tradebot.data.model.response.StatisticsResponse
import com.neniukov.tradebot.data.repository.StatisticsRepository
import com.neniukov.tradebot.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) : BaseViewModel() {

    private val _statistics = MutableStateFlow<List<StatisticsResponse>>(emptyList())
    val statisticsFlow = _statistics.asStateFlow()

    fun loadStatistics() {
        doLaunch(
            job = {
                val startTime = System.currentTimeMillis() - SEVEN_DAYS
                statisticsRepository.getStatistics(startTime)
            },
            onSuccess = {
                _statistics.emit(it)
            }
        )
    }

    companion object {
        private const val SEVEN_DAYS = 604800000
    }
}