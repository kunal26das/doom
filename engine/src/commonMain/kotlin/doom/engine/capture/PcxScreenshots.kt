// PCX compatibility derived from linuxdoom-1.10 m_misc.c.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
package doom.engine.capture

/** Screenshot numbering belongs to one session, independently of game configuration. */
internal class PcxScreenshots {
    private var nextNumber = 0
    private val encoder = PcxEncoder()

    fun nextFileName(reject: (String) -> Nothing): String {
        val number = nextNumber
        if (number == 100) reject("M_ScreenShot: Couldn't create a PCX")
        nextNumber++
        return "DOOM${'0' + number / 10}${'0' + number % 10}.pcx"
    }

    fun encode(data: ByteArray, width: Int, height: Int, palette: ByteArray): ByteArray =
        encoder.encode(data, width, height, palette)
}
