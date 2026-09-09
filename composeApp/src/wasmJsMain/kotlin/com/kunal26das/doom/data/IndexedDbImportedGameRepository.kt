@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.kunal26das.doom.data

import com.kunal26das.doom.domain.ImportedGameSelection

import com.kunal26das.doom.domain.FileRepository
import com.kunal26das.doom.domain.ImportedGameRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.Int8Array
import org.khronos.webgl.toByteArray
import org.khronos.webgl.toInt8Array
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.JsAny

actual fun createImportedGameRepository(files: FileRepository): ImportedGameRepository =
    IndexedDbImportedGameRepository()

private class IndexedDbImportedGameRepository : ImportedGameRepository {
    override suspend fun load(): ImportedGameSelection? =
        StoredImportedGame.decode(transact("load"))

    override suspend fun save(game: ImportedGameSelection) {
        transact("save", StoredImportedGame.encode(game))
    }

    override suspend fun clear() { transact("clear") }

    private suspend fun transact(operation: String, value: ByteArray = ByteArray(0)): ByteArray =
        suspendCancellableCoroutine { continuation ->
            val handle = accessImportedGameStorage(
                operation,
                value.toInt8Array(),
                StoredImportedGame.MAX_RECORD_BYTES,
                { bytes -> if (continuation.isActive) continuation.resume(bytes.toByteArray()) },
                { message -> if (continuation.isActive) continuation.resumeWithException(IllegalStateException(message)) },
            )
            continuation.invokeOnCancellation { cancelImportedGameStorage(handle) }
        }
}

private fun accessImportedGameStorage(
    operation: String,
    value: Int8Array,
    limit: Int,
    onSuccess: (Int8Array) -> Unit,
    onError: (String) -> Unit,
): JsAny = js("""{
    const handle = { active: true, database: null, transaction: null };
    const finish = (error, result) => {
        if (!handle.active) return;
        handle.active = false;
        if (handle.database) handle.database.close();
        if (error) onError(error);
        else onSuccess(result || new Int8Array(0));
    };
    try {
        if (typeof indexedDB === 'undefined') {
            finish('This browser cannot remember game files. You can still play after choosing your WAD.');
            return handle;
        }
        const request = indexedDB.open('doom-imported-game', 1);
        request.onupgradeneeded = () => {
            if (!handle.active) { request.transaction.abort(); return; }
            request.result.createObjectStore('games');
        };
        request.onerror = () => finish('Could not open saved game storage. Choose your DOOM.WAD to play.');
        request.onblocked = () => finish('Another game tab is blocking storage. Close it and try again.');
        request.onsuccess = () => {
            const database = request.result;
            if (!handle.active) { database.close(); return; }
            handle.database = database;
            database.onversionchange = () => {
                database.close();
                finish('Game storage changed in another tab. Reload this page and try again.');
            };
            try {
                const transaction = database.transaction('games', operation === 'load' ? 'readonly' : 'readwrite');
                handle.transaction = transaction;
                const store = transaction.objectStore('games');
                let result = new Int8Array(0);
                let invalid = false;
                if (operation === 'load') {
                    const read = store.get('selected');
                    read.onsuccess = () => {
                        if (read.result === undefined) return;
                        if (!(read.result instanceof ArrayBuffer) || read.result.byteLength > limit) {
                            invalid = true;
                        } else {
                            result = new Int8Array(read.result);
                        }
                    };
                } else if (operation === 'save') {
                    store.put(value.slice().buffer, 'selected');
                } else {
                    store.delete('selected');
                }
                transaction.oncomplete = () => finish(
                    invalid ? 'The saved game file is damaged. Choose your original DOOM.WAD again.' : null,
                    result
                );
                transaction.onabort = () => finish(
                    transaction.error && transaction.error.name === 'QuotaExceededError'
                        ? 'This browser has no space to remember the game. You can still play this session.'
                        : 'Could not access saved game storage. You can still choose your WAD to play.'
                );
                transaction.onerror = () => {};
            } catch (error) {
                finish('Could not access saved game storage. You can still choose your WAD to play.');
            }
        };
    } catch (error) {
        finish('This browser cannot remember game files. You can still play after choosing your WAD.');
    }
    return handle;
}""")

private fun cancelImportedGameStorage(handle: JsAny): Unit = js("""{
    handle.active = false;
    if (handle.transaction) {
        try { handle.transaction.abort(); } catch (error) {}
    }
    if (handle.database) handle.database.close();
}""")
