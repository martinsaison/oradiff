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

class Table(
    val owner: String, val name: String, val tablespace: String, val comments: String?
)  {
    val columns = ArrayList<Column>()
    val indexes = ArrayList<Index>()
    val constraints = ArrayList<Constraint>()
    val grants = ArrayList<Grant>()
    val publicSynonyms = ArrayList<PublicSynonym>()

    private fun columnChanges(diffOptions: DiffOptions, destination: Table): String {
        return (sqlNewColumns(destination) + sqlDropColumns(destination) + sqlAlterColumns(
            diffOptions,
            destination
        ))
    }

    private fun getColumnsComments(columns: ArrayList<Column>): String {
        var res = ""
        for (column in columns) {
            res += column.sqlComments(name)
        }
        return res
    }

    private fun indexChanges(diffOptions: DiffOptions, destination: Table): String {
        return (sqlNewIndexes(diffOptions, destination) + sqlDropIndexes(destination) + sqlAlterIndexes(
            diffOptions,
            destination
        ))
    }

    private fun newIndexes(destination: Table): ArrayList<Index> {
        val res = ArrayList<Index>()
        for (dst in destination.indexes) {
            var found = false
            for (src in indexes) {
                if (src.name == dst.name) {
                    found = true
                    break
                }
            }
            if (found) continue
            res.add(dst)
        }
        return res
    }

    fun escape(s: String?): String {
        return s?.replace("'", "''") ?: ""
    }

    private fun sqlAlterColumns(diffOptions: DiffOptions, destination: Table): String {
        var res = ""
        // columns diffs
        val columnsDiffs = ArrayList<Column?>()
        for (dst in destination.columns) {
            // research in current table
            val src = getColumnByName(dst.name)
            if (src != null) {
                if (!src.dbEquals(dst)) columnsDiffs.add(dst)
            }
        }
        if (columnsDiffs.size > 0) {
            res += """alter table $name modify
("""
            var first = true
            for (dst in columnsDiffs) {
                if (first) first = false else res += ", "
                res += """
  ${dst!!.sqlCreate()}"""
            }
            res += "\n);\n"
        }
        // comments diffs
        if (!diffOptions.ignoreObjectComments) {
            for (dst in destination.columns) {
                // research in current table
                val src = getColumnByName(dst.name)
                if (src != null) {
                    if (trimLines(src.comment) != trimLines(dst.comment)) {
                        res += dst.sqlComments(name)
                    }
                }
            }
        }
        return res
    }

    private fun sqlAlterIndexes(diffOptions: DiffOptions, destination: Table): String {
        var res = ""
        // index diffs
        for (dst in destination.indexes) {
            // research in current table
            val src = getIndexByName(dst.name)
            if (src != null) {
                if (!src.dbEquals(dst)) {
                    res += src.sqlDrop()
                    res += dst.sqlCreate(diffOptions)
                }
            }
        }
        return res
    }

    fun escapeName(name: String): String {
        return "\"" + name + "\""
    }

    private fun sqlDropColumns(destination: Table): String {
        var res = ""
        val columnsToDrop = destination.newColumns(this)
        for (column in columnsToDrop) {
            res += ("alter table " + escapeName(name) + " drop column " + escapeName(column.name) + ";\n")
        }
        return res
    }

    private fun sqlDropIndexes(destination: Table): String {
        var res = ""
        val indexesToDrop = destination.newIndexes(this)
        for (index in indexesToDrop) {
            res += index.sqlDrop()
        }
        return res
    }

    private fun sqlNewColumns(destination: Table): String {
        var res = ""
        val columnsToAdd = newColumns(destination)
        if (columnsToAdd.size > 0) {
            res += "alter table " + escapeName(name) + " add ("
            var first = true
            for (column in columnsToAdd) {
                if (first) {
                    first = false
                } else {
                    res += ","
                }
                res += """
  ${column.sqlCreate()}"""
            }
            res += "\n);\n"
            // new column comments
            res += getColumnsComments(columnsToAdd)
        }
        return res
    }

    private fun sqlNewIndexes(diffOptions: DiffOptions, destination: Table): String {
        var res = ""
        val indexesToAdd = newIndexes(destination)
        for (index in indexesToAdd) {
            res += index.sqlCreate(diffOptions)
        }
        return res
    }

    private fun trimLines(txt: String?): String {
        var res = ""
        for (line in txt!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val line1 = line.trim { it <= ' ' }
            if (line1.isNotEmpty()) res += line1 + "\n"
        }
        return res
    }

    fun dbEquals(diffOptions: DiffOptions, dst: Table): Boolean {
        return sqlUpdate(diffOptions, dst).isEmpty()
    }

    fun getColumnByName(name: String?): Column? {
        for (c in columns) {
            if (c.name == name) return c
        }
        return null
    }

    fun getIndexByName(name: String?): Index? {
        for (c in indexes) {
            if (c.name == name) return c
        }
        return null
    }

    val typeName: String
        get() = "TABLE"

    // returns columns which are in destination table but not in current table
    private fun newColumns(destination: Table): ArrayList<Column> {
        val res = ArrayList<Column>()
        for (dst in destination.columns) {
            var found = false
            for (src in columns) {
                if (src.name == dst.name) {
                    found = true
                    break
                }
            }
            if (found) continue
            res.add(dst)
        }
        return res
    }

     fun sqlCreate(diffOptions: DiffOptions): String {
        // table
        var res: String = "create table " + escapeName(name) + " ("
        var first = true
        for (column in columns) {
            if (first) first = false else res += ","
            res += """
  ${column.sqlCreate()}"""
        }
        res += """
            
            )${tablespaceSql(diffOptions)};
            
            """.trimIndent()
        // table comments
        if (comments != null) res += """comment on table $name
  is '${escape(comments)}';
"""
        // column comments
        res += getColumnsComments(columns)
        // indexes
        if (indexes.size > 0) {
            for (index in indexes) {
                res += index.sqlCreate(diffOptions)
            }
        }
        // grants
        if ((!diffOptions.ignoreGrantChanges) && grants.size > 0) {
            for (grant in grants) {
                res += grant.sqlCreate(this)
            }
        }
        return res
    }

     fun sqlUpdate(diffOptions: DiffOptions, destination: Table): String {
        var res = ""
         res += columnChanges(diffOptions, destination)
        res += indexChanges(diffOptions, destination)
        if (destination.name != name) {
            res += ("ALTER TABLE " + name + " RENAME TO " + destination.name + ";")
        }
        return res
    }

    fun tablespaceSql(diffOptions: DiffOptions): String {
        return if (diffOptions.withTablespace && tablespace.isNotEmpty()) " tablespace $tablespace" else ""
    }

}
