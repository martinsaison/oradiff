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
package com.khodev.oradiff.dbobjects

class Sequence(
    val name: String,
    val minValue: String,
    val maxValue: String,
    val incrementBy: String,
    val isCycleFlag: Boolean,
    val isOrderFlag: Boolean,
    val cacheSize: Int,
    val lastNumber: String
) {

    fun dbEquals(dst: Sequence): Boolean {
        return dst.minValue == minValue && dst.maxValue == maxValue && dst.incrementBy == incrementBy && dst.isCycleFlag == isCycleFlag && dst.isOrderFlag == isOrderFlag && dst.cacheSize == cacheSize
    }

    val typeName: String
        get() = "SEQUENCE"

    fun sqlCreate(): String {
        var res = """
             create sequence $name
             minvalue $minValue
             maxvalue $maxValue
             start with $lastNumber
             increment by $incrementBy
             
             """.trimIndent()
        res += if (cacheSize == 0) "nocache\n" else "cache $cacheSize\n"
        if (isCycleFlag) {
            res += "cycle\n"
        }
        if (isOrderFlag) {
            res += "order\n"
        }
        res += ";\n"
        return res
    }

    fun <T> sqlUpdate(dst: T): String {
        var res = "alter sequence $name\n"
        if ((dst as Sequence).minValue != minValue) {
            res += "minvalue " + dst.minValue + "\n"
        }
        if (dst.maxValue != maxValue) {
            res += "maxvalue " + dst.maxValue + "\n"
        }
        if (dst.incrementBy != incrementBy) {
            res += "increment by " + dst.incrementBy + "\n"
        }
        if (dst.cacheSize != cacheSize) {
            res += if (dst.cacheSize == 0) "nocache\n" else "cache " + dst.cacheSize + "\n"
        }
        if (dst.isCycleFlag != isCycleFlag) {
            res += if (dst.isCycleFlag) {
                "cycle\n"
            } else {
                "nocycle\n"
            }
        }
        if (dst.isOrderFlag != isOrderFlag) {
            res += if (dst.isOrderFlag) {
                "order\n"
            } else {
                "noorder\n"
            }
        }
        res += ";\n"
        return res
    }
}
