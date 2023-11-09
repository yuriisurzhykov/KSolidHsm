package com.yuriisurzhykov.ksolidhsm.example

import com.yuriisurzhykov.ksolidhsm.context.ServiceLocator

interface CheckHasSavedScore {
    suspend fun savedScoreValue(): Int

    class Base : CheckHasSavedScore {
        override suspend fun savedScoreValue(): Int = 0
    }
}

interface ExampleServiceLocator : ServiceLocator {

    fun checkHasSavedScoreUseCase(): CheckHasSavedScore

    class Base : ExampleServiceLocator {
        override fun checkHasSavedScoreUseCase(): CheckHasSavedScore = CheckHasSavedScore.Base()
    }
}