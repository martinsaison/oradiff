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

public class Column extends SubDBObject {
    private int id;

    private String type;

    private int length;

    private int precision;

    private int scale;

    private boolean nullable;

    private String comment;

    private String defaultValue;

    public Column(String name, int id, String type, int length, int precision,
                  int scale, boolean nullable, String comment, String defaultValue) {
        super(name, null);
        this.id = id;
        this.type = type;
        this.length = length;
        this.precision = precision;
        this.scale = scale;
        this.nullable = nullable;
        this.comment = comment;
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comments) {
        this.comment = comments;
    }

    public String sqlCreate() {
        String res = escapeName(this.getName()) + " " + this.typeAsSql();
        if (!nullable)
            res += " not null";
        if (defaultValue.length() > 0)
            res += " default " + defaultValue;
        return res;
    }

    private String typeAsSql() {
        switch (type) {
            case "NUMBER":
                if (precision == 0)
                    return "NUMBER";
                else if (scale == 0)
                    return "NUMBER(" + precision + ")";
                else
                    return "NUMBER(" + precision + ", " + scale + ")";
            case "CHAR":
                return "CHAR(" + length + ")";
            case "VARCHAR2":
                return "VARCHAR2(" + length + ")";
            case "DATE":
                return "DATE";
            case "LONG":
                return "LONG";
            case "ROWID":
                return "ROWID";
            case "RAW":
                return "RAW(" + length + ")";
            case "LONG RAW":
                return "LONG RAW";
            default:
                return "UNKNOWN";
        }
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public boolean dbEquals(Column column) {
        return (type.equals(column.type)) && (length == column.length)
                && (precision == column.precision) && (scale == column.scale)
                && (nullable == column.nullable)
                && (defaultValue.trim().equals(column.defaultValue.trim()));
    }

    public String sqlComments(String tablename) {
        String res = "";
        String comments = getComment();
        if (comments == null)
            comments = "";
        res += "comment on column " + tablename + "." + getName() + "\n  is '"
                + escape(comments) + "';\n";
        return res;
    }

    @Override
    public String getTypeName() {
        return "COLUMN";
    }

    @Override
    public String sqlUpdate(DBObject destination) {
        return sqlCreate();
    }

}
