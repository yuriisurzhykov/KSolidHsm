# KSolidHsm
### Description
This library represents HSM(hierarchical state machine) written in kotlin with usage of suspend function as main blocking way for execution.
Instead of using declarative approach, it provides OOP(object oriented programming) approach to create and build the HSM.

# HSM library components

In order to code an HSM according to the diagram, there is a 'stateMachine' library inside of the project. The main components of a state machine are the following interfaces and their basic implementation: `State`, `Event`, `StateMachine`, `StateMachineContext`, `ServiceLocator`.

## State

The State interface declares abstract `onEnter()` , `onExit()` and `processEvent()` functions that can be overridden by any state. There are 2 abstract implementations of State interface: `Normal` and `Transient`, which you can find inside of State interface. You have to inherit any state you want to create from one of these.

### Normal

**Normal** class implements the `State` interface. This abstract class contains an empty implementation for onExit() and a transition implementation for onEnter() which executes depends on constructor parameters. `State.Normal` has 2 constructor parameters: 

`private val parent` – Used to build a hierarchy of states. When your state doesn’t know how to process an event, if it has a parent, you are able to call state machine will call processEvent() on the parent, and then the responsibility of processing the event passes to the parent

`private val initialTransition` – the child state to transition to when the state is entered. When a state that has an initialTransition is entered, onEnter is called first, then transitionTo(initialTransition) is called, then onExit is called.

Every state must override the processEvent() function and provide the result of processing an event. That result of processing an event is a sealed interface[1] ProcessResult that has 3 implementations: 

**TransitonTo** – the implementation of this strategy is to make transition to the new state. A state to go to must be provided in the constructor of TransitionTo.

**Unknown** – the implementation of this class is to find parent of class(if it has) and try to get result from parent’s processEvent(). 

**Ignored** – the implementation for this class is to do nothing. This class is just to say to State Machine that we handled an event and the state machine should do nothing for this event

For more clear code, for each of ProcessResult strategies it created 3 corresponding functions: transitionTo(), unhandled() and handled().This functions would help you to write more readable and clear code.

### Transient

**Transient** class is inherited by transient states that don’t consume any events. A transient state's only job is to check conditions, then move immediately to another state during `onEnter()`. The Transient class redefines the onEnter logic to call an abstract `initialTransitionState()` function that every Transient state must provide a definition for.

## Event

TheEvent interface is just a marker that defines a class as an event.

## StateMachine

StateMachine is an interface that contains an declaration of what state machine can do. The StateMachine interface allows you to build an HSM that receives events as input and makes transitions from one state to another based on those events.

StateMachine interface has abstract(default) implementation inside of Abstract class for methods declared in interface. Default implementation of state machine calls onEnter() and onExit() functions for states that come in/out to/from, and also performs checks for initial transitions of State.

## StateMachineContext

StateMachineContext is an interface that provides you ability to get access to current state machine in which the current state is running. StateMachineContext has 3 main function to get access to current ServiceLocator(DI holder for use cases, timers, and additional utility tools), current StateMachine, and current OperateStateMachine. 

OperateStateMachine marked with DelicateStateMachineApi which means it will break compilation if someone try to use it without acknowledge of use. This function marked with DelicateStateMachineApi because by default you don’t must to switch from one state to another without sending an event, because it may break the logic of whole system.

```kotlin
interface StateMachineContext {
    @DelicateStateMachineApi
    fun operateStateMachine(): OperateStateMachine

    fun currentStateMachine(): StateMachine

    fun serviceLocator(): ServiceLocator

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
```

## ServiceLocator

Service Locator – in simple words it is a pattern that defines how to provide instances of classes that are used in the system. In our case ServiceLocator is an interface, where we declare dependencies which may be used for conditions or other logic inside of state’s onEnter(), onExit() or processEvent() functions. In general, your service locator may have use cases to check conditions, or it may provide access to timers, that might be used within your HSM. So that using this ServiceLocator you can create testable state machine, because every condition and every logic of state machine will be well-testable. 

## State Machine library diagram

For better understanding of what is the structure of stateMachine library you can see the diagram that represents classes and interfaces that stateMachine library has.

<img src="https://github.com/yuriysurzhikov/KSolidHsm/assets/44873047/fe055e1d-2aaa-4a89-927f-0c350392f195" width="300" height="240" alt="State Machine library class diagram">

Also, for better understanding of how new states applied to StateMachine below diagram describes how does processing event works in StateMachine and how new states are applied to StateMachine.

<img src="https://github.com/yuriysurzhikov/KSolidHsm/assets/44873047/78389796-823a-47d2-afa3-8c0aee5f19f3" width="300" height="200" alt="Processing event with further transition">

More detaild diagram with description of how initial transition is made:

<img src="https://github.com/yuriysurzhikov/KSolidHsm/assets/44873047/18c139e3-94e0-48ea-91b0-75ae9200ba67" width="300" height="270" alt="Processing event with further transition">

# Coding documentation
Coming soon
