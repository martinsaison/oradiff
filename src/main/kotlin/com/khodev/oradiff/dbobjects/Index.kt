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

class Index(
    val owner: String,
    val name: String,
    val tablespace: String,
    val type: String,
    val isUnique: Boolean,
    var compression: String,
    private val parent: Table
)  {
    val columns: MutableCollection<IndexColumn> = ArrayList()
    fun sqlCreate(diffOptions: DiffOptions): String {
        var res = ""
        res += "create"
        if (isUnique) res += " unique"
        if (type == "BITMAP") res += " bitmap"
        res += " index " + name + " on " + parent.name
        res += "\n("
        var first = true
        for (indexColumn in columns) {
            if (first) first = false else res += ", "
            res += indexColumn.name
        }
        res += ")${tablespaceSql(diffOptions)};\n"
        return res
    }

    fun sqlDrop(): String {
        return "drop index $name;\n"
    }

    fun dbEquals(index: Index?): Boolean {
        if (index!!.isUnique != isUnique) return false
        if (index.type != type) return false
        if (columns.size != index.columns.size) return false
        val it1: Iterator<IndexColumn?> = columns.iterator()
        val it2: Iterator<IndexColumn?> = index.columns.iterator()
        while (it1.hasNext() && it2.hasNext()) {
            val col1 = it1.next()
            val col2 = it2.next()
            if (col1?.name != col2?.name) return false
            if (col1?.position != col2?.position) return false
        }
        return true
    }

    val typeName: String
        get() = "INDEX"

    fun sqlUpdate(diffOptions: DiffOptions): String {
        return sqlCreate(diffOptions)
    }

    fun tablespaceSql(diffOptions: DiffOptions): String {
        return if (diffOptions.withTablespace && tablespace.isNotEmpty()) " tablespace $tablespace" else ""
    }

}
