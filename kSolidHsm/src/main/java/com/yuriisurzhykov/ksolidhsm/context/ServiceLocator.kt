package com.yuriisurzhykov.ksolidhsm.context

/**
 *  Service locator is a pattern which describes how to provide dependencies
 *  to every part of application. In short words, it creates instances for the
 *  interfaces and gives them to everyone who is interested in them.
 *  Because of each state machine may use different UseCases, different utils
 *  classes, so this [ServiceLocator] implementations provides correct dependencies
 *  to every part.
 *  [ServiceLocator] is just an empty interface that provides nothing by default.
 *  To use this class, you have to create your own interface and inherit it from
 *  this one.
 *  For example
 *  ```
 *  interface CertainServiceLocator : ServiceLocator {
 *      fun someClassToProvide(): SomeClassToProvide
 *
 *      class Base : CertainServiceLocator {
 *          override fun someClassToProvide(): SomeClassToProvide = SomeClassToProvide.Base()
 *      }
 *  }
 *  ```
 *  After this for your state machine you provide implementation for your general
 *  [ServiceLocator] interface.
 * */
interface ServiceLocator {

    /**
     *  If you don't need service locator for your state machine, you may use this dummy class,
     *  to provide it as a Service Locator. But you have to be aware, if you really don't need
     *  service locator and you don't keep any business logic inside of states.
     * */
    @Suppress("unused")
    class Empty : ServiceLocator
}