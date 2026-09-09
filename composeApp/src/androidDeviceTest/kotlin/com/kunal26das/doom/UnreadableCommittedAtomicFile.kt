package com.kunal26das.doom

import android.util.AtomicFile
import java.io.File
import java.io.IOException

internal class UnreadableCommittedAtomicFile(file: File, private val failure: IOException) : AtomicFile(file) {
    override fun readFully(): ByteArray = throw failure
}
