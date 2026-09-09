package com.kunal26das.doom.presentation.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.kunal26das.doom.domain.MAX_WAD_BYTES
import java.awt.FileDialog
import java.awt.Frame
import java.awt.KeyboardFocusManager
import java.io.ByteArrayOutputStream
import java.io.File
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
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var activeDialog by remember { mutableStateOf<FileDialog?>(null) }
    DisposableEffect(Unit) { onDispose { activeDialog?.dispose() } }

    return {
        if (!busy) {
            busy = true
            scope.launch {
                try {
                    val owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow as? Frame
                    val dialog = FileDialog(owner, "Choose a DOOM IWAD", FileDialog.LOAD)
                    activeDialog = dialog
                    val file = try {
                        dialog.isMultipleMode = false
                        dialog.setFilenameFilter { _, name -> name.endsWith(".wad", ignoreCase = true) }
                        dialog.isVisible = true
                        dialog.file?.let { File(dialog.directory, it) }
                    } finally {
                        dialog.dispose()
                        activeDialog = null
                    }
                    if (file != null) {
                        val bytes = withContext(Dispatchers.IO) { readWad(file) }
                        selected(file.name, bytes)
                    }
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
}

private suspend fun readWad(file: File): ByteArray {
    require(file.length() <= MAX_WAD_BYTES) { "Choose a WAD no larger than 64 MB." }
    return file.inputStream().use { input ->
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
}
