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
package com.khodev.oradiff.util

import com.khodev.oradiff.dbobjects.*
import com.khodev.oradiff.diff.*
import java.io.*

class DiffExporter(
    private val initialSchema: Schema?,
    private val finalSchema: Schema?,
    private var destFolder: String
) : DiffListener {
    private var fAll: PrintWriter? = null
    private val manager = DiffManager()

    init {
        manager.addDiffListener(this)
    }

    private fun getFileName(diff: Operation, obj: ObjectType, name: String?): String {
        var diffStr: String
        diffStr = when (diff) {
            Operation.CREATE -> "create"
            Operation.UPDATE -> "update"
            Operation.DROP -> "drop"
        }
        val objStr: String
        when (obj) {
            ObjectType.TABLE -> {
                objStr = "table"
                if (diff == Operation.UPDATE) diffStr = "alter"
            }

            ObjectType.PACKAGE -> objStr = "package"
            ObjectType.PROCEDURE -> objStr = "procedure"
            ObjectType.FUNCTION -> objStr = "function"
            ObjectType.JOB -> objStr = "job"
            ObjectType.SEQUENCE -> objStr = "sequence"
            ObjectType.VIEW -> objStr = "view"
            ObjectType.TRIGGER -> objStr = "trigger"
            ObjectType.SYNONYM -> objStr = "synonym"
            ObjectType.PUBLIC_SYNONYM -> objStr = "public_synonym"
        }
        val folder = destFolder + "\\" + objStr
        val dest = File(folder)
        if (!dest.exists()) dest.mkdir()
        return folder + "\\" + diffStr + "_" + objStr + "_" + name + ".sql"
    }

    override fun diffPerformed(
        diffOptions: DiffOptions,
        diffType: Operation,
        diffObject: ObjectType,
        diff: DBObjectDiff<*>
    ) {
        try {
            var f = PrintWriter(
                BufferedOutputStream(
                    FileOutputStream(
                        getFileName(diffType, diffObject, diff.getName())
                    )
                )
            )
            f.write(sqlCreate(diff, diffOptions))
            f.close()
            fAll!!.write(sqlCreate(diff, diffOptions))
            if (diff.src != null && Configuration.createOldNew) {
                f = PrintWriter(
                    BufferedOutputStream(
                        FileOutputStream(
                            getFileName(
                                diffType, diffObject,
                                diff.getName()
                            ) + "_old"
                        )
                    )
                )
                val create = getSqlCreate(diff.src, diffOptions)
                f.write(create)
                f.close()
            }
            if (diff.dst != null && Configuration.createOldNew) {
                f = PrintWriter(
                    BufferedOutputStream(
                        FileOutputStream(
                            getFileName(
                                diffType, diffObject,
                                diff.getName()
                            ) + "_new"
                        )
                    )
                )
                f.write(getSqlCreate(diff.dst, diffOptions))
                f.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun <T> sqlDrop(o: T): String {
        return when (o) {
            is Table -> "drop ${o.typeName} ${o.name};"
            is DBPackage -> "drop package ${o.name};"
            is Procedure -> "drop procedure ${o.name};"
            is com.khodev.oradiff.dbobjects.Function -> "drop function ${o.name};"
            is Job -> "drop job ${o.name};"
            is Sequence -> "drop sequence ${o.name};"
            is View -> "drop view ${o.name};"
            is Trigger -> "drop trigger ${o.name};"
            is Synonym -> "drop synonym ${o.name};"
            is PublicSynonym -> "drop public synonym ${o.name};"
            else -> throw RuntimeException("Unknown object type")
        }
    }


    fun sqlCreate(diff: DBObjectDiff<*>, diffOptions: DiffOptions): String {
        return if (diff.src == null)
            getSqlCreate(diff.dst!!, diffOptions)
        else
            if (diff.dst == null)
                sqlDrop(diff.src)
            else
                getSqlUpdate(diff.src, diff.dst, diffOptions)
    }

    private fun <T> getSqlUpdate(src: T, dst: T, diffOptions: DiffOptions): String {
        return when {
            src is Table && dst is Table -> src.sqlUpdate(diffOptions, dst)
            src is DBPackage && dst is DBPackage -> src.sqlUpdate(dst)
            src is Procedure && dst is Procedure -> src.sqlUpdate(dst)
            src is com.khodev.oradiff.dbobjects.Function && dst is com.khodev.oradiff.dbobjects.Function -> src.sqlUpdate(
                dst
            )

            src is Job && dst is Job -> src.sqlUpdate(dst)
            src is Sequence && dst is Sequence -> src.sqlUpdate(dst)
            src is View && dst is View -> src.sqlUpdate(dst)
            src is Trigger && dst is Trigger -> src.sqlUpdate(dst)
            src is Synonym && dst is Synonym -> src.sqlUpdate()
            src is PublicSynonym && dst is PublicSynonym -> src.sqlUpdate()
            else -> throw Exception("Unknown object type")
        }
    }

    private fun <T> getSqlCreate(o: T, diffOptions: DiffOptions): String {
        return when (o) {
            is Table -> o.sqlCreate(diffOptions)
            is DBPackage -> o.sqlCreate()
            is Procedure -> o.sqlCreate()
            is com.khodev.oradiff.dbobjects.Function -> o.sqlCreate()
            is Job -> o.sqlCreate()
            is Sequence -> o.sqlCreate()
            is View -> o.sqlCreate()
            is Trigger -> o.sqlCreate()
            is Synonym -> o.sqlCreate()
            is PublicSynonym -> o.sqlCreate()
            else -> throw Exception("Unknown object type")
        }
    }

    @Throws(IOException::class)
    fun run(diffOptions: DiffOptions) {
        var dest = File(destFolder)
        var i = 0
        var newName = destFolder
        if (Configuration.renameFolderIfExists) {
            while (dest.exists()) {
                newName = destFolder + "(" + ++i + ")"
                dest = File(newName)
            }
            if (destFolder != newName) println(
                "Folder " + destFolder
                        + " already exists, using " + newName
            )
        }
        destFolder = newName
        dest.mkdir()
        fAll = PrintWriter(File("$destFolder\\all_data.sql"))
        manager.doPatch(diffOptions, initialSchema, finalSchema)
        fAll!!.close()
    }


}
