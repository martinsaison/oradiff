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

import java.io.*;
import java.util.Hashtable;
import java.util.Map.Entry;

public class ReplaceManager {

    private final String name;
    private final Hashtable<String, String> keysValues;
    private static Hashtable<String, ReplaceManager> managers = null;
    boolean loaded = false;

    private ReplaceManager(String name) {
        this.name = name;
        keysValues = new Hashtable<>();
    }

    private String getFileName() {
        return name + ".txt";
    }

    private void load() {
        if (loaded)
            return;
        try {
            File file = new File(getFileName());
            if (!file.exists())
                return;
            String line;
            BufferedReader r = new BufferedReader(new FileReader(file));
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0)
                    continue;
                String[] vars = line.split("=");
                String tablespace = vars[0].trim();
                String varname = vars[1].trim();
                keysValues.put(tablespace, varname);
            }
            r.close();
            loaded = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try {
            File file = new File(getFileName());
            BufferedWriter w = new BufferedWriter(new FileWriter(file));
            for (Entry<String, String> entry : keysValues.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                w.write(key + "=" + value + "\n");
            }
            w.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getSubstitute(String key) {
        if (key.length() == 0)
            return key;
        load();
        if (keysValues.containsKey(key)) {
            return keysValues.get(key);
        }
        if (Configuration.saveNewSubstitutes && !keysValues.containsValue(key)) {
            keysValues.put(key, key);
            save();
        }
        return key;
    }

    public static ReplaceManager getManager(String name) {
        if (managers == null)
            managers = new Hashtable<>();
        if (!managers.containsKey(name))
            managers.put(name, new ReplaceManager(name));
        return managers.get(name);
    }

}
