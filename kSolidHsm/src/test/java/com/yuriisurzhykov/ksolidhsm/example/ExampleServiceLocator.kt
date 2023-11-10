package com.yuriisurzhykov.ksolidhsm.example

import com.yuriisurzhykov.ksolidhsm.context.ServiceLocator

interface CheckHasSavedScoreUseCase {
    suspend fun savedScoreValue(): Int

    class Base : CheckHasSavedScoreUseCase {
        override suspend fun savedScoreValue(): Int = 0
    }
}

interface ExampleServiceLocator : ServiceLocator {

    fun checkHasSavedScoreUseCase(): CheckHasSavedScoreUseCase

    class Base : ExampleServiceLocator {
        override fun checkHasSavedScoreUseCase(): CheckHasSavedScoreUseCase =
            CheckHasSavedScoreUseCase.Base()
    }
}
