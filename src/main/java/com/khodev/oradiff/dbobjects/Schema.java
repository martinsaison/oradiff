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

package com.khodev.oradiff.dbobjects;

import com.khodev.oradiff.diff.DBObjectDiff;
import com.khodev.oradiff.diff.Equivalence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

public class Schema {

    private Hashtable<String, Function> dbFunctions = new Hashtable<>();

    private Hashtable<String, Job> dbJobs = new Hashtable<>();

    private Hashtable<String, DBPackage> dbPackages = new Hashtable<>();

    private Hashtable<String, Procedure> dbProcedures = new Hashtable<>();

    private Hashtable<String, Sequence> dbSequences = new Hashtable<>();

    private Hashtable<String, Synonym> dbSynonyms = new Hashtable<>();

    public Hashtable<String, Function> getDbFunctions() {
        return dbFunctions;
    }

    public Hashtable<String, Job> getDbJobs() {
        return dbJobs;
    }

    public Hashtable<String, DBPackage> getDbPackages() {
        return dbPackages;
    }

    public Hashtable<String, Procedure> getDbProcedures() {
        return dbProcedures;
    }

    public Hashtable<String, Sequence> getDbSequences() {
        return dbSequences;
    }

    public Hashtable<String, Synonym> getDbSynonyms() {
        return dbSynonyms;
    }

    public Hashtable<String, Table> getDbTables() {
        return dbTables;
    }

    public Hashtable<String, Trigger> getDbTriggers() {
        return dbTriggers;
    }

    public Hashtable<String, View> getDbViews() {
        return dbViews;
    }

    public void setDbFunctions(Hashtable<String, Function> dbFunctions) {
        this.dbFunctions = dbFunctions;
    }

    public void setDbJobs(Hashtable<String, Job> dbJobs) {
        this.dbJobs = dbJobs;
    }

    public void setDbPackages(Hashtable<String, DBPackage> dbPackages) {
        this.dbPackages = dbPackages;
    }

    public void setDbProcedures(Hashtable<String, Procedure> dbProcedures) {
        this.dbProcedures = dbProcedures;
    }

    public void setDbSequences(Hashtable<String, Sequence> dbSequences) {
        this.dbSequences = dbSequences;
    }

    public void setDbSynonyms(Hashtable<String, Synonym> dbSynonyms) {
        this.dbSynonyms = dbSynonyms;
    }

    public void setDbTables(Hashtable<String, Table> dbTables) {
        this.dbTables = dbTables;
    }

    public void setDbTriggers(Hashtable<String, Trigger> dbTriggers) {
        this.dbTriggers = dbTriggers;
    }

    public void setDbViews(Hashtable<String, View> dbViews) {
        this.dbViews = dbViews;
    }

    private Hashtable<String, Table> dbTables = new Hashtable<>();

    private Hashtable<String, Trigger> dbTriggers = new Hashtable<>();

    private Hashtable<String, View> dbViews = new Hashtable<>();

    public Source createNewDBSource(String name, String type) {
        switch (type) {
            case "PACKAGE":
            case "PACKAGE BODY":
                return addPackage(new DBPackage(name));
            case "FUNCTION":
                return addFunction(new Function(name));
            case "PROCEDURE":
                return addProcedure(new Procedure(name));
        }
        return null;
    }

    private Function addFunction(Function o) {
        dbFunctions.put(o.getName(), o);
        return o;
    }

    private Function getFunctionByName(String name) {
        return dbFunctions.get(name);
    }

    public Job addJob(Job o) {
        dbJobs.put(o.getName(), o);
        return o;
    }

    private Job getJobByName(String name) {
        return dbJobs.get(name);
    }

    private DBPackage addPackage(DBPackage o) {
        dbPackages.put(o.getName(), o);
        return o;
    }

    private DBPackage getPackageByName(String name) {
        return dbPackages.get(name);
    }

    private Procedure addProcedure(Procedure o) {
        dbProcedures.put(o.getName(), o);
        return o;
    }

    private Procedure getProcedureByName(String name) {
        return dbProcedures.get(name);
    }

    public Sequence addSequence(Sequence o) {
        dbSequences.put(o.getName(), o);
        return o;
    }

    private Sequence getSequenceByName(String name) {
        return dbSequences.get(name);
    }

    private Synonym addSynonym(Synonym o) {
        dbSynonyms.put(o.getName(), o);
        return o;
    }

    private Synonym getSynonymByName(String name) {
        return dbSynonyms.get(name);
    }

    public Table addTable(Table o) {
        dbTables.put(o.getName(), o);
        return o;
    }

    public Table getTableByName(String name) {
        return dbTables.get(name);
    }

    private Column getColumnByName(String tableName, String columnName) {
        Table table = getTableByName(tableName);
        if (table == null)
            return null;
        return table.getColumnByName(columnName);
    }

    public Trigger addTrigger(Trigger o) {
        dbTriggers.put(o.getName(), o);
        return o;
    }

    private Trigger getTriggerByName(String name) {
        return dbTriggers.get(name);
    }

    public View addView(View o) {
        dbViews.put(o.getName(), o);
        return o;
    }

    private View getViewByName(String name) {
        return dbViews.get(name);
    }

    public static <T extends DBObject> void cleanObjectsWithFilter(
            String filter, Hashtable<String, T> objects) {
        if (filter == null)
            return;
        ArrayList<String> trash = new ArrayList<>();
        for (String key : objects.keySet()) {
            DBObject dbo = objects.get(key);
            if (!dbo.getName().toLowerCase().matches((filter.toLowerCase())))
                trash.add(key);
        }
        for (String key : trash)
            objects.remove(key);
    }

    public ArrayList<Function> newFunctions(Schema destination) {
        ArrayList<Function> res = new ArrayList<>();
        for (Function dst : destination.dbFunctions.values()) {
            if (getFunctionByName(dst.getName()) == null)
                res.add(dst);
        }
        return res;
    }

    public ArrayList<Job> newJobs(Schema destination) {
        ArrayList<Job> res = new ArrayList<>();
        for (Job dst : destination.dbJobs.values()) {
            if (getJobByName(dst.getName()) == null)
                res.add(dst);
        }
        return res;
    }

    public ArrayList<DBPackage> newPackages(Schema destination) {
        ArrayList<DBPackage> res = new ArrayList<>();
        for (DBPackage dst : destination.dbPackages.values()) {
            if (getPackageByName(dst.getName()) == null)
                res.add(dst);
        }
        return res;
    }

    public ArrayList<Procedure> newProcedures(Schema destination) {
        ArrayList<Procedure> res = new ArrayList<>();
        for (Procedure dst : destination.dbProcedures.values()) {
            if (getProcedureByName(dst.getName()) == null)
                res.add(dst);
        }
        return res;
    }

    public ArrayList<Sequence> newSequences(Schema destination) {
        ArrayList<Sequence> res = new ArrayList<>();
        for (Sequence dst : destination.dbSequences.values()) {
            if (getSequenceByName(dst.getName()) == null)
                res.add(dst);
        }
        return res;
    }

    public ArrayList<Synonym> newSynonyms(Schema destination) {
        ArrayList<Synonym> res = new ArrayList<>();
        for (Synonym dst : destination.dbSynonyms.values()) {
            if (getSynonymByName(dst.getName()) == null)
                res.add(dst);
        }
        return res;
    }

    // returns tables which are in destination schema but not in current schema
    public ArrayList<Table> newTables(Schema destination) throws IOException {
        ArrayList<Table> res = new ArrayList<>();
        for (Table dst : destination.dbTables.values()) {
            boolean existsHere = false;
            for (Table src : dbTables.values()) {
                if (Equivalence.matches(src.getName(), dst.getName())) {
                    existsHere = true;
                    break;
                }
            }
            if (!existsHere)
                res.add(dst);
        }
        return res;
    }

    public ArrayList<Trigger> newTriggers(Schema destination) {
        ArrayList<Trigger> res = new ArrayList<>();
        for (Trigger dst : destination.dbTriggers.values()) {
            if (getTriggerByName(dst.getName()) == null)
                res.add(dst);
        }
        return res;
    }

    public ArrayList<View> newViews(Schema destination) {
        ArrayList<View> res = new ArrayList<>();
        for (View dst : destination.dbViews.values()) {
            if (getViewByName(dst.getName()) == null)
                res.add(dst);
        }
        return res;
    }

    public String tablesChanges(Schema destination) {
        String res = "";
        // common tables
        for (Table dst : destination.dbTables.values()) {
            // research in current table
            Table src = getTableByName(dst.getName());
            if (src != null)
                res += src.sqlUpdate(dst);
        }
        return res;
    }

    public ArrayList<DBObjectDiff<Function>> updateFunctions(Schema destination) {
        ArrayList<DBObjectDiff<Function>> res = new ArrayList<>();
        for (Function dst : destination.dbFunctions.values()) {
            Function src = getFunctionByName(dst.getName());
            if (src != null)
                if (!src.dbEquals(dst))
                    res.add(new DBObjectDiff<>(src, dst));
        }
        return res;
    }

    public ArrayList<DBObjectDiff<Job>> updateJobs(Schema destination) {
        ArrayList<DBObjectDiff<Job>> res = new ArrayList<>();
        for (Job dst : destination.dbJobs.values()) {
            Job src = getJobByName(dst.getName());
            if (src != null)
                if (!src.dbEquals(dst))
                    res.add(new DBObjectDiff<>(src, dst));
        }
        return res;
    }

    public ArrayList<DBObjectDiff<DBPackage>> updatePackages(Schema destination) {
        ArrayList<DBObjectDiff<DBPackage>> res = new ArrayList<>();
        for (DBPackage dst : destination.dbPackages.values()) {
            // research in current table
            DBPackage src = getPackageByName(dst.getName());
            if (src != null)
                if (!src.dbEquals(dst))
                    res.add(new DBObjectDiff<>(src, dst));
        }
        return res;
    }

    public ArrayList<DBObjectDiff<Procedure>> updateProcedures(Schema destination) {
        ArrayList<DBObjectDiff<Procedure>> res = new ArrayList<>();
        for (Procedure dst : destination.dbProcedures.values()) {
            Procedure src = getProcedureByName(dst.getName());
            if (src != null)
                if (!src.dbEquals(dst))
                    res.add(new DBObjectDiff<>(src, dst));
        }
        return res;
    }

    public ArrayList<DBObjectDiff<Sequence>> updateSequences(Schema destination) {
        ArrayList<DBObjectDiff<Sequence>> res = new ArrayList<>();
        for (Sequence dst : destination.dbSequences.values()) {
            Sequence src = getSequenceByName(dst.getName());
            if (src != null)
                if (!src.dbEquals(dst))
                    res.add(new DBObjectDiff<>(src, dst));
        }
        return res;
    }

    public ArrayList<DBObjectDiff<Synonym>> updateSynonyms(Schema destination) {
        ArrayList<DBObjectDiff<Synonym>> res = new ArrayList<>();
        for (Synonym dst : destination.dbSynonyms.values()) {
            Synonym src = getSynonymByName(dst.getName());
            if (src != null)
                if (!src.dbEquals(dst))
                    res.add(new DBObjectDiff<>(src, dst));
        }
        return res;
    }

    public ArrayList<DBObjectDiff<Table>> updateTables(Schema destination)
            throws IOException {
        ArrayList<DBObjectDiff<Table>> res = new ArrayList<>();
        for (Table dst : destination.dbTables.values()) {
            // boolean existsHere = false;
            for (Table src : dbTables.values()) {
                if (Equivalence.matches(src.getName(), dst.getName())) {
                    // existsHere = true;
                    if (!src.dbEquals(dst)) // exists and changed
                        res.add(new DBObjectDiff<>(src, dst));
                }
            }
        }
        return res;
    }

    public ArrayList<DBObjectDiff<Trigger>> updateTriggers(Schema destination) {
        ArrayList<DBObjectDiff<Trigger>> res = new ArrayList<>();
        for (Trigger dst : destination.dbTriggers.values()) {
            Trigger src = getTriggerByName(dst.getName());
            if (src != null)
                if (!src.dbEquals(dst))
                    res.add(new DBObjectDiff<>(src, dst));
        }
        return res;
    }

    public ArrayList<DBObjectDiff<View>> updateViews(Schema destination) {
        ArrayList<DBObjectDiff<View>> res = new ArrayList<>();
        for (View dst : destination.dbViews.values()) {
            View src = getViewByName(dst.getName());
            if (src != null)
                if (!src.dbEquals(dst))
                    res.add(new DBObjectDiff<>(src, dst));
        }
        return res;
    }
}
