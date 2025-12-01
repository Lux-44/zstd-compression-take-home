package com.github.lux44.zstdcompressiontakehome

import actions.CompressOpenFileAction
import com.github.luben.zstd.Zstd
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.File
import org.junit.Assert.assertArrayEquals

class CompressOpenFileActionTest: BasePlatformTestCase() {

    fun testCompressionCreatesOutput(){
        val virtFile= myFixture.createFile("test.kt","testString")
        val outFile= File("test.kt.zst")
        outFile.writeBytes(Zstd.compress(virtFile.contentsToByteArray()))
        assertTrue("Output file exists",outFile.exists())
    }

    fun testCompressionCreatesNonEmptyOutput(){
        val virtFile= myFixture.createFile("test.kt","testString")
        val outFile= File("test.kt.zst")
        outFile.writeBytes(Zstd.compress(virtFile.contentsToByteArray()))
        assertTrue("Output file not Empty",outFile.length()>0)
    }

    fun testCompressionDecompression(){
        val virtFile= myFixture.createFile("test.kt","testString")
        val outFile= File("test.kt.zst")
        val inBytes=virtFile.contentsToByteArray()
        outFile.writeBytes(Zstd.compress(inBytes))
        val decompBytes=Zstd.decompress(outFile.readBytes())
        assertArrayEquals(inBytes, decompBytes)
    }


    fun testActionEnabled(){
        val virtFile= myFixture.createFile("test.kt","testString")
        val action = CompressOpenFileAction()
        val event = TestActionEvent.createTestEvent{id->
            when(id){
                CommonDataKeys.VIRTUAL_FILE.name -> virtFile
                else -> null
            }
        }
        action.update(event)
        assertTrue("Action enabled",event.presentation.isEnabledAndVisible)
    }

    fun testActionDisabledNoFile(){
        val action = CompressOpenFileAction()
        val event = TestActionEvent.createTestEvent{id->
            when(id){
                else -> null
            }
        }
        action.update(event)
        assertFalse("Action should be disabled when no file available",event.presentation.isEnabledAndVisible)
    }

}