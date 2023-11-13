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

For more clear code, for each of ProcessResult strategies it created 3 corresponding functions: `transitionTo()`, `unknown()` and `ignore()`.This functions would help you to write more readable and clear code.

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
## Creation of HSM
To create an HSM, you have to declare an HSM class, a `ServiceLocator`, and each state for your state machine. The following steps describe how to code a state machine to implement an HSM diagram.

## Coding the states
As mentioned above, on diagram a state might be ordinary(`State.Normal` in code), transient that doesn't consume any events and just switches to new state immediately, and hierarchical that has parent and may talk to parent to process event. In addition, you may have a situation where a state might be a parent and also it might have  initial transition to child state. So, let's take a look on how to code all of these situations:

### Ordinary states without parent
To create ordinary state without hierarchy(without parent) you have to:

- Create state class that corresponds your HSM state. Your class has to be a Kotlin object so that your class declaration be object StateName;
- Inherit this class from State.Normal() class
- Pass null to Normal’s constructor for parent property
- Then Android Studio will require you to override the processEvent function.
- In processEvent body write your condition for required event for your state and return result:
  - If you handled event and want to move to a new state return transitionTo(NewStateName)
  - If you handled event, but want to do nothing, then return handled()
  - If your state don’t know about event came to it, then just return unhandled(event)

Here is an example for default state that processes only one event AlarmEvents.ProgrammingModeEnd and then returns transition to VirtualDisarmed state on this event:
```kotlin
class Inactive : State.Normal(null) {

    override suspend fun processEvent(event: Event, context: StateMachineContext): State {
        return when {
            event is AlarmEvents.ProgrammingModeEnd && hasZones(context) -> transitionTo(VirtualDisarmed)
            else -> unhandled(event)
        }
    }

    private suspend fun hasZones(context: StateMachineContext): Boolean {
        return serviceLocator<AlarmServiceLocator>(context)
            .checkHasZonesUseCase()
            .hasZones()
    }
}
```
### Hierarchical states(with parent)
If your state should have parent, you have to follow steps:
- Create class for parent and define it as a normal state
- Create processEvent logic for parent state
- Create new class for your state, that you intended to create and inherit your class also from State.Normal()
- Pass an instance of parent class, that you just created, as a constructor parameter to `State.Normal(ParentState)` (don’t use parentheses because class should be declared as an object)
- Implement your processEvent logic and, if it required, override `onEnter`/`onExit` functions.
- When you create logic to process all of events that current state needs to handle, then in else branch add `unhandled(event)`
```kotlin
object YourParentState : State.Normal(null) {

    override fun processEvent(event: Event): State {
        return transitionTo(YourNextState)
    }
}

object ChildStateOne : State.Normal(YourParentState) {
    override fun processEvent(event: Event): State {
        return when(event) {
            is OtherEventType -> transitionTo(OtherNextState)
            else -> unhandled(event)
        }
    }
}
```
### Parent state that has initial transition
If your parent has initial transition, when you declare your parent state, pass null for parent, override `initialTransitionState()` method and return the state your want to move to. The `State.Normal` will trigger initial transition to the provided state right after execution of `onEnter()`, if you return any State other then null.
```kotlin
object StateAfterInitialTransition : State.Normal(null) {
    override suspend fun processEvent(event: Event, context: StateMachineContext): State {
        return YourSomeNewState
    }
}

object ParentWithTransition : State.Normal(null) {
    
    override suspend fun initialTransitionState() = StateAfterInitialTransition
    
    override suspend fun processEvent(event: Event, context: StateMachineContext): State {
        return YourAdditionalState
    }
}
```
### Transient state
As mentioned, a Transient state is a state that has no events to process, it simply switches to a new state when it itself is applied to the state machine.
To create transient state you have to:
- First of all define non-transient state to move to, using the descriptions above
- After create class for transient state
- After you declared class inherit your class from State.Transient()
- Override `initialTransitionState()` method and provide state to move to
```kotlin
object TransientReadyCheck : State.Transient() {

    override suspend fun initialTransitionState(context: StateMachineContext): State {
        return if (checkZonesReady(context)) Ready else NotReady
    }

    private suspend fun checkZonesReady(context: StateMachineContext): Boolean {
        return serviceLocator<AlarmServiceLocator>(context)
            .checkZonesReadyUseCase()
            .allZonesReady()
    }
}
```
## Coding the ServiceLocator
As you may noticed, in last example to run condition check checkZonesReady we call serviceLocator() method and then call available functions from it.

Now simple example:
![image](https://github.com/yuriysurzhikov/KSolidHsm/assets/44873047/f122c56e-34aa-48c7-8808-a3466dbb1fe0)

On the diagram above we have red diamond choice block, and depends on we have zones or not, we go to one or to another state. We may write this logic  inside of the state to check is we have zones, but we don’t want to write code that will have duplicates or which is not testable. That is why we have to create a class, that does this check and gives us just a boolean result of checking. This means we have to create UseCase for this checking and declare function inside of it.

```kotlin
interface CheckHasZonesUseCase {
  
  suspend fun hasZones(): Boolean
  
  class Base: CheckHasZonesUseCase {
    override suspend fun hasZones(): Boolean {
      // do your check here
    }
  }
}
```
After you created use case interface that performs check, you have to provide your implementation of use case in your service locator. If service locator have not defined yet, you have to create service locator for your state machine:
- Create interface with the following name pattern: <state machine name>ServiceLocator
- Inherit your interface from the ServiceLocator interface
- Then when you have service locator for your certain state machine, declare function to provide your use case inside of your ServiceLocator interface
- Then if you don’t have implementation for your service locator interface, create class Base inside of interface
- After this provide instances for the classes that declared in interface.
- The final interface of service locator may looks the following way: 
```kotlin
// Your use case interface
interface CheckHasZonesUseCase {
    suspend fun hasZones(): Boolean

    // Some implementation for use case interface
    class Base : CheckHasZonesUseCase {
        override suspend fun hasZones(): Boolean {
            TODO("Not yet implemented")
        }
    }
}

// Your service locator interface
interface ExampleServiceLocator : ServiceLocator {

    // Function for getting use case
    fun checkHasZonesUseCase(): CheckHasZonesUseCase

    // Default implementation for service locator
    class Base : ExampleServiceLocator {
        override fun checkHasZonesUseCase(): CheckHasZonesUseCase = CheckHasZonesUseCase.Base()
    }
}
```
***Note***: If you 100% sure, that you don’t need service locator, because you don’t have conditions on your diagram, then you may use ServiceLocator.Empty class as a dummy object to provide to StateMachine.Abstract() constructor, just to satisfy the compiler requirements. In other cases you must create service locator to make state machine as more testable as possible

## Coding the StateMachine
Now, when you have everything read for state machine, i.e. you defined states, you defined service locator, after this you may define state machine class.

Your state machine have to inherit StateMachine.Abstract() class and provide required resources to the Abstract’s constructor.


Example of state machine class
```kotlin
class ExampleStateMachine(
    initialState: State = YourInitialState()
) : StateMachine.Abstract(initialState, ExampleServiceLocator.Base())
```
# Example
Now the simple example of how to create state machine

For example we have the next state machine diagram:
![image](https://github.com/yuriysurzhikov/KSolidHsm/assets/44873047/a6578db2-bb30-4c3e-a1a0-f1b25976c85f)


The logic of this diagram the following. The initial state of this state machine is App state which is transient state. When the state machine is initializing, it goes to Menu state. When the Menu state receives Play event the state machine moves to Play state, which is transient by itself and during the initializing it jump to Ping state. The Ping state listens for Pong event and once it occurred the state machine moves to Pong state. In addition, Ping and Pong states are derived states from Play state, that is Play is also hierarchical state that listens and reacts on Menu event and goes to Menu state when Menu event is occurred. 

So lets code it. 

Create events
Let’s firstly create events that we want to handle. If you don’t know how to create events, read the documentation
```kotlin
sealed interface PingPongEvents : Event {
    data object Play : ExampleEvents
    data object Menu : ExampleEvents
    data object Ping : ExampleEvents
    data object Pong : ExampleEvents
}
```
Create states
After we defined events, we have to create states according to the diagram. So, the hierarchy of state will be next:
```
App: State.Normal(null)
  |___Menu: State.Normal(App)
  |___Play: State.Normal(App)
      |___Ping: State.Normal(Play)
      |___Pong: State.Normal(Play)
```
And also we have the next transitions: 
```
App → Menu
Menu → Play
Play → Ping
Ping → Pong
Pong → Ping
Play → Menu
```

Now let’s create classes for the states.

Coded states
```kotlin
// Initial state that is transient
object App : ExampleStates(null) {

    // On the diagram we have transition to Menu once the App is going 
    // to be a current state
    override suspend fun initialTransitionState(context: StateMachineContext) = Menu

    override suspend fun processEvent(
        event: Event,
        context: StateMachineContext
    ): ProcessResult = ignore()
}

object Menu : ExampleStates({ App }) {

    // When receive Play event then go to Play state
    override suspend fun processEvent(
        event: Event,
        context: StateMachineContext
    ): ProcessResult =
        if (event is ExampleEvents.Play) transitionTo(Play)
        else unknown(event)
}

// Play is parent transitive state that is also a child of App, 
// so we pass App to be a parent
class Play : ExampleStates(App) {

    override suspend fun initialTransitionState(context: StateMachineContext) = Ping

    // Based on diagram it doesn't metter in what state we are, when the
    // Menu event is occurred we have to go to Menu state
    override suspend fun processEvent(event: Event, context: StateMachineContext): ProcessResult =
        if (event is ExampleEvents.Menu) transitionTo(Menu)
        else unknown(event)

}

// Ping has Play as a parent so we pass this state
class Ping : ExampleStates({ Play }) {
    override suspend fun processEvent(
        event: Event,
        context: StateMachineContext
    ): ProcessResult =
        if (event is ExampleEvents.Pong) transitionTo(Pong)
        else unknown(event)
}

// Pong is also a child of Play
class Pong : ExampleStates({ Play }) {
    override suspend fun processEvent(
        event: Event,
        context: StateMachineContext
    ): ProcessResult =
        if (event is ExampleEvents.Ping) transitionTo(Ping)
        else unknown(event)
}
```
## Create service locator
For any check conditions inside of your state machine its preferred to create Service Locator and provide use cases for every check in your HSM. This will help to easy test state machine transitions under various conditions. For current state machine we don’t have any condition blocks, so we are not required to create service locator and we may use `ServiceLocator.Empty()` instance.

## Create state machine
So we created states, events and we don’t need service locator according we don’t have conditions on it. Now, we are able to create state machine class. For out example we will create PingPongStateMachine:
```kotlin
class PingPongStateMachine(
    initialState: State = App
) : StateMachine.Abstract(initialState, ServiceLocator.Empty())
```
