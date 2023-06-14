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

class Job(
    val name: String,
    private val what: String,
    private val nextdate: String,
    private val interval: String,
    private val broken: Boolean
)  {

    fun dbEquals(dst: Job): Boolean {
        return what == dst.what && nextdate == dst.nextdate && interval == dst.interval
    }

    val typeName: String
        get() = "JOB"

    fun sqlCreate(): String {
        var res = """		begin
		  sys.dbms_job.submit(job => $name,
		                      what => '${what}',
		                      next_date => to_date('${nextdate}', 'dd-mm-yyyy hh24:mi:ss'),
		                      interval => '${interval}');
"""
        if (broken) {
            res += """		  sys.dbms_job.broken(job => $name,
		                      broken => true,
		                      next_date => to_date('${nextdate}', 'dd-mm-yyyy hh24:mi:ss'));
"""
        }
        res += """		  commit;
		end;
"""
        return res
    }

    fun sqlUpdate(destination: Job): String {
        return destination.sqlCreate()
    }
}
