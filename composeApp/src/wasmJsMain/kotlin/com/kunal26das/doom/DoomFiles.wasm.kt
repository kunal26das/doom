package com.kunal26das.doom

import kotlinx.browser.localStorage

/** localStorage with base64 payloads (savegames are ~10-180KB, well within quota). */
actual fun platformReadFile(name: String): ByteArray? {
    val b64 = localStorage.getItem("doom:$name") ?: return null
    return b64decode(b64)
}

actual fun platformWriteFile(name: String, data: ByteArray) {
    localStorage.setItem("doom:$name", b64encode(data))
}

private const val B64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

private fun b64encode(data: ByteArray): String {
    val sb = StringBuilder((data.size + 2) / 3 * 4)
    var i = 0
    while (i < data.size) {
        val b0 = data[i].toInt() and 0xFF
        val b1 = if (i + 1 < data.size) data[i + 1].toInt() and 0xFF else 0
        val b2 = if (i + 2 < data.size) data[i + 2].toInt() and 0xFF else 0
        sb.append(B64[b0 shr 2])
        sb.append(B64[((b0 and 3) shl 4) or (b1 shr 4)])
        sb.append(if (i + 1 < data.size) B64[((b1 and 15) shl 2) or (b2 shr 6)] else '=')
        sb.append(if (i + 2 < data.size) B64[b2 and 63] else '=')
        i += 3
    }
    return sb.toString()
}

private fun b64decode(s: String): ByteArray {
    val clean = s.trimEnd('=')
    val out = ArrayList<Byte>(clean.length * 3 / 4)
    var buffer = 0
    var bits = 0
    for (c in clean) {
        val v = B64.indexOf(c)
        if (v < 0) continue
        buffer = (buffer shl 6) or v
        bits += 6
        if (bits >= 8) {
            bits -= 8
            out.add(((buffer shr bits) and 0xFF).toByte())
        }
    }
    return out.toByteArray()
}
