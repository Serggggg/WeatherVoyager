package com.example.weathervoyager.domain.location

/**
 * Интерфейс, абстрагирующий получение текущих GPS-координат пользователя.
 */
interface LocationTracker {
    suspend fun getCurrentLocation(): Location?
}
