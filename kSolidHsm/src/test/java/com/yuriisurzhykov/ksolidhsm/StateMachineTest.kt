package com.yuriisurzhykov.ksolidhsm

import com.yuriisurzhykov.ksolidhsm.context.ServiceLocator
import com.yuriisurzhykov.ksolidhsm.context.StateMachineContext
import com.yuriisurzhykov.ksolidhsm.extentions.transitionTo
import com.yuriisurzhykov.ksolidhsm.extentions.unknown
import com.yuriisurzhykov.ksolidhsm.states.State
import com.yuriisurzhykov.ksolidhsm.strategy.EventProcessResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class StateMachineTest {

    private val testInitialStateName = TestInitialState::class.simpleName
    private val testStateName = TestState::class.simpleName

    @Test
    fun `test onExit called before onEnter for normal state`(): Unit = runBlocking {
        val stateToGoTo = TestState()
        val initialState = TestInitialState(stateToGoTo)
        val stateMachine = TestStateMachine(initialState)

        stateMachine.initialize()

        var actualCallOrder = callsOrderList
        var expectedOrder =
            listOf(
                "$testInitialStateName\$onEnter",
                "$testInitialStateName\$initialTransitionState"
            )

        assertEquals(expectedOrder, actualCallOrder)

        stateMachine.processEvent(object : Event {})

        expectedOrder =
            listOf(
                "$testInitialStateName\$onEnter",
                "$testInitialStateName\$initialTransitionState",
                "$testInitialStateName\$onExit",
                "$testStateName\$onEnter",
                "$testStateName\$initialTransitionState",
            )
        actualCallOrder = callsOrderList

        assertEquals(expectedOrder, actualCallOrder)

        assertEquals(1, initialState.onEnterCallsCount)
        assertEquals(1, initialState.onExitCallsCount)
        assertEquals(1, initialState.initialTransitionCallsCount)
        assertEquals(1, stateToGoTo.onEnterCallsCount)
        assertEquals(0, stateToGoTo.onExitCallsCount)
        assertEquals(1, stateToGoTo.initialTransitionCallsCount)
        callsOrderList.clear()
    }
}

internal val callsOrderList = mutableListOf<String>()

private class TestStateMachine(initialState: State) :
    StateMachine.Abstract(initialState, ServiceLocator.Empty())

internal abstract class AbstractTestState : State.Normal(null) {

    var processEventCallsCount: Int = 0
    var initialTransitionCallsCount: Int = 0
    var onEnterCallsCount: Int = 0
    var onExitCallsCount: Int = 0

    override suspend fun onEnter(context: StateMachineContext) {
        onEnterCallsCount++
        callsOrderList.add("${this::class.simpleName}\$onEnter")
    }

    override suspend fun initialTransitionState(context: StateMachineContext): State? {
        initialTransitionCallsCount++
        callsOrderList.add("${this::class.simpleName}\$initialTransitionState")
        return null
    }

    override suspend fun processEvent(
        event: Event,
        context: StateMachineContext
    ): EventProcessResult {
        processEventCallsCount++
        callsOrderList.add("${this::class.simpleName}\$processEvent")
        return unknown(event)
    }

    override suspend fun onExit(context: StateMachineContext) {
        onExitCallsCount++
        callsOrderList.add("${this::class.simpleName}\$onExit")
    }
}

private class TestInitialState(
    private val stateToGoTo: State
) : AbstractTestState() {
    override suspend fun processEvent(
        event: Event,
        context: StateMachineContext
    ): EventProcessResult {
        return transitionTo(stateToGoTo)
    }
}

internal class TestState : AbstractTestState()