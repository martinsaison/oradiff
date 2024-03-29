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

import com.khodev.oradiff.util.ReplaceManager

class Grant(
    val grantee: String,
    val isSelectPriv: Boolean,
    val isInsertPriv: Boolean,
    val isDeletePriv: Boolean,
    val isUpdatePriv: Boolean,
    val isReferencesPriv: Boolean,
    val isAlterPriv: Boolean,
    val isIndexPriv: Boolean
) {

    fun dbEquals(): Boolean {
        return true
    }

    val typeName: String
        get() = "GRANT"

    fun sqlCreate(parent: Table): String {
        val gl = ArrayList<String>()
        if (isSelectPriv) gl.add("SELECT")
        if (isInsertPriv) gl.add("INSERT")
        if (isDeletePriv) gl.add("DELETE")
        if (isUpdatePriv) gl.add("UPDATE")
        if (isReferencesPriv) gl.add("REFERENCES")
        if (isAlterPriv) gl.add("ALTER")
        if (isIndexPriv) gl.add("INDEX")
        var res = "GRANT "
        var first = true
        for (gs in gl) {
            if (first) first = false else res += ", "
            res += gs
        }
        res += " ON " + parent.name + " to " + ReplaceManager.getManager("usersroles").getSubstitute(grantee) + ";\n"
        return res
    }

    fun sqlUpdate(parent: Table): String {
        return sqlCreate(parent)
    }
}
