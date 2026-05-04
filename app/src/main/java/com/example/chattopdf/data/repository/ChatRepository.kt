import android.content.Context
import android.net.Uri
import com.example.chattopdf.data.local.dao.ChatDao
import com.example.chattopdf.data.local.entity.ChatMessage
import com.example.chattopdf.data.local.entity.ChatSession
import kotlinx.coroutines.flow.Flow
import java.io.File
import kotlin.collections.map

class ChatRepository(
    private val chatDao: ChatDao,
    private val context: Context
) {
    // 1. Fetch all project sessions for the History screen
    val allSessions: Flow<List<ChatSession>> = chatDao.getAllSessions()

    // 2. Create a new project session
    suspend fun createNewSession(title: String): Long {
        return chatDao.insertSession(ChatSession(title = title))
    }

    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    // 3. The "Smart" Save: Copies images and saves the message
    suspend fun saveUserMessage(sessionId: Long, text: String, uris: List<Uri>) {
        // Physicalize the images first
        val internalPaths = uris.map { uri ->
            saveUriToInternalStorage(uri)
        }

        val message = ChatMessage(
            parentSessionId = sessionId,
            text = text,
            isBot = false,
            imagePaths = internalPaths
        )
        chatDao.insertMessage(message)
    }

    // 4. Helper to move images from Gallery to App Storage
    private fun saveUriToInternalStorage(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file =
            File(context.filesDir, "img_${System.currentTimeMillis()}_${uri.lastPathSegment}.jpg")

        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }
}