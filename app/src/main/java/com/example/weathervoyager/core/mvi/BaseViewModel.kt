package com.example.weathervoyager.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base MVI ViewModel.
 */
abstract class BaseViewModel<S : UiState, E : UiEvent, F : UiEffect>(
    initialState: S
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _uiEffect = Channel<F>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    /**
     * Handle UI events.
     */
    abstract fun handleEvent(event: E)

    /**
     * Sets new UI State.
     */
    protected fun setState(reduce: S.() -> S) {
        _uiState.update { it.reduce() }
    }

    /**
     * Sends a one-time effect (such as navigation or snackbar).
     */
    protected fun setEffect(builder: () -> F) {
        viewModelScope.launch {
            _uiEffect.send(builder())
        }
    }
}
