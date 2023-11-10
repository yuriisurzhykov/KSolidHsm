package com.yuriisurzhykov.ksolidhsm.states

import com.yuriisurzhykov.ksolidhsm.Event
import com.yuriisurzhykov.ksolidhsm.context.StateMachineContext
import com.yuriisurzhykov.ksolidhsm.extentions.ignore
import com.yuriisurzhykov.ksolidhsm.strategy.EventProcessResult

/**
 *  Just a dummy class to be returned when [State.Normal.parent] is null.
 * */
class EmptyState : State.Normal(null) {
    override suspend fun processEvent(
        event: Event,
        context: StateMachineContext
    ): EventProcessResult =
        ignore()
}