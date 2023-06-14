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

class View(name: String, source: String) : DBObject(name) {
    val columns = ArrayList<String?>()
    var source: String

    init {
        this.source = DBObject.Companion.removeR4(source)
    }

    fun addColumn(column: String?) {
        columns.add(column)
    }

    fun dbEquals(dst: View): Boolean {
        return DBObject.Companion.textForDiff(dst.source) == DBObject.Companion.textForDiff(source) && columns == dst.columns
    }

    override val typeName: String
        get() = "VIEW"

    override fun sqlCreate(diffOptions: DiffOptions): String {
        var res: String = """
            CREATE OR REPLACE VIEW $name
            (
            """.trimIndent()
        var first = true
        for (column in columns) {
            if (first) first = false else res += ", "
            res += column
        }
        res += ")\n AS\n$source\n/\n"
        return res
    }

    override fun sqlUpdate(diffOptions: DiffOptions, destination: DBObject): String {
        return destination.sqlCreate(diffOptions)
    }
}
