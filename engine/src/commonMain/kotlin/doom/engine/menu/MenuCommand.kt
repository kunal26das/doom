package doom.engine.menu

/** Intent produced by a game-menu item; choice retains its item index or slider direction. */
internal enum class MenuCommand {
    NEW_GAME,
    OPTIONS,
    LOAD_GAME,
    SAVE_GAME,
    HELP,
    QUIT,
    SELECT_EPISODE,
    SELECT_SKILL,
    END_GAME,
    TOGGLE_MESSAGES,
    TOGGLE_DETAIL,
    RESIZE_VIEW,
    ADJUST_SENSITIVITY,
    SOUND,
    HELP_NEXT,
    HELP_CLOSE,
    ADJUST_SFX_VOLUME,
    ADJUST_MUSIC_VOLUME,
    SELECT_LOAD_SLOT,
    SELECT_SAVE_SLOT,
}
