package com.yuriisurzhykov.ksolidhsm

import com.yuriisurzhykov.ksolidhsm.example.ExampleStates
import kotlinx.coroutines.runBlocking

abstract class AbstractTest {
    protected fun runTest(block: suspend () -> ExampleStates): Unit = runBlocking {
        block.invoke().also {
            it.enterCallCount = 0
            it.exitCallCount = 0
        }
    }
}