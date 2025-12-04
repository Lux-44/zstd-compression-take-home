package com.github.lux44.zstdcompressiontakehome

import actions.CompressOpenFileAction
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CompressOpenFileActionTest : BasePlatformTestCase() {

    fun testActionEnabled() {
        val virtualFile = myFixture.createFile("test.kt", "testString")
        val action = CompressOpenFileAction()
        val event = TestActionEvent.createTestEvent { id ->
            when (id) {
                CommonDataKeys.VIRTUAL_FILE.name -> virtualFile
                else -> null
            }
        }
        action.update(event)
        assertTrue("Action should be enabled for file", event.presentation.isEnabledAndVisible)
    }

    fun testActionDisabledNoFile() {
        val action = CompressOpenFileAction()
        val event = TestActionEvent.createTestEvent { id ->
            when (id) {
                else -> null
            }
        }
        action.update(event)
        assertFalse("Action should be disabled when no file available", event.presentation.isEnabledAndVisible)
    }
}