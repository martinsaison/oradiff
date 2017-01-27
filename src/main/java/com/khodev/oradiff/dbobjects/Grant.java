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

import com.khodev.oradiff.util.ReplaceManager;

import java.util.ArrayList;

public class Grant extends SubDBObject {

    private final boolean selectPriv;
    private final boolean insertPriv;
    private final boolean deletePriv;
    private final boolean updatePriv;
    private final boolean referencesPriv;
    private final boolean alterPriv;
    private final boolean indexPriv;

    public Grant(String grantee, boolean selectPriv, boolean insertPriv,
                 boolean deletePriv, boolean updatePriv, boolean referencesPriv,
                 boolean alterPriv, boolean indexPriv, Table parent) {
        super(grantee, parent);
        this.selectPriv = selectPriv;
        this.insertPriv = insertPriv;
        this.deletePriv = deletePriv;
        this.updatePriv = updatePriv;
        this.referencesPriv = referencesPriv;
        this.alterPriv = alterPriv;
        this.indexPriv = indexPriv;
    }

    public boolean dbEquals(Grant index) {
        return true;
    }

    @Override
    public String getTypeName() {
        return "GRANT";
    }

    public String sqlCreate() {
        ArrayList<String> gl = new ArrayList<>();
        if (selectPriv)
            gl.add("SELECT");
        if (insertPriv)
            gl.add("INSERT");
        if (deletePriv)
            gl.add("DELETE");
        if (updatePriv)
            gl.add("UPDATE");
        if (referencesPriv)
            gl.add("REFERENCES");
        if (alterPriv)
            gl.add("ALTER");
        if (indexPriv)
            gl.add("INDEX");
        String res = "GRANT ";
        boolean first = true;
        for (String gs : gl) {
            if (first)
                first = false;
            else
                res += ", ";
            res += gs;
        }
        res += " ON " + getParent().getName() + " to " + ReplaceManager.getManager("usersroles").getSubstitute(getName()) + ";\n";
        return res;
    }

    public String sqlDrop() {
        return "drop constraint " + getName() + ";\n";
    }

    @Override
    public String sqlUpdate(DBObject destination) {
        return sqlCreate();
    }

    public boolean isSelectPriv() {
        return selectPriv;
    }

    public boolean isInsertPriv() {
        return insertPriv;
    }

    public boolean isDeletePriv() {
        return deletePriv;
    }

    public boolean isUpdatePriv() {
        return updatePriv;
    }

    public boolean isReferencesPriv() {
        return referencesPriv;
    }

    public boolean isAlterPriv() {
        return alterPriv;
    }

    public boolean isIndexPriv() {
        return indexPriv;
    }

}
