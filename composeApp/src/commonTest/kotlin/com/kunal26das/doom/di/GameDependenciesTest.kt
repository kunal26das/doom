package com.kunal26das.doom.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kunal26das.doom.presentation.GameHostViewModel
import com.kunal26das.doom.presentation.GameViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class GameDependenciesTest {
    @Test
    fun applicationRepositoriesDoNotShareHostOrGameLifetimes() {
        val stores = List(2) { ViewModelStore() }
        val dependencies = GameDependencies(GameDependenciesUnusedFiles)
        try {
            val hosts = stores.map { store ->
                ViewModelProvider.create(
                    store,
                    viewModelFactory { initializer { dependencies.createHostViewModel() } },
                )[GameHostViewModel::class]
            }
            assertNotSame(hosts[0], hosts[1])
            val entries = hosts.map { it.game.value }
            val models = entries.map { entry ->
                ViewModelProvider.create(
                    entry.viewModelStore,
                    viewModelFactory { initializer { dependencies.createViewModel() } },
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
            assertNotSame(entries[0], hosts[0].game.value)
            assertSame(entries[1], hosts[1].game.value)

            stores[1].clear()
            assertEquals(1, secondClosed)
            assertEquals(1, firstClosed)
        } finally {
            stores.forEach(ViewModelStore::clear)
        }
    }
}
