package com.yuriisurzhykov.ksolidhsm

import com.yuriisurzhykov.ksolidhsm.context.ContextProvider
import com.yuriisurzhykov.ksolidhsm.context.DelicateStateMachineApi
import com.yuriisurzhykov.ksolidhsm.context.OperateStateMachine
import com.yuriisurzhykov.ksolidhsm.context.ServiceLocator
import com.yuriisurzhykov.ksolidhsm.context.StateMachineContext
import com.yuriisurzhykov.ksolidhsm.exceptions.IllegalInheritanceException
import com.yuriisurzhykov.ksolidhsm.exceptions.StateMachineInitializedException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Interface for representing a Hierarchical State Machine (HSM).
 * In HSM, every state may have derived states.
 * Extends [OperateStateMachine] for state operation and [ContextProvider] for context provisioning.
 */
@Suppress("KDocUnresolvedReference")
interface StateMachine : OperateStateMachine, ContextProvider {

    /**
     * Processes the given [event] and returns the new [State]. In terms of HSM that tied to
     * specific AO([ActiveObject]), the event is the one that AO received.
     *
     * @param event The event to process.
     * @return The new state after processing the event.
     */
    suspend fun processEvent(event: Event)

    /**
     *  Initializes the state machine, setting the initial state and performing any necessary setup.
     * */
    suspend fun initialize()

    fun current(): StateFlow<State>

    /**
     * Abstract class providing a skeletal implementation of [StateMachine]. This uses [StateFlow]
     * for current machine state representation. For [StateMachineContext] it creates
     * [StateMachineContext.ContextImpl] with given [serviceLocator].
     *
     * @property initialState The initial state of the state machine.
     * @property serviceLocator A service locator for dependency injection.
     */
    abstract class Abstract(
        private val initialState: State,
        private val serviceLocator: ServiceLocator
    ) : StateMachine {

        private var hasInitialized: Boolean = false
        private val state = MutableStateFlow(initialState)
        private val currentSMContext: StateMachineContext by lazy {
            StateMachineContext.ContextImpl(this, serviceLocator)
        }

        override val context: StateMachineContext
            get() = currentSMContext

        /**
         * Initializes the state machine if it hasn't been initialized already.
         * On initialization, invokes [State.onEnter] on the [initialState]. It has __final__
         * modifier to no one will change the logic for state machine initialization.
         *
         * @throws IllegalStateException If the state machine is already initialized.
         * @throws IllegalInheritanceException If next state is derived from current state
         *          and doesn't override transition
         */
        final override suspend fun initialize() {
            if (!hasInitialized) {
                val currentState = state.value
                currentState.onEnter(context)
                hasInitialized = true
            } else throw StateMachineInitializedException(this::class.simpleName.orEmpty())
        }

        override suspend fun processEvent(event: Event) {
            state.value.processEvent(event, context).execute(context)
        }

        @DelicateStateMachineApi
        override suspend fun nextState(nextState: State) {
            if (nextState != state.value) {
                state.value.onExit(context)
                state.emit(nextState)
                state.value.onEnter(context)
            }
        }

        override fun current(): StateFlow<State> = state.asStateFlow()
    }
}