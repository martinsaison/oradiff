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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;

public class Equivalence {

    //	private String type;
    private final ArrayList<String> names = new ArrayList<>();
    private static ArrayList<Equivalence> equivalences = null;

    private Equivalence(String line) {
        String[] data = line.split(":");
//		type = data[0];
        String[] names = data[1].split(",");
        Collections.addAll(this.names, names);
    }

    private static void loadEquivalences(String filename)
            throws IOException {
        equivalences = new ArrayList<>();
        BufferedReader b;
        String line;
        File file = new File(filename);
        if (file.exists()) {
            b = new BufferedReader(new FileReader(filename));
            while ((line = b.readLine()) != null) {
                equivalences.add(new Equivalence(line));
            }
            b.close();
        }
    }

    private static ArrayList<Equivalence> getEquivalences()
            throws IOException {
        if (equivalences == null)
            loadEquivalences("equivalences.txt");
        return equivalences;
    }

    public static boolean matches(String name1, String name2)
            throws IOException {
        if (name1.toUpperCase().equals(name2.toUpperCase()))
            return true;
        for (Equivalence equivalence : getEquivalences()) {
            if (equivalence.unitMatches(name1, name2))
                return true;
        }
        return false;
    }

    private boolean unitMatches(String name1, String name2) {
        boolean found1 = false;
        boolean found2 = false;
        for (String name : names) {
            if (name1.toUpperCase().equals(name.toUpperCase()))
                found1 = true;
            if (name2.toUpperCase().equals(name.toUpperCase()))
                found2 = true;
        }
        return found1 && found2;
    }

}
