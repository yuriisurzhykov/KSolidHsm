package com.yuriisurzhykov.ksolidhsm.example

import com.yuriisurzhykov.ksolidhsm.Event
import com.yuriisurzhykov.ksolidhsm.context.StateMachineContext
import com.yuriisurzhykov.ksolidhsm.extentions.ignore
import com.yuriisurzhykov.ksolidhsm.extentions.transitionTo
import com.yuriisurzhykov.ksolidhsm.extentions.unknown
import com.yuriisurzhykov.ksolidhsm.states.State
import com.yuriisurzhykov.ksolidhsm.strategy.EventProcessResult

abstract class ExampleStates : State.Normal {


    constructor(parent: State?) : super(parent)
    constructor(parentInit: () -> State?) : super(parentInit)


    var exitCallCount: Int = 0
    var enterCallCount: Int = 0

    override suspend fun onExit(context: StateMachineContext) {
        super.onExit(context)
        exitCallCount++
    }

    override suspend fun onEnter(context: StateMachineContext) {
        enterCallCount++
        return super.onEnter(context)
    }
}

// Initial state that is transient
internal object App : ExampleStates(null) {

    override suspend fun initialTransitionState(context: StateMachineContext) = Menu

    override suspend fun processEvent(
        event: Event,
        context: StateMachineContext
    ): EventProcessResult = ignore()
}

object Menu : ExampleStates(App) {

    override suspend fun processEvent(
        event: Event,
        context: StateMachineContext
    ): EventProcessResult =
        if (event is ExampleEvents.Play) transitionTo(Play)
        else unknown(event)
}

object Play : ExampleStates(App) {

    override suspend fun initialTransitionState(context: StateMachineContext) = Ping

    override suspend fun processEvent(
        event: Event,
        context: StateMachineContext
    ): EventProcessResult =
        if (event is ExampleEvents.Menu) transitionTo(Menu)
        else unknown(event)

}


object Ping : ExampleStates(Play) {
    override suspend fun processEvent(
        event: Event,
        context: StateMachineContext
    ): EventProcessResult =
        if (event is ExampleEvents.Pong) transitionTo(Pong)
        else unknown(event)
}

object Pong : ExampleStates(Play) {
    override suspend fun processEvent(
        event: Event,
        context: StateMachineContext
    ): EventProcessResult =
        if (event is ExampleEvents.Ping) transitionTo(Ping)
        else unknown(event)
}
