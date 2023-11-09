package com.yuriisurzhykov.ksolidhsm.example

import com.yuriisurzhykov.ksolidhsm.Event
import com.yuriisurzhykov.ksolidhsm.State
import com.yuriisurzhykov.ksolidhsm.context.DelicateStateMachineApi
import com.yuriisurzhykov.ksolidhsm.context.StateMachineContext
import com.yuriisurzhykov.ksolidhsm.extentions.ignore
import com.yuriisurzhykov.ksolidhsm.extentions.transitionTo
import com.yuriisurzhykov.ksolidhsm.extentions.unknown
import com.yuriisurzhykov.ksolidhsm.strategy.ProcessResult

abstract class ExampleStates : State.Normal {

    constructor(parentInit: () -> State?, initialTransition: () -> State? = { null }) : super(
        parentInit,
        initialTransition
    )

    constructor(parent: State?, initialTransition: State? = null) : super(parent, initialTransition)


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
internal class App : ExampleStates(null, Menu()) {

    override suspend fun processEvent(
        event: Event,
        context: StateMachineContext
    ): ProcessResult = ignore()
}

class Menu : ExampleStates({ App() }) {

    override suspend fun processEvent(
        event: Event,
        context: StateMachineContext
    ): ProcessResult =
        if (event is ExampleEvents.Play) transitionTo(Play())
        else unknown(event)
}

class Play : ExampleStates(App(), Ping()) {

    @OptIn(DelicateStateMachineApi::class)
    override suspend fun onEnter(context: StateMachineContext) {
        super.onEnter(context)
        context.operateStateMachine().nextState(Ping())
    }

    override suspend fun processEvent(event: Event, context: StateMachineContext): ProcessResult =
        if (event is ExampleEvents.Menu) transitionTo(Menu())
        else unknown(event)

}


class Ping : ExampleStates({ Play() }) {
    override suspend fun processEvent(
        event: Event,
        context: StateMachineContext
    ): ProcessResult =
        if (event is ExampleEvents.Pong) transitionTo(Pong())
        else unknown(event)
}

class Pong : ExampleStates(Play()) {
    override suspend fun processEvent(
        event: Event,
        context: StateMachineContext
    ): ProcessResult =
        if (event is ExampleEvents.Ping) transitionTo(Ping())
        else unknown(event)
}