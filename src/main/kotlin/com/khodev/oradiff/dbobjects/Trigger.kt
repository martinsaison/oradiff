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

class Trigger(
    val name: String,
    val type: String,
    val event: String,
    val table: String,
    val `when`: String,
    val status: String,
    val description: String,
    val body: String
) {

    fun textForDiff(text: String): String {
        val lines = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var res = ""
        for (line in lines) {
            val trimmedLine = line.trim { it <= ' ' }
            if (trimmedLine.isEmpty()) continue
            res += trimmedLine
        }
        return res
    }

    fun dbEquals(dst: Trigger): Boolean {
        return textForDiff(dst.description) == textForDiff(description) && textForDiff(
            dst.body
        ) == textForDiff(body)
    }

    val typeName: String
        get() = "TRIGGER"

    private fun sanitizeTriggerDescription(description: String): String {
        // "C4S01_DEVS"."TRG_ARTUC_FOUCEATRC" -> TRG_ARTUC_FOUCEATRC
        return description.replace(""""[A-Za-z0-9_]+"\."([A-Za-z0-9_]+)"""".toRegex(), "$1")
            .replace("""[A-Za-z0-9_]+\.([A-Za-z0-9_]+)""".toRegex(), "$1")
    }


    fun sqlCreate(): String {
        return """
            CREATE OR REPLACE TRIGGER ${sanitizeTriggerDescription(description)}
            $body
            /
            
            """.trimIndent()
    }

    fun sqlUpdate(destination: Trigger): String {
        return destination.sqlCreate()
    }
}
