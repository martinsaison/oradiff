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

import com.khodev.oradiff.diff.DBObjectDiff
import com.khodev.oradiff.diff.DiffOptions
import com.khodev.oradiff.diff.Equivalence
import java.io.IOException
import java.util.*

class Schema(
    val dbFunctions: Map<String, Function>,
    private val dbJobs: Map<String, Job>,
    val dbPackages: Map<String, DBPackage>,
    val dbProcedures: Map<String, Procedure>,
    val dbSequences: Map<String, Sequence>,
    private val dbSynonyms: Map<String, Synonym>,
    val dbTables: Map<String, Table>,
    val dbTriggers: Map<String, Trigger>,
    val dbViews: Map<String, View>,

    ) {

    private fun getFunctionByName(name: String?): Function? {
        return dbFunctions[name]
    }

    private fun getJobByName(name: String?): Job? {
        return dbJobs[name]
    }

    private fun getPackageByName(name: String?): DBPackage? {
        return dbPackages[name]
    }

    private fun getProcedureByName(name: String?): Procedure? {
        return dbProcedures[name]
    }

    private fun getSequenceByName(name: String?): Sequence? {
        return dbSequences[name]
    }

    private fun getSynonymByName(name: String?): Synonym? {
        return dbSynonyms[name]
    }

    private fun getTriggerByName(name: String?): Trigger? {
        return dbTriggers[name]
    }

    private fun getViewByName(name: String?): View? {
        return dbViews[name]
    }

    fun newFunctions(destination: Schema?): ArrayList<Function> {
        val res = ArrayList<Function>()
        for (dst in destination!!.dbFunctions.values) {
            if (getFunctionByName(dst.name) == null) res.add(dst)
        }
        return res
    }

    fun newJobs(destination: Schema?): ArrayList<Job> {
        val res = ArrayList<Job>()
        for (dst in destination!!.dbJobs.values) {
            if (getJobByName(dst.name) == null) res.add(dst)
        }
        return res
    }

    fun newPackages(destination: Schema?): ArrayList<DBPackage> {
        val res = ArrayList<DBPackage>()
        for (dst in destination!!.dbPackages.values) {
            if (getPackageByName(dst.name) == null) res.add(dst)
        }
        return res
    }

    fun newProcedures(destination: Schema?): ArrayList<Procedure> {
        val res = ArrayList<Procedure>()
        for (dst in destination!!.dbProcedures.values) {
            if (getProcedureByName(dst.name) == null) res.add(dst)
        }
        return res
    }

    fun newSequences(destination: Schema?): ArrayList<Sequence> {
        val res = ArrayList<Sequence>()
        for (dst in destination!!.dbSequences.values) {
            if (getSequenceByName(dst.name) == null) res.add(dst)
        }
        return res
    }

    fun newSynonyms(destination: Schema?): ArrayList<Synonym> {
        val res = ArrayList<Synonym>()
        for (dst in destination!!.dbSynonyms.values) {
            if (getSynonymByName(dst.name) == null) res.add(dst)
        }
        return res
    }

    // returns tables which are in destination schema but not in current schema
    @Throws(IOException::class)
    fun newTables(destination: Schema?): ArrayList<Table> {
        val res = ArrayList<Table>()
        for (dst in destination!!.dbTables.values) {
            var existsHere = false
            for (src in dbTables.values) {
                if (Equivalence.matches(src.name, dst.name)) {
                    existsHere = true
                    break
                }
            }
            if (!existsHere) res.add(dst)
        }
        return res
    }

    fun newTriggers(destination: Schema?): ArrayList<Trigger> {
        val res = ArrayList<Trigger>()
        for (dst in destination!!.dbTriggers.values) {
            if (getTriggerByName(dst.name) == null) res.add(dst)
        }
        return res
    }

    fun newViews(destination: Schema?): ArrayList<View> {
        val res = ArrayList<View>()
        for (dst in destination!!.dbViews.values) {
            if (getViewByName(dst.name) == null) res.add(dst)
        }
        return res
    }

    fun updateFunctions(diffOptions: DiffOptions, destination: Schema?): ArrayList<DBObjectDiff<Function>> {
        val res = ArrayList<DBObjectDiff<Function>>()
        for (dst in destination!!.dbFunctions.values) {
            val src = getFunctionByName(dst.name)
            if (src != null) if (!src.dbEquals(diffOptions, dst)) res.add(DBObjectDiff(src, dst))
        }
        return res
    }

    fun updateJobs(destination: Schema?): ArrayList<DBObjectDiff<Job>> {
        val res = ArrayList<DBObjectDiff<Job>>()
        for (dst in destination!!.dbJobs.values) {
            val src = getJobByName(dst.name)
            if (src != null) if (!src.dbEquals(dst)) res.add(DBObjectDiff(src, dst))
        }
        return res
    }

    fun updatePackages(diffOptions: DiffOptions, destination: Schema?): ArrayList<DBObjectDiff<DBPackage>> {
        val res = ArrayList<DBObjectDiff<DBPackage>>()
        for (dst in destination!!.dbPackages.values) {
            // research in current table
            val src = getPackageByName(dst.name)
            if (src != null) if (!src.dbEquals(diffOptions, dst)) res.add(DBObjectDiff(src, dst))
        }
        return res
    }

    fun updateProcedures(diffOptions: DiffOptions, destination: Schema?): ArrayList<DBObjectDiff<Procedure>> {
        val res = ArrayList<DBObjectDiff<Procedure>>()
        for (dst in destination!!.dbProcedures.values) {
            val src = getProcedureByName(dst.name)
            if (src != null) if (!src.dbEquals(diffOptions, dst)) res.add(DBObjectDiff(src, dst))
        }
        return res
    }

    fun updateSequences(destination: Schema?): ArrayList<DBObjectDiff<Sequence>> {
        val res = ArrayList<DBObjectDiff<Sequence>>()
        for (dst in destination!!.dbSequences.values) {
            val src = getSequenceByName(dst.name)
            if (src != null) if (!src.dbEquals(dst)) res.add(DBObjectDiff(src, dst))
        }
        return res
    }

    fun updateSynonyms(destination: Schema?): ArrayList<DBObjectDiff<Synonym>> {
        val res = ArrayList<DBObjectDiff<Synonym>>()
        for (dst in destination!!.dbSynonyms.values) {
            val src = getSynonymByName(dst.name)
            if (src != null) if (!src.dbEquals()) res.add(DBObjectDiff(src, dst))
        }
        return res
    }

    @Throws(IOException::class)
    fun updateTables(diffOptions: DiffOptions, destination: Schema?): ArrayList<DBObjectDiff<Table>> {
        val res = ArrayList<DBObjectDiff<Table>>()
        for (dst in destination!!.dbTables.values) {
            // boolean existsHere = false;
            for (src in dbTables.values) {
                if (Equivalence.matches(src.name, dst.name)) {
                    // existsHere = true;
                    if (!src.dbEquals(diffOptions, dst)) // exists and changed
                        res.add(DBObjectDiff(src, dst))
                }
            }
        }
        return res
    }

    fun updateTriggers(destination: Schema?): ArrayList<DBObjectDiff<Trigger>> {
        val res = ArrayList<DBObjectDiff<Trigger>>()
        for (dst in destination!!.dbTriggers.values) {
            val src = getTriggerByName(dst.name)
            if (src != null) if (!src.dbEquals(dst)) res.add(DBObjectDiff(src, dst))
        }
        return res
    }

    fun updateViews(destination: Schema?): ArrayList<DBObjectDiff<View>> {
        val res = ArrayList<DBObjectDiff<View>>()
        for (dst in destination!!.dbViews.values) {
            val src = getViewByName(dst.name)
            if (src != null) if (!src.dbEquals(dst)) res.add(DBObjectDiff(src, dst))
        }
        return res
    }

}
