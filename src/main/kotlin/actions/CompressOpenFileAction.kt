package actions


// Import for zstd
import com.github.luben.zstd.Zstd
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent


class CompressOpenFileAction : AnAction(){
    override fun actionPerformed(event: AnActionEvent) {
        try{
            compressFile()
        }catch (e: Exception){
        }
    }
    private fun compressFile(){

    }
}