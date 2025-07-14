package com.neniukov.tradebot.domain.model

data class TradingState(
    val isActive: Boolean = false, // Бот активен или нет
    val isInPosition: Boolean = false, // Находимся ли мы в позиции
    val initialEntryPrice: Double = 0.0, // Цена первой точки входа
    val currentAverageEntryPrice: Double = 0.0, // Текущая средняя цена входа
    val totalPositionQuantity: Double = 0.0, // Общее количество ETH в позиции
    val baseOrderQuantity: Double = 0.0, // Базовый объем для каждого ордера (например, 0.01 ETH)
    val lastCandleTime: Long = 0L, // Время последней обработанной свечи, чтобы не обрабатывать одну и ту же свечу дважды
    val isSecondEntry: Boolean = false, // Флаг, указывающий на то, что мы находимся во второй точке входа
)