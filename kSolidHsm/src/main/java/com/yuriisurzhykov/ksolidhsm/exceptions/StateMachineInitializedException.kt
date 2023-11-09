package com.yuriisurzhykov.ksolidhsm.exceptions

class StateMachineInitializedException(smName: String) :
    IllegalStateException("State Machine $smName is already initialized!")