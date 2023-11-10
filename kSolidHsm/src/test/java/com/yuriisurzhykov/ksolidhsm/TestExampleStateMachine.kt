package com.yuriisurzhykov.ksolidhsm

import com.yuriisurzhykov.ksolidhsm.example.App
import com.yuriisurzhykov.ksolidhsm.example.ExampleEvents
import com.yuriisurzhykov.ksolidhsm.example.ExampleStateMachine
import com.yuriisurzhykov.ksolidhsm.example.ExampleStates
import com.yuriisurzhykov.ksolidhsm.example.Menu
import com.yuriisurzhykov.ksolidhsm.example.Ping
import com.yuriisurzhykov.ksolidhsm.example.Pong
import com.yuriisurzhykov.ksolidhsm.exceptions.StateMachineInitializedException
import org.junit.Assert.assertEquals
import org.junit.Test

class TestExampleStateMachine : AbstractTest() {

    @Test(expected = StateMachineInitializedException::class)
    fun `test multiple initialization calls`() = runTest {
        val stateMachine = ExampleStateMachine()
        stateMachine.initialize()
        stateMachine.initialize()
        stateMachine.initialize()
        stateMachine.current().value as ExampleStates
    }

    @Test
    fun `test initial onEnter calls count`() = runTest {
        val state = App
        state.enterCallCount = 0

        val stateMachine = ExampleStateMachine(state)
        stateMachine.initialize()

        val actual = state.enterCallCount
        val expected = 1
        assertEquals(expected, actual)
        state
    }

    @Test
    fun `test initial onEnter calls count for Menu`() = runTest {
        Menu.enterCallCount = 0
        App.enterCallCount = 0

        val stateMachine = ExampleStateMachine()

        stateMachine.initialize()

        val currentState = stateMachine.current().value as Menu
        val actual = currentState.enterCallCount
        val expected = 1
        assertEquals(expected, actual)
        currentState
    }

    @Test
    fun `test initial onExit calls count for App state`() = runTest {
        val state = App
        val stateMachine = ExampleStateMachine(state)
        stateMachine.initialize()

        val actual = state.exitCallCount
        val expected = 1
        assertEquals(expected, actual)
        state
    }

    @Test
    fun `test transition to menu during initializing`() = runTest {
        val stateMachine = ExampleStateMachine()
        stateMachine.initialize()

        val expected = Menu
        val actual = stateMachine.current().value
        assertEquals(expected, actual)
        actual as ExampleStates
    }

    @Test
    fun `test from menu to play on Play event`() = runTest {
        val stateMachine = ExampleStateMachine(Menu)
        stateMachine.initialize()

        stateMachine.processEvent(ExampleEvents.Play)

        val expected = Ping
        val actual = stateMachine.current().value
        assertEquals(expected, actual)
        actual as ExampleStates
    }

    @Test
    fun `test from game to menu on Menu event`() = runTest {
        val stateMachine = ExampleStateMachine(Ping)
        stateMachine.initialize()

        stateMachine.processEvent(ExampleEvents.Menu)

        val expected = Menu
        val actual = stateMachine.current().value
        assertEquals(expected, actual)
        actual as ExampleStates
    }

    @Test
    fun `test from Ping state to Pong on Pong event`() = runTest {
        val stateMachine = ExampleStateMachine(Ping)
        stateMachine.initialize()

        stateMachine.processEvent(ExampleEvents.Pong)

        val expected = Pong
        val actual = stateMachine.current().value
        assertEquals(expected, actual)
        actual as ExampleStates
    }

    @Test
    fun `test from Pong state to Ping on Ping event`() = runTest {
        val stateMachine = ExampleStateMachine(Ping)
        stateMachine.initialize()

        stateMachine.processEvent(ExampleEvents.Pong)

        val expected = Pong
        val actual = stateMachine.current().value
        assertEquals(expected, actual)
        actual as ExampleStates
    }
}
