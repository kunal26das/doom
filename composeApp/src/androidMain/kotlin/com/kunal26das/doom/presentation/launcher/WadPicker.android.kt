package com.kunal26das.doom.presentation.launcher

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.kunal26das.doom.domain.MAX_WAD_BYTES
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberWadPicker(onSelected: (String, ByteArray) -> Unit, onError: (String) -> Unit): () -> Unit {
    val selected by rememberUpdatedState(onSelected)
    val failed by rememberUpdatedState(onError)
    val resolver = LocalContext.current.contentResolver
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            busy = false
        } else {
            scope.launch {
                try {
                    val (name, bytes) = withContext(Dispatchers.IO) { resolver.readWad(uri) }
                    selected(name, bytes)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    failed(if (error is IllegalArgumentException) error.message.orEmpty() else "Could not read this file. Choose a locally available WAD and try again.")
                } finally {
                    busy = false
                }
            }
        }
    }
    return {
        if (!busy) {
            busy = true
            try {
                launcher.launch(arrayOf("*/*"))
            } catch (_: Exception) {
                busy = false
                failed("The file picker could not open. Please try again.")
            }
        }
    }
}

private suspend fun ContentResolver.readWad(uri: Uri): Pair<String, ByteArray> {
    var name = "Imported WAD"
    query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameColumn >= 0) name = cursor.getString(nameColumn) ?: name
            val sizeColumn = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeColumn >= 0 && !cursor.isNull(sizeColumn)) {
                require(cursor.getLong(sizeColumn) <= MAX_WAD_BYTES) { "Choose a WAD no larger than 64 MB." }
            }
        }
    }
    val bytes = checkNotNull(openInputStream(uri)) { "The selected file could not be opened." }.use { input ->
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            currentCoroutineContext().ensureActive()
            val count = input.read(buffer)
            if (count < 0) break
            require(output.size().toLong() + count <= MAX_WAD_BYTES) { "Choose a WAD no larger than 64 MB." }
            output.write(buffer, 0, count)
        }
        output.toByteArray()
    }
    return name to bytes
}
