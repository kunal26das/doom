package doom.engine.capture

internal class PcxEncoder {
    fun encode(data: ByteArray, width: Int, height: Int, palette: ByteArray): ByteArray {
        val pcx = ByteArray(width * height * 2 + 1000)

        fun putShort(offset: Int, value: Int) {
            pcx[offset] = (value and 0xff).toByte()
            pcx[offset + 1] = ((value shr 8) and 0xff).toByte()
        }

        pcx[0] = 0x0a
        pcx[1] = 5
        pcx[2] = 1
        pcx[3] = 8
        putShort(4, 0)
        putShort(6, 0)
        putShort(8, width - 1)
        putShort(10, height - 1)
        putShort(12, width)
        putShort(14, height)
        pcx[65] = 1
        putShort(66, width)
        putShort(68, 2)

        var output = 128
        for (index in 0 until width * height) {
            if ((data[index].toInt() and 0xc0) == 0xc0) pcx[output++] = 0xc1.toByte()
            pcx[output++] = data[index]
        }
        pcx[output++] = 0x0c
        for (index in 0 until 768) pcx[output++] = palette[index]
        return pcx.copyOf(output)
    }
}
