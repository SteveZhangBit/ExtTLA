package edu.cmu.isr;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 *
 */
public class ExtTLAConverter {

  private CommonTokenStream tokens;
  private edu.cmu.isr.ExtTLAParser parser;

  public ExtTLAConverter(InputStream in) {
    try {
      edu.cmu.isr.ExtTLALexer lexer = new edu.cmu.isr.ExtTLALexer(
          new ANTLRInputStream(in));
      tokens = new CommonTokenStream(lexer);
      parser = new edu.cmu.isr.ExtTLAParser(tokens);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // Set the command line options
    Options options = new Options();
    options.addOption("o", true, "The output directory");

    CommandLineParser optionParser = new DefaultParser();
    try {
      CommandLine line = optionParser.parse(options, args);
      String outputDir = "./";
      // Set the output directory if any
      if (line.hasOption("o")) {
        outputDir = line.getOptionValue("o");
        File outputDirFile = new File(outputDir);
        if (!outputDirFile.exists()) {
          outputDirFile.mkdirs();
        }
      }

      // Initialize a ExtTLA converter for the file
      ExtTLASpec spec = new ExtTLASpec();
      for (String arg : line.getArgs()) {
        ExtTLAConverter converter = new ExtTLAConverter(
            new FileInputStream(arg));
        // Convert the file into TLA specs
        spec.getModules().putAll(converter.convert().getModules());
      }

      for (ExtTLAModule m : spec.getModules().values()) {
        ExtTLAModule extModule = spec.extendModule(m);
//        System.out.println(extModule);
        Path filePath = FileSystems.getDefault().getPath(outputDir,
            m.getName() + ".tla");
        BufferedWriter out = new BufferedWriter(
            new FileWriter(filePath.toFile()));
        out.write(extModule.toString());
        out.flush();
        out.close();
        System.out.println("Create TLA+ Spec: " + extModule.getName());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   *
   */
  public ExtTLASpec convert() {
    // Walk through the tree
    ParseTree tree = parser.spec();
    ParseTreeWalker walker = new ParseTreeWalker();
    ExtractSpecListener listener = new ExtractSpecListener(tokens);
    walker.walk(listener, tree);
    return listener.getSpecObj();
  }

}
