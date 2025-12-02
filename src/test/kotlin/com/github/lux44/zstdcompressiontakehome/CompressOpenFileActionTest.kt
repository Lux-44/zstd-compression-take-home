package com.github.lux44.zstdcompressiontakehome

import actions.CompressOpenFileAction
import com.github.luben.zstd.Zstd
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert.assertArrayEquals
import java.io.File

class CompressOpenFileActionTest : BasePlatformTestCase() {

    fun testCompressionCreatesNonEmptyOutput() {
        val virtFile = myFixture.createFile("test.kt", "testString")
        val outFile = File("test.kt.zst")
        outFile.writeBytes(Zstd.compress(virtFile.contentsToByteArray()))
        assertTrue("Output file should exist", outFile.exists())
        assertTrue("Output file should be not empty", outFile.length() > 0)
        outFile.delete()
    }

    fun testCompressionDecompression() {
        val virtFile = myFixture.createFile("test.kt", "testString")
        val outFile = File("test.kt.zst")
        val inBytes = virtFile.contentsToByteArray()
        outFile.writeBytes(Zstd.compress(inBytes))
        val decompBytes = Zstd.decompress(outFile.readBytes())
        outFile.delete()
        assertArrayEquals("Compressed and decompressed contents should be the same as original", inBytes, decompBytes)
    }

    fun testActionEnabled() {
        val virtFile = myFixture.createFile("test.kt", "testString")
        val action = CompressOpenFileAction()
        val event = TestActionEvent.createTestEvent { id ->
            when (id) {
                CommonDataKeys.VIRTUAL_FILE.name -> virtFile
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