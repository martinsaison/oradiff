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

import java.util.ArrayList;

public class Table extends TablespaceObject {

    private ArrayList<Column> columns = new ArrayList<>();

    private String comments;

    private ArrayList<Index> indexes = new ArrayList<>();
    private ArrayList<Constraint> constraints = new ArrayList<>();
    private ArrayList<Grant> grants = new ArrayList<>();
    private ArrayList<PublicSynonym> publicSynonyms = new ArrayList<>();

    public ArrayList<PublicSynonym> getPublicSynonyms() {
        return publicSynonyms;
    }

    public ArrayList<Constraint> getConstraints() {
        return constraints;
    }

    public ArrayList<Grant> getGrants() {
        return grants;
    }

    public Table(String owner, String tableName, String tablespace,
                 String comments) {
        super(owner, tableName, tablespace);
        this.comments = comments;
    }

    private String columnChanges(Table destination) {
        return sqlNewColumns(destination) + sqlDropColumns(destination)
                + sqlAlterColumns(destination);
    }

    private String getColumnsComments(ArrayList<Column> columns) {
        String res = "";
        for (Column column : columns) {
            if (column.getComment() != null)
                res += column.sqlComments(getName());
        }
        return res;
    }

    private String indexChanges(Table destination) {
        return sqlNewIndexes(destination) + sqlDropIndexes(destination)
                + sqlAlterIndexes(destination);
    }

    private ArrayList<Index> newIndexes(Table destination) {
        return newObjects(indexes, destination.getIndexes());
    }

    private String sqlAlterColumns(Table destination) {
        String res = "";
        // columns diffs
        ArrayList<Column> columnsDiffs = new ArrayList<>();
        for (Column dst : destination.getColumns()) {
            // research in current table
            Column src = getColumnByName(dst.getName());
            if (src != null) {
                if (!src.dbEquals(dst))
                    columnsDiffs.add(dst);
            }
        }
        if (columnsDiffs.size() > 0) {
            res += "alter table " + getName() + " modify\n(";
            boolean first = true;
            for (Column dst : columnsDiffs) {
                if (first)
                    first = false;
                else
                    res += ", ";
                res += "\n  " + dst.sqlCreate();
            }
            res += "\n);\n";
        }
        // comments diffs

        for (Column dst : destination.getColumns()) {
            // research in current table
            Column src = getColumnByName(dst.getName());
            if (src != null) {
                if ((src.getComment() == null && dst.getComment() != null)
                        || (src.getComment() != null && dst.getComment() == null)
                        || ((!(src.getComment() == null && dst.getComment() == null)) && (!trimLines(
                        src.getComment()).equals(
                        trimLines(dst.getComment()))))) {
                    res += dst.sqlComments(getName());
                }
            }
        }

        return res;
    }

    private String sqlAlterIndexes(Table destination) {
        String res = "";
        // index diffs
        for (Index dst : destination.getIndexes()) {
            // research in current table
            Index src = getIndexByName(dst.getName());
            if (src != null) {
                if (!src.dbEquals(dst)) {
                    res += src.sqlDrop();
                    res += dst.sqlCreate();
                }
            }
        }
        return res;
    }

    private String sqlDropColumns(Table destination) {
        String res = "";
        ArrayList<Column> columnsToDrop = destination.newColumns(this);
        for (Column column : columnsToDrop) {
            res += "alter table " + escapeName(getName()) + " drop column "
                    + escapeName(column.getName()) + ";\n";
        }
        return res;
    }

    private String sqlDropIndexes(Table destination) {
        String res = "";
        ArrayList<Index> indexesToDrop = destination.newIndexes(this);
        for (Index index : indexesToDrop) {
            res += index.sqlDrop();
        }
        return res;
    }

    private String sqlNewColumns(Table destination) {
        String res = "";
        ArrayList<Column> columnsToAdd = newColumns(destination);
        if (columnsToAdd.size() > 0) {
            res += "alter table " + escapeName(getName()) + " add (";
            boolean first = true;
            for (Column column : columnsToAdd) {
                if (first) {
                    first = false;
                } else {
                    res += ",";
                }
                res += "\n  " + column.sqlCreate();
            }
            res += "\n);\n";
            // new column comments
            res += getColumnsComments(columnsToAdd);
        }
        return res;
    }

    private String sqlNewIndexes(Table destination) {
        String res = "";
        ArrayList<Index> indexesToAdd = newIndexes(destination);
        for (Index index : indexesToAdd) {
            res += index.sqlCreate();
        }
        return res;
    }

    private String trimLines(String txt) {
        String res = "";
        for (String line : txt.split("\n")) {
            line = line.trim();
            if (line.length() > 0)
                res += line + "\n";
        }
        return res;
    }

    public boolean dbEquals(Table dst) {
        return sqlUpdate(dst).length() == 0;
    }

    public Column getColumnByName(String name) {
        for (Column c : columns) {
            if (c.getName().equals(name))
                return c;
        }
        return null;
    }

    public ArrayList<Column> getColumns() {
        return columns;
    }

    public String getComments() {
        return comments;
    }

    public Index getIndexByName(String name) {
        for (Index c : indexes) {
            if (c.getName().equals(name))
                return c;
        }
        return null;
    }

    public ArrayList<Index> getIndexes() {
        return indexes;
    }

    @Override
    public String getTypeName() {
        return "TABLE";
    }

    // returns columns which are in destination table but not in current table
    private ArrayList<Column> newColumns(Table destination) {
        return newObjects(columns, destination.getColumns());
    }

    public String sqlCreate() {
        // table
        String res = "create table " + escapeName(getName()) + " (";
        boolean first = true;
        for (Column column : columns) {
            if (first)
                first = false;
            else
                res += ",";
            res += "\n  " + column.sqlCreate();
        }
        res += "\n)" + getTablespaceSql() + ";\n";
        // table comments
        if (comments != null)
            res += "comment on table " + getName() + "\n  is '"
                    + escape(comments) + "';\n";
        // column comments
        res += getColumnsComments(columns);
        // indexes
        if (indexes.size() > 0) {
            for (Index index : indexes) {
                res += index.sqlCreate();
            }
        }
        // grants
        if (grants.size() > 0) {
            for (Grant grant : grants) {
                res += grant.sqlCreate();
            }
        }
        return res;
    }

    public String sqlUpdate(DBObject destination) {
        String res = "";
        Table dst = (Table) destination;
        res += columnChanges(dst);
        res += indexChanges(dst);
        if (!dst.getName().equals(this.getName())) {
            res += "ALTER TABLE " + getName() + " RENAME TO " + dst.getName()
                    + ";";
        }
        return res;
    }

    public void setColumns(ArrayList<Column> columns) {
        this.columns = columns;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setIndexes(ArrayList<Index> indexes) {
        this.indexes = indexes;
    }

    public void setConstraints(ArrayList<Constraint> constraints) {
        this.constraints = constraints;
    }

    public void setGrants(ArrayList<Grant> grants) {
        this.grants = grants;
    }

    public void setPublicSynonyms(ArrayList<PublicSynonym> publicSynonyms) {
        this.publicSynonyms = publicSynonyms;
    }

}
