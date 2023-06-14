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
package com.khodev.oradiff.diff

import com.khodev.oradiff.dbobjects.*
import java.io.IOException

class DiffManager {
    private val diffListeners = ArrayList<DiffListener>()
    fun addDiffListener(diffListener: DiffListener) {
        diffListeners.add(diffListener)
    }

    private fun <T> doDiff(
        diffOptions: DiffOptions,
        diffType: Operation, diffObject: ObjectType,
        diff: DBObjectDiff<T>
    ) {
        for (diffListener in diffListeners) diffListener.diffPerformed(diffOptions, diffType, diffObject, diff)
    }

    @Throws(IOException::class)
    fun doPatch(diffOptions: DiffOptions, initialSchema: Schema?, finalSchema: Schema?) {
        // Tables
        for (o in initialSchema!!.newTables(finalSchema)) {
            doDiff(
                diffOptions,
                Operation.CREATE, ObjectType.TABLE,
                DBObjectDiff(null, o)
            )
            for (s in o.publicSynonyms) {
                doDiff(
                    diffOptions,
                    Operation.CREATE, ObjectType.PUBLIC_SYNONYM,
                    DBObjectDiff(null, s)
                )
            }
        }
        for (d in initialSchema.updateTables(diffOptions, finalSchema)) {
            doDiff(diffOptions, Operation.UPDATE, ObjectType.TABLE, d)
        }
        for (o in finalSchema!!.newTables(initialSchema)) {
            doDiff(
                diffOptions,
                Operation.DROP, ObjectType.TABLE, DBObjectDiff(
                    o, null
                )
            )
            for (s in o.publicSynonyms) {
                doDiff(
                    diffOptions,
                    Operation.DROP, ObjectType.PUBLIC_SYNONYM,
                    DBObjectDiff(s, null)
                )
            }
        }
        // Packages
        for (o in initialSchema.newPackages(finalSchema)) {
            doDiff(
                diffOptions,
                Operation.CREATE, ObjectType.PACKAGE,
                DBObjectDiff(null, o)
            )
        }
        for (d in initialSchema
            .updatePackages(diffOptions, finalSchema)) {
            doDiff(diffOptions, Operation.UPDATE, ObjectType.PACKAGE, d)
        }
        for (o in finalSchema.newPackages(initialSchema)) {
            doDiff(
                diffOptions,
                Operation.DROP, ObjectType.PACKAGE,
                DBObjectDiff(o, null)
            )
        }
        // Procedures
        for (o in initialSchema.newProcedures(finalSchema)) {
            doDiff(
                diffOptions,
                Operation.CREATE, ObjectType.PROCEDURE,
                DBObjectDiff(null, o)
            )
        }
        for (d in initialSchema
            .updateProcedures(diffOptions, finalSchema)) {
            doDiff(diffOptions, Operation.UPDATE, ObjectType.PROCEDURE, d)
        }
        for (o in finalSchema.newProcedures(initialSchema)) {
            doDiff(
                diffOptions,
                Operation.DROP, ObjectType.PROCEDURE,
                DBObjectDiff(o, null)
            )
        }
        // Functions
        for (o in initialSchema.newFunctions(finalSchema)) {
            doDiff(
                diffOptions,
                Operation.CREATE, ObjectType.FUNCTION,
                DBObjectDiff(null, o)
            )
        }
        for (d in initialSchema
            .updateFunctions(diffOptions, finalSchema)) {
            doDiff(diffOptions, Operation.UPDATE, ObjectType.FUNCTION, d)
        }
        for (o in finalSchema.newFunctions(initialSchema)) {
            doDiff(
                diffOptions,
                Operation.DROP, ObjectType.FUNCTION,
                DBObjectDiff(o, null)
            )
        }

        // Jobs
        for (o in initialSchema.newJobs(finalSchema)) {
            doDiff(
                diffOptions,
                Operation.CREATE, ObjectType.JOB, DBObjectDiff(
                    null,
                    o
                )
            )
        }
        for (d in initialSchema.updateJobs(finalSchema)) {
            doDiff(
                diffOptions,
                Operation.UPDATE, ObjectType.JOB,
                d
            )
        }
        for (o in finalSchema.newJobs(initialSchema)) {
            doDiff(
                diffOptions,
                Operation.DROP, ObjectType.JOB,
                DBObjectDiff(o, null)
            )
        }

        // Sequences
        for (o in initialSchema.newSequences(finalSchema)) {
            doDiff(
                diffOptions,
                Operation.CREATE, ObjectType.SEQUENCE,
                DBObjectDiff(null, o)
            )
        }
        for (d in initialSchema
            .updateSequences(finalSchema)) {
            doDiff(diffOptions, Operation.UPDATE, ObjectType.SEQUENCE, d)
        }
        for (o in finalSchema.newSequences(initialSchema)) {
            doDiff(
                diffOptions,
                Operation.DROP, ObjectType.SEQUENCE,
                DBObjectDiff(o, null)
            )
        }
        // Triggers
        for (o in initialSchema.newTriggers(finalSchema)) {
            doDiff(
                diffOptions,
                Operation.CREATE, ObjectType.TRIGGER,
                DBObjectDiff(null, o)
            )
        }
        for (d in initialSchema
            .updateTriggers(finalSchema)) {
            doDiff(diffOptions, Operation.UPDATE, ObjectType.TRIGGER, d)
        }
        for (o in finalSchema.newTriggers(initialSchema)) {
            doDiff(
                diffOptions, Operation.DROP, ObjectType.TRIGGER,
                DBObjectDiff(o, null)
            )
        }
        // Synonyms
        for (o in initialSchema.newSynonyms(finalSchema)) {
            doDiff(
                diffOptions, Operation.CREATE, ObjectType.SYNONYM,
                DBObjectDiff(null, o)
            )
        }
        for (d in initialSchema
            .updateSynonyms(finalSchema)) {
            doDiff(diffOptions, Operation.UPDATE, ObjectType.SYNONYM, d)
        }
        for (o in finalSchema.newSynonyms(initialSchema)) {
            doDiff(
                diffOptions, Operation.DROP, ObjectType.SYNONYM,
                DBObjectDiff(o, null)
            )
        }
        // Views
        for (o in initialSchema.newViews(finalSchema)) {
            doDiff(
                diffOptions, Operation.CREATE, ObjectType.VIEW, DBObjectDiff(
                    null, o
                )
            )
        }
        for (d in initialSchema.updateViews(finalSchema)) {
            doDiff(diffOptions, Operation.UPDATE, ObjectType.VIEW, d)
        }
        for (o in finalSchema.newViews(initialSchema)) {
            doDiff(
                diffOptions, Operation.DROP, ObjectType.VIEW, DBObjectDiff(
                    o,
                    null
                )
            )
        }
    }
}
