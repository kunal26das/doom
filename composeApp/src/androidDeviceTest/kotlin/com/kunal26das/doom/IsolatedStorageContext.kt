package com.kunal26das.doom

import android.content.Context
import android.content.ContextWrapper
import java.io.File

internal class IsolatedStorageContext(context: Context, private val directory: File) : ContextWrapper(context) {
    override fun getFilesDir(): File = directory
    override fun getApplicationContext(): Context = this
}
