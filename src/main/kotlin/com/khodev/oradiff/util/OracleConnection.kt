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
package com.khodev.oradiff.util

import java.sql.*
import java.util.*

class OracleConnection(
    serverName: String?, portNumber: String?, sid: String?,
    username: String?, password: String?, tableFilter: String?,
    packageFilter: String?, functionFilter: String?,
    procedureFilter: String?, viewFilter: String?, sequenceFilter: String?,
    triggerFilter: String?
) {
    var connection: Connection? = null

    @get:Throws(SQLException::class)
    private var statement: Statement? = null
        private get() {
            if (field == null) {
                field = connection!!.createStatement()
            }
            return field
        }
    var tableFilter: String?
    var packageFilter: String?
    var functionFilter: String?
    var procedureFilter: String?
    var viewFilter: String?
    var sequenceFilter: String?
    var triggerFilter: String?

    init {
        // Load the JDBC driver
        val driverName = "oracle.jdbc.driver.OracleDriver"
        Class.forName(driverName)
        val url = String.format(
            "jdbc:oracle:thin:%1\$s/%2\$s@%3\$s:%4\$s:%5\$s",
            username, password, serverName, portNumber, sid
        )
        val info = Properties()
        info["defaultRowPrefetch "] = "100000"
        info["useFetchSizeWithLongColumn"] = "true"
        println(url)
        connection = DriverManager.getConnection(url, info)
        this.tableFilter = tableFilter
        this.packageFilter = packageFilter
        this.functionFilter = functionFilter
        this.procedureFilter = procedureFilter
        this.viewFilter = viewFilter
        this.sequenceFilter = sequenceFilter
        this.triggerFilter = triggerFilter
    }

    @Throws(SQLException::class)
    fun query(query: String?): ResultSet {
        return statement!!.executeQuery(query)
    }
}
