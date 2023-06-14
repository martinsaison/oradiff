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

package com.khodev.oradiff.io;

import com.khodev.oradiff.dbobjects.*;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;

public class XmlSchemaWriter implements SchemaWriter {

    private final String filename;

    private static <T extends DBObject> Element convertToXml(
            String containerTagName, String tagName,
            Hashtable<String, T> dbObjects) {
        return convertToXml(containerTagName, tagName, dbObjects.values());
    }

    private static <T extends DBObject> Element convertToXml(
            String containerTagName, String tagName, Collection<T> dbObjects) {
        Element elementList = new Element(containerTagName);
        for (DBObject object : dbObjects) {
            elementList.addContent(getXml(tagName, object));
        }
        return elementList;
    }

    private static Element getXml(String tagName, DBObject object) {
        Element element = new Element(tagName);
        element.setAttribute("name", object.getName());
        if (object instanceof Constraint) {
            Constraint constraint = (Constraint) object;
            element.setAttribute("constraintType", constraint.getConstraintType());
            element.setAttribute("deferrable", constraint.getDeferrable());
            element.setAttribute("deferred", constraint.getDeferred());
            element.setAttribute("deleteRule", constraint.getDeleteRule());
            element.setAttribute("generated", constraint.getGenerated());
            element.setAttribute("refConstraintName", constraint.getRefConstraintName());
            element.setAttribute("refUserName", constraint.getRefUserName());
            element.setAttribute("searchCondition", constraint.getSearchCondition());
            element.setAttribute("status", constraint.getStatus());
            element.setAttribute("validated", constraint.getValidated());
            Element xmlColumns = new Element("columns");
            for (IndexColumn column : constraint.getColumns())
                xmlColumns.addContent(getXml("column", column));
            element.addContent(xmlColumns);
        }
        if (object instanceof TablespaceObject) {
            element.setAttribute("tablespace", ((TablespaceObject) object).getTablespace());
        }
        if (object instanceof Source) {
            Source source = (Source) object;
            Element xmlBody = new Element("body");
            String bodyStr = "";
            for (String line : source.getBody())
                bodyStr += line;
            xmlBody.setText(bodyStr);
            element.addContent(xmlBody);
        }
        if (object instanceof DBPackage) {
            DBPackage pkg = (DBPackage) object;
            Element xmlDeclaration = new Element("declaration");
            String bodyStr = "";
            for (String line : pkg.getDeclaration())
                bodyStr += line;
            xmlDeclaration.setText(bodyStr);
            element.addContent(xmlDeclaration);
        }
        if (object instanceof Sequence) {
            Sequence sequence = (Sequence) object;
            element.setAttribute("cacheSize", Integer.toString(sequence.getCacheSize()));
            element.setAttribute("cycleFlag", Boolean.toString(sequence.isCycleFlag()));
            element.setAttribute("incrementBy", sequence.getIncrementBy());
            element.setAttribute("lastNumber", sequence.getLastNumber());
            element.setAttribute("maxValue", sequence.getMaxValue());
            element.setAttribute("minValue", sequence.getMinValue());
            element.setAttribute("orderFlag", Boolean.toString(sequence.isOrderFlag()));
        }
        if (object instanceof Table) {
            Table table = (Table) object;
            element.setAttribute("comments", table.getComments());
            element.addContent(XmlSchemaWriter.convertToXml("columns", "column", table.getColumns()));
            element.addContent(XmlSchemaWriter.convertToXml("indexes", "index", table.getIndexes()));
            element.addContent(XmlSchemaWriter.convertToXml("constraints", "constraint",
                    table.getConstraints()));
            element.addContent(XmlSchemaWriter.convertToXml("grants", "grant", table.getGrants()));
            element.addContent(XmlSchemaWriter.convertToXml("publicSynonyms", "publicSynonym",
                    table.getPublicSynonyms()));
            return element;

        }
        if (object instanceof Trigger) {
            Trigger trigger = (Trigger) object;
            element.setAttribute("description", trigger.getDescription());
            element.setAttribute("event", trigger.getEvent());
            element.setAttribute("status", trigger.getStatus());
            element.setAttribute("table", trigger.getTable());
            element.setAttribute("type", trigger.getType());
            element.addContent(new Element("when").setText(trigger.getWhen()));
            element.addContent(new Element("body").setText(trigger.getBody()));
        }
        if (object instanceof View) {
            View view = (View) object;
            Element xmlColumns = new Element("columns");
            for (String column : view.getColumns())
                xmlColumns.addContent(new Element("column").setAttribute(
                        "name", column));
            element.addContent(xmlColumns);
            element.addContent(new Element("source").setText(view.getSource()));
        }
        if (object instanceof Column) {
            Column column = (Column) object;
            element.setAttribute("id", Integer.toString(column.getId()));
            element.setAttribute("type", column.getType());
            element.setAttribute("length", Integer.toString(column.getLength()));
            element.setAttribute("precision", Integer.toString(column.getPrecision()));
            element.setAttribute("scale", Integer.toString(column.getScale()));
            element.setAttribute("nullable", Boolean.toString(column.isNullable()));
            element.setAttribute("comments", column.getComment());
            element.setAttribute("defaultValue", column.getDefaultValue());
        }
        if (object instanceof Index) {
            Index index = (Index) object;
            element.setAttribute("type", index.getType());
            element.setAttribute("isUnique", Boolean.toString(index.isUnique()));
            element.setAttribute("compression", index.getCompression());
            Element xmlColumns = new Element("columns");
            for (IndexColumn column : index.getColumns())
                xmlColumns.addContent(getXml("column", column));
            element.addContent(xmlColumns);
        }
        if (object instanceof Grant) {
            Grant grant = (Grant) object;
            element.setAttribute("selectPriv", Boolean.toString(grant.isSelectPriv()));
            element.setAttribute("insertPriv", Boolean.toString(grant.isInsertPriv()));
            element.setAttribute("deletePriv", Boolean.toString(grant.isDeletePriv()));
            element.setAttribute("updatePriv", Boolean.toString(grant.isUpdatePriv()));
            element.setAttribute("referencesPriv",
                    Boolean.toString(grant.isReferencesPriv()));
            element.setAttribute("alterPriv", Boolean.toString(grant.isAlterPriv()));
            element.setAttribute("indexPriv", Boolean.toString(grant.isIndexPriv()));
        }

        if (object instanceof IndexColumn) {
            IndexColumn indexColumn = (IndexColumn) object;
            element.setAttribute("position", Integer.toString(indexColumn.getPosition()));
        }
        return element;
    }

    public String getFilename() {
        return filename;
    }

    public XmlSchemaWriter(String filename) {
        this.filename = filename;
    }

    public void write(Schema schema) {
        XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
        XmlSchemaWriter writer = new XmlSchemaWriter(filename.replaceAll(".ini", "")
                + "_autosave.xml");
        try {
            output.output(getXml(schema),
                    new FileOutputStream(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Element getXml(Schema schema) throws IOException {
        Element database = new Element("database");
        database.addContent(convertToXml("tables", "table", schema.getDbTables()));
        database.addContent(convertToXml("packages", "package",
                schema.getDbPackages()));
        database.addContent(convertToXml("functions", "function",
                schema.getDbFunctions()));
        database.addContent(convertToXml("procedures", "procedure",
                schema.getDbProcedures()));
        database.addContent(convertToXml("views", "view", schema.getDbViews()));
        database.addContent(convertToXml("sequences", "sequence",
                schema.getDbSequences()));
        database.addContent(convertToXml("triggers", "trigger",
                schema.getDbTriggers()));
        return database;
    }
}
