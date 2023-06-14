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

package com.khodev.oradiff.dbobjects;

public class Trigger extends DBObject {

    private String body;
    private String description;
    private String event;
    private String status;
    private String table;
    private String type;
    private String when;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public Trigger(String name, String type, String event, String table,
                   String when, String status, String description, String body) {
        super(name);
        this.type = type;
        this.event = event;
        this.table = table;
        this.when = when;
        this.status = status;
        this.description = removeR4(description);
        this.body = removeR4(body);
    }

    public boolean dbEquals(Trigger dst) {
        return textForDiff(dst.description).equals(textForDiff(description))
                && textForDiff(dst.body).equals(textForDiff(body));
    }

    @Override
    public String getTypeName() {
        return "TRIGGER";
    }

    @Override
    public String sqlCreate() {
        return "CREATE OR REPLACE TRIGGER " + description + "\n" + body
                + "\n/\n";
    }

    @Override
    public String sqlUpdate(DBObject destination) {
        return destination.sqlCreate();
    }

}
