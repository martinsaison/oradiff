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

public abstract class DBObject {

    static String escape(String s) {
        if (s == null)
            return "";
        return s.replace("'", "''");
    }

    static <T extends DBObject> ArrayList<T> newObjects(
            ArrayList<T> srcSet, ArrayList<T> dstSet) {
        ArrayList<T> res = new ArrayList<>();
        for (T dst : dstSet) {
            boolean found = false;
            for (T src : srcSet) {
                if (src.getName().equals(dst.getName())) {
                    found = true;
                    break;
                }
            }
            if (found)
                continue;
            res.add(dst);
        }
        return res;
    }

    public static String removeR4(String s) {
        return s;
        /*
		 * return s.replace("r4", "").replace("R4", "").replace("\"r4\".",
		 * "").replace("\"R4\".", "");
		 */
    }

    static String textForDiff(String text) {
        String[] lines = text.split("\n");
        String res = "";
        for (String line : lines) {
            line = line.trim();
            if (line.length() == 0)
                continue;
            res += line;
        }
        return res;
    }

    static ArrayList<String> textForDiff(ArrayList<String> text) {
        ArrayList<String> res = new ArrayList<>();
        for (String line : text) {
            line = removeR4(line.trim());
            if (line.length() == 0)
                continue;
            res.add(line);
        }
        return res;
    }

    private String name;

    DBObject(String name) {
        this.name = name;
    }

    String escapeName(String name) {
        return "\"" + name + "\"";
    }

    public String getName() {
        return name;
    }

    protected abstract String getTypeName();

    public void setName(String name) {
        this.name = name;
    }

    public abstract String sqlCreate();

    public String sqlDrop() {
        return "drop " + getTypeName() + " " + getName() + ";";
    }

    public abstract String sqlUpdate(DBObject destination);

}
