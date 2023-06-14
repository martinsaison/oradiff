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

import com.khodev.oradiff.diff.DiffOptions

class Trigger(
    name: String, var type: String, var event: String, var table: String,
    var `when`: String, var status: String, description: String, body: String
) : DBObject(name) {
    var body: String
    var description: String

    init {
        this.description = DBObject.Companion.removeR4(description)
        this.body = DBObject.Companion.removeR4(body)
    }

    fun dbEquals(dst: Trigger): Boolean {
        return DBObject.Companion.textForDiff(dst.description) == DBObject.Companion.textForDiff(description) && DBObject.Companion.textForDiff(
            dst.body
        ) == DBObject.Companion.textForDiff(body)
    }

    override val typeName: String
        get() = "TRIGGER"

    private fun sanitizeTriggerDescription(description: String): String {
        // "C4S01_DEVS"."TRG_ARTUC_FOUCEATRC" -> TRG_ARTUC_FOUCEATRC
        return description
            .replace(""""[A-Za-z0-9_]+"\."([A-Za-z0-9_]+)"""".toRegex(), "$1")
            .replace("""[A-Za-z0-9_]+\.([A-Za-z0-9_]+)""".toRegex(), "$1")
    }


    override fun sqlCreate(diffOptions: DiffOptions): String {
        return """
            CREATE OR REPLACE TRIGGER ${sanitizeTriggerDescription(description)}
            $body
            /
            
            """.trimIndent()
    }

    override fun sqlUpdate(diffOptions: DiffOptions, destination: DBObject): String {
        return destination.sqlCreate(diffOptions)
    }
}
