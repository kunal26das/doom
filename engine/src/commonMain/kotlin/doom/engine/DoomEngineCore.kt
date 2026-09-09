package doom.engine

import doom.engine.capture.PcxScreenshots
import doom.engine.configuration.StartupArguments
import doom.engine.rendering.FrameBuffers
import doom.engine.rendering.PaletteVideoOutput
import doom.engine.rendering.ScreenWipe
import doom.engine.runtime.EngineClock
import doom.engine.runtime.HostAttachment
import doom.engine.runtime.QuitSignal
import doom.engine.simulation.ThinkerScheduler

/**
 * Composition and remaining mutable model of the original game algorithms.
 * Independent services receive explicit ports, while compatibility adapters bind
 * those services to this model. This type is never a dependency of a new service.
 */
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
    internal val statePSight = PSightState()
    internal val configuration by lazy(LazyThreadSafetyMode.NONE) { createConfiguration(host) }
    internal val stateCeiling = CeilingState()
    internal val statePInter = PInterState()
    internal val stateHuLib = HuLibState()
    internal val statePUser = PUserState()
    internal val stateActionRegistry = ActionRegistryState()
    internal val wadArchive = WadArchive()
    internal val stateSoundDefinition = SoundDefinitionState()
    internal val stateActorDefinition = ActorDefinitionState()
    internal val stateStLib = StLibState()
    internal val stateDItems = DItemsState()
    internal val stateSwitch = SwitchState()
    internal val random = RandomSequences()
    internal val stateGameLoop = GameLoopState()
    internal val statePSaveg = PSavegState()
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
