package com.github.lux44.zstdcompressiontakehome

import com.github.luben.zstd.Zstd
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert.assertArrayEquals
import java.io.File

class CompressionTest : BasePlatformTestCase() {

    fun testCompressionCreatesNonEmptyOutput() {
        val virtualFile = myFixture.createFile("test.kt", "testString")
        val outFile = File("test.kt.zst")
        outFile.writeBytes(Zstd.compress(virtualFile.contentsToByteArray()))
        assertTrue("Output file should exist", outFile.exists())
        assertTrue("Output file should be not empty", outFile.length() > 0)
        outFile.delete()
    }

    fun testCompressionCreatesOutputOnEmptyInput() {
        val virtualFile = myFixture.createFile("test.kt","")
        val outFile = File("test.kt.zst")
        outFile.writeBytes(Zstd.compress(virtualFile.contentsToByteArray()))
        assertTrue("Output file should exist", outFile.exists())
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
}