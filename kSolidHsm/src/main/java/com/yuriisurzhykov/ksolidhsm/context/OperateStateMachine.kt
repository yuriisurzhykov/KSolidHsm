package com.yuriisurzhykov.ksolidhsm.context

import com.yuriisurzhykov.ksolidhsm.states.State
import com.yuriisurzhykov.ksolidhsm.StateMachine

interface OperateStateMachine {

    /**
     *  This function proceeds [StateMachine] to the [nextState] without rising an event.
     *  Usage of this function requires careful consideration if you really want to
     *  move state machine to the indicated state.
     *  The only one reason to do this if you have initial state to do something, and
     *  then you must switch to the next state. In other cases you are prohibited to use this
     *  function.
     * */
    @DelicateStateMachineApi
    suspend fun nextState(nextState: State)
}