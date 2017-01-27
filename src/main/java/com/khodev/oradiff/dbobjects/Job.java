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

public class Job extends DBObject {

    private final boolean broken;
    private final String interval;
    private final String nextdate;
    private final String what;

    public Job(String name, String what, String nextdate, String interval,
               boolean broken) {
        super(name);
        this.what = what;
        this.nextdate = nextdate;
        this.interval = interval;
        this.broken = broken;
    }

    public boolean dbEquals(Job dst) {
        return what.equals(dst.getWhat()) && nextdate.equals(dst.getNextdate())
                && interval.equals(dst.getInterval());
    }

    public boolean getBroken() {
        return broken;
    }

    private String getInterval() {
        return interval;
    }

    private String getNextdate() {
        return nextdate;
    }

    @Override
    public String getTypeName() {
        return "JOB";
    }

    private String getWhat() {
        return what;
    }

    public String sqlCreate() {
        String res = "		begin\n" + "		  " + "sys.dbms_job.submit(job => "
                + getName() + ",\n" + "		                      " + "what => '"
                + getWhat() + "',\n" + "		                      "
                + "next_date => to_date('" + getNextdate()
                + "', 'dd-mm-yyyy hh24:mi:ss'),\n"
                + "		                      interval => '" + getInterval()
                + "');\n";
        if (broken) {
            res += "		  sys.dbms_job.broken(job => " + getName() + ",\n"
                    + "		                      " + "broken => " + "true"
                    + ",\n" + "		                      "
                    + "next_date => to_date('" + getNextdate()
                    + "', 'dd-mm-yyyy hh24:mi:ss'));\n";
        }
        res += "		  commit;\n" + "		end;\n";
        return res;
    }

    @Override
    public String sqlUpdate(DBObject destination) {
        return destination.sqlCreate();
    }
}
