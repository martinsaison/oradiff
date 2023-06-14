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
package com.khodev.oradiff.dbobjects

import com.khodev.oradiff.diff.DiffOptions

abstract class DBObject internal constructor(var name: String) {

    fun escapeName(name: String): String {
        return "\"" + name + "\""
    }

    protected abstract val typeName: String
    abstract fun sqlCreate(diffOptions: DiffOptions): String
    open fun sqlDrop(): String {
        return "drop $typeName $name;"
    }

    abstract fun sqlUpdate(diffOptions: DiffOptions, destination: DBObject): String
    companion object {
        fun escape(s: String?): String {
            return s?.replace("'", "''") ?: ""
        }

        fun <T : DBObject?> newObjects(
            srcSet: ArrayList<T>, dstSet: ArrayList<T>
        ): ArrayList<T> {
            val res = ArrayList<T>()
            for (dst in dstSet) {
                var found = false
                for (src in srcSet) {
                    if (src!!.name == dst!!.name) {
                        found = true
                        break
                    }
                }
                if (found) continue
                res.add(dst)
            }
            return res
        }

        fun removeR4(s: String): String {
            return s
            /*
		 * return s.replace("r4", "").replace("R4", "").replace("\"r4\".",
		 * "").replace("\"R4\".", "");
		 */
        }

        fun textForDiff(text: String): String {
            val lines = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var res = ""
            for (line in lines) {
                val trimmedLine = line.trim { it <= ' ' }
                if (trimmedLine.isEmpty()) continue
                res += trimmedLine
            }
            return res
        }

        fun textForDiff(diffOptions: DiffOptions, text: ArrayList<String>): ArrayList<String> {
            val res = ArrayList<String>()
            for (line in text) {
                val trimmedLine = removeR4(line.trim { it <= ' ' }).replace("\\s+".toRegex(), " ").lowercase().replace("\"", "").replace(" (", "(")
                if (trimmedLine.isEmpty()) continue
                if (diffOptions.ignoreSourceComments && trimmedLine.startsWith("--")) continue
                res.add(trimmedLine)
            }
            return res
        }
    }
}
