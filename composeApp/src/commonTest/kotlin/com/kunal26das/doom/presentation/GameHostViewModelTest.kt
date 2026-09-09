package com.kunal26das.doom.presentation

import com.kunal26das.doom.domain.BuiltInGameSelection
import com.kunal26das.doom.domain.ImportedGameSelection

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kunal26das.doom.domain.GameLaunchSelection
import com.kunal26das.doom.domain.ImportedGameRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GameHostViewModelTest {
    @Test
    fun savedGameStartsAutomaticallyWithoutWritingItAgain() = runTest {
        val saved = ImportedGameSelection("DOOM.WAD", wad())
        val repository = GameHostFakeRepository().apply { onLoad = { saved } }

        withHost(repository) { host, _ ->
            assertTrue(host.state.value.isBusy)
            testScheduler.runCurrent()

            assertSame(saved, assertNotNull(host.state.value.game).selection)
            assertFalse(host.state.value.isBusy)
            assertNull(host.state.value.error)
            assertEquals(1, repository.loadCalls)
            assertTrue(repository.saveAttempts.isEmpty())

            val running = host.state.value.game
            host.playDemo()
            assertSame(running, host.state.value.game)
        }
    }

    @Test
    fun deviceWithoutSavedDataWaitsForAnImport() = runTest {
        withHost(GameHostFakeRepository()) { host, _ ->
            testScheduler.runCurrent()
            host.playWithoutSaving()

            assertFalse(host.state.value.isBusy)
            assertNull(host.state.value.game)
            assertNull(host.state.value.error)
            assertFalse(host.state.value.canPlayWithoutSaving)
        }
    }

    @Test
    fun demoCanStartRestartAndReturnToTheChooserWithoutChangingStoredData() = runTest {
        val repository = GameHostFakeRepository()
        withHost(repository) { host, _ ->
            testScheduler.runCurrent()

            host.playDemo()

            val demo = assertNotNull(host.state.value.game)
            val demoLifetime = trackLifetime(demo.viewModelStore)
            assertSame(BuiltInGameSelection, demo.selection)
            assertFalse(host.state.value.isBusy)
            assertNull(host.state.value.error)

            host.restart()

            val restarted = assertNotNull(host.state.value.game)
            val restartedLifetime = trackLifetime(restarted.viewModelStore)
            assertNotSame(demo, restarted)
            assertSame(BuiltInGameSelection, restarted.selection)
            assertEquals(1, demoLifetime.clearCalls)

            host.changeFile()

            assertNull(host.state.value.game)
            assertFalse(host.state.value.isBusy)
            assertEquals(1, restartedLifetime.clearCalls)
            assertTrue(repository.saveAttempts.isEmpty())
            assertEquals(0, repository.clearCalls)
            assertEquals(1, repository.loadCalls)
        }
    }

    @Test
    fun demoDoesNotInterruptRestoringAPurchasedGame() = runTest {
        val saved = ImportedGameSelection("DOOM.WAD", wad())
        val restoreFinished = CompletableDeferred<ImportedGameSelection?>()
        val repository = GameHostFakeRepository().apply { onLoad = { restoreFinished.await() } }
        withHost(repository) { host, _ ->
            testScheduler.runCurrent()

            host.playDemo()

            assertTrue(host.state.value.isBusy)
            assertNull(host.state.value.game)
            restoreFinished.complete(saved)
            testScheduler.runCurrent()

            assertSame(saved, assertNotNull(host.state.value.game).selection)
            assertTrue(repository.saveAttempts.isEmpty())
            assertEquals(0, repository.clearCalls)
        }
    }

    @Test
    fun demoDoesNotInterruptSavingAPurchasedGame() = runTest {
        val writeFinished = CompletableDeferred<Unit>()
        val repository = GameHostFakeRepository().apply { onSave = { writeFinished.await() } }
        withHost(repository) { host, _ ->
            testScheduler.runCurrent()
            host.importGame("DOOM.WAD", wad())
            testScheduler.runCurrent()

            host.playDemo()

            assertTrue(host.state.value.isBusy)
            assertNull(host.state.value.game)
            writeFinished.complete(Unit)
            testScheduler.runCurrent()

            assertSame(repository.saveAttempts.single(), assertNotNull(host.state.value.game).selection)
            assertEquals(0, repository.clearCalls)
        }
    }

    @Test
    fun validImportIsSavedBeforeItStarts() = runTest {
        val writeFinished = CompletableDeferred<Unit>()
        val repository = GameHostFakeRepository().apply { onSave = { writeFinished.await() } }
        withHost(repository) { host, _ ->
            testScheduler.runCurrent()
            host.importGame("DOOM.WAD", wad())
            testScheduler.runCurrent()

            assertEquals(1, repository.saveAttempts.size)
            assertTrue(host.state.value.isBusy)
            assertNull(host.state.value.game)

            writeFinished.complete(Unit)
            testScheduler.runCurrent()

            val saved = repository.saveAttempts.single()
            assertEquals("DOOM.WAD", saved.name)
            assertSame(saved, assertNotNull(host.state.value.game).selection)
            assertFalse(host.state.value.isBusy)
            assertNull(host.state.value.error)
        }
    }

    @Test
    fun invalidImportIsNeitherSavedNorStarted() = runTest {
        val repository = GameHostFakeRepository()
        withHost(repository) { host, _ ->
            testScheduler.runCurrent()
            host.importGame("DOOM.WAD", byteArrayOf(1, 2, 3))
            testScheduler.runCurrent()

            assertTrue(repository.saveAttempts.isEmpty())
            assertNull(host.state.value.game)
            assertFalse(host.state.value.isBusy)
            assertFalse(host.state.value.canPlayWithoutSaving)
            assertTrue(assertNotNull(host.state.value.error).isNotBlank())
        }
    }

    @Test
    fun storageFailureAllowsPlayingTheValidatedImportWithoutAnotherWrite() = runTest {
        val repository = GameHostFakeRepository().apply { onSave = { error("Storage is full") } }
        withHost(repository) { host, _ ->
            testScheduler.runCurrent()
            host.importGame("DOOM.WAD", wad())
            testScheduler.runCurrent()

            assertNull(host.state.value.game)
            assertFalse(host.state.value.isBusy)
            assertNotNull(host.state.value.error)
            assertTrue(host.state.value.canPlayWithoutSaving)

            host.playWithoutSaving()

            assertSame(repository.saveAttempts.single(), assertNotNull(host.state.value.game).selection)
            assertNull(host.state.value.error)
            assertFalse(host.state.value.canPlayWithoutSaving)
        }
    }

    @Test
    fun restartReleasesThePriorGameAndRetainsItsSelection() = runTest {
        val saved = ImportedGameSelection("DOOM.WAD", wad())
        val repository = GameHostFakeRepository().apply { onLoad = { saved } }
        withHost(repository) { host, ownerStore ->
            testScheduler.runCurrent()
            val previous = assertNotNull(host.state.value.game)
            val previousLifetime = trackLifetime(previous.viewModelStore)

            host.restart()

            val restarted = assertNotNull(host.state.value.game)
            assertNotSame(previous, restarted)
            assertSame(saved, restarted.selection)
            assertEquals(1, previousLifetime.clearCalls)
            assertEquals(1, repository.loadCalls)
            assertTrue(repository.saveAttempts.isEmpty())

            val restartedLifetime = trackLifetime(restarted.viewModelStore)
            ownerStore.clear()
            assertEquals(1, restartedLifetime.clearCalls)
            assertEquals(1, previousLifetime.clearCalls)
        }
    }

    @Test
    fun failedRestoreCanBeReplacedByANewImport() = runTest {
        val repository = GameHostFakeRepository().apply { onLoad = { error("Unreadable saved file") } }
        withHost(repository) { host, _ ->
            testScheduler.runCurrent()
            assertFalse(host.state.value.isBusy)
            assertNull(host.state.value.game)
            assertNotNull(host.state.value.error)

            host.importGame("DOOM.WAD", wad())
            testScheduler.runCurrent()

            assertSame(repository.saveAttempts.single(), assertNotNull(host.state.value.game).selection)
            assertNull(host.state.value.error)
        }
    }

    @Test
    fun changingTheFileReleasesTheRunningGameAndAllowsANewImport() = runTest {
        val repository = GameHostFakeRepository().apply {
            onLoad = { ImportedGameSelection("DOOM.WAD", wad()) }
        }
        withHost(repository) { host, _ ->
            testScheduler.runCurrent()
            val previousLifetime = trackLifetime(assertNotNull(host.state.value.game).viewModelStore)

            host.changeFile()

            assertEquals(1, previousLifetime.clearCalls)
            assertNull(host.state.value.game)
            assertFalse(host.state.value.isBusy)

            host.importGame("DOOM2.WAD", wad("MAP01"))
            testScheduler.runCurrent()
            val imported = assertNotNull(host.state.value.game).selection as ImportedGameSelection
            assertEquals("DOOM2.WAD", imported.name)
        }
    }

    @Test
    fun clearingTheHostCancelsPendingRestoreWithoutReportingAFileError() = runTest {
        var restoreCancelled = false
        val repository = GameHostFakeRepository().apply {
            onLoad = {
                try {
                    awaitCancellation()
                } finally {
                    restoreCancelled = true
                }
            }
        }
        withHost(repository) { host, ownerStore ->
            testScheduler.runCurrent()
            assertEquals(1, repository.loadCalls)

            ownerStore.clear()
            testScheduler.runCurrent()

            assertTrue(restoreCancelled)
            assertNull(host.state.value.error)
            assertNull(host.state.value.game)
            assertFalse(host.state.value.canPlayWithoutSaving)
        }
    }

    private fun TestScope.withHost(
        repository: ImportedGameRepository,
        block: (GameHostViewModel, ViewModelStore) -> Unit,
    ) {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val provider = ViewModelProvider.create(
                store,
                viewModelFactory { initializer { GameHostViewModel(repository) } },
            )
            block(provider[GameHostViewModel::class], store)
        } finally {
            store.clear()
            testScheduler.runCurrent()
            Dispatchers.resetMain()
        }
    }

    private fun trackLifetime(store: ViewModelStore): GameHostTrackedViewModel =
        ViewModelProvider.create(store, viewModelFactory { initializer { GameHostTrackedViewModel() } })[
            GameHostTrackedViewModel::class,
        ]

    private fun wad(map: String = "E1M1"): ByteArray {
        val names = listOf("PLAYPAL", "COLORMAP", "TEXTURE1", "PNAMES", "M_DOOM", map)
        return ByteArray(12 + names.size * 16).also { bytes ->
            "IWAD".forEachIndexed { index, char -> bytes[index] = char.code.toByte() }
            bytes.writeInt(4, names.size)
            bytes.writeInt(8, 12)
            names.forEachIndexed { index, name ->
                name.forEachIndexed { character, char ->
                    bytes[12 + index * 16 + 8 + character] = char.code.toByte()
                }
            }
        }
    }

    private fun ByteArray.writeInt(offset: Int, value: Int) {
        repeat(4) { this[offset + it] = (value ushr (8 * it)).toByte() }
    }
}
