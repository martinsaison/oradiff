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

import java.util.*;

public class Index extends TablespaceObject {

    private String type;

    private boolean unique;

    private String compression;

    private final Collection<IndexColumn> columns = new ArrayList<>();

    private final Table parent;

    public Index(String owner, String name, String tablespace, String type,
                 boolean isUnique, String compression, Table parent) {
        super(owner, name, tablespace);
        this.type = type;
        this.unique = isUnique;
        this.compression = compression;
        this.parent = parent;
    }

    public String getCompression() {
        return compression;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean isUnique) {
        this.unique = isUnique;
    }

    public Collection<IndexColumn> getColumns() {
        return columns;
    }

    public String sqlCreate() {
        String res = "";
        res += "create";
        if (isUnique())
            res += " unique";

        if (type.equals("BITMAP"))
            res += " bitmap";
        res += " index " + getName() + " on " + parent.getName();
        res += "\n(";
        boolean first = true;
        for (IndexColumn indexColumn : getColumns()) {
            if (first)
                first = false;
            else
                res += ", ";
            res += indexColumn.getName();
        }
        res += ")" + getTablespaceSql() + ";\n";

        return res;
    }

    public String sqlDrop() {
        return "drop index " + getName() + ";\n";
    }

    public boolean dbEquals(Index index) {
        if (index.isUnique() != unique)
            return false;
        if (!index.getType().equals(type))
            return false;
        if (columns.size() != index.getColumns().size())
            return false;
        Iterator<IndexColumn> it1 = columns.iterator();
        Iterator<IndexColumn> it2 = index.getColumns().iterator();
        while (it1.hasNext() && it2.hasNext()) {
            IndexColumn col1 = it1.next();
            IndexColumn col2 = it2.next();
            if (!col1.getName().equals(col2.getName()))
                return false;
            if (col1.getPosition() != col2.getPosition())
                return false;
        }
        return true;
    }

    @Override
    public String getTypeName() {
        return "INDEX";
    }

    @Override
    public String sqlUpdate(DBObject destination) {
        return sqlCreate();
    }

}
