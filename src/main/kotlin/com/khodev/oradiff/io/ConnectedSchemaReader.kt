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

package com.khodev.oradiff.io;


import com.khodev.oradiff.dbobjects.*;
import com.khodev.oradiff.util.OracleConnection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectedSchemaReader implements SchemaReader {

    private String host;
    private String port;
    private String sid;
    private String user;
    private String pass;
    private String tableFilter;
    private String packageFilter;
    private String functionFilter;
    private String procedureFilter;
    private String viewFilter;
    private String sequenceFilter;
    private String triggerFilter;

    public ConnectedSchemaReader(String filename) throws IOException {
        BufferedReader b;
        String line;
        tableFilter = null;
        packageFilter = null;
        functionFilter = null;
        procedureFilter = null;
        viewFilter = null;
        sequenceFilter = null;
        triggerFilter = null;
        b = new BufferedReader(new FileReader(filename));
        while ((line = b.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0)
                continue;
            String[] vars = line.split("=");
            String var = vars[0].trim().toLowerCase();
            String val = vars[1].trim();
            switch (var) {
                case "host":
                    host = val;
                    break;
                case "port":
                    port = val;
                    break;
                case "sid":
                    sid = val;
                    break;
                case "user":
                    user = val;
                    break;
                case "pass":
                    pass = val;
                    break;
                case "tablefilter":
                    tableFilter = val;
                    break;
                case "packagefilter":
                    packageFilter = val;
                    break;
                case "functionfilter":
                    functionFilter = val;
                    break;
                case "procedurefilter":
                    procedureFilter = val;
                    break;
                case "viewfilter":
                    viewFilter = val;
                    break;
                case "sequencefilter":
                    sequenceFilter = val;
                    break;
                case "triggerfilter":
                    triggerFilter = val;
                    break;
            }
        }
        b.close();
    }

    public ConnectedSchemaReader(String host, String port, String sid, String user, String pass) {
        this.host = host;
        this.port = port;
        this.sid = sid;
        this.user = user;
        this.pass = pass;
    }

    private static String dbconv(String src)
            throws UnsupportedEncodingException {
        if (src == null)
            return "";
        return new String(src.getBytes("US-ASCII"), "US-ASCII")
                .replace((char) 0x00, ' ').replace((char) 0x11, ' ')
                .replace((char) 0x12, ' ').replace((char) 0x1b, ' ')
                .replace((char) 0x18, ' ');
    }


    @Override
    public Schema get() {
        Schema schema = new Schema();
        try {
            OracleConnection con = new OracleConnection(host, port, sid, user, pass, tableFilter,
                    packageFilter, functionFilter, procedureFilter, viewFilter,
                    sequenceFilter, triggerFilter);
            loadSchema(con, schema);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return schema;
    }

    private void loadSchema(OracleConnection c, Schema schema) throws SQLException,
            UnsupportedEncodingException {
        System.out.print("loading tables...");
        loadTables(c, schema);
        System.out.println("done.");
        System.out.print("loading views...");
        loadViews(c, schema);
        System.out.println("done.");
        System.out.print("loading synonyms...");
        loadSynonyms(c, schema);
        System.out.println("done.");
        System.out.print("loading sequences...");
        loadSequences(c, schema);
        System.out.println("done.");
        System.out.print("loading sources...");
        loadSources(c, schema);
        System.out.println("done.");
        System.out.print("loading jobs...");
        loadJobs(c, schema);
        System.out.println("done.");
        System.out.print("loading triggers...");
        loadTriggers(c, schema);
        System.out.println("done.");
        // cleaning
        System.out.print("cleaning tables...");
        Schema.cleanObjectsWithFilter(c.getTableFilter(), schema.getDbTables());
        System.out.println("done.");
        System.out.print("cleaning packages...");
        Schema.cleanObjectsWithFilter(c.getPackageFilter(), schema.getDbPackages());
        System.out.println("done.");
        System.out.print("cleaning functions...");
        Schema.cleanObjectsWithFilter(c.getFunctionFilter(), schema.getDbFunctions());
        System.out.println("done.");
        System.out.print("cleaning procedures...");
        Schema.cleanObjectsWithFilter(c.getProcedureFilter(), schema.getDbProcedures());
        System.out.println("done.");
        System.out.print("cleaning views...");
        Schema.cleanObjectsWithFilter(c.getViewFilter(), schema.getDbViews());
        System.out.println("done.");
        System.out.print("cleaning sequences...");
        Schema.cleanObjectsWithFilter(c.getSequenceFilter(), schema.getDbSequences());
        System.out.println("done.");
        System.out.print("cleaning triggers...");
        Schema.cleanObjectsWithFilter(c.getTriggerFilter(), schema.getDbTriggers());
        System.out.println("done.");
    }

    private void loadJobs(OracleConnection c, Schema schema) throws SQLException,
            UnsupportedEncodingException {
        ResultSet r = c
                .query("SELECT job, what, to_char(next_date, 'dd-mm-yyyy hh24:mi:ss'), interval, broken FROM all_jobs WHERE log_user = USER");
        r.setFetchSize(10000);
        while (r.next()) {
            schema.addJob(new Job(dbconv(r.getString(1)), dbconv(r.getString(2)),
                    dbconv(r.getString(3)), dbconv(r.getString(4)), dbconv(
                    r.getString(5)).equals("Y")));
        }
    }

    private void loadSequences(OracleConnection c, Schema schema) throws SQLException,
            UnsupportedEncodingException {
        ResultSet r = c
                .query("SELECT sequence_name, min_value, max_value, increment_by, cycle_flag, order_flag, cache_size, last_number FROM all_sequences WHERE sequence_owner = USER");
        r.setFetchSize(10000);
        while (r.next()) {
            schema.addSequence(new Sequence(dbconv(r.getString(1)),
                    dbconv(r.getString(2)), dbconv(r.getString(3)),
                    dbconv(r.getString(4)), dbconv(r.getString(5)).equals("Y"),
                    dbconv(r.getString(6)).equals("Y"), r.getInt(7),
                    dbconv(r.getString(8))));
        }
    }

    private void loadSources(OracleConnection c, Schema schema) throws SQLException,
            UnsupportedEncodingException {
        ResultSet r = c
                .query("SELECT NAME, TYPE, line, text FROM all_source WHERE owner = USER and type IN ('PACKAGE', 'PACKAGE BODY', 'FUNCTION', 'PROCEDURE') ORDER BY NAME, TYPE, line");
        r.setFetchSize(10000);
        String oname = "";
        String nname;
        String otype = "";
        String ntype;
        String line;
        Source dbSource = null;
        while (r.next()) {
            nname = dbconv(r.getString(1));
            ntype = dbconv(r.getString(2));
            line = DBObject.removeR4(dbconv(r.getString(4)));
            if ((!nname.equals(oname)) || (!ntype.equals(otype))) {
                if (!(oname.equals(nname) && otype.equals("PACKAGE") && ntype
                        .equals("PACKAGE BODY")))
                    dbSource = schema.createNewDBSource(nname, ntype);
                oname = nname;
                otype = ntype;
            }
            if (dbSource == null) {
                System.err.println(nname);
                System.err.println(ntype);
            } else {
                dbSource.append(ntype, r.getInt(3), line);
            }
        }

    }

    private void loadSynonyms(OracleConnection c, Schema schema) {
    }

    private void loadTables(OracleConnection c, Schema schema) throws SQLException,
            UnsupportedEncodingException {
        ResultSet r;
        Table currentTable;
        Column currentColumn = null;
        // Tables
        System.out.print(" tables");
        r = c.query("SELECT t.owner, t.table_name, t.tablespace_name, (SELECT comments FROM all_tab_comments c WHERE c.table_name = t.table_name AND c.owner = t.owner) FROM all_tables t WHERE t.owner = USER");
        r.setFetchSize(10000);
        while (r.next()) {
            schema.addTable(new Table(dbconv(r.getString(1)), dbconv(r.getString(2)),
                    dbconv(r.getString(3)), dbconv(r.getString(4))));
        }
        System.out.print(" tables from synonyms");
        r = c.query("SELECT t.owner, t.table_name, t.tablespace_name, (SELECT comments FROM all_tab_comments c WHERE c.table_name = t.table_name AND c.owner = t.owner) FROM all_tables t, all_synonyms s WHERE s.owner = USER AND s.table_name = t.table_name AND s.table_owner = t.owner");
        r.setFetchSize(10000);
        while (r.next()) {
            schema.addTable(new Table(dbconv(r.getString(1)), dbconv(r.getString(2)),
                    dbconv(r.getString(3)), dbconv(r.getString(4))));
        }
        // columns
        System.out.print(" columns");
        r = c.query("SELECT t.owner, t.table_name, t.column_name, t.column_id, t.data_type, decode(t.char_length, 0, t.data_length, t.char_length), t.data_precision, t.data_scale, t.nullable, (SELECT comments FROM all_col_comments c WHERE c.owner = USER AND c.table_name = t.table_name AND c.column_name = t.column_name), t.data_default FROM all_tab_columns t WHERE t.owner = USER");
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
        // indexes
        System.out.print(" indexes");
        r = c.query("SELECT owner, table_name, index_name, tablespace_name, index_type, uniqueness, compression FROM all_indexes WHERE owner = USER ORDER BY table_name, index_name");
        r.setFetchSize(10000);
        while (r.next()) {
            currentTable = schema.getTableByName(dbconv(r.getString(2)));
            if (currentTable != null)
                currentTable.getIndexes().add(
                        new Index(dbconv(r.getString(1)),
                                dbconv(r.getString(3)), dbconv(r.getString(4)),
                                dbconv(r.getString(5)), dbconv(r.getString(6)).equals("UNIQUE"),
                                dbconv(r.getString(7)), currentTable));
        }
        // index columns
        System.out.print(" index columns");
        r = c.query("SELECT b.table_name, b.index_name, b.column_name, b.column_position, c.column_expression,CASE WHEN c.column_expression IS NULL THEN 0 ELSE 1 END FROM all_ind_columns b, all_ind_expressions c WHERE b.index_owner = USER AND c.index_owner(+) = USER AND c.index_name(+) = b.index_name AND c.column_position(+) = b.column_position AND b.table_name IN (SELECT table_name FROM all_tables) ORDER BY b.table_name, b.index_name, b.column_position");
        r.setFetchSize(10000);
        while (r.next()) {
            String table = dbconv(r.getString(1));
            String index = dbconv(r.getString(2));
            String column = dbconv(r.getString(3));
            int position = r.getInt(4);
            String expression = dbconv(r.getString(5));
            boolean isExpression = r.getInt(6) == 1;
            if (isExpression)
                column = expression;
            currentTable = schema.getTableByName(table);
            if (currentTable == null)
                continue;
            Index currentIndex = currentTable.getIndexByName(index);
            if (currentIndex == null) {
                System.out.println(table + "." + index);
            } else {
                currentIndex.getColumns().add(new IndexColumn(column, position));
            }
        }

        // constraints
        System.out.print(" constraints");
        r = c.query("SELECT table_name, constraint_name, constraint_type, search_condition, r_owner, r_constraint_name, delete_rule, status, deferrable, deferred, validated, generated FROM all_constraints WHERE owner = USER");
        r.setFetchSize(10000);
        while (r.next()) {
            currentTable = schema.getTableByName(dbconv(r.getString(1)));
            if (currentTable == null)
                continue;
            currentTable.getConstraints().add(
                    new Constraint(dbconv(r.getString(2)), dbconv(r
                            .getString(3)), dbconv(r.getString(4)), dbconv(r
                            .getString(5)), dbconv(r.getString(6)), dbconv(r
                            .getString(7)), dbconv(r.getString(8)), dbconv(r
                            .getString(9)), dbconv(r.getString(10)), dbconv(r
                            .getString(11)), dbconv(r.getString(12)),
                            currentTable));
        }
        // grants
        System.out.print(" grants");
        r = c.query("SELECT table_name, grantee, select_priv, insert_priv, delete_priv, update_priv, references_priv, alter_priv, index_priv FROM table_privileges WHERE owner = USER AND grantor = USER");
        r.setFetchSize(10000);
        while (r.next()) {
            currentTable = schema.getTableByName(dbconv(r.getString(1)));
            if (currentTable != null) {
                currentTable.getGrants().add(
                        new Grant(dbconv(r.getString(2)), !dbconv(
                                r.getString(3)).equals("N"), !dbconv(
                                r.getString(4)).equals("N"), !dbconv(
                                r.getString(5)).equals("N"), !dbconv(
                                r.getString(6)).equals("N"), !dbconv(
                                r.getString(7)).equals("N"), !dbconv(
                                r.getString(8)).equals("N"), !dbconv(
                                r.getString(9)).equals("N"), currentTable));
            }
        }
        // public synonyms
        System.out.print(" public synonyms");
        r = c.query("SELECT table_name, synonym_name FROM all_synonyms WHERE owner = 'PUBLIC' AND table_owner = USER");
        r.setFetchSize(10000);
        while (r.next()) {
            currentTable = schema.getTableByName(dbconv(r.getString(1)));
            if (currentTable != null) {
                currentTable.getPublicSynonyms()
                        .add(new PublicSynonym(dbconv(r.getString(2)),
                                currentTable));
            }
        }
    }

    private void loadTriggers(OracleConnection c, Schema schema) throws SQLException,
            UnsupportedEncodingException {
        ResultSet r = c
                .query("SELECT trigger_name, trigger_type, triggering_event, table_name, when_clause, status, description, trigger_body FROM all_triggers WHERE owner = USER");
        r.setFetchSize(10000);
        while (r.next()) {
            schema.addTrigger(new Trigger(dbconv(r.getString(1)),
                    dbconv(r.getString(2)), dbconv(r.getString(3)),
                    dbconv(r.getString(4)), dbconv(r.getString(5)),
                    dbconv(r.getString(6)), dbconv(r.getString(7)),
                    dbconv(r.getString(8))));
        }
    }

    private void loadViews(OracleConnection c, Schema schema) throws SQLException,
            UnsupportedEncodingException {
        ResultSet r = c
                .query("SELECT view_name, text FROM all_views WHERE owner = USER");
        r.setFetchSize(10000);
        while (r.next()) {
            schema.addView(new View(dbconv(r.getString(1)), dbconv(r.getString(2))));
        }
        r.close();
        for (View view : schema.getDbViews().values()) {
            r = c.query("select column_name from all_tab_columns where table_name='"
                    + view.getName() + "' order by column_id");
            r.setFetchSize(10000);
            while (r.next()) {
                view.addColumn(dbconv(r.getString(1)));
            }
            r.close();
        }
    }


}
