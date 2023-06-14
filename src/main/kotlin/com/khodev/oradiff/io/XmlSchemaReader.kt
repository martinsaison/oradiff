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
import com.khodev.oradiff.util.ReplaceManager;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

public class XmlSchemaReader implements SchemaReader {

    private final String filename;

    public XmlSchemaReader(String filename) {
        this.filename = filename;
    }

    @Override
    public Schema get() {
        Schema schema = new Schema();
        try {
            SAXBuilder sxb = new SAXBuilder();
            org.jdom2.Document document = sxb.build(new File(filename));
            Element database = document.getRootElement();

            schema.setDbTables(getTables(database));

            schema.setDbPackages(getPackages(database));

            schema.setDbFunctions(getFunctions(database));

            schema.setDbProcedures(getProcedures(database));

            schema.setDbViews(getViews(database));

            schema.setDbSequences(getSequences(database));

            schema.setDbTriggers(getTriggers(database));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return schema;
    }

    private Hashtable<String, Trigger> getTriggers(Element database) {
        Hashtable<String, Trigger> triggers = new Hashtable<>();
        String elementType = "triggers";
        List<Element> children = getChildren(database, elementType);
        for (Element child : children) {
            try {
                Trigger obj = getTrigger(child);
                triggers.put(obj.getName(), obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return triggers;
    }

    private List<Element> getChildren(Element element, String name) {
        return element.getChild(name).getChildren();
    }

    private Trigger getTrigger(Element child) {
        return new Trigger(child.getAttributeValue("name"), child.getAttributeValue("type"), child.getAttributeValue("event"), child.getAttributeValue("table"),
                child.getChildText("when"), child.getAttributeValue("status"), child.getAttributeValue("description"),
                child.getChildText("body"));
    }

    private Hashtable<String, Sequence> getSequences(Element database) {
        Hashtable<String, Sequence> sequences = new Hashtable<>();
        for (Element e : getChildren(database, "sequences")) {
            try {
                Sequence sequence = getSequence(e);
                sequences.put(sequence.getName(), sequence);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return sequences;
    }

    private Sequence getSequence(Element element) {
        return new Sequence(element.getAttributeValue("name"),
                element.getAttributeValue("minValue"),
                element.getAttributeValue("maxValue"),
                element.getAttributeValue("incrementBy"),
                Boolean.parseBoolean(element.getAttributeValue("cycleFlag")),
                Boolean.parseBoolean(element.getAttributeValue("orderFlag")),
                Integer.parseInt(element.getAttributeValue("cacheSize")),
                element.getAttributeValue("lastNumber"));
    }

    private Hashtable<String, View> getViews(Element database) {
        Hashtable<String, View> views = new Hashtable<>();
        List<Element> children2 = getChildren(database, "views");
        for (Element child2 : children2) {
            try {
                View obj2 = getView(child2);
                views.put(obj2.getName(), obj2);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return views;
    }

    private View getView(Element element) {
        View view = new View(element.getAttributeValue("name"), element.getChildText("source"));
        List<Element> children = getChildren(element, "columns");
        for (Element child : children)
            try {
                view.getColumns().add(child.getAttributeValue("name"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        return view;
    }

    private Hashtable<String, Procedure> getProcedures(Element database) {
        Hashtable<String, Procedure> procedures = new Hashtable<>();
        List<Element> children3 = getChildren(database, "procedures");
        for (Element child3 : children3) {
            try {
                Procedure obj3 = getProcedure(child3);
                procedures.put(obj3.getName(), obj3);
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        }
        return procedures;
    }

    private Procedure getProcedure(Element element) {
        Procedure procedure = new Procedure(element.getAttributeValue("name"));
        procedure.getBody().clear();
        String bodyStr = element.getChild("body").getText();
        String[] lines = bodyStr.split("\n");
        for (String line : lines)
            procedure.getBody().add(line + "\n");
        return procedure;
    }

    private Hashtable<String, Function> getFunctions(Element database) {
        Hashtable<String, Function> functions = new Hashtable<>();
        List<Element> children4 = getChildren(database, "functions");
        for (Element child4 : children4) {
            try {
                Function obj4 = getFunction(child4);
                functions.put(obj4.getName(), obj4);
            } catch (Exception e4) {
                e4.printStackTrace();
            }
        }
        return functions;
    }

    private Function getFunction(Element element) {
        Function function = new Function(element.getAttributeValue("name"));
        function.getBody().clear();
        String bodyStr = element.getChild("body").getText();
        String[] lines = bodyStr.split("\n");
        for (String line : lines)
            function.getBody().add(line + "\n");
        return function;
    }

    private Hashtable<String, DBPackage> getPackages(Element database) {
        Hashtable<String, DBPackage> packages = new Hashtable<>();
        List<Element> children5 = getChildren(database, "packages");
        for (Element child5 : children5) {
            DBPackage obj5 = getPackage(child5);
            packages.put(obj5.getName(), obj5);
        }
        return packages;
    }

    private DBPackage getPackage(Element element) {
        DBPackage pkg = new DBPackage(element.getAttributeValue("name"));
        pkg.getDeclaration().clear();
        String declarationStr = element.getChild("declaration").getText();
        String[] lines = declarationStr.split("\n");
        for (String line : lines)
            pkg.getDeclaration().add(line + "\n");
        pkg.getBody().clear();
        String bodyStr = element.getChild("body").getText();
        lines = bodyStr.split("\n");
        for (String line : lines)
            pkg.getBody().add(line + "\n");
        return pkg;
    }

    private Hashtable<String, Table> getTables(Element database) {
        Hashtable<String, Table> tables = new Hashtable<>();
        for (Element element : getChildren(database, "tables")) {
            tables.put(getTable(element).getName(), getTable(element));
        }
        return tables;
    }

    private Table getTable(Element element) {
        Table table = new Table("", element.getAttributeValue("name"), ReplaceManager.getManager("tablespaces").getSubstitute(element.getAttributeValue("tablespace")), element.getAttributeValue("comments"));

        table.getColumns().clear();
        List<Element> children = getChildren(element, "columns");
        for (Element child : children) {
            table.getColumns().add(getColumn(table, child));
        }

        table.getIndexes().clear();
        children = getChildren(element, "indexes");
        for (Element child : children) {
            table.getIndexes().add(getIndex(table, child));
        }

        table.getConstraints().clear();
        children = getChildren(element, "constraints");
        for (Element child : children) {
            table.getConstraints().add(getConstraint(table, child));
        }

        table.getGrants().clear();
        children = getChildren(element, "grants");
        for (Element child : children) {
            table.getGrants().add(getGrant(table, child));
        }

        table.getPublicSynonyms().clear();
        children = getChildren(element, "publicSynonyms");
        for (Element child : children) {
            table.getPublicSynonyms().add(getPublicSynonym(table, child));
        }

        return table;
    }

    private PublicSynonym getPublicSynonym(Table table, Element element) {
        return new PublicSynonym(element.getAttributeValue("name"), table);
    }

    private Grant getGrant(Table table, Element element) {
        return new Grant(
                element.getAttributeValue("name"),
                Boolean.parseBoolean(element.getAttributeValue("insertPriv")),
                Boolean.parseBoolean(element.getAttributeValue("selectPriv")),
                Boolean.parseBoolean(element.getAttributeValue("deletePriv")),
                Boolean.parseBoolean(element.getAttributeValue("updatePriv")),
                Boolean.parseBoolean(element.getAttributeValue("referencesPriv")),
                Boolean.parseBoolean(element.getAttributeValue("alterPriv")),
                Boolean.parseBoolean(element.getAttributeValue("indexPriv")),
                table);
    }

    private Constraint getConstraint(Table table, Element element) {
        Constraint constraint = new Constraint(element.getAttributeValue("name"),
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
                table);
        Hashtable<String, IndexColumn> container = new Hashtable<>();
        List<Element> children = getChildren(element, "columns");
        for (Element child : children) {
            IndexColumn indexColumn = new IndexColumn(child.getAttributeValue("name"), Integer.parseInt(child.getAttributeValue("position")));
            container.put(indexColumn.getName(), indexColumn);
        }
        constraint.getColumns().addAll(container.values());
        return constraint;
    }

    private Index getIndex(Table table, Element element) {
        Index index = new Index(
                element.getAttributeValue("owner"),
                element.getAttributeValue("name"),
                element.getAttributeValue("tablespace"),
                element.getAttributeValue("type"),
                Boolean.parseBoolean(element.getAttributeValue("isUnique")),
                element.getAttributeValue("compression"),
                table);

        Hashtable<String, IndexColumn> container = new Hashtable<>();
        List<Element> children = getChildren(element, "columns");
        for (Element child : children) {
            IndexColumn indexColumn = new IndexColumn(child.getAttributeValue("name"), Integer.parseInt(child.getAttributeValue("position")));
            container.put(indexColumn.getName(), indexColumn);
        }
        index.getColumns().addAll(container.values());
        return index;
    }

    private Column getColumn(Table table, Element element) {
        Column column = new Column(element.getAttributeValue("name"),
                Integer.parseInt(element.getAttributeValue("id")),
                element.getAttributeValue("type"),
                Integer.parseInt(element.getAttributeValue("length")),
                Integer.parseInt(element.getAttributeValue("precision")),
                Integer.parseInt(element.getAttributeValue("scale")),
                Boolean.parseBoolean(element.getAttributeValue("nullable")),
                element.getAttributeValue("comments"),
                element.getAttributeValue("defaultValue"));
        column.setParent(table);
        return column;
    }

}
