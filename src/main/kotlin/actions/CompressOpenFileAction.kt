package actions

// Import for zstd
import com.github.luben.zstd.Zstd
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class CompressOpenFileAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        // Only files can be compressed
        e.presentation.isEnabledAndVisible = virtualFile != null && !virtualFile.isDirectory && virtualFile.isInLocalFileSystem
    }

    override fun actionPerformed(event: AnActionEvent) {
        // If no file is currently open, do nothing
        val currentFile = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val project = event.project ?: return
        // Only compress file if it exists in the local file system
        if (!currentFile.isInLocalFileSystem) {
            Messages.showMessageDialog(
                "No physical file to be compressed!",
                "Compression Error",
                Messages.getErrorIcon()
            )
            return
        }
        project.service<FileCompressionService>().compressFile(currentFile)
    }
}

@Service(Service.Level.PROJECT)
class FileCompressionService(private val project: Project, private val coroutineScope: CoroutineScope) {

    fun compressFile(currentFile: VirtualFile) {
        coroutineScope.launch {
            // Attempt compression
            try {
                withBackgroundProgress(project, "Compressing open file", cancellable = true) {
                    compressFileInternal(currentFile)
                }
            } catch (e: Exception) {
                // Dispatch error message onto EDT
                withContext(Dispatchers.EDT){
                    Messages.showMessageDialog(
                        "Could not compress currently open file!: " + e.message,
                        "Compression Error",
                        Messages.getErrorIcon()
                    )
                }
            }
        }
    }

    internal suspend fun compressFileInternal(currentFile:VirtualFile){
        val file = withContext(Dispatchers.IO) {
            File(currentFile.path)
        }
        val fileBytes =withContext(Dispatchers.IO) {
            file.readBytes()
        }
        // Compression Level 3 as it is default
        val compBytes = Zstd.compress(fileBytes, 3)
        // Write compressed file to same location as input file
        val compFile =  withContext(Dispatchers.IO) {
            File(file.absolutePath + ".zst")
        }
        withContext(Dispatchers.IO) {
            compFile.writeBytes(compBytes)
        }
        // Dispatch onto EDT for UI refresh
        withContext(Dispatchers.EDT){
            // Refresh to make result visible faster
            LocalFileSystem.getInstance().refreshAndFindFileByPath(compFile.absolutePath)
        }
    }
}