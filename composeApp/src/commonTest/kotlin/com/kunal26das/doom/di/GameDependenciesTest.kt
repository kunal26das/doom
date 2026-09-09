package com.kunal26das.doom.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kunal26das.doom.presentation.GameHostViewModel
import com.kunal26das.doom.presentation.GameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertSame

@OptIn(ExperimentalCoroutinesApi::class)
class GameDependenciesTest {
    @Test
    fun applicationRepositoriesDoNotShareHostOrGameLifetimes() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val stores = List(2) { ViewModelStore() }
        val importedGames = GameDependenciesEmptyImportedGames()
        val dependencies = GameDependencies(GameDependenciesUnusedFiles, importedGames)
        try {
            val hosts = stores.map { store ->
                ViewModelProvider.create(
                    store,
                    viewModelFactory { initializer { dependencies.createHostViewModel() } },
                )[GameHostViewModel::class]
            }
            assertNotSame(hosts[0], hosts[1])
            testScheduler.runCurrent()
            assertEquals(2, importedGames.loads)
            hosts.forEach(GameHostViewModel::playDemo)

            val entries = hosts.map { assertNotNull(it.state.value.game) }
            val models = entries.map { entry ->
                ViewModelProvider.create(
                    entry.viewModelStore,
                    viewModelFactory { initializer { dependencies.createViewModel(entry.selection) } },
                )[GameViewModel::class]
            }
            assertNotSame(models[0], models[1])
            var firstClosed = 0
            var secondClosed = 0
            models[0].addCloseable(AutoCloseable { firstClosed++ })
            models[1].addCloseable(AutoCloseable { secondClosed++ })

            hosts[0].restart()

            assertEquals(1, firstClosed)
            assertEquals(0, secondClosed)
            assertNotSame(entries[0], hosts[0].state.value.game)
            assertSame(entries[1], hosts[1].state.value.game)
            assertEquals(2, importedGames.loads, "Restart must reuse the repository without restoring again")

            stores[1].clear()
            assertEquals(1, secondClosed)
            assertEquals(1, firstClosed)
        } finally {
            stores.forEach(ViewModelStore::clear)
            testScheduler.runCurrent()
            Dispatchers.resetMain()
        }
    }

}
