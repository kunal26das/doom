package com.kunal26das.doom.presentation.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import com.kunal26das.doom.domain.MAX_WAD_BYTES
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.Foundation.NSInputStream
import platform.Foundation.NSURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTTypeData
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberWadPicker(onSelected: (String, ByteArray) -> Unit, onError: (String) -> Unit): () -> Unit {
    val selected by rememberUpdatedState(onSelected)
    val failed by rememberUpdatedState(onError)
    val presenter = LocalUIViewController.current
    val scope = rememberCoroutineScope()
    // UIKit keeps a weak reference to its delegate, so Compose retains the owner.
    val picker = remember(presenter, scope) {
        NativeWadPicker(presenter, scope, { name, bytes -> selected(name, bytes) }, { failed(it) })
    }
    DisposableEffect(picker) { onDispose { picker.dispose() } }
    return { picker.open() }
}

@OptIn(ExperimentalForeignApi::class)
private class NativeWadPicker(
    private val presenter: UIViewController,
    private val scope: CoroutineScope,
    private val onSelected: (String, ByteArray) -> Unit,
    private val onError: (String) -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {
    private var controller: UIDocumentPickerViewController? = null
    private var readJob: Job? = null
    private var busy = false
    private var disposed = false

    fun open() {
        if (disposed || busy) return
        busy = true
        try {
            // Open in place with scoped access; persistence is handled by the repository.
            val documentPicker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeData), asCopy = false)
            controller = documentPicker
            documentPicker.delegate = this
            documentPicker.allowsMultipleSelection = false
            documentPicker.shouldShowFileExtensions = true
            presenter.presentViewController(documentPicker, animated = true, completion = null)
        } catch (_: Exception) {
            busy = false
            controller = null
            onError("The file picker could not open. Please try again.")
        }
    }

    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        this.controller = null
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        if (url == null || disposed) {
            busy = false
            return
        }
        readJob = scope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) { readWad(url) }
                if (!disposed) onSelected(url.lastPathComponent ?: "Imported WAD", bytes)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (!disposed) {
                    onError(if (error is IllegalArgumentException) error.message.orEmpty() else "Could not read this file. Choose a locally available WAD and try again.")
                }
            } finally {
                busy = false
                readJob = null
            }
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        this.controller = null
        busy = false
    }

    fun dispose() {
        disposed = true
        readJob?.cancel()
        controller?.let {
            it.delegate = null
            it.dismissViewControllerAnimated(false, completion = null)
        }
        controller = null
    }
}

@OptIn(ExperimentalForeignApi::class)
private suspend fun readWad(url: NSURL): ByteArray {
    val scopedAccess = url.startAccessingSecurityScopedResource()
    try {
        val input = NSInputStream(uRL = url)
        input.open()
        try {
            val chunks = mutableListOf<ByteArray>()
            val buffer = ByteArray(8192)
            var size = 0
            while (true) {
                currentCoroutineContext().ensureActive()
                val count = buffer.usePinned { input.read(it.addressOf(0).reinterpret(), buffer.size.toULong()) }.toInt()
                check(count >= 0) { "The selected file could not be read." }
                if (count == 0) break
                require(size.toLong() + count <= MAX_WAD_BYTES) { "Choose a WAD no larger than 64 MB." }
                chunks += buffer.copyOf(count)
                size += count
            }
            return ByteArray(size).also { result ->
                var offset = 0
                chunks.forEach { chunk ->
                    chunk.copyInto(result, offset)
                    offset += chunk.size
                }
            }
        } finally {
            input.close()
        }
    } finally {
        if (scopedAccess) url.stopAccessingSecurityScopedResource()
    }
}
