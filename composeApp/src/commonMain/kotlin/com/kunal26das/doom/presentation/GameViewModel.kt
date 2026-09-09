package com.kunal26das.doom.presentation

import androidx.lifecycle.ViewModel
import com.kunal26das.doom.domain.FrameClock
import com.kunal26das.doom.domain.GameInput
import com.kunal26das.doom.domain.RunGameSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex

/** Holds presentation state while the view owns lifecycle and display pacing. */
class GameViewModel(private val session: RunGameSession) : ViewModel() {
    private val mutableState = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = mutableState.asStateFlow()

    private val runGuard = Mutex()
    private var terminated = false

    /**
     * A lifecycle cancellation pauses this ViewModel and permits a later resume.
     * Engine exit or failure is terminal for this ViewModel instance.
     */
    suspend fun run(clock: FrameClock) {
        if (terminated || !runGuard.tryLock()) return
        try {
            mutableState.value = mutableState.value.copy(phase = GamePhase.Loading, error = null)
            session.run(clock) { frame ->
                mutableState.value = GameUiState(phase = GamePhase.Running, frame = frame)
            }
            terminated = true
            mutableState.value = mutableState.value.copy(phase = GamePhase.Stopped)
        } catch (cancelled: CancellationException) {
            mutableState.value = mutableState.value.copy(phase = GamePhase.Stopped)
            throw cancelled
        } catch (failure: Throwable) {
            terminated = true
            mutableState.value = mutableState.value.copy(
                phase = GamePhase.Failed,
                error = failure.message ?: failure.toString(),
            )
        } finally {
            runGuard.unlock()
        }
    }

    fun onInput(input: GameInput) {
        session.postInput(input)
    }
}
