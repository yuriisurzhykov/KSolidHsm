package com.yuriisurzhykov.ksolidhsm.strategy

import com.yuriisurzhykov.ksolidhsm.Event
import com.yuriisurzhykov.ksolidhsm.states.State
import com.yuriisurzhykov.ksolidhsm.context.DelicateStateMachineApi
import com.yuriisurzhykov.ksolidhsm.context.StateMachineContext
import com.yuriisurzhykov.ksolidhsm.exceptions.RecursiveHierarchyException


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