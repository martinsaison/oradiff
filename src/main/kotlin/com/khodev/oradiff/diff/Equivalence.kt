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
package com.khodev.oradiff.diff

import java.io.*
import java.util.*

class Equivalence private constructor(line: String) {
    //	private String type;
    private val names = ArrayList<String>()

    init {
        val data = line.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        //		type = data[0];
        val names = data[1].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        Collections.addAll(this.names, *names)
    }

    private fun unitMatches(name1: String?, name2: String?): Boolean {
        var found1 = false
        var found2 = false
        for (name in names) {
            if (name1!!.uppercase(Locale.getDefault()) == name.uppercase(Locale.getDefault())) found1 = true
            if (name2!!.uppercase(Locale.getDefault()) == name.uppercase(Locale.getDefault())) found2 = true
        }
        return found1 && found2
    }

    companion object {
        private var equivalences: ArrayList<Equivalence>? = null
        @Throws(IOException::class)
        private fun loadEquivalences(filename: String) {
            equivalences = ArrayList()
            val b: BufferedReader
            var line: String
            val file = File(filename)
            if (file.exists()) {
                b = BufferedReader(FileReader(filename))
                while (b.readLine().also { line = it } != null) {
                    equivalences!!.add(Equivalence(line))
                }
                b.close()
            }
        }

        @Throws(IOException::class)
        private fun getEquivalences(): ArrayList<Equivalence>? {
            if (equivalences == null) loadEquivalences("equivalences.txt")
            return equivalences
        }

        @Throws(IOException::class)
        fun matches(name1: String?, name2: String?): Boolean {
            if (name1!!.uppercase(Locale.getDefault()) == name2!!.uppercase(Locale.getDefault())) return true
            for (equivalence in getEquivalences()!!) {
                if (equivalence.unitMatches(name1, name2)) return true
            }
            return false
        }
    }
}
