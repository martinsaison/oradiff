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
            else -> "unknown"
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
            else -> objStr = "unknown"
        }
        val folder = destFolder + "\\" + objStr
        val dest = File(folder)
        if (!dest.exists()) dest.mkdir()
        return folder + "\\" + diffStr + "_" + objStr + "_" + name + ".sql"
    }

    override fun diffPerformed(diffOptions: DiffOptions, diffType: Operation, diffObject: ObjectType, name: String, diff: DBObjectDiff<*>) {
        try {
            var f = PrintWriter(
                BufferedOutputStream(
                    FileOutputStream(
                        getFileName(diffType, diffObject, name)
                    )
                )
            )
            f.write(diff.sqlCreate(diffOptions))
            f.close()
            fAll!!.write(diff.sqlCreate(diffOptions))
            if (diff.src != null && Configuration.createOldNew) {
                f = PrintWriter(
                    BufferedOutputStream(
                        FileOutputStream(
                            getFileName(
                                diffType, diffObject,
                                name
                            ) + "_old"
                        )
                    )
                )
                f.write(diff.src.sqlCreate(diffOptions))
                f.close()
            }
            if (diff.dst != null && Configuration.createOldNew) {
                f = PrintWriter(
                    BufferedOutputStream(
                        FileOutputStream(
                            getFileName(
                                diffType, diffObject,
                                name
                            ) + "_new"
                        )
                    )
                )
                f.write(diff.dst.sqlCreate(diffOptions))
                f.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
