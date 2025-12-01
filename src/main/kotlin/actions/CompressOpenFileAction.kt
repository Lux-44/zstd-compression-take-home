package actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.ui.Messages
import java.io.File
// Import for zstd
import com.github.luben.zstd.Zstd


class CompressOpenFileAction : AnAction(){
    override fun actionPerformed(event: AnActionEvent) {
        val currentFile = event.getData(CommonDataKeys.VIRTUAL_FILE)?:return
        compressFile(currentFile)
    }
    private fun compressFile(currentFile: VirtualFile){
        try {
            val file = File(currentFile.path)
            val fileBytes = file.readBytes()
            // Compression Level 3 as it is default
            val compBytes = Zstd.compress(fileBytes,3)
            val compFile=File(file.absolutePath+".zst")
            compFile.writeBytes(compBytes)
        }catch (e: Exception){
            Messages.showMessageDialog("Could not compress currently open file!","Compression Error",Messages.getErrorIcon())
        }
    }
}