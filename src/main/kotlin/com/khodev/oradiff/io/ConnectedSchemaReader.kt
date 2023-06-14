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
package com.khodev.oradiff.io

import com.khodev.oradiff.dbobjects.*
import com.khodev.oradiff.dbobjects.Function
import com.khodev.oradiff.util.OracleConnection
import java.io.*
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class ConnectedSchemaReader : SchemaReader {
    private var host: String? = null
    private var port: String? = null
    private var sid: String? = null
    private var user: String? = null
    private var pass: String? = null
    private var tableFilter: String? = null
    private var packageFilter: String? = null
    private var functionFilter: String? = null
    private var procedureFilter: String? = null
    private var viewFilter: String? = null
    private var sequenceFilter: String? = null
    private var triggerFilter: String? = null

    constructor(filename: String?) {
        val b: BufferedReader
        var line: String
        tableFilter = null
        packageFilter = null
        functionFilter = null
        procedureFilter = null
        viewFilter = null
        sequenceFilter = null
        triggerFilter = null
        b = BufferedReader(FileReader(filename))
        while (b.readLine().also { line = it } != null) {
            line = line.trim { it <= ' ' }
            if (line.length == 0) continue
            val vars = line.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val `var` = vars[0].trim { it <= ' ' }.lowercase(Locale.getDefault())
            val `val` = vars[1].trim { it <= ' ' }
            when (`var`) {
                "host" -> host = `val`
                "port" -> port = `val`
                "sid" -> sid = `val`
                "user" -> user = `val`
                "pass" -> pass = `val`
                "tablefilter" -> tableFilter = `val`
                "packagefilter" -> packageFilter = `val`
                "functionfilter" -> functionFilter = `val`
                "procedurefilter" -> procedureFilter = `val`
                "viewfilter" -> viewFilter = `val`
                "sequencefilter" -> sequenceFilter = `val`
                "triggerfilter" -> triggerFilter = `val`
            }
        }
        b.close()
    }

    constructor(host: String?, port: String?, sid: String?, user: String?, pass: String?) {
        this.host = host
        this.port = port
        this.sid = sid
        this.user = user
        this.pass = pass
    }

    override fun get(): Schema {
        val schema = Schema()
        try {
            val con = OracleConnection(
                host, port, sid, user, pass, tableFilter,
                packageFilter, functionFilter, procedureFilter, viewFilter,
                sequenceFilter, triggerFilter
            )
            loadSchema(con, schema)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return schema
    }

    @Throws(SQLException::class, UnsupportedEncodingException::class)
    private fun loadSchema(c: OracleConnection, schema: Schema) {
        print("loading tables...")
        loadTables(c, schema)
        println("done.")
        print("loading views...")
        loadViews(c, schema)
        println("done.")
        print("loading synonyms...")
        loadSynonyms(c, schema)
        println("done.")
        print("loading sequences...")
        loadSequences(c, schema)
        println("done.")
        print("loading sources...")
        loadSources(c, schema)
        println("done.")
        print("loading jobs...")
        loadJobs(c, schema)
        println("done.")
        print("loading triggers...")
        loadTriggers(c, schema)
        println("done.")
        // cleaning
        print("cleaning tables...")
        Schema.Companion.cleanObjectsWithFilter<Table>(c.tableFilter, schema.dbTables)
        println("done.")
        print("cleaning packages...")
        Schema.Companion.cleanObjectsWithFilter<DBPackage>(c.packageFilter, schema.dbPackages)
        println("done.")
        print("cleaning functions...")
        Schema.Companion.cleanObjectsWithFilter<Function>(c.functionFilter, schema.dbFunctions)
        println("done.")
        print("cleaning procedures...")
        Schema.Companion.cleanObjectsWithFilter<Procedure>(c.procedureFilter, schema.dbProcedures)
        println("done.")
        print("cleaning views...")
        Schema.Companion.cleanObjectsWithFilter<View>(c.viewFilter, schema.dbViews)
        println("done.")
        print("cleaning sequences...")
        Schema.Companion.cleanObjectsWithFilter<Sequence>(c.sequenceFilter, schema.dbSequences)
        println("done.")
        print("cleaning triggers...")
        Schema.Companion.cleanObjectsWithFilter<Trigger>(c.triggerFilter, schema.dbTriggers)
        println("done.")
    }

    @Throws(SQLException::class, UnsupportedEncodingException::class)
    private fun loadJobs(c: OracleConnection, schema: Schema) {
        val r = c
            .query("SELECT job, what, to_char(next_date, 'dd-mm-yyyy hh24:mi:ss'), interval, broken FROM all_jobs WHERE log_user = USER")
        r!!.fetchSize = 10000
        while (r.next()) {
            schema.addJob(
                Job(
                    dbconv(r.getString(1)), dbconv(
                        r.getString(2)
                    ),
                    dbconv(r.getString(3)), dbconv(r.getString(4)), dbconv(
                        r.getString(5)
                    ) == "Y"
                )
            )
        }
    }

    @Throws(SQLException::class, UnsupportedEncodingException::class)
    private fun loadSequences(c: OracleConnection, schema: Schema) {
        val r = c
            .query("SELECT sequence_name, min_value, max_value, increment_by, cycle_flag, order_flag, cache_size, last_number FROM all_sequences WHERE sequence_owner = USER")
        r!!.fetchSize = 10000
        while (r.next()) {
            schema.addSequence(
                Sequence(
                    dbconv(r.getString(1)),
                    dbconv(r.getString(2)), dbconv(r.getString(3)),
                    dbconv(r.getString(4)), dbconv(r.getString(5)) == "Y", dbconv(
                        r.getString(6)
                    ) == "Y", r.getInt(7),
                    dbconv(r.getString(8))
                )
            )
        }
    }

    @Throws(SQLException::class, UnsupportedEncodingException::class)
    private fun loadSources(c: OracleConnection, schema: Schema) {
        val r = c
            .query("SELECT NAME, TYPE, line, text FROM all_source WHERE owner = USER and type IN ('PACKAGE', 'PACKAGE BODY', 'FUNCTION', 'PROCEDURE') ORDER BY NAME, TYPE, line")
        r!!.fetchSize = 10000
        var oname = ""
        var nname: String
        var otype = ""
        var ntype: String
        var line: String
        var dbSource: Source? = null
        while (r.next()) {
            nname = dbconv(r.getString(1))
            ntype = dbconv(r.getString(2))
            line = DBObject.Companion.removeR4(dbconv(r.getString(4)))
            if (nname != oname || ntype != otype) {
                if (!(oname == nname && otype == "PACKAGE" && (ntype
                            == "PACKAGE BODY"))
                ) dbSource = schema.createNewDBSource(nname, ntype)
                oname = nname
                otype = ntype
            }
            if (dbSource == null) {
                System.err.println(nname)
                System.err.println(ntype)
            } else {
                // TRIGGER "C4S01_DEVS"."TRG_ADM_DICTIONARY_PRFUPD" => CREATE OR REPLACE TRIGGER TRG_ADM_DICTIONARY_PRFUPD
                dbSource.append(ntype, r.getInt(3), line)
            }
        }
    }

    @Throws(SQLException::class, UnsupportedEncodingException::class)
    private fun loadSynonyms(c: OracleConnection, schema: Schema) {
        val r: ResultSet?
        var currentSynonym: Synonym
        // Synonyms
        r =
            c.query("SELECT s.owner, s.synonym_name, s.table_owner, s.table_name FROM all_synonyms s WHERE s.owner = USER")
        r.fetchSize = 10000
        while (r.next()) {
            currentSynonym = Synonym(
                dbconv(r.getString(1)), dbconv(r.getString(2)),
                dbconv(r.getString(3)), dbconv(r.getString(4))
            )
            schema.addSynonym(currentSynonym)
        }
    }

    @Throws(SQLException::class, UnsupportedEncodingException::class)
    private fun loadTables(c: OracleConnection, schema: Schema) {
        var r: ResultSet?
        var currentTable: Table?
        val currentColumn: Column? = null
        // Tables
        print(" tables")
        r =
            c.query("SELECT t.owner, t.table_name, t.tablespace_name, c.comments FROM all_tables t LEFT JOIN all_tab_comments c ON c.table_name = t.table_name AND c.owner = t.owner WHERE t.owner = USER")
        r.fetchSize = 10000
        while (r.next()) {
            schema.addTable(
                Table(
                    dbconv(r.getString(1)), dbconv(r.getString(2)),
                    dbconv(r.getString(3)), dbconv(r.getString(4))
                )
            )
        }
        /*
        System.out.print(" tables from synonyms");
        r = c.query("SELECT t.owner, t.table_name, t.tablespace_name, (SELECT comments FROM all_tab_comments c WHERE c.table_name = t.table_name AND c.owner = t.owner) FROM all_tables t, all_synonyms s WHERE s.owner = USER AND s.table_name = t.table_name AND s.table_owner = t.owner");
        r.setFetchSize(10000);
        while (r.next()) {
            schema.addTable(new Table(dbconv(r.getString(1)), dbconv(r.getString(2)),
                    dbconv(r.getString(3)), dbconv(r.getString(4))));
        }
*/
        // columns
        print(" columns")
        r =
            c.query("SELECT t.owner, t.table_name, t.column_name, t.column_id, t.data_type, decode(t.char_length, 0, t.data_length, t.char_length), t.data_precision, t.data_scale, t.nullable, c.comments, t.data_default FROM all_tab_columns t LEFT JOIN all_col_comments c ON c.owner = USER AND c.table_name = t.table_name AND c.column_name = t.column_name WHERE t.owner = USER")
        r.fetchSize = 10000
        while (r.next()) {
            currentTable = schema.getTableByName(dbconv(r.getString(2)))
            currentTable?.columns?.add(
                Column(
                    dbconv(r.getString(3)), r.getInt(4),
                    dbconv(r.getString(5)), r.getInt(6), r
                        .getInt(7), r.getInt(8), dbconv(
                        r.getString(9)
                    ) == "Y", dbconv(
                        r
                            .getString(10)
                    ),
                    dbconv(r.getString(11)),
                    currentTable
                )
            )
        }
        /*
        System.out.print(" columns (tables from synonyms)");
        r = c.query("SELECT t.owner, t.table_name, t.column_name, t.column_id, t.data_type, decode(t.char_length, 0, t.data_length, t.char_length), t.data_precision, t.data_scale, t.nullable, (SELECT comments FROM all_col_comments c WHERE c.owner = USER AND c.table_name = t.table_name AND c.column_name = t.column_name), t.data_default FROM all_tab_columns t, all_synonyms s WHERE s.owner = USER AND t.owner = s.table_owner AND t.table_name = s.table_name");
        r.setFetchSize(10000);
        while (r.next()) {
            currentTable = schema.getTableByName(dbconv(r.getString(2)));
            if (currentTable != null)
                currentTable.getColumns().add(
                        new Column(dbconv(r.getString(3)), r.getInt(4),
                                dbconv(r.getString(5)), r.getInt(6), r
                                .getInt(7), r.getInt(8), dbconv(
                                r.getString(9)).equals("Y"), dbconv(r
                                .getString(10)),
                                dbconv(r.getString(11))));

        }
*/
        // indexes
        print(" indexes")
        r =
            c.query("SELECT owner, table_name, index_name, tablespace_name, index_type, uniqueness, compression FROM all_indexes WHERE owner = USER ORDER BY table_name, index_name")
        r.fetchSize = 10000
        while (r.next()) {
            currentTable = schema.getTableByName(dbconv(r.getString(2)))
            currentTable?.indexes?.add(
                Index(
                    dbconv(r.getString(1)),
                    dbconv(r.getString(3)),
                    dbconv(r.getString(4)),
                    dbconv(r.getString(5)),
                    dbconv(r.getString(6)) == "UNIQUE",
                    dbconv(r.getString(7)),
                    currentTable
                )
            )
        }
        // index columns
        print(" index columns")
        r =
            c.query("SELECT b.table_name, b.index_name, b.column_name, b.column_position, c.column_expression,CASE WHEN c.column_expression IS NULL THEN 0 ELSE 1 END FROM all_ind_columns b, all_ind_expressions c WHERE b.index_owner = USER AND c.index_owner(+) = USER AND c.index_name(+) = b.index_name AND c.column_position(+) = b.column_position AND b.table_name IN (SELECT table_name FROM all_tables) ORDER BY b.table_name, b.index_name, b.column_position")
        r.fetchSize = 10000
        while (r.next()) {
            val table = dbconv(r.getString(1))
            val index = dbconv(r.getString(2))
            var column = dbconv(r.getString(3))
            val position = r.getInt(4)
            val expression = dbconv(r.getString(5))
            val isExpression = r.getInt(6) == 1
            if (isExpression) column = expression
            currentTable = schema.getTableByName(table)
            if (currentTable == null) continue
            val currentIndex = currentTable.getIndexByName(index)
            currentIndex?.columns?.add(IndexColumn(column, position)) ?: println("$table.$index")
        }

        // constraints
        print(" constraints")
        r =
            c.query("SELECT table_name, constraint_name, constraint_type, search_condition, r_owner, r_constraint_name, delete_rule, status, deferrable, deferred, validated, generated FROM all_constraints WHERE owner = USER")
        r.fetchSize = 10000
        while (r.next()) {
            currentTable = schema.getTableByName(dbconv(r.getString(1)))
            if (currentTable == null) continue
            currentTable.constraints.add(
                Constraint(
                    dbconv(r.getString(2)), dbconv(
                        r
                            .getString(3)
                    ), dbconv(r.getString(4)), dbconv(
                        r
                            .getString(5)
                    ), dbconv(r.getString(6)), dbconv(
                        r
                            .getString(7)
                    ), dbconv(r.getString(8)), dbconv(
                        r
                            .getString(9)
                    ), dbconv(r.getString(10)), dbconv(
                        r
                            .getString(11)
                    ), dbconv(r.getString(12)),
                    currentTable
                )
            )
        }
        // grants
        print(" grants")
        r =
            c.query("SELECT table_name, grantee, select_priv, insert_priv, delete_priv, update_priv, references_priv, alter_priv, index_priv FROM table_privileges WHERE owner = USER AND grantor = USER")
        r.fetchSize = 10000
        while (r.next()) {
            currentTable = schema.getTableByName(dbconv(r.getString(1)))
            currentTable?.grants?.add(
                Grant(
                    dbconv(r.getString(2)), dbconv(
                        r.getString(3)
                    ) != "N", dbconv(
                        r.getString(4)
                    ) != "N", dbconv(
                        r.getString(5)
                    ) != "N", dbconv(
                        r.getString(6)
                    ) != "N", dbconv(
                        r.getString(7)
                    ) != "N", dbconv(
                        r.getString(8)
                    ) != "N", dbconv(
                        r.getString(9)
                    ) != "N", currentTable
                )
            )
        }
        // public synonyms
        print(" public synonyms")
        r = c.query("SELECT table_name, synonym_name FROM all_synonyms WHERE owner = 'PUBLIC' AND table_owner = USER")
        r.fetchSize = 10000
        while (r.next()) {
            currentTable = schema.getTableByName(dbconv(r.getString(1)))
            currentTable?.publicSynonyms?.add(
                PublicSynonym(
                    dbconv(r.getString(2)),
                    currentTable
                )
            )
        }
    }

    @Throws(SQLException::class, UnsupportedEncodingException::class)
    private fun loadTriggers(c: OracleConnection, schema: Schema) {
        val r = c
            .query("SELECT trigger_name, trigger_type, triggering_event, table_name, when_clause, status, description, trigger_body FROM all_triggers WHERE owner = USER")
        r!!.fetchSize = 10000
        while (r.next()) {
            schema.addTrigger(
                Trigger(
                    dbconv(r.getString(1)),
                    dbconv(r.getString(2)), dbconv(r.getString(3)),
                    dbconv(r.getString(4)), dbconv(r.getString(5)),
                    dbconv(r.getString(6)), dbconv(r.getString(7)),
                    dbconv(r.getString(8))
                )
            )
        }
    }

    @Throws(SQLException::class, UnsupportedEncodingException::class)
    private fun loadViews(c: OracleConnection, schema: Schema) {
        var r = c
            .query("SELECT view_name, text FROM all_views WHERE owner = USER")
        r!!.fetchSize = 10000
        while (r.next()) {
            schema.addView(
                View(
                    dbconv(r.getString(1)), dbconv(
                        r.getString(2)
                    )
                )
            )
        }
        r.close()
        for (view in schema.dbViews.values) {
            r = c.query(
                "select column_name from all_tab_columns where table_name='"
                        + view.name + "' order by column_id"
            )
            r.fetchSize = 10000
            while (r.next()) {
                view.addColumn(dbconv(r.getString(1)))
            }
            r.close()
        }
    }

    companion object {
        @Throws(UnsupportedEncodingException::class)
        private fun dbconv(src: String?): String {
            return if (src == null) "" else String(
                src.toByteArray(charset("US-ASCII")),
                charset("US-ASCII")
            ).replace(0x00.toChar(), ' ').replace(0x11.toChar(), ' ')
                .replace(0x12.toChar(), ' ').replace(0x1b.toChar(), ' ')
                .replace(0x18.toChar(), ' ')
        }
    }
}
