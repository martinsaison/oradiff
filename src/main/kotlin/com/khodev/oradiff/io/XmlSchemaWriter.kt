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

class XmlSchemaWriter(private val filename: String) : SchemaWriter {

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
        private fun <T> convertToXml(
            containerTagName: String, tagName: String,
            dbObjects: Map<String, T>
        ): Element {
            return convertToXml(containerTagName, tagName, dbObjects.values)
        }

        private fun <T> convertToXml(
            containerTagName: String, tagName: String, dbObjects: Collection<T>?
        ): Element {
            val elementList = Element(containerTagName)
            for (`object` in dbObjects!!) {
                elementList.addContent(getXml(tagName, `object`))
            }
            return elementList
        }

        private fun <T> getXml(tagName: String, `object`: T): Element {
            val element = Element(tagName)
            if (`object` is Constraint) {
                element.setAttribute("name", `object`.name)
                element.setAttribute("constraintType", `object`.constraintType)
                element.setAttribute("deferrable", `object`.deferrable)
                element.setAttribute("deferred", `object`.deferred)
                element.setAttribute("deleteRule", `object`.deleteRule)
                element.setAttribute("generated", `object`.generated)
                element.setAttribute("refConstraintName", `object`.refConstraintName)
                element.setAttribute("refUserName", `object`.refUserName)
                element.setAttribute("searchCondition", `object`.searchCondition)
                element.setAttribute("status", `object`.status)
                element.setAttribute("validated", `object`.validated)
                val xmlColumns = Element("columns")
                for (column in `object`.columns) xmlColumns.addContent(getXml("column", column))
                element.addContent(xmlColumns)
            }
            if (`object` is Source) {
                element.setAttribute("name", `object`.name)
                val xmlBody = Element("body")
                var bodyStr: String = ""
                for (line in `object`.body) bodyStr += line
                xmlBody.setText(bodyStr)
                element.addContent(xmlBody)
            }
            if (`object` is DBPackage) {
                element.setAttribute("name", `object`.name)
                val xmlDeclaration = Element("declaration")
                var bodyStr: String = ""
                for (line in `object`.declaration) bodyStr += line
                xmlDeclaration.setText(bodyStr)
                element.addContent(xmlDeclaration)
            }
            if (`object` is Sequence) {
                element.setAttribute("name", `object`.name)
                element.setAttribute("cacheSize", `object`.cacheSize.toString())
                element.setAttribute("cycleFlag", `object`.isCycleFlag.toString())
                element.setAttribute("incrementBy", `object`.incrementBy)
                element.setAttribute("lastNumber", `object`.lastNumber)
                element.setAttribute("maxValue", `object`.maxValue)
                element.setAttribute("minValue", `object`.minValue)
                element.setAttribute("orderFlag", `object`.isOrderFlag.toString())
            }
            if (`object` is Table) {
                element.setAttribute("tablespace", `object`.tablespace)
                element.setAttribute("name", `object`.name)
                element.setAttribute("owner", `object`.owner)
                element.setAttribute("comments", `object`.comments)
                element.addContent(convertToXml("columns", "column", `object`.columns))
                element.addContent(convertToXml("indexes", "index", `object`.indexes))
                element.addContent(
                    convertToXml(
                        "constraints", "constraint",
                        `object`.constraints
                    )
                )
                element.addContent(convertToXml("grants", "grant", `object`.grants))
                element.addContent(
                    convertToXml(
                        "publicSynonyms", "publicSynonym",
                        `object`.publicSynonyms
                    )
                )
                return element
            }
            if (`object` is Synonym) {
                element.setAttribute("name", `object`.name)
                element.setAttribute("owner", `object`.owner)
                element.setAttribute("name", `object`.name)
                element.setAttribute("tableOwner", `object`.tableOwner)
                element.setAttribute("tableName", `object`.tableName)
                return element
            }
            if (`object` is Trigger) {
                element.setAttribute("name", `object`.name)
                element.setAttribute("description", `object`.description)
                element.setAttribute("event", `object`.event)
                element.setAttribute("status", `object`.status)
                element.setAttribute("table", `object`.table)
                element.setAttribute("type", `object`.type)
                element.addContent(Element("when").setText(`object`.`when`))
                element.addContent(Element("body").setText(`object`.body))
            }
            if (`object` is View) {
                element.setAttribute("name", `object`.name)
                val xmlColumns = Element("columns")
                for (column in `object`.columns) xmlColumns.addContent(
                    Element("column").setAttribute(
                        "name", column
                    )
                )
                element.addContent(xmlColumns)
                element.addContent(Element("source").setText(`object`.source))
            }
            if (`object` is Column) {
                element.setAttribute("name", `object`.name)
                element.setAttribute("id", `object`.id.toString())
                element.setAttribute("type", `object`.type)
                element.setAttribute("length", `object`.length.toString())
                element.setAttribute("precision", `object`.precision.toString())
                element.setAttribute("scale", `object`.scale.toString())
                element.setAttribute("nullable", `object`.isNullable.toString())
                element.setAttribute("comments", `object`.comment)
                element.setAttribute("defaultValue", `object`.defaultValue)
            }
            if (`object` is Index) {
                element.setAttribute("tablespace", `object`.tablespace)
                element.setAttribute("name", `object`.name)
                element.setAttribute("owner", `object`.owner)
                element.setAttribute("type", `object`.type)
                element.setAttribute("isUnique", `object`.isUnique.toString())
                element.setAttribute("compression", `object`.compression)
                val xmlColumns = Element("columns")
                for (column in `object`.columns) xmlColumns.addContent(getXml("column", column))
                element.addContent(xmlColumns)
            }
            if (`object` is Grant) {
                element.setAttribute("name", `object`.grantee)
                element.setAttribute("selectPriv", (`object`.isSelectPriv.toString()))
                element.setAttribute("insertPriv", (`object`.isInsertPriv.toString()))
                element.setAttribute("deletePriv", (`object`.isDeletePriv.toString()))
                element.setAttribute("updatePriv", (`object`.isUpdatePriv.toString()))
                element.setAttribute(
                    "referencesPriv",
                    (`object`.isReferencesPriv.toString())
                )
                element.setAttribute("alterPriv", (`object`.isAlterPriv.toString()))
                element.setAttribute("indexPriv", (`object`.isIndexPriv.toString()))
            }
            if (`object` is IndexColumn) {
                element.setAttribute("name", `object`.name)
                element.setAttribute("position", `object`.position.toString())
            }
            return element
        }
    }
}
