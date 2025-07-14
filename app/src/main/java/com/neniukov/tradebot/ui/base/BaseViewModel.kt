package com.neniukov.tradebot.ui.base

import android.util.Log
import androidx.lifecycle.ViewModel
import com.neniukov.tradebot.ui.base.model.DelayFlow
import com.neniukov.tradebot.ui.base.model.ProgressState
import com.neniukov.tradebot.ui.base.model.SingleAction
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : ViewModel(), CoroutineScope {

    private val dispatcher = Dispatchers.Main.immediate
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        handleError(e)
    }

    private val job = SupervisorJob()

    final override val coroutineContext = dispatcher + job + coroutineExceptionHandler

    private val _progressState: MutableStateFlow<ProgressState> = MutableStateFlow(ProgressState.Hide)
    val progressState = _progressState.asStateFlow()

    private val _errorFlow = MutableStateFlow<String?>(null)
    val errorFlow = _errorFlow.asStateFlow()

    protected val action = MutableStateFlow<SingleAction>(SingleAction.None)
    val actionFlow = action.asStateFlow()

    override fun onCleared() {
        coroutineContext.cancelChildren()
        super.onCleared()
    }

    fun showProgress() = _progressState.tryEmit(ProgressState.Show)

    fun hideProgress() {
        _progressState.tryEmit(ProgressState.Hide)
    }

    fun cleanErrorFlow() = _errorFlow.tryEmit(null)

    protected fun handleError(error: Throwable) {
        launch {
            error.printStackTrace()
            Log.e(ERROR_TAG, "${error.message}")
            hideProgress()
            if (error.message?.contains(HTTP_401) == true) {
                _errorFlow.emit("API keys are invalid or expired. Please re-enter your API key and secret key.")
            } else {
                _errorFlow.emit(error.message)
            }
            delay(DelayFlow.Long.delay)
            _errorFlow.emit(null)
        }
    }

    protected fun handleError(error: String) {
        handleError(Throwable(error))
    }

    protected fun <T> doLaunch(
        job: suspend () -> T,
        onSuccess: suspend ((T) -> Unit) = {},
        showProgress: Boolean = true,
        isSwipeProgress: Boolean = false,
        context: CoroutineContext = Dispatchers.IO
    ) = launch(context) {
        if (showProgress) showProgress()
        val result = job.invoke()
        if (showProgress || isSwipeProgress) hideProgress()
        onSuccess.invoke(result)
    }

    inline fun <reified T> MutableStateFlow<T>.updateStateWithDelay(
        newValue: T,
        delay: DelayFlow = DelayFlow.Short
    ) {
        launch {
            delay(delay.delay)
            this@updateStateWithDelay.emit(newValue)
        }
    }


    companion object {
        private const val ERROR_TAG = "APP_ERROR"
        private const val HTTP_401 = "HTTP 401"
    }
}