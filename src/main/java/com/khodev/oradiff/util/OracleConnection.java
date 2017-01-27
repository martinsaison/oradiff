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

package com.khodev.oradiff.util;

import java.sql.*;
import java.util.Properties;

public class OracleConnection {

    public String getTriggerFilter() {
        return triggerFilter;
    }

    public void setTriggerFilter(String triggerFilter) {
        this.triggerFilter = triggerFilter;
    }

    private Connection connection;

    private Statement statement;

    private String tableFilter;

    private String packageFilter;

    private String functionFilter;

    private String procedureFilter;

    private String viewFilter;

    private String sequenceFilter;

    private String triggerFilter;

    private Statement getStatement() throws SQLException {
        if (statement == null) {
            statement = connection.createStatement();
        }
        return statement;
    }

    public OracleConnection(String serverName, String portNumber, String sid,
                            String username, String password, String tableFilter,
                            String packageFilter, String functionFilter,
                            String procedureFilter, String viewFilter, String sequenceFilter,
                            String triggerFilter) throws ClassNotFoundException, SQLException {
        connection = null;
        // Load the JDBC driver
        String driverName = "oracle.jdbc.driver.OracleDriver";
        Class.forName(driverName);
        String url = String.format("jdbc:oracle:thin:%1$s/%2$s@%3$s:%4$s:%5$s",
                username, password, serverName, portNumber, sid);
        Properties info = new Properties();
        info.put("defaultRowPrefetch ", "100000");
        info.put("useFetchSizeWithLongColumn", "true");
        System.out.println(url);
        connection = DriverManager.getConnection(url, info);
        this.tableFilter = tableFilter;
        this.packageFilter = packageFilter;
        this.functionFilter = functionFilter;
        this.procedureFilter = procedureFilter;
        this.viewFilter = viewFilter;
        this.sequenceFilter = sequenceFilter;
        this.triggerFilter = triggerFilter;
    }

    public String getSequenceFilter() {
        return sequenceFilter;
    }

    public void setSequenceFilter(String sequenceFilter) {
        this.sequenceFilter = sequenceFilter;
    }

    public ResultSet query(String query) throws SQLException {
        return getStatement().executeQuery(query);
    }

    public Connection getConnection() {
        return connection;
    }

    public String getTableFilter() {
        return tableFilter;
    }

    public void setTableFilter(String tableFilter) {
        this.tableFilter = tableFilter;
    }

    public String getPackageFilter() {
        return packageFilter;
    }

    public void setPackageFilter(String packageFilter) {
        this.packageFilter = packageFilter;
    }

    public String getFunctionFilter() {
        return functionFilter;
    }

    public void setFunctionFilter(String functionFilter) {
        this.functionFilter = functionFilter;
    }

    public String getProcedureFilter() {
        return procedureFilter;
    }

    public void setProcedureFilter(String procedureFilter) {
        this.procedureFilter = procedureFilter;
    }

    public String getViewFilter() {
        return viewFilter;
    }

    public void setViewFilter(String viewFilter) {
        this.viewFilter = viewFilter;
    }

}
