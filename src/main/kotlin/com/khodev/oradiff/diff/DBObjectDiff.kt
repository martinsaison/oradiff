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

package com.khodev.oradiff.diff;

import com.khodev.oradiff.dbobjects.DBObject;

public class DBObjectDiff<T extends DBObject> {

    private final T src;

    public T getSrc() {
        return src;
    }

    public T getDst() {
        return dst;
    }

    private final T dst;

    public DBObjectDiff(T src, T dst) {
        this.src = src;
        this.dst = dst;
    }

    public String getName() {
        if (src == null)
            return dst.getName();
        else
            return src.getName();
    }

    public String sqlCreate() {
        if (src == null)
            return dst.sqlCreate();
        else if (dst == null)
            return src.sqlDrop();
        else
            return src.sqlUpdate(dst);
    }

}
