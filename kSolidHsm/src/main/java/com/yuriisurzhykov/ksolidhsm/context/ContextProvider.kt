package com.yuriisurzhykov.ksolidhsm.context

import com.yuriisurzhykov.ksolidhsm.StateMachine

/**
 *  Context provider is an interface that every [StateMachine] have to implement.
 *  This provides reference to [StateMachineContext] that by default is [StateMachineContext.ContextImpl]
 * */
interface ContextProvider {
    val context: StateMachineContext
}