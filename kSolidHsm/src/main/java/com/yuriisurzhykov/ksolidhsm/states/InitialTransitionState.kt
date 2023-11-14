package com.yuriisurzhykov.ksolidhsm.states

import com.yuriisurzhykov.ksolidhsm.context.StateMachineContext


interface InitialTransitionState {


    /**
     *  Defines the transition during initialization to be made from this state, if any.
     *
     *  @param context The context of the current state machine.
     *  @return The [State] to transition to, or null if no need to do initial transition
     */
    suspend fun initialTransitionState(context: StateMachineContext): State?
}