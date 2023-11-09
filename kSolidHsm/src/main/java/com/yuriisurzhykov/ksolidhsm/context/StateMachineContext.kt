package com.yuriisurzhykov.ksolidhsm.context

import com.yuriisurzhykov.ksolidhsm.StateMachine

/**
 *  In the terms of [StateMachine] context means environment where states should
 *  be and where it will be applied. In other words context is mostly reference
 *  to current state machine instance. In addition to this it holds reference to
 *  [ServiceLocator] instance which holds dependencies for current state machine.
 * */
interface StateMachineContext {

    /**
     *  Provides interface to operate the current [StateMachine]. Operate means to talk directly to
     *  [StateMachine] to move it to new state without sending an event. That is why this function
     *  is marked with [DelicateStateMachineApi] annotation to make you think whether you really
     *  need to change state without sending event.
     *  @return [OperateStateMachine] instance that represents current state machine. Usage of this
     *  function requires careful review and you must be aware of why you want to use it.
     * */
    fun operateStateMachine(): OperateStateMachine

    /**
     *  Provides the reference to current state machine in which state is running.
     *  @return [StateMachine] instance
     * */
    fun currentStateMachine(): StateMachine

    /**
     *  @return [ServiceLocator] instance that [StateMachine] keeps as reference. This will give
     *  you ability to get any of required dependencies declared for your specific [StateMachine]
     * */
    fun serviceLocator(): ServiceLocator

    /**
     *  Default implementation for context. [StateMachine.Abstract] provides this implementation by
     *  default.
     * */
    class ContextImpl(
        private val stateMachine: StateMachine,
        private val sl: ServiceLocator
    ) : StateMachineContext {
        @DelicateStateMachineApi
        override fun operateStateMachine(): OperateStateMachine = stateMachine
        override fun currentStateMachine(): StateMachine = stateMachine
        override fun serviceLocator(): ServiceLocator = sl
    }
}