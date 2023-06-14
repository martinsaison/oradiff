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

public class View extends DBObject {

    private final ArrayList<String> columns = new ArrayList<>();
    private String source;

    public View(String name, String source) {
        super(name);
        this.source = removeR4(source);
    }

    public ArrayList<String> getColumns() {
        return columns;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void addColumn(String column) {
        columns.add(column);
    }

    public boolean dbEquals(View dst) {
        return textForDiff(dst.source).equals(textForDiff(source))
                && columns.equals(dst.columns);
    }

    @Override
    public String getTypeName() {
        return "VIEW";
    }

    @Override
    public String sqlCreate() {
        String res = "CREATE OR REPLACE VIEW " + getName() + "\n(";
        boolean first = true;
        for (String column : columns) {
            if (first)
                first = false;
            else
                res += ", ";
            res += column;
        }
        res += ")\n AS\n" + source + "\n/\n";
        return res;
    }

    @Override
    public String sqlUpdate(DBObject destination) {
        return destination.sqlCreate();
    }


}
