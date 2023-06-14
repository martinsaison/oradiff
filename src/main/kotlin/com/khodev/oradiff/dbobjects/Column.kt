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

class Column(
    val name: String,
    val id: Int,
    val type: String,
    val length: Int,
    val precision: Int,
    val scale: Int,
    val isNullable: Boolean,
    val comment: String,
    val defaultValue: String
)  {

    fun escapeName(name: String): String {
        return "\"" + name + "\""
    }

    fun sqlCreate(): String {
        var res = escapeName(name) + " " + typeAsSql()
        if (!isNullable) res += " not null"
        if (defaultValue.isNotEmpty()) res += " default $defaultValue"
        return res
    }

    private fun typeAsSql(): String {
        return when (type) {
            "NUMBER" -> if (precision == 0) "NUMBER" else if (scale == 0) "NUMBER($precision)" else "NUMBER($precision, $scale)"
            "CHAR" -> "CHAR($length)"
            "VARCHAR2" -> "VARCHAR2($length)"
            "DATE" -> "DATE"
            "LONG" -> "LONG"
            "ROWID" -> "ROWID"
            "RAW" -> "RAW($length)"
            "LONG RAW" -> "LONG RAW"
            else -> "UNKNOWN"
        }
    }

    fun dbEquals(column: Column): Boolean {
        return type == column.type
                && length == column.length
                && precision == column.precision
                && scale == column.scale
                && isNullable == column.isNullable
                && defaultValue.trim { it <= ' ' } == column.defaultValue.trim { it <= ' ' }
    }

    fun sqlComments(tablename: String?): String {
        var res = ""
        res += """comment on column $tablename.$name
  is '${escape(comment)}';
"""
        return res
    }

    fun escape(s: String?): String {
        return s?.replace("'", "''") ?: ""
    }

    val typeName: String
        get() = "COLUMN"

    fun sqlUpdate(): String {
        return sqlCreate()
    }
}
