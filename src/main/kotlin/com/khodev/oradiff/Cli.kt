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
package com.khodev.oradiff

import com.khodev.oradiff.dbobjects.*
import com.khodev.oradiff.diff.DiffOptions
import com.khodev.oradiff.io.*
import com.khodev.oradiff.util.Configuration
import com.khodev.oradiff.util.DiffExporter
import org.apache.commons.cli.*
import org.xml.sax.SAXException
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.sql.SQLException
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.xml.parsers.ParserConfigurationException
import kotlin.system.exitProcess

class Cli private constructor(private val args: Array<String>) {
    private val options = Options()

    internal interface Argument {
        fun matches(): Boolean
        fun load(): Schema?
    }

    internal inner class DBArgument(arg: String) : Argument {
        private val pattern = Pattern.compile("(.*):(.*):(.*):(.*):(.*)")
        private val matcher: Matcher

        init {
            matcher = pattern.matcher(arg)
        }

        override fun matches(): Boolean {
            return matcher.matches()
        }

        override fun load(): Schema {
            matcher.reset()
            if (!matcher.find()) {
                return Schema()
            }
            val host = matcher.group(1)
            val port = matcher.group(2)
            val sid = matcher.group(3)
            val user = matcher.group(4)
            val pass = matcher.group(5)
            val reader: SchemaReader = ConnectedSchemaReader(host, port, sid, user, pass)
            return reader.get()
        }
    }

    internal inner class XmlFileArgument(val filename: String) : Argument {
        private val pattern = Pattern.compile("(.*\\.xml)")
        private val matcher: Matcher = pattern.matcher(filename)

        override fun matches(): Boolean {
            return matcher.matches()
        }

        override fun load(): Schema {
            return XmlSchemaReader(filename).get()
        }
    }

    internal inner class NullArgument(private var arg: String) : Argument {
        override fun matches(): Boolean {
            return arg.lowercase(Locale.getDefault()) == "null"
        }

        override fun load(): Schema {
            return Schema()
        }
    }

    @Throws(Exception::class)
    private fun getArgument(arg: String): Argument {
        var argument: Argument = DBArgument(arg)
        if (argument.matches()) {
            return argument
        }
        argument = XmlFileArgument(arg)
        if (argument.matches()) {
            return argument
        }
        argument = NullArgument(arg)
        if (argument.matches()) {
            return argument
        }
        throw Exception("Argument $arg is incorrect")
    }

    init {
        val commands = OptionGroup()
        commands.addOption(Option("h", "help", false, "show this help"))
        commands.addOption(
            Option.builder("s").longOpt("save").desc("save a schema to a file").hasArg(true).numberOfArgs(2)
                .argName("host:port:sid:user:pass> <filename.xml").build()
        )
        commands.addOption(
            Option.builder("d").longOpt("diff").desc("generate a diff between 2 schemas").hasArg(true).numberOfArgs(3)
                .argName("from> <to> <destdir").build()
        )
        commands.isRequired = true
        options.addOptionGroup(commands)
        options.addOption("sm", "save-map", false, "save new substitutions into a file")
        options.addOption(
            "rf",
            "rename-folder",
            false,
            "automatically change the destination folder to a new one if already exists"
        )
        options.addOption("on", "old-new", false, "generate old and new files")
        options.addOption("ts", "with-tablespace", false, "include tablespace in the diff")
        options.addOption("ioc", "with-object-comments", false, "include object comments in the diff")
        options.addOption("isc", "with-soruce-comments", false, "include source comments in the diff")
        options.addOption("igc", "with-grant-changes", false, "include grant changes in the diff")
    }

    private fun parse() {
        val parser: CommandLineParser = DefaultParser()
        try {
            val cmd = parser.parse(options, args)
            if (cmd.hasOption("h")) {
                help()
            }
            Configuration.saveNewSubstitutes = cmd.hasOption("sm")
            Configuration.renameFolderIfExists = cmd.hasOption("nf")
            Configuration.createOldNew = cmd.hasOption("on")
            if (cmd.hasOption('s')) {
                val db = getArgument(cmd.getOptionValues('s')[0]) as? DBArgument
                    ?: throw Exception("first parameter should be a database")
                val file = getArgument(cmd.getOptionValues('s')[1]) as? XmlFileArgument
                    ?: throw Exception("second parameter should be an xml file")
                val writer = XmlSchemaWriter(file.filename)
                writer.write(db.load())
                exitProcess(0)
            } else if (cmd.hasOption('d')) {
                val src = getArgument(cmd.getOptionValues('d')[0]).load()
                val dst = getArgument(cmd.getOptionValues('d')[1]).load()
                val path = cmd.getOptionValues('d')[2]
                val exporter = DiffExporter(src, dst, path)
                val diffOptions = DiffOptions(
                    withTablespace = cmd.hasOption("ts"),
                    ignoreSourceComments = !cmd.hasOption("isc"),
                    ignoreObjectComments = !cmd.hasOption("ioc"),
                    ignoreGrantChanges = !cmd.hasOption("igc")
                )
                exporter.run(diffOptions)
                exitProcess(0)
            }
        } catch (e: Exception) {
            println(e.message)
            exitProcess(1)
        }
    }

    private fun help() {
        val formater = HelpFormatter()
        formater.printHelp("java -jar oradiff.jar -<command> [...]", options)
        println("<from> and <to> can be either a database connection host:port:sid:user:pass or a file.xml")
        println("Examples")
        println("java -jar oradiff.jar --save localhost:1521:MYDB:USER:PASS backup.xml")
        println("java -jar oradiff.jar --diff localhost:1521:DB1:USER:PASS localhost:1521:DB2:USER:PASS path/to/results")
        println("java -jar oradiff.jar --diff backup1.xml backup2.xml path/to/results")
        println("java -jar oradiff.jar --diff backup1.xml localhost:1521:MYDB:USER:PASS backup.xml path/to/results")
        exitProcess(0)
    }

    companion object {
        @Throws(
            IOException::class,
            NoProviderFound::class,
            InstantiationException::class,
            IllegalAccessException::class,
            NoSuchMethodException::class,
            SQLException::class,
            ParserConfigurationException::class,
            InvocationTargetException::class,
            SAXException::class,
            ClassNotFoundException::class
        )
        @JvmStatic
        fun main(args: Array<String>) {
            val cli = Cli(args)
            cli.parse()
        }
    }
}