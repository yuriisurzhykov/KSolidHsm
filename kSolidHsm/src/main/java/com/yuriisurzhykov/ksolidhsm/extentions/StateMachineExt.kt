@file:Suppress("UnusedReceiverParameter", "unused")

package com.yuriisurzhykov.ksolidhsm.extentions

import com.yuriisurzhykov.ksolidhsm.Event
import com.yuriisurzhykov.ksolidhsm.states.State
import com.yuriisurzhykov.ksolidhsm.context.StateMachineContext
import com.yuriisurzhykov.ksolidhsm.strategy.EventProcessResult

fun State.Normal.transitionTo(state: State) = EventProcessResult.TransitionTo(state)

fun State.Normal.ignore() = EventProcessResult.Ignore

fun State.Normal.handle(block: suspend (StateMachineContext) -> Unit) =
    EventProcessResult.Handled(block)

fun State.Normal.unknown(event: Event) = EventProcessResult.Unknown(event, this)