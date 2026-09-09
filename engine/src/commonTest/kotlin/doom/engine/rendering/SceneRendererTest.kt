package doom.engine.rendering

import kotlin.test.Test
import kotlin.test.assertEquals

class SceneRendererTest {
    @Test
    fun preservesInputCheckpointsBetweenRenderPasses() {
        val calls = mutableListOf<String>()
        val renderer = SceneRenderer(object : ScenePasses<String> {
            override fun prepare(player: String) { calls += "prepare $player" }
            override fun drawWorld() { calls += "world" }
            override fun drawPlanes() { calls += "planes" }
            override fun drawMasked() { calls += "masked" }
        }) { calls += "poll" }
        renderer.render("player")
        assertEquals(listOf("prepare player", "poll", "world", "poll", "planes", "poll", "masked", "poll"), calls)
    }
}
