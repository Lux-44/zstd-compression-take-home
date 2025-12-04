package com.github.lux44.zstdcompressiontakehome

import actions.FileCompressionService
import com.github.luben.zstd.Zstd
import com.intellij.openapi.components.service
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.io.delete
import com.intellij.util.io.write
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.io.path.pathString
import kotlin.io.path.readBytes

class FileCompressionServiceTest : BasePlatformTestCase() {

    fun testActionProducesFile() {
        val inFile = createTempFile("test", ".kt")
        inFile.write("testString")
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(inFile.pathString)
        val outFile = File(inFile.pathString + ".zst")

        // This method of executing the asynchronous work and waiting for it may be suboptimal
        val service = project.service<FileCompressionService>()
        runBlocking {
            service.compressFileInternal(virtualFile!!)
        }
        LocalFileSystem.getInstance().refresh(false)
        assertTrue("Output file should exist", outFile.exists())
        inFile.delete()
        outFile.delete()
    }

    fun testActionProducesCorrectOutput() {
        val inFile = createTempFile("test", ".kt")
        inFile.write("testString")
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(inFile.pathString)
        val outFile = File(inFile.pathString + ".zst")

        // This method of executing the asynchronous work and waiting for it may be suboptimal
        val service = project.service<FileCompressionService>()
        runBlocking {
            service.compressFileInternal(virtualFile!!)
        }
        LocalFileSystem.getInstance().refresh(false)
        assertTrue("Output file should exist", outFile.exists())
        val decompBytes = Zstd.decompress(outFile.readBytes())
        assertArrayEquals(
            "Compressed and decompressed contents should be the same as original",
            inFile.readBytes(),
            decompBytes
        )
        inFile.delete()
        outFile.delete()
    }
}