package com.yuriisurzhykov.ksolidhsm.states

import com.yuriisurzhykov.ksolidhsm.Event
import com.yuriisurzhykov.ksolidhsm.StateMachine
import com.yuriisurzhykov.ksolidhsm.states.State.Initial
import com.yuriisurzhykov.ksolidhsm.states.State.Normal
import com.yuriisurzhykov.ksolidhsm.states.State.Transient
import com.yuriisurzhykov.ksolidhsm.context.DelicateStateMachineApi
import com.yuriisurzhykov.ksolidhsm.context.OperateStateMachine
import com.yuriisurzhykov.ksolidhsm.context.ServiceLocator
import com.yuriisurzhykov.ksolidhsm.context.StateMachineContext
import com.yuriisurzhykov.ksolidhsm.strategy.EventProcessResult

/**
 *  A `State` represents a distinct condition or configuration within a state machine, reflecting
 *  a specific circumstance that an entity, governed by the state machine, might be in.
 *
 *  In a state machine, a `State` encapsulates specific behaviors and transitions. Each `State`
 *  can define actions that occur when entering or exiting the state, as well as behaviors and
 *  transitions to other states based on events. States can be connected through transitions,
 *  forming a graph that represents the entire lifecycle of an entity.
 *
 *  In a Hierarchical State Machine (HSM), states can have a parent-child relationship, forming a
 *  hierarchy of states. A `State` in HSM can be a composite state containing sub-states, or
 *  a simple state with no sub-states. This hierarchy allows for more organized and modular design,
 *  where common behaviors can be grouped and managed in parent states, while specific behaviors
 *  are handled in child states.
 *
 *  The `State` interface in this package provides a blueprint for implementing states in a state
 *  machine, defining lifecycle methods such as [onEnter] and [onExit], and a method for processing
 *  events to handle transitions.
 *
 *  Nested within are also special types of states like [Initial] for designating a starting point
 *  in the state machine, [Normal] for providing a basic implementation, and [Transient] for
 *  handling initial transitions.
 *
 *  __Usage__:
 *  Implementing classes may define the behavior on entering and exiting the state(if they inherits
 *  from State interface), and how events are processed to determine transitions to other
 *  states.
 */
interface State {

    /**
     *  Does nothing. May be used just as an indicator or marker for initial state for [StateMachine]
     * */
    interface Initial


    /**
     *  This function called once this state has been applied as a current state for [StateMachine].
     *  This may happen when [StateMachine] initialize itself or when it applies new [State] when
     *  processing an [Event]. During the call of this function you may be able to influence to
     *  the [StateMachine] because of you have access to [OperateStateMachine] from context.
     *
     *  @param context The context of current state machine to which state is being applied.
     * */
    suspend fun onEnter(context: StateMachineContext)


    /**
     *  This function is called once the current state is going to be replaced
     *  with another [State]. Firstly [StateMachine] calls [onExit] for current
     *  state and after [onEnter] for new state.
     *
     *  @param context The context of current [StateMachine].
     * */
    suspend fun onExit(context: StateMachineContext)

    /**
     *  Everyone who wants to handle event from state machine has to override this function.
     *  @param event This is an event that [StateMachine] consumed for processing.
     *  @param context [StateMachineContext] that gives you access to state machine where the event
     *  is processing now.
     *  @return [State] to which the [StateMachine] should change its current one. If you want just
     *  to process event and do not change state of state machine just return `this` and state
     *  will not be changed.
     * */
    suspend fun processEvent(event: Event, context: StateMachineContext): EventProcessResult


    /**
     *  Default implementation of [State] provides empty [onEnter] and [onExit] functions.
     *  In addition to these functions it also provides convenience way for accessing specific
     *  implementation of [ServiceLocator]
     *  @property parent The parent state in hierarchy that has additional logic for handling events
     * */
    abstract class Normal : State, InitialTransitionState {

        private val parentRef: Lazy<State?>

        constructor(parent: State?) {
            parentRef = lazy { parent }
        }

        constructor(parentInit: () -> State?) {
            parentRef = lazy { parentInit.invoke() }
        }

        /**
         *  Abstract implementation for [onEnter] is to do nothing, just to not force user to
         *  override this function in every case but only if user have to do something on entering
         *  state.
         * */
        override suspend fun onEnter(context: StateMachineContext) {
        }

        /**
         *  Abstract implementation of [onExit] is just empty function with no logic inside of it.
         *  This helps to get at least control above who will implement this function, because of
         *  Kotlin compiler will not break compilation process because of `function not overridden`
         *  because the function [onExit] is already implemented.
         * */
        override suspend fun onExit(context: StateMachineContext) {}

        /**
         * Returns null by default to not force to override this method, but provides ability to
         * define initial transition for end-user.
         * */
        override suspend fun initialTransitionState(context: StateMachineContext): State? = null

        /**
         *  Provides reference to parent. If [parent] property is null, then it will create empty
         *  (`dummy`) object that provides empty implementation for [processEvent]
         * */
        internal fun parent(): State {
            val parent = parentRef.value
            return parent ?: EmptyState()
        }

        internal fun hasParent() = parentRef.value != null

        /**
         *  Provides a typed [ServiceLocator] instance from the [context].
         *
         *  @param context The context of the current state machine.
         *  @return A typed [ServiceLocator] instance.
         *  @throws ClassCastException if the service locator is not of type [T].
         */
        @Suppress("UNCHECKED_CAST")
        protected fun <T : ServiceLocator> serviceLocator(context: StateMachineContext): T {
            return context.serviceLocator() as T
        }

        /*
        Overrides equals and hashCode for the purpose to compare any of State class using '=='
        operator
        */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }

    /**
     *  A Transient is a state that initiates a transition to another state upon entering.
     *  It extends [Normal] to inherit its behavior and overrides [onEnter] to potentially
     *  initiate a transition using [initialTransitionState]. In addition, it overrides [processEvent]
     *  with `final` modifier so children are not allowed to override them and place the process
     *  event logic.
     */
    abstract class Transient : State {

        /**
         *  Overrides the [processEvent] and [onEnter] function in order to prevent unnecessary
         *  overriding in child classes.
         * */
        final override suspend fun processEvent(
            event: Event,
            context: StateMachineContext
        ): EventProcessResult = EventProcessResult.Ignore

        /**
         *  Defines the transition during initialization to be made from this state, if any.
         *
         *  @param context The context of the current state machine.
         *  @return The [State] to transition to.
         */
        protected abstract suspend fun initialTransitionState(context: StateMachineContext): State

        @OptIn(DelicateStateMachineApi::class)
        final override suspend fun onEnter(context: StateMachineContext) {
            context.operateStateMachine().nextState(initialTransitionState(context))
        }

        override suspend fun onExit(context: StateMachineContext) {
        }
    }
}