package com.yuriisurzhykov.ksolidhsm.strategy

import com.yuriisurzhykov.ksolidhsm.Event
import com.yuriisurzhykov.ksolidhsm.State
import com.yuriisurzhykov.ksolidhsm.context.DelicateStateMachineApi
import com.yuriisurzhykov.ksolidhsm.context.StateMachineContext
import com.yuriisurzhykov.ksolidhsm.exceptions.RecursiveHierarchyException


sealed interface ProcessResult {

    suspend fun execute(context: StateMachineContext)

    data class TransitionTo(private val state: State) : ProcessResult {
        @OptIn(DelicateStateMachineApi::class)
        override suspend fun execute(context: StateMachineContext) {
            context.operateStateMachine().nextState(state)
        }
    }

    data class Unknown(
        private val event: Event,
        private val currentState: State.Normal
    ) : ProcessResult {
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

    data object Ignore : ProcessResult {
        override suspend fun execute(context: StateMachineContext) {
            // Do nothing because we ignored an event
        }
    }
}