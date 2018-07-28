package edu.cmu.isr.exttla

import edu.cmu.isr.exttla.grammar.ExtTLALexer
import edu.cmu.isr.exttla.grammar.ExtTLAParser
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import java.io.*
import java.nio.file.FileSystems

class ExtTLAConverter(input: InputStream) {
  private val tokens: CommonTokenStream
  private val parser: ExtTLAParser

  init {
    val lexer = ExtTLALexer(ANTLRInputStream(input))
    tokens = CommonTokenStream(lexer)
    parser = ExtTLAParser(tokens)
  }

  fun convert(): ExtTLASpec {
    // Walk through the tree
    val tree = parser.spec()
    val walker = ParseTreeWalker()
    val listener = ModuleBuilder(tokens)
    walker.walk(listener, tree)
    return listener.specObj
  }
}

fun main(args: Array<String>) {
  // Set the command line options
  val options = Options()
  options.addOption("o", true, "The output directory")
  options.addOption("m", true, "The main module name to generate the TLA+ file")

  val optionParser = DefaultParser()
  try {
    val line = optionParser.parse(options, args)
    var outputDir = "./"
    var mainModule: String? = null

    // Set the output directory if any
    if (line.hasOption("o")) {
      outputDir = line.getOptionValue("o")
      val outputDirFile = File(outputDir)
      if (!outputDirFile.exists()) {
        outputDirFile.mkdirs()
      }
    }

    if (line.hasOption("m")) {
      mainModule = line.getOptionValue("m")
    }

    // Initialize a ExtTLA converter for the file
    val spec = ExtTLASpec()
    for (arg in line.args) {
      val converter = ExtTLAConverter(FileInputStream(arg))
      // Convert the file into TLA specs
      spec.modules.putAll(converter.convert().modules)
    }

    if (mainModule != null && mainModule in spec.modules) {
      val m = spec.extendModule(spec.modules[mainModule]!!)
      writeTLAModule(outputDir, m)
    } else {
      for (m in spec.modules.values) {
        val extModule = spec.extendModule(m)
        //        System.out.println(extModule);
        writeTLAModule(outputDir, extModule)
      }
    }
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

private fun writeTLAModule(
  outputDir: String,
  m: ExtTLAModule
) {
  val filePath =
    FileSystems.getDefault().getPath(outputDir, m.name + ".tla")
  val out = BufferedWriter(FileWriter(filePath.toFile()))
  out.write(m.getText())
  out.flush()
  out.close()
  println("Create TLA+ Spec: " + m.name)
}