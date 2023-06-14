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
package com.khodev.oradiff.io

import com.khodev.oradiff.dbobjects.*
import org.jdom2.Element
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class XmlSchemaWriter(val filename: String) : SchemaWriter {

    fun write(schema: Schema) {
        val output = XMLOutputter(Format.getPrettyFormat())
        val writer = XmlSchemaWriter(
            filename.replace(".ini".toRegex(), "")
                    + "_autosave.xml"
        )
        try {
            output.output(
                getXml(schema),
                FileOutputStream(filename)
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun getXml(schema: Schema): Element {
        val database = Element("database")
        database.addContent(convertToXml("tables", "table", schema.dbTables))
        database.addContent(
            convertToXml(
                "packages", "package",
                schema.dbPackages
            )
        )
        database.addContent(
            convertToXml(
                "functions", "function",
                schema.dbFunctions
            )
        )
        database.addContent(
            convertToXml(
                "procedures", "procedure",
                schema.dbProcedures
            )
        )
        database.addContent(convertToXml("views", "view", schema.dbViews))
        database.addContent(
            convertToXml(
                "sequences", "sequence",
                schema.dbSequences
            )
        )
        database.addContent(
            convertToXml(
                "triggers", "trigger",
                schema.dbTriggers
            )
        )
        return database
    }

    companion object {
        private fun <T : DBObject> convertToXml(
            containerTagName: String, tagName: String,
            dbObjects: Hashtable<String, T>
        ): Element {
            return convertToXml(containerTagName, tagName, dbObjects.values)
        }

        private fun <T : DBObject> convertToXml(
            containerTagName: String, tagName: String, dbObjects: Collection<T>?
        ): Element {
            val elementList = Element(containerTagName)
            for (`object` in dbObjects!!) {
                elementList.addContent(getXml(tagName, `object`))
            }
            return elementList
        }

        private fun getXml(tagName: String, `object`: DBObject): Element {
            val element = Element(tagName)
            element.setAttribute("name", `object`.name)
            if (`object` is Constraint) {
                val constraint = `object`
                element.setAttribute("constraintType", constraint.constraintType)
                element.setAttribute("deferrable", constraint.deferrable)
                element.setAttribute("deferred", constraint.deferred)
                element.setAttribute("deleteRule", constraint.deleteRule)
                element.setAttribute("generated", constraint.generated)
                element.setAttribute("refConstraintName", constraint.refConstraintName)
                element.setAttribute("refUserName", constraint.refUserName)
                element.setAttribute("searchCondition", constraint.searchCondition)
                element.setAttribute("status", constraint.status)
                element.setAttribute("validated", constraint.validated)
                val xmlColumns = Element("columns")
                for (column in constraint.columns) xmlColumns.addContent(getXml("column", column))
                element.addContent(xmlColumns)
            }
            if (`object` is TablespaceObject) {
                element.setAttribute("tablespace", `object`.tablespace)
            }
            if (`object` is Source) {
                val xmlBody = Element("body")
                var bodyStr: String = ""
                for (line in `object`.body) bodyStr += line
                xmlBody.setText(bodyStr)
                element.addContent(xmlBody)
            }
            if (`object` is DBPackage) {
                val xmlDeclaration = Element("declaration")
                var bodyStr: String = ""
                for (line in `object`.declaration) bodyStr += line
                xmlDeclaration.setText(bodyStr)
                element.addContent(xmlDeclaration)
            }
            if (`object` is Sequence) {
                val sequence = `object`
                element.setAttribute("cacheSize", Integer.toString(sequence.cacheSize))
                element.setAttribute("cycleFlag", sequence.isCycleFlag.toString())
                element.setAttribute("incrementBy", sequence.incrementBy)
                element.setAttribute("lastNumber", sequence.lastNumber)
                element.setAttribute("maxValue", sequence.maxValue)
                element.setAttribute("minValue", sequence.minValue)
                element.setAttribute("orderFlag", sequence.isOrderFlag.toString())
            }
            if (`object` is Table) {
                val table = `object`
                element.setAttribute("owner", table.owner)
                element.setAttribute("comments", table.comments)
                element.addContent(convertToXml("columns", "column", table.columns))
                element.addContent(convertToXml("indexes", "index", table.indexes))
                element.addContent(
                    convertToXml(
                        "constraints", "constraint",
                        table.constraints
                    )
                )
                element.addContent(convertToXml("grants", "grant", table.grants))
                element.addContent(
                    convertToXml(
                        "publicSynonyms", "publicSynonym",
                        table.publicSynonyms
                    )
                )
                return element
            }
            if (`object` is Synonym) {
                val synonym = `object`
                element.setAttribute("owner", synonym.owner)
                element.setAttribute("name", synonym.name)
                element.setAttribute("tableOwner", synonym.tableOwner)
                element.setAttribute("tableName", synonym.tableName)
                return element
            }
            if (`object` is Trigger) {
                val trigger = `object`
                element.setAttribute("description", trigger.description)
                element.setAttribute("event", trigger.event)
                element.setAttribute("status", trigger.status)
                element.setAttribute("table", trigger.table)
                element.setAttribute("type", trigger.type)
                element.addContent(Element("when").setText(trigger.`when`))
                element.addContent(Element("body").setText(trigger.body))
            }
            if (`object` is View) {
                val view = `object`
                val xmlColumns = Element("columns")
                for (column in view.columns) xmlColumns.addContent(
                    Element("column").setAttribute(
                        "name", column
                    )
                )
                element.addContent(xmlColumns)
                element.addContent(Element("source").setText(view.source))
            }
            if (`object` is Column) {
                val column = `object`
                element.setAttribute("id", Integer.toString(column.id))
                element.setAttribute("type", column.type)
                element.setAttribute("length", Integer.toString(column.length))
                element.setAttribute("precision", Integer.toString(column.precision))
                element.setAttribute("scale", Integer.toString(column.scale))
                element.setAttribute("nullable", column.isNullable.toString())
                element.setAttribute("comments", column.comment)
                element.setAttribute("defaultValue", column.defaultValue)
            }
            if (`object` is Index) {
                val index = `object`
                element.setAttribute("owner", index.owner)
                element.setAttribute("type", index.type)
                element.setAttribute("isUnique", index.isUnique.toString())
                element.setAttribute("compression", index.compression)
                val xmlColumns = Element("columns")
                for (column in index.columns) xmlColumns.addContent(getXml("column", column))
                element.addContent(xmlColumns)
            }
            if (`object` is Grant) {
                val grant = `object`
                element.setAttribute("selectPriv", (grant.isSelectPriv.toString()))
                element.setAttribute("insertPriv", (grant.isInsertPriv.toString()))
                element.setAttribute("deletePriv", (grant.isDeletePriv.toString()))
                element.setAttribute("updatePriv", (grant.isUpdatePriv.toString()))
                element.setAttribute(
                    "referencesPriv",
                    (grant.isReferencesPriv.toString())
                )
                element.setAttribute("alterPriv", (grant.isAlterPriv.toString()))
                element.setAttribute("indexPriv", (grant.isIndexPriv.toString()))
            }
            if (`object` is IndexColumn) {
                element.setAttribute("position", Integer.toString(`object`.position))
            }
            return element
        }
    }
}
