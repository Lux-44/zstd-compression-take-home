package actions

// Import for zstd
import com.github.luben.zstd.Zstd
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File


class CompressOpenFileAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        val virtFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        // Only files can be compressed
        e.presentation.isEnabledAndVisible = virtFile != null && !virtFile.isDirectory
    }

    override fun actionPerformed(event: AnActionEvent) {
        // If no file is currently open, do nothing
        val currentFile = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        compressFile(currentFile)
    }

    private fun compressFile(currentFile: VirtualFile) {
        try {
            val file = File(currentFile.path)
            val fileBytes = file.readBytes()
            // Compression Level 3 as it is default
            val compBytes = Zstd.compress(fileBytes, 3)
            // Write compressed file to same location as input file
            val compFile = File(file.absolutePath + ".zst")
            compFile.writeBytes(compBytes)
            // Refresh to make result visible faster
            ApplicationManager.getApplication().invokeLater {
                LocalFileSystem.getInstance().refreshAndFindFileByPath(compFile.absolutePath)
            }
        } catch (e: Exception) {
            Messages.showMessageDialog(
                "Could not compress currently open file!: " + e.message,
                "Compression Error",
                Messages.getErrorIcon()
            )
        }
    }
}