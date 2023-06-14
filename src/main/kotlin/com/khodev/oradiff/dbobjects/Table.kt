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
    owner: String, tableName: String, tablespace: String,
    var comments: String?
) : TablespaceObject(owner, tableName, tablespace) {
    var columns = ArrayList<Column>()
    var indexes = ArrayList<Index>()
    var constraints = ArrayList<Constraint>()
    var grants = ArrayList<Grant>()
    var publicSynonyms = ArrayList<PublicSynonym>()

    private fun columnChanges(diffOptions: DiffOptions, destination: Table): String {
        return (sqlNewColumns(diffOptions, destination) + sqlDropColumns(destination)
                + sqlAlterColumns(diffOptions, destination))
    }

    private fun getColumnsComments(columns: ArrayList<Column>): String {
        var res = ""
        for (column in columns) {
            res += column.sqlComments(name)
        }
        return res
    }

    private fun indexChanges(diffOptions: DiffOptions, destination: Table): String {
        return (sqlNewIndexes(diffOptions, destination) + sqlDropIndexes(destination)
                + sqlAlterIndexes(diffOptions, destination))
    }

    private fun newIndexes(destination: Table): ArrayList<Index> {
        return newObjects(indexes, destination.indexes)
    }

    private fun sqlAlterColumns(diffOptions: DiffOptions, destination: Table): String {
        var res = ""
        // columns diffs
        val columnsDiffs = ArrayList<Column?>()
        for (dst in destination.columns) {
            // research in current table
            val src = getColumnByName(dst.name)
            if (src != null) {
                if (!src.dbEquals(diffOptions, dst)) columnsDiffs.add(dst)
            }
        }
        if (columnsDiffs.size > 0) {
            res += """alter table $name modify
("""
            var first = true
            for (dst in columnsDiffs) {
                if (first) first = false else res += ", "
                res += """
  ${dst!!.sqlCreate(diffOptions)}"""
            }
            res += "\n);\n"
        }
        // comments diffs
        if (!diffOptions.ignoreObjectComments) {
            for (dst in destination.columns) {
                // research in current table
                val src = getColumnByName(dst.name)
                if (src != null) {
                    if (trimLines(src.comment) != trimLines(dst.comment)
                    ) {
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

    private fun sqlDropColumns(destination: Table): String {
        var res = ""
        val columnsToDrop = destination.newColumns(this)
        for (column in columnsToDrop) {
            res += ("alter table " + escapeName(name) + " drop column "
                    + escapeName(column.name) + ";\n")
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

    private fun sqlNewColumns(diffOptions: DiffOptions, destination: Table): String {
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
  ${column.sqlCreate(diffOptions)}"""
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

    override val typeName: String
        get() = "TABLE"

    // returns columns which are in destination table but not in current table
    private fun newColumns(destination: Table): ArrayList<Column> {
        return newObjects(columns, destination.columns)
    }

    override fun sqlCreate(diffOptions: DiffOptions): String {
        // table
        var res: String = "create table " + escapeName(name) + " ("
        var first = true
        for (column in columns) {
            if (first) first = false else res += ","
            res += """
  ${column.sqlCreate(diffOptions)}"""
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
                res += grant.sqlCreate(diffOptions)
            }
        }
        return res
    }

    override fun sqlUpdate(diffOptions: DiffOptions, destination: DBObject): String {
        var res = ""
        val dst = destination as Table
        res += columnChanges(diffOptions, dst)
        res += indexChanges(diffOptions, dst)
        if (dst.name != name) {
            res += ("ALTER TABLE " + name + " RENAME TO " + dst.name
                    + ";")
        }
        return res
    }
}
