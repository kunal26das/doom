package com.kunal26das.doom.presentation

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class GameHostViewModelTest {
    @Test
    fun bundledGameIsReadyAsSoonAsTheHostExists() {
        withHost { host, _ ->
            val running = host.game.value
            val lifetime = trackLifetime(running.viewModelStore)

            assertSame(running, host.game.value)
            assertEquals(0, lifetime.clearCalls)
        }
    }

    @Test
    fun restartReleasesOnlyThePriorGameAndStartsAFreshEntry() {
        withHost { host, ownerStore ->
            val first = host.game.value
            val firstLifetime = trackLifetime(first.viewModelStore)

            host.restart()

            val second = host.game.value
            val secondLifetime = trackLifetime(second.viewModelStore)
            assertNotSame(first, second)
            assertEquals(1, firstLifetime.clearCalls)
            assertEquals(0, secondLifetime.clearCalls)

            host.restart()

            assertNotSame(second, host.game.value)
            assertEquals(1, firstLifetime.clearCalls)
            assertEquals(1, secondLifetime.clearCalls)

            val thirdLifetime = trackLifetime(host.game.value.viewModelStore)
            ownerStore.clear()
            assertEquals(1, thirdLifetime.clearCalls)
            assertEquals(1, secondLifetime.clearCalls)
            assertEquals(1, firstLifetime.clearCalls)
        }
    }

    private fun withHost(block: (GameHostViewModel, ViewModelStore) -> Unit) {
        val store = ViewModelStore()
        try {
            val provider = ViewModelProvider.create(
                store,
                viewModelFactory { initializer { GameHostViewModel() } },
            )
            block(provider[GameHostViewModel::class], store)
        } finally {
            store.clear()
        }
    }

    private fun trackLifetime(store: ViewModelStore): GameHostTrackedViewModel =
        ViewModelProvider.create(store, viewModelFactory { initializer { GameHostTrackedViewModel() } })[
            GameHostTrackedViewModel::class,
        ]
}
