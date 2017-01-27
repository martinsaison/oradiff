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

package com.khodev.oradiff.diff;

import com.khodev.oradiff.dbobjects.*;
import java.io.IOException;
import java.util.ArrayList;

public class DiffManager {

    private final ArrayList<DiffListener> diffListeners = new ArrayList<>();

    public void addDiffListener(DiffListener diffListener) {
        diffListeners.add(diffListener);
    }

    public void removeDiffListener(DiffListener diffListener) {
        diffListeners.remove(diffListener);
    }

    private <T extends DBObject> void doDiff(Operation diffType, ObjectType diffObject,
                                             String name, DBObjectDiff<T> diff) {
        for (DiffListener diffListener : diffListeners)
            diffListener.diffPerformed(diffType, diffObject, name, diff);
    }

    public void doPatch(Schema initialSchema, Schema finalSchema)
            throws IOException {
        // Tables
        for (Table o : initialSchema.newTables(finalSchema)) {
            doDiff(Operation.CREATE, ObjectType.TABLE, o.getName(),
                    new DBObjectDiff<>(null, o));
            for (PublicSynonym s : o.getPublicSynonyms()) {
                doDiff(Operation.CREATE, ObjectType.PUBLIC_SYNONYM, s.getName(),
                        new DBObjectDiff<>(null, s));
            }
        }
        for (DBObjectDiff<Table> d : initialSchema.updateTables(finalSchema)) {
            doDiff(Operation.UPDATE, ObjectType.TABLE, d.getName(), d);
        }
        for (Table o : finalSchema.newTables(initialSchema)) {
            doDiff(Operation.DROP, ObjectType.TABLE, o.getName(), new DBObjectDiff<>(
                    o, null));
            for (PublicSynonym s : o.getPublicSynonyms()) {
                doDiff(Operation.DROP, ObjectType.PUBLIC_SYNONYM, s.getName(),
                        new DBObjectDiff<>(s, null));
            }
        }
        // Packages
        for (DBPackage o : initialSchema.newPackages(finalSchema)) {
            doDiff(Operation.CREATE, ObjectType.PACKAGE, o.getName(),
                    new DBObjectDiff<>(null, o));
        }
        for (DBObjectDiff<DBPackage> d : initialSchema
                .updatePackages(finalSchema)) {
            doDiff(Operation.UPDATE, ObjectType.PACKAGE, d.getName(), d);
        }
        for (DBPackage o : finalSchema.newPackages(initialSchema)) {
            doDiff(Operation.DROP, ObjectType.PACKAGE, o.getName(),
                    new DBObjectDiff<>(o, null));
        }
        // Procedures
        for (Procedure o : initialSchema.newProcedures(finalSchema)) {
            doDiff(Operation.CREATE, ObjectType.PROCEDURE, o.getName(),
                    new DBObjectDiff<>(null, o));
        }
        for (DBObjectDiff<Procedure> d : initialSchema
                .updateProcedures(finalSchema)) {
            doDiff(Operation.UPDATE, ObjectType.PROCEDURE, d.getName(), d);
        }
        for (Procedure o : finalSchema.newProcedures(initialSchema)) {
            doDiff(Operation.DROP, ObjectType.PROCEDURE, o.getName(),
                    new DBObjectDiff<>(o, null));
        }
        // Functions
        for (Function o : initialSchema.newFunctions(finalSchema)) {
            doDiff(Operation.CREATE, ObjectType.FUNCTION, o.getName(),
                    new DBObjectDiff<>(null, o));
        }
        for (DBObjectDiff<Function> d : initialSchema
                .updateFunctions(finalSchema)) {
            doDiff(Operation.UPDATE, ObjectType.FUNCTION, d.getName(), d);
        }
        for (Function o : finalSchema.newFunctions(initialSchema)) {
            doDiff(Operation.DROP, ObjectType.FUNCTION, o.getName(),
                    new DBObjectDiff<>(o, null));
        }

        // Jobs

        for (Job o : initialSchema.newJobs(finalSchema)) {
            doDiff(Operation.CREATE, ObjectType.JOB, o.getName(), new DBObjectDiff<>(null,
                    o));
        }
        for (DBObjectDiff<Job> d :
                initialSchema.updateJobs(finalSchema)) {
            doDiff(Operation.UPDATE, ObjectType.JOB,
                    d.getName(), d);
        }
        for (Job o :
                finalSchema.newJobs(initialSchema)) {
            doDiff(Operation.DROP, ObjectType.JOB,
                    o.getName(), new DBObjectDiff<>(o, null));
        }

        // Sequences
        for (Sequence o : initialSchema.newSequences(finalSchema)) {
            doDiff(Operation.CREATE, ObjectType.SEQUENCE, o.getName(),
                    new DBObjectDiff<>(null, o));
        }
        for (DBObjectDiff<Sequence> d : initialSchema
                .updateSequences(finalSchema)) {
            doDiff(Operation.UPDATE, ObjectType.SEQUENCE, d.getName(), d);
        }
        for (Sequence o : finalSchema.newSequences(initialSchema)) {
            doDiff(Operation.DROP, ObjectType.SEQUENCE, o.getName(),
                    new DBObjectDiff<>(o, null));
        }
        // Triggers
        for (Trigger o : initialSchema.newTriggers(finalSchema)) {
            doDiff(Operation.CREATE, ObjectType.TRIGGER, o.getName(),
                    new DBObjectDiff<>(null, o));
        }
        for (DBObjectDiff<Trigger> d : initialSchema
                .updateTriggers(finalSchema)) {
            doDiff(Operation.UPDATE, ObjectType.TRIGGER, d.getName(), d);
        }
        for (Trigger o : finalSchema.newTriggers(initialSchema)) {
            doDiff(Operation.DROP, ObjectType.TRIGGER, o.getName(),
                    new DBObjectDiff<>(o, null));
        }
        // Synonyms
        for (Synonym o : initialSchema.newSynonyms(finalSchema)) {
            doDiff(Operation.CREATE, ObjectType.SYNONYM, o.getName(),
                    new DBObjectDiff<>(null, o));
        }
        for (DBObjectDiff<Synonym> d : initialSchema
                .updateSynonyms(finalSchema)) {
            doDiff(Operation.UPDATE, ObjectType.SYNONYM, d.getName(), d);
        }
        for (Synonym o : finalSchema.newSynonyms(initialSchema)) {
            doDiff(Operation.DROP, ObjectType.SYNONYM, o.getName(),
                    new DBObjectDiff<>(o, null));
        }
        // Views
        for (View o : initialSchema.newViews(finalSchema)) {
            doDiff(Operation.CREATE, ObjectType.VIEW, o.getName(), new DBObjectDiff<>(
                    null, o));
        }
        for (DBObjectDiff<View> d : initialSchema.updateViews(finalSchema)) {
            doDiff(Operation.UPDATE, ObjectType.VIEW, d.getName(), d);
        }
        for (View o : finalSchema.newViews(initialSchema)) {
            doDiff(Operation.DROP, ObjectType.VIEW, o.getName(), new DBObjectDiff<>(o,
                    null));
        }
    }
}
