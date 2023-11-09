package com.yuriisurzhykov.ksolidhsm.example

import com.yuriisurzhykov.ksolidhsm.Event

sealed interface ExampleEvents : Event {
    data object Play : ExampleEvents
    data object Menu : ExampleEvents
    data object Ping : ExampleEvents
    data object Pong : ExampleEvents
}