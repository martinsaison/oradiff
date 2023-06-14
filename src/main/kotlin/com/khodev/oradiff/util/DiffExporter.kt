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

import com.khodev.oradiff.dbobjects.Schema;
import com.khodev.oradiff.diff.*;

import java.io.*;

public class DiffExporter implements DiffListener {

    private String destFolder;
    private PrintWriter fAll;
    private final DiffManager manager = new DiffManager();
    private final Schema initialSchema;
    private final Schema finalSchema;

    public DiffExporter(Schema initialSchema, Schema finalSchema, String destFolder) {
        this.initialSchema = initialSchema;
        this.finalSchema = finalSchema;
        this.destFolder = destFolder;
        manager.addDiffListener(this);
    }

    private String getFileName(Operation diff, ObjectType obj, String name) {
        String diffStr;
        switch (diff) {
            case CREATE:
                diffStr = "create";
                break;
            case UPDATE:
                diffStr = "update";
                break;
            case DROP:
                diffStr = "drop";
                break;
            default:
                diffStr = "unknown";
        }
        String objStr;
        switch (obj) {
            case TABLE:
                objStr = "table";
                if (diff == Operation.UPDATE)
                    diffStr = "alter";
                break;
            case PACKAGE:
                objStr = "package";
                break;
            case PROCEDURE:
                objStr = "procedure";
                break;
            case FUNCTION:
                objStr = "function";
                break;
            case JOB:
                objStr = "job";
                break;
            case SEQUENCE:
                objStr = "sequence";
                break;
            case VIEW:
                objStr = "view";
                break;
            case TRIGGER:
                objStr = "trigger";
                break;
            case SYNONYM:
                objStr = "synonym";
                break;
            case PUBLIC_SYNONYM:
                objStr = "public_synonym";
                break;
            default:
                objStr = "unknown";
        }
        String folder = destFolder + "\\" + objStr;
        File dest = new File(folder);
        if (!dest.exists())
            dest.mkdir();
        return folder + "\\" + diffStr + "_" + objStr + "_" + name + ".sql";
    }

    public void diffPerformed(Operation diffType, ObjectType diffObject, String name, DBObjectDiff diff) {
        try {
            PrintWriter f = new PrintWriter(new BufferedOutputStream(new FileOutputStream(
                    getFileName(diffType, diffObject, name))));
            f.write(diff.sqlCreate());
            f.close();
            fAll.write(diff.sqlCreate());
            if (diff.getSrc() != null && Configuration.createOldNew) {
                f = new PrintWriter(new BufferedOutputStream(
                        new FileOutputStream(getFileName(diffType, diffObject,
                                name) + "_old")));
                f.write(diff.getSrc().sqlCreate());
                f.close();
            }
            if (diff.getDst() != null && Configuration.createOldNew) {
                f = new PrintWriter(new BufferedOutputStream(
                        new FileOutputStream(getFileName(diffType, diffObject,
                                name) + "_new")));
                f.write(diff.getDst().sqlCreate());
                f.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() throws IOException {
        File dest = new File(destFolder);
        int i = 0;
        String newName = destFolder;
        if (Configuration.renameFolderIfExists) {
            while (dest.exists()) {
                newName = destFolder + "(" + (++i) + ")";
                dest = new File(newName);
            }
            if (!destFolder.equals(newName))
                System.out.println("Folder " + destFolder
                        + " already exists, using " + newName);
        }
        destFolder = newName;
        dest.mkdir();
        fAll = new PrintWriter(new File(destFolder + "\\all_data.sql"));
        manager.doPatch(initialSchema, finalSchema);
        fAll.close();
    }
}
