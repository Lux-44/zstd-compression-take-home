package com.github.lux44.zstdcompressiontakehome

import actions.CompressOpenFileAction
import com.github.luben.zstd.Zstd
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.io.delete
import com.intellij.util.io.write
import org.junit.Assert.assertArrayEquals
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.io.path.pathString
import kotlin.io.path.readBytes

class CompressOpenFileActionTest : BasePlatformTestCase() {

    fun testCompressionCreatesNonEmptyOutput() {
        val virtualFile = myFixture.createFile("test.kt", "testString")
        val outFile = File("test.kt.zst")
        outFile.writeBytes(Zstd.compress(virtualFile.contentsToByteArray()))
        assertTrue("Output file should exist", outFile.exists())
        assertTrue("Output file should be not empty", outFile.length() > 0)
        outFile.delete()
    }

    fun testCompressionDecompression() {
        val virtualFile = myFixture.createFile("test.kt", "testString")
        val outFile = File("test.kt.zst")
        val inBytes = virtualFile.contentsToByteArray()
        outFile.writeBytes(Zstd.compress(inBytes))
        val decompBytes = Zstd.decompress(outFile.readBytes())
        outFile.delete()
        assertArrayEquals("Compressed and decompressed contents should be the same as original", inBytes, decompBytes)
    }

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

    fun testActionProducesFile() {
        val inFile = createTempFile("test",".kt")
        inFile.write("testString")
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(inFile.pathString)
        val outFile =File(inFile.pathString +".zst")
        val action = CompressOpenFileAction()
        val event = TestActionEvent.createTestEvent { id ->
            when (id) {
                CommonDataKeys.VIRTUAL_FILE.name -> virtualFile
                else -> null
            }
        }
        action.actionPerformed(event)
        LocalFileSystem.getInstance().refresh(false)
        assertTrue("Output file should exist", outFile.exists())
        inFile.delete()
        outFile.delete()
    }

    fun testActionProducesCorrectOutput() {
        val inFile = createTempFile("test",".kt")
        inFile.write("testString")
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(inFile.pathString)
        val outFile =File(inFile.pathString +".zst")
        val action = CompressOpenFileAction()
        val event = TestActionEvent.createTestEvent { id ->
            when (id) {
                CommonDataKeys.VIRTUAL_FILE.name -> virtualFile
                else -> null
            }
        }
        action.actionPerformed(event)
        LocalFileSystem.getInstance().refresh(false)
        assertTrue("Output file should exist", outFile.exists())
        val decompBytes = Zstd.decompress(outFile.readBytes())
        assertArrayEquals("Compressed and decompressed contents should be the same as original", inFile.readBytes(), decompBytes)
        inFile.delete()
        outFile.delete()
    }
}