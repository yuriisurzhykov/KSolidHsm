package com.yuriisurzhykov.ksolidhsm

import kotlinx.serialization.Polymorphic

/**
 *  This class doesn't have any functions. It works just as an indicator to mark classes as events.
 *  It also annotated with [Polymorphic] annotation to make all further events serializable.
 *  In addition there is [Sticky] interface that is also only indicator interface without functions.
 *  The process of creating event may be the following:
 *  - You have to create sealed interface for your group of events inside _core/events_ application
 *  folder.
 *  - Then you also have to annotate it with [Polymorphic] annotation.
 *  - Create inner classes (data class/data object) for your certain event(-s).
 *  ```
 *  @Polymorphic
 *  sealed interface GroupOfEvents {
 *      @Serializable
 *      data object CertainEvent: GroupOfEvents
 *
 *      @Serializable
 *      data object CertainStickyEvent: GroupOfEvents, Event.Sticky
 *  }
 *  ```
 * */
@Polymorphic
interface Event {

    interface Sticky : Event
}