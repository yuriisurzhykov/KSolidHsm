@file:Suppress("UnusedReceiverParameter")

package com.yuriisurzhykov.ksolidhsm.extentions

import com.yuriisurzhykov.ksolidhsm.Event
import com.yuriisurzhykov.ksolidhsm.State
import com.yuriisurzhykov.ksolidhsm.strategy.ProcessResult

fun State.Normal.transitionTo(state: State) = ProcessResult.TransitionTo(state)

fun State.Normal.ignore() = ProcessResult.Ignore

fun State.Normal.unknown(event: Event) = ProcessResult.Unknown(event, this)