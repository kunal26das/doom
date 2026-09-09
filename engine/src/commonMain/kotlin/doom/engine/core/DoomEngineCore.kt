package doom.engine.core

import doom.engine.DoomClock
import doom.engine.TICRATE
import doom.engine.audio.SoundDefinitionState
import doom.engine.audio.SoundPlaybackState
import doom.engine.automap.AutomapSubsystemState
import doom.engine.capture.PcxScreenshots
import doom.engine.cheats.CheatDecoderState
import doom.engine.configuration.StartupArguments
import doom.engine.configuration.createConfiguration
import doom.engine.finale.FinaleState
import doom.engine.gameplay.GameIdentityState
import doom.engine.gameplay.GameSessionState
import doom.engine.gameplay.actors.ActionRegistryState
import doom.engine.gameplay.actors.ActorDefinitionState
import doom.engine.gameplay.actors.ActorSpawnState
import doom.engine.gameplay.actors.MonsterBehaviorState
import doom.engine.gameplay.interactions.ItemInteractionState
import doom.engine.gameplay.player.PlayerMovementState
import doom.engine.gameplay.weapons.WeaponDefinitionState
import doom.engine.gameplay.weapons.WeaponSpriteState
import doom.engine.hud.HudState
import doom.engine.hud.HudTextState
import doom.engine.input.EngineInputQueue
import doom.engine.intermission.IntermissionState
import doom.engine.menu.createMenuState
import doom.engine.rendering.BspRendererState
import doom.engine.rendering.FrameBuffers
import doom.engine.rendering.PaletteVideoOutput
import doom.engine.rendering.PlaneRendererState
import doom.engine.rendering.RasterizerState
import doom.engine.rendering.RendererViewState
import doom.engine.rendering.ScreenWipe
import doom.engine.rendering.SkyRendererState
import doom.engine.rendering.SpriteRendererState
import doom.engine.rendering.WallRendererState
import doom.engine.rendering.createSceneRenderer
import doom.engine.rendering.resources.TextureResourceState
import doom.engine.resources.WadArchive
import doom.engine.runtime.EngineClock
import doom.engine.runtime.HostAttachment
import doom.engine.runtime.QuitSignal
import doom.engine.savegame.SaveGameSerializationState
import doom.engine.simulation.RandomSequences
import doom.engine.simulation.ThinkerScheduler
import doom.engine.simulation.createTickScheduler
import doom.engine.simulation.createWorldTicker
import doom.engine.statusbar.StatusBarState
import doom.engine.statusbar.StatusBarWidgetState
import doom.engine.world.WorldGeometryState
import doom.engine.world.collision.CollisionState
import doom.engine.world.collision.TraversalState
import doom.engine.world.movers.CeilingState
import doom.engine.world.movers.PlatformMotionState
import doom.engine.world.specials.SectorSpecialState
import doom.engine.world.specials.SwitchState
import doom.engine.world.visibility.SightTraceState

internal class DoomEngineCore(clockSource: DoomClock? = null) {
    internal val host = HostAttachment()
    internal val clock = EngineClock(clockSource)
    internal val quit = QuitSignal()
    internal val inputQueue = EngineInputQueue()
    internal val stateTraversal = TraversalState()
    internal val stateWeaponSprite = WeaponSpriteState()
    internal val tickScheduler = createTickScheduler(this)
    internal val sceneRenderer = createSceneRenderer(this)
    internal val videoOutput = PaletteVideoOutput(host)
    internal val stateSoundPlayback = SoundPlaybackState()
    internal val stateSectorSpecial = SectorSpecialState()
    internal val stateWorldGeometry = WorldGeometryState()
    internal val frameBuffers = FrameBuffers()
    internal val stateBspRenderer = BspRendererState()
    internal val stateHud = HudState()
    internal val stateGameSession = GameSessionState()
    internal val stateMonsterBehavior = MonsterBehaviorState()
    internal val statePSight = SightTraceState()
    internal val configuration by lazy(LazyThreadSafetyMode.NONE) { createConfiguration(host) }
    internal val stateCeiling = CeilingState()
    internal val statePInter = ItemInteractionState()
    internal val stateHuLib = HudTextState()
    internal val statePUser = PlayerMovementState()
    internal val stateActionRegistry = ActionRegistryState()
    internal val wadArchive = WadArchive()
    internal val stateSoundDefinition = SoundDefinitionState()
    internal val stateActorDefinition = ActorDefinitionState()
    internal val stateStLib = StatusBarWidgetState()
    internal val stateDItems = WeaponDefinitionState()
    internal val stateSwitch = SwitchState()
    internal val random = RandomSequences()
    internal val stateGameLoop = GameLoopState()
    internal val statePSaveg = SaveGameSerializationState()
    internal val stateSpriteRenderer = SpriteRendererState()
    internal val stateActorSpawn = ActorSpawnState()
    internal val screenWipe = ScreenWipe(frameBuffers, random::nextPresentation)
    internal val stateFinale = FinaleState()
    internal val stateIntermission = IntermissionState()
    internal val stateAutomap = AutomapSubsystemState()
    internal val stateMenu = createMenuState(this)
    internal val stateCollision = CollisionState()
    internal val startupArguments = StartupArguments(TICRATE)
    internal val screenshots = PcxScreenshots()
    internal val thinkers = ThinkerScheduler()
    internal val worldTicker = createWorldTicker(this, thinkers)
    internal val stateRendererView = RendererViewState()
    internal val stateSkyRenderer = SkyRendererState()
    internal val stateWallRenderer = WallRendererState()
    internal val stateRasterizer = RasterizerState()
    internal val stateStatusBar = StatusBarState()
    internal val statePlaneRenderer = PlaneRendererState()
    internal val stateCheatDecoder = CheatDecoderState()
    internal val stateTextureResource = TextureResourceState()
    internal val stateGameIdentity = GameIdentityState()
    internal val statePlatformMotion = PlatformMotionState()
}
