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

import java.util.Collection;
import java.util.ArrayList;

public class Constraint extends SubDBObject {

    private final Collection<IndexColumn> columns = new ArrayList<>();
    private final String constraintType;
    private final String deferrable;
    private final String deferred;
    private final String deleteRule;
    private final String generated;
    private final String refConstraintName;
    private final String refUserName;
    private final String searchCondition;
    private final String status;
    private final String validated;

    public Constraint(String name, String constraintType,
                      String searchCondition, String refUserName,
                      String refConstraintName, String deleteRule, String status,
                      String deferrable, String deferred, String validated,
                      String generated, Table parent) {
        super(name, parent);
        this.constraintType = constraintType;
        this.searchCondition = searchCondition;
        this.refConstraintName = refConstraintName;
        this.refUserName = refUserName;
        this.deleteRule = deleteRule;
        this.status = status;
        this.deferrable = deferrable;
        this.deferred = deferred;
        this.validated = validated;
        this.generated = generated;
    }

    public boolean dbEquals(Constraint index) {
        return true;
    }

    public Collection<IndexColumn> getColumns() {
        return columns;
    }

    public String getConstraintType() {
        return constraintType;
    }

    public String getDeferrable() {
        return deferrable;
    }

    public String getDeferred() {
        return deferred;
    }

    public String getDeleteRule() {
        return deleteRule;
    }

    public String getGenerated() {
        return generated;
    }

    public String getRefConstraintName() {
        return refConstraintName;
    }

    public String getRefUserName() {
        return refUserName;
    }

    public String getSearchCondition() {
        return searchCondition;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String getTypeName() {
        return "CONSTRAINT";
    }

    public String getValidated() {
        return validated;
    }

    public String sqlCreate() {
        return "";
    }

    public String sqlDrop() {
        return "drop constraint " + getName() + ";\n";
    }

    @Override
    public String sqlUpdate(DBObject destination) {
        return sqlCreate();
    }

}
