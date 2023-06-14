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
package com.khodev.oradiff.diff

import com.khodev.oradiff.dbobjects.*
import com.khodev.oradiff.dbobjects.Sequence

class DBObjectDiff<T>(src: T, dst: T) {
    fun getName(): String {
        val o = src ?: dst
        return when(o) {
            is Table -> o.name
            is DBPackage -> o.name
            is Procedure -> o.name
            is com.khodev.oradiff.dbobjects.Function -> o.name
            is Job -> o.name
            is Sequence -> o.name
            is View -> o.name
            is Trigger -> o.name
            is Synonym -> o.name
            is PublicSynonym -> o.name
            else -> throw IllegalArgumentException("Unknown object type")
        }
    }

    val src: T?
    val dst: T?

    init {
        this.src = src
        this.dst = dst
    }

}
