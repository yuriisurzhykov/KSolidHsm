package com.yuriisurzhykov.ksolidhsm

import com.yuriisurzhykov.ksolidhsm.context.DelicateStateMachineApi
import com.yuriisurzhykov.ksolidhsm.context.ServiceLocator
import com.yuriisurzhykov.ksolidhsm.context.StateMachineContext
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class StateMachineContextTest {
    @OptIn(DelicateStateMachineApi::class)
    @Test
    fun `test context impl provides correct instances`(): Unit = runBlocking {
        val testSl = TestServiceLocator()

        val stateMachine: StateMachine =
            object : StateMachine.Abstract(TestState(), testSl) {}

        val contextImpl = StateMachineContext.ContextImpl(stateMachine, testSl)

        assertEquals(stateMachine, contextImpl.currentStateMachine())
        assertEquals(stateMachine, contextImpl.operateStateMachine())
        assertEquals(testSl, contextImpl.serviceLocator())
    }

    @Test
    fun `test state machine provides ContextImpl`(): Unit = runBlocking {
        val testSl = TestServiceLocator()

        val stateMachine: StateMachine =
            object : StateMachine.Abstract(TestState(), testSl) {}

        val contextImpl = StateMachineContext.ContextImpl(stateMachine, testSl)

        assertEquals(contextImpl, stateMachine.context)
    }
}

private class TestServiceLocator : ServiceLocator