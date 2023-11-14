package com.yuriisurzhykov.ksolidhsm.states

import com.yuriisurzhykov.ksolidhsm.StateMachine
import com.yuriisurzhykov.ksolidhsm.context.StateMachineContext

/**
 *  Interface [InitialTransitionState] provides declaration of what states with initial transitions
 *  can do. Now its only one method [initialTransitionState], that called by [StateMachine] after
 *  [State.onEnter].
 * */
interface InitialTransitionState {


    /**
     *  Defines the transition during initialization to be made from this state, if any.
     *
     *  @param context The context of the current state machine.
     *  @return The [State] to transition to, or null if no need to do initial transition
     */
    suspend fun initialTransitionState(context: StateMachineContext): State?
}