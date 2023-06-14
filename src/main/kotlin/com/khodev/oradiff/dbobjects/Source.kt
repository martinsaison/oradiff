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

public abstract class Source extends DBObject {

    private final ArrayList<String> body = new ArrayList<>();

    Source(String name) {
        super(name);
    }

    ArrayList<String> getSource(String type) {
        return body;
    }

    public void append(String type, int line, String text) {
        getSource(type).add(text);
    }

    public boolean dbEquals(Source dst) {
        return textForDiff(body).equals(textForDiff(dst.getBody()));
    }

    public ArrayList<String> getBody() {
        return body;
    }

    public String sqlCreate() {
        String res = "CREATE OR REPLACE ";
        for (String line : getBody()) {
            res += removeR4(line);
        }
        res += "/\n";
        return res;
    }

    public String sqlUpdate(DBObject destination) {
        return destination.sqlCreate();
    }

}
