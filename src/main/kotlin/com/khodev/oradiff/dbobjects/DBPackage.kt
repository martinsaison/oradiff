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

class DBPackage(name: String) : Source(name) {
    val declaration = ArrayList<String>()
    fun dbEquals(diffOptions: DiffOptions, dst: DBPackage): Boolean {
        return super.dbEquals(diffOptions, dst) && textForDiff(
            diffOptions, declaration
        ) == textForDiff(diffOptions, dst.declaration)
    }

    override fun getSource(type: String): ArrayList<String> {
        return when (type) {
            "PACKAGE" -> declaration
            "PACKAGE BODY" -> body
            else -> error("Error: unknown type $type")
        }
    }


    val typeName: String
        get() = "PACKAGE"

    fun sqlCreate(): String {
        var res = "CREATE OR REPLACE "
        for (line in declaration) res += line
        res += "/\n"
        res += "CREATE OR REPLACE "
        for (line in body) res += line
        res += "/\n"
        return res
    }

    fun sqlUpdate(destination: DBPackage): String {
        return destination.sqlCreate()
    }

}
