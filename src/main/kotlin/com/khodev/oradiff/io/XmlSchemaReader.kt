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
import com.khodev.oradiff.dbobjects.Function
import com.khodev.oradiff.util.ReplaceManager
import org.jdom2.Element
import org.jdom2.input.SAXBuilder
import java.io.*
import java.lang.Boolean
import java.util.*
import kotlin.Exception
import kotlin.String

class XmlSchemaReader(private val filename: String) : SchemaReader {
    override fun get(): Schema {
        val schema = Schema()
        try {
            val sxb = SAXBuilder()
            val document = sxb.build(File(filename))
            val database = document.rootElement
            schema.dbTables = getTables(database)
            schema.dbPackages = getPackages(database)
            schema.dbFunctions = getFunctions(database)
            schema.dbProcedures = getProcedures(database)
            schema.dbViews = getViews(database)
            schema.dbSequences = getSequences(database)
            schema.dbTriggers = getTriggers(database)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return schema
    }

    private fun getTriggers(database: Element): Hashtable<String, Trigger> {
        val triggers = Hashtable<String, Trigger>()
        val elementType = "triggers"
        val children = getChildren(database, elementType)
        for (child in children) {
            try {
                val obj = getTrigger(child)
                triggers[obj.name] = obj
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return triggers
    }

    private fun getChildren(element: Element, name: String): List<Element> {
        return element.getChild(name).children
    }

    private fun getTrigger(child: Element): Trigger {
        return Trigger(
            child.getAttributeValue("name"),
            child.getAttributeValue("type"),
            child.getAttributeValue("event"),
            child.getAttributeValue("table"),
            child.getChildText("when"),
            child.getAttributeValue("status"),
            child.getAttributeValue("description"),
            child.getChildText("body")
        )
    }

    private fun getSequences(database: Element): Hashtable<String, Sequence> {
        val sequences = Hashtable<String, Sequence>()
        for (e in getChildren(database, "sequences")) {
            try {
                val sequence = getSequence(e)
                sequences[sequence.name] = sequence
            } catch (e1: Exception) {
                e1.printStackTrace()
            }
        }
        return sequences
    }

    private fun getSequence(element: Element): Sequence {
        return Sequence(
            element.getAttributeValue("name"),
            element.getAttributeValue("minValue"),
            element.getAttributeValue("maxValue"),
            element.getAttributeValue("incrementBy"),
            Boolean.parseBoolean(element.getAttributeValue("cycleFlag")),
            Boolean.parseBoolean(element.getAttributeValue("orderFlag")),
            element.getAttributeValue("cacheSize").toInt(),
            element.getAttributeValue("lastNumber")
        )
    }

    private fun getViews(database: Element): Hashtable<String, View> {
        val views = Hashtable<String, View>()
        val children2 = getChildren(database, "views")
        for (child2 in children2) {
            try {
                val obj2 = getView(child2)
                views[obj2.name] = obj2
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
        return views
    }

    private fun getView(element: Element): View {
        val view = View(element.getAttributeValue("name"), element.getChildText("source"))
        val children = getChildren(element, "columns")
        for (child in children) try {
            view.columns.add(child.getAttributeValue("name"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return view
    }

    private fun getProcedures(database: Element): Hashtable<String, Procedure> {
        val procedures = Hashtable<String, Procedure>()
        val children3 = getChildren(database, "procedures")
        for (child3 in children3) {
            try {
                val obj3 = getProcedure(child3)
                procedures[obj3.name] = obj3
            } catch (e3: Exception) {
                e3.printStackTrace()
            }
        }
        return procedures
    }

    private fun getProcedure(element: Element): Procedure {
        val procedure = Procedure(element.getAttributeValue("name"))
        procedure.body.clear()
        val bodyStr = element.getChild("body").text
        val lines = bodyStr.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (line in lines) procedure.body.add(line + "\n")
        return procedure
    }

    private fun getFunctions(database: Element): Hashtable<String, Function> {
        val functions = Hashtable<String, Function>()
        val children4 = getChildren(database, "functions")
        for (child4 in children4) {
            try {
                val obj4 = getFunction(child4)
                functions[obj4.name] = obj4
            } catch (e4: Exception) {
                e4.printStackTrace()
            }
        }
        return functions
    }

    private fun getFunction(element: Element): Function {
        val function = Function(element.getAttributeValue("name"))
        function.body.clear()
        val bodyStr = element.getChild("body").text
        val lines = bodyStr.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (line in lines) function.body.add(line + "\n")
        return function
    }

    private fun getPackages(database: Element): Hashtable<String, DBPackage> {
        val packages = Hashtable<String, DBPackage>()
        val children5 = getChildren(database, "packages")
        for (child5 in children5) {
            val obj5 = getPackage(child5)
            packages[obj5.name] = obj5
        }
        return packages
    }

    private fun getPackage(element: Element): DBPackage {
        val pkg = DBPackage(element.getAttributeValue("name"))
        pkg.declaration.clear()
        val declarationStr = element.getChild("declaration").text
        var lines = declarationStr.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (line in lines) pkg.declaration.add(line + "\n")
        pkg.body.clear()
        val bodyStr = element.getChild("body").text
        lines = bodyStr.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (line in lines) pkg.body.add(line + "\n")
        return pkg
    }

    private fun getTables(database: Element): Hashtable<String, Table> {
        val tables = Hashtable<String, Table>()
        for (element in getChildren(database, "tables")) {
            tables[getTable(element).name] = getTable(element)
        }
        return tables
    }

    private fun getTable(element: Element): Table {
        val table = Table(
            "", element.getAttributeValue("name"), ReplaceManager.Companion.getManager("tablespaces")!!
                .getSubstitute(element.getAttributeValue("tablespace")), element.getAttributeValue("comments")
        )
        table.columns.clear()
        var children = getChildren(element, "columns")
        for (child in children) {
            table.columns.add(getColumn(table, child))
        }
        table.indexes.clear()
        children = getChildren(element, "indexes")
        for (child in children) {
            table.indexes.add(getIndex(table, child))
        }
        table.constraints.clear()
        children = getChildren(element, "constraints")
        for (child in children) {
            table.constraints.add(getConstraint(table, child))
        }
        table.grants.clear()
        children = getChildren(element, "grants")
        for (child in children) {
            table.grants.add(getGrant(table, child))
        }
        table.publicSynonyms.clear()
        children = getChildren(element, "publicSynonyms")
        for (child in children) {
            table.publicSynonyms.add(getPublicSynonym(table, child))
        }
        return table
    }

    private fun getPublicSynonym(table: Table, element: Element): PublicSynonym {
        return PublicSynonym(element.getAttributeValue("name"), table)
    }

    private fun getGrant(table: Table, element: Element): Grant {
        return Grant(
            element.getAttributeValue("name"),
            Boolean.parseBoolean(element.getAttributeValue("insertPriv")),
            Boolean.parseBoolean(element.getAttributeValue("selectPriv")),
            Boolean.parseBoolean(element.getAttributeValue("deletePriv")),
            Boolean.parseBoolean(element.getAttributeValue("updatePriv")),
            Boolean.parseBoolean(element.getAttributeValue("referencesPriv")),
            Boolean.parseBoolean(element.getAttributeValue("alterPriv")),
            Boolean.parseBoolean(element.getAttributeValue("indexPriv")),
            table
        )
    }

    private fun getConstraint(table: Table, element: Element): Constraint {
        val constraint = Constraint(
            element.getAttributeValue("name"),
            element.getAttributeValue("constraintType"),
            element.getAttributeValue("searchCondition"),
            element.getAttributeValue("refUserName"),
            element.getAttributeValue("refConstraintName"),
            element.getAttributeValue("deleteRule"),
            element.getAttributeValue("status"),
            element.getAttributeValue("deferrable"),
            element.getAttributeValue("deferred"),
            element.getAttributeValue("validated"),
            element.getAttributeValue("generated"),
            table
        )
        val container = Hashtable<String, IndexColumn>()
        val children = getChildren(element, "columns")
        for (child in children) {
            val indexColumn = IndexColumn(child.getAttributeValue("name"), child.getAttributeValue("position").toInt())
            container[indexColumn.name] = indexColumn
        }
        constraint.columns.addAll(container.values)
        return constraint
    }

    private fun getIndex(table: Table, element: Element): Index {
        val index = Index(
            element.getAttributeValue("owner"),
            element.getAttributeValue("name"),
            element.getAttributeValue("tablespace"),
            element.getAttributeValue("type"),
            Boolean.parseBoolean(element.getAttributeValue("isUnique")),
            element.getAttributeValue("compression"),
            table
        )
        val container = Hashtable<String, IndexColumn>()
        val children = getChildren(element, "columns")
        for (child in children) {
            val indexColumn = IndexColumn(child.getAttributeValue("name"), child.getAttributeValue("position").toInt())
            container[indexColumn.name] = indexColumn
        }
        index.columns.addAll(container.values)
        return index
    }

    private fun getColumn(table: Table, element: Element): Column {
        val column = Column(
            element.getAttributeValue("name"),
            element.getAttributeValue("id").toInt(),
            element.getAttributeValue("type"),
            element.getAttributeValue("length").toInt(),
            element.getAttributeValue("precision").toInt(),
            element.getAttributeValue("scale").toInt(),
            Boolean.parseBoolean(element.getAttributeValue("nullable")),
            element.getAttributeValue("comments"),
            element.getAttributeValue("defaultValue"),
            table
        )
        return column
    }
}
