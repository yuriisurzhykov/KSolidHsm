@file:Suppress("UnusedReceiverParameter", "unused")

package com.yuriisurzhykov.ksolidhsm.extentions

import com.yuriisurzhykov.ksolidhsm.Event
import com.yuriisurzhykov.ksolidhsm.StateMachine
import com.yuriisurzhykov.ksolidhsm.context.StateMachineContext
import com.yuriisurzhykov.ksolidhsm.states.State
import com.yuriisurzhykov.ksolidhsm.strategy.EventProcessResult

/**
 *  Transitions [StateMachine] to the state provided as [state] parameter.
 * */
fun State.Normal.transitionTo(state: State) = EventProcessResult.TransitionTo(state)

/**
 *  If [Event] is processed and there is nothing to do, this function should be called.
 * */
fun State.Normal.ignore() = EventProcessResult.Ignore

/**
 *  If [Event] is processed and there is action you need to run, this function should be called.
 * */
fun State.Normal.handle(block: suspend (StateMachineContext) -> Unit) =
    EventProcessResult.Handled(block)

/**
 *  If [State] doesn't know how to process an [Event], but it has parent, this function
 *  should be called
 * */
fun State.Normal.unknown(event: Event) = EventProcessResult.Unknown(event, this)