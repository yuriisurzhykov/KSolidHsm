package com.yuriisurzhykov.ksolidhsm.strategy

import com.yuriisurzhykov.ksolidhsm.Event
import com.yuriisurzhykov.ksolidhsm.StateMachine
import com.yuriisurzhykov.ksolidhsm.context.DelicateStateMachineApi
import com.yuriisurzhykov.ksolidhsm.context.StateMachineContext
import com.yuriisurzhykov.ksolidhsm.exceptions.RecursiveHierarchyException
import com.yuriisurzhykov.ksolidhsm.states.State
import com.yuriisurzhykov.ksolidhsm.strategy.EventProcessResult.Handled
import com.yuriisurzhykov.ksolidhsm.strategy.EventProcessResult.Ignore
import com.yuriisurzhykov.ksolidhsm.strategy.EventProcessResult.TransitionTo

/**
 *  ProcessResult is a strategy pattern for processing events. When [State] consume [Event],
 *  the last one should return result of what to do after checking the type of event.
 *  There 3 main strategies:
 *  - [TransitionTo] moves [StateMachine] to the state provided in [TransitionTo.state].
 *  - [Unknown] causes [StateMachine] to talk to [State] parent if it exists, and calls [State.processEvent]
 *  on parent state.
 *  - [Ignore] does nothing, indicates only that event is processed and everything is ok.
 *  - [Handled] does nothing with an event, but executes [Handled.eventOperation] when strategy is
 *  being executed.
 * */
sealed interface EventProcessResult {

    suspend fun execute(context: StateMachineContext)

    data class TransitionTo(private val state: State) : EventProcessResult {
        @OptIn(DelicateStateMachineApi::class)
        override suspend fun execute(context: StateMachineContext) {
            context.operateStateMachine().nextState(state)
        }
    }

    data class Unknown(
        private val event: Event,
        private val currentState: State.Normal
    ) : EventProcessResult {
        override suspend fun execute(context: StateMachineContext) {
            if (currentState.hasParent()) {
                val parent = currentState.parent()
                if (parent is State.Normal && parent.hasParent() && parent.parent() == currentState) {
                    throw RecursiveHierarchyException(currentState, parent)
                }
                currentState
                    .parent()
                    .processEvent(event, context)
                    .execute(context)
            }
        }
    }

    data object Ignore : EventProcessResult {
        override suspend fun execute(context: StateMachineContext) {
            // Do nothing because we ignored an event
        }
    }

    class Handled(
        private val eventOperation: (suspend (StateMachineContext) -> Unit)? = null
    ) : EventProcessResult {
        override suspend fun execute(context: StateMachineContext) {
            // Execute operation, if not null
            eventOperation?.invoke(context)
            // Then do nothing because we ignored an event
        }
    }
}