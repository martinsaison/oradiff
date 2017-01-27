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

public class Sequence extends DBObject {

    private int cacheSize;

    private boolean cycleFlag;

    private String incrementBy;

    private String lastNumber;

    private String maxValue;

    private String minValue;

    private boolean orderFlag;

    public Sequence(String name, String minValue, String maxValue,
                    String incrementBy, boolean cycleFlag, boolean orderFlag,
                    int cacheSize, String lastNumber) {
        super(name);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.incrementBy = incrementBy;
        this.cycleFlag = cycleFlag;
        this.orderFlag = orderFlag;
        this.cacheSize = cacheSize;
        this.lastNumber = lastNumber;
    }

    public boolean dbEquals(Sequence dst) {
        return dst.minValue.equals(minValue) && dst.maxValue.equals(maxValue)
                && dst.incrementBy.equals(incrementBy)
                && dst.cycleFlag == cycleFlag && dst.orderFlag == orderFlag
                && dst.cacheSize == cacheSize;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public String getIncrementBy() {
        return incrementBy;
    }

    public String getLastNumber() {
        return lastNumber;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public String getMinValue() {
        return minValue;
    }

    @Override
    public String getTypeName() {
        return "SEQUENCE";
    }

    public boolean isCycleFlag() {
        return cycleFlag;
    }

    public boolean isOrderFlag() {
        return orderFlag;
    }

    public String sqlCreate() {
        String res = "create sequence " + getName() + "\nminvalue " + minValue
                + "\n" + "maxvalue " + maxValue + "\n" + "start with "
                + lastNumber + "\n" + "increment by " + incrementBy + "\n";
        if (cacheSize == 0)
            res += "nocache\n";
        else
            res += "cache " + cacheSize + "\n";
        if (cycleFlag) {
            res += "cycle\n";
        }
        if (orderFlag) {
            res += "order\n";
        }
        res += ";\n";
        return res;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public void setCycleFlag(boolean cycleFlag) {
        this.cycleFlag = cycleFlag;
    }

    public void setIncrementBy(String incrementBy) {
        this.incrementBy = incrementBy;
    }

    public void setLastNumber(String lastNumber) {
        this.lastNumber = lastNumber;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public void setOrderFlag(boolean orderFlag) {
        this.orderFlag = orderFlag;
    }

    public String sqlUpdate(DBObject dst) {
        String res = "alter sequence " + getName() + "\n";
        if (!((Sequence) dst).minValue.equals(minValue)) {
            res += "minvalue " + ((Sequence) dst).minValue + "\n";
        }
        if (!((Sequence) dst).maxValue.equals(maxValue)) {
            res += "maxvalue " + ((Sequence) dst).maxValue + "\n";
        }
        if (!((Sequence) dst).incrementBy.equals(incrementBy)) {
            res += "increment by " + ((Sequence) dst).incrementBy + "\n";
        }
        if (((Sequence) dst).cacheSize != cacheSize) {
            if (((Sequence) dst).cacheSize == 0)
                res += "nocache\n";
            else
                res += "cache " + ((Sequence) dst).cacheSize + "\n";
        }
        if (((Sequence) dst).cycleFlag != cycleFlag) {
            if (((Sequence) dst).cycleFlag) {
                res += "cycle\n";
            } else {
                res += "nocycle\n";
            }
        }
        if (((Sequence) dst).orderFlag != orderFlag) {
            if (((Sequence) dst).orderFlag) {
                res += "order\n";
            } else {
                res += "noorder\n";
            }
        }
        res += ";\n";
        return res;

    }

}
