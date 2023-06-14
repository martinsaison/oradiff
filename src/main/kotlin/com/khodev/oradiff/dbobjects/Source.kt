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

abstract class Source internal constructor(name: String) : DBObject(name) {
    val body = ArrayList<String>()
    open fun getSource(type: String): ArrayList<String> {
        return body
    }

    fun append(type: String, line: Int, text: String) {
        getSource(type)!!.add(text)
    }

    fun dbEquals(diffOptions: DiffOptions, dst: Source): Boolean {
        return DBObject.Companion.textForDiff(diffOptions, body) == DBObject.Companion.textForDiff(diffOptions, dst.body)
    }

    override fun sqlCreate(diffOptions: DiffOptions): String {
        var res = "CREATE OR REPLACE "
        for (line in body) {
            res += DBObject.Companion.removeR4(line)
        }
        res += "/\n"
        return res
    }

    override fun sqlUpdate(diffOptions: DiffOptions, destination: DBObject): String {
        return destination.sqlCreate(diffOptions)
    }
}
