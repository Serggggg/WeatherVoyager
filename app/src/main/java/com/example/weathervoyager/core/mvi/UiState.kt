package com.example.weathervoyager.core.mvi

/**
 * Base (marker) interface for all UI states.
 * Specific screens should define their own sealed classes
 * and include states such as Idle, Loading, Content, and Error.
 */
interface UiState
