package com.yuriisurzhykov.ksolidhsm.example

import com.yuriisurzhykov.ksolidhsm.State
import com.yuriisurzhykov.ksolidhsm.StateMachine

/**
 * [State machine diagram](diagram.png)
 * */
class ExampleStateMachine(
    initialState: State = App()
) : StateMachine.Abstract(initialState, ExampleServiceLocator.Base())