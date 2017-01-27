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

package com.khodev.oradiff;


import com.khodev.oradiff.dbobjects.Schema;
import com.khodev.oradiff.io.*;
import com.khodev.oradiff.util.Configuration;
import com.khodev.oradiff.util.DiffExporter;
import org.apache.commons.cli.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cli {

    private final String[] args;
    private final Options options = new Options();

    public static void main(String[] args) throws IOException, NoProviderFound, InstantiationException, IllegalAccessException, NoSuchMethodException, SQLException, ParserConfigurationException, InvocationTargetException, SAXException, ClassNotFoundException {
        Cli cli = new Cli(args);
        cli.parse();
    }

    interface Argument {
        boolean matches();

        Schema load();
    }

    class DBArgument implements Argument {

        final Pattern pattern = Pattern.compile("(.*):(.*):(.*):(.*):(.*)");
        final Matcher matcher;

        public DBArgument(String arg) {
            matcher = pattern.matcher(arg);
        }

        @Override
        public boolean matches() {
            return matcher.matches();
        }

        @Override
        public Schema load() {
            matcher.reset();
            if (!matcher.find()) {
                return new Schema();
            }
            String host = matcher.group(1);
            String port = matcher.group(2);
            String sid = matcher.group(3);
            String user = matcher.group(4);
            String pass = matcher.group(5);
            SchemaReader reader = new ConnectedSchemaReader(host, port, sid, user, pass);
            return reader.get();
        }

    }

    class XmlFileArgument implements Argument {

        private final String filename;

        public String getFilename() {
            return filename;
        }

        final Pattern pattern = Pattern.compile("(.*\\.xml)");
        final Matcher matcher;

        public XmlFileArgument(String arg) {
            matcher = pattern.matcher(arg);
            this.filename = arg;
        }

        @Override
        public boolean matches() {
            return matcher.matches();
        }

        @Override
        public Schema load() {
            return new XmlSchemaReader(filename).get();
        }

    }

    private Argument getArgument(String arg) throws Exception {
        Argument argument = new DBArgument(arg);
        if (argument.matches()) {
            return argument;
        }
        argument = new XmlFileArgument(arg);
        if (argument.matches()) {
            return argument;
        }
        throw new Exception("Argument " + arg + " is incorrect");
    }

    private Cli(String[] args) {
        this.args = args;
        OptionGroup commands = new OptionGroup();
        commands.addOption(new Option("h", "help", false, "show this help"));
        commands.addOption(Option.builder("s").longOpt("save").desc("save a schema to a file").hasArg(true).numberOfArgs(2).argName("host:port:sid:user:pass> <filename.xml").build());
        commands.addOption(Option.builder("d").longOpt("diff").desc("generate a diff between 2 schemas").hasArg(true).numberOfArgs(3).argName("from> <to> <destdir").build());
        commands.setRequired(true);
        options.addOptionGroup(commands);
        options.addOption("sm", "save-map", false, "save new substitutions into a file");
        options.addOption("rf", "rename-folder", false, "automatically change the destination folder to a new one if already exists");
        options.addOption("on", "old-new", false, "generate old and new files");
    }

    private void parse() {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                help();
            }

            Configuration.saveNewSubstitutes = cmd.hasOption("sm");
            Configuration.renameFolderIfExists = cmd.hasOption("nf");
            Configuration.createOldNew = cmd.hasOption("on");

            if (cmd.hasOption('s')) {
                Argument db = getArgument(cmd.getOptionValues('s')[0]);
                if (!(db instanceof DBArgument)) {
                    throw new Exception("first parameter should be a database");
                }
                Argument file = getArgument(cmd.getOptionValues('s')[1]);
                if (!(file instanceof XmlFileArgument)) {
                    throw new Exception("second parameter should be an xml file");
                }
                XmlSchemaWriter writer = new XmlSchemaWriter(((XmlFileArgument) file).getFilename());
                writer.write(db.load());
                System.exit(0);
            } else if (cmd.hasOption('d')) {
                Schema src = getArgument(cmd.getOptionValues('d')[0]).load();
                Schema dst = getArgument(cmd.getOptionValues('d')[1]).load();
                String path = cmd.getOptionValues('d')[2];
                DiffExporter exporter = new DiffExporter(src, dst, path);
                exporter.run();
                System.exit(0);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private void help() {
        HelpFormatter formater = new HelpFormatter();
        formater.printHelp("java -jar oradiff.jar -<command> [...]", options);
        System.out.println("<from> and <to> can be either a database connection host:port:sid:user:pass or a file.xml");
        System.out.println("Examples");
        System.out.println("java -jar oradiff.jar --save localhost:1521:MYDB:USER:PASS backup.xml");
        System.out.println("java -jar oradiff.jar --diff localhost:1521:DB1:USER:PASS localhost:1521:DB2:USER:PASS path/to/results");
        System.out.println("java -jar oradiff.jar --diff backup1.xml backup2.xml path/to/results");
        System.out.println("java -jar oradiff.jar --diff backup1.xml localhost:1521:MYDB:USER:PASS backup.xml path/to/results");
        System.exit(0);
    }

}