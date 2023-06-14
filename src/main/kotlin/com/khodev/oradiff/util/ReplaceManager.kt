/*
 * Copyright (c) 2017 Martin Saison
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package com.khodev.oradiff.util

import java.io.*
import java.util.*

class ReplaceManager private constructor(private val name: String) {
    private val keysValues: Hashtable<String, String> = Hashtable()
    private var loaded = false

    private val fileName: String
        get() = "$name.txt"

    private fun load() {
        if (loaded) return
        try {
            val file = File(fileName)
            if (!file.exists()) return
            var line: String
            val r = BufferedReader(FileReader(file))
            while (r.readLine().also { line = it } != null) {
                line = line.trim { it <= ' ' }
                if (line.isEmpty()) continue
                val vars = line.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val tablespace = vars[0].trim { it <= ' ' }
                val varName = vars[1].trim { it <= ' ' }
                keysValues[tablespace] = varName
            }
            r.close()
            loaded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun save() {
        try {
            val file = File(fileName)
            val w = BufferedWriter(FileWriter(file))
            for ((key, value) in keysValues) {
                w.write("$key=$value\n")
            }
            w.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSubstitute(key: String): String {
        if (key.isEmpty()) return key
        load()
        if (keysValues.containsKey(key)) {
            return keysValues[key]!!
        }
        if (Configuration.saveNewSubstitutes && !keysValues.containsValue(key)) {
            keysValues[key] = key
            save()
        }
        return key
    }

    companion object {
        private var managers: Hashtable<String, ReplaceManager>? = null
        fun getManager(name: String): ReplaceManager {
            if (managers == null) managers = Hashtable()
            if (!managers!!.containsKey(name)) managers!![name] = ReplaceManager(name)
            return managers!![name]!!
        }
    }
}
