package edu.cmu.isr;

import edu.cmu.isr.module.*;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class ExtTLAModule {

  private static final int HeaderLength = 77;

  private String name;
  private String preComment = "";
  private List<String> extendModules = new LinkedList<>();
  private List<ExtTLAImport> importModules = new LinkedList<>();
  private List<ExtTLAInstantiation> instanceModules = new LinkedList<>();
  private List<ExtTLAConstant> constants = new LinkedList<>();
  private List<ExtTLAEnumeration> enumerations = new LinkedList<>();
  private List<ExtTLAAssumption> assumptions = new LinkedList<>();
  private List<ExtTLAVariable> variables = new LinkedList<>();
  private List<ExtTLAOperation> operations = new LinkedList<>();

  private List<String> shadowed = new LinkedList<>();

  public ExtTLAModule(String name) {
    this.name = name;
  }

  /**
   * @param name
   */
  public void addExtend(String name) {
    extendModules.add(name);
  }

  /**
   * @param name
   */
  public ExtTLAImport addImport(String name) {
    ExtTLAImport i = new ExtTLAImport(name);
    importModules.add(i);
    return i;
  }

  /**
   * @param name
   */
  public ExtTLAInstantiation addInstance(String name) {
    ExtTLAInstantiation i = new ExtTLAInstantiation(name);
    instanceModules.add(i);
    return i;
  }

  /**
   * @param name
   * @param tla
   */
  public ExtTLAInstantiation addInstance(String name, String tla) {
    ExtTLAInstantiation i = new ExtTLAInstantiation(name, tla);
    instanceModules.add(i);
    return i;
  }

  /**
   * @param c
   */
  public ExtTLAConstant addConstant(ExtTLAConstant c) {
    constants.add(c);
    return c;
  }

  public ExtTLAEnumeration addEnumeration(ExtTLAEnumeration e) {
    enumerations.add(e);
    return e;
  }

  /**
   * @param exp
   */
  public ExtTLAAssumption addAssumption(String exp) {
    ExtTLAAssumption a = new ExtTLAAssumption(exp);
    assumptions.add(a);
    return a;
  }

  /**
   * @param v
   */
  public ExtTLAVariable addVariable(ExtTLAVariable v) {
    variables.add(v);
    return v;
  }

  public ExtTLAOperation addOperation(ExtTLAOperation op) {
    operations.add(op);
    return op;
  }

  public void addShadow(String name) {
    shadowed.add(name);
  }

  /**
   * @param modules
   */
  public ExtTLAModule extendWith(List<ExtTLAModule> modules) {
    if (modules.size() == 0) {
      return this;
    }

    modules.add(this);

    ExtTLAModule extModule = new ExtTLAModule(name);
    extModule.setPreComment(preComment);
    for (ExtTLAModule m : modules) {
      extModule.importModules.addAll(m.importModules);
      extModule.instanceModules.addAll(m.instanceModules);
      m.constants.forEach(i -> {
        if (i.isOverride()) {
          int idx;
          for (idx = 0; idx < extModule.constants.size(); idx++) {
            if (extModule.constants.get(idx).getName().equals(i.getName())) {
              break;
            }
          }
          if (idx < extModule.constants.size()) {
            extModule.constants.set(idx, i);
          } else {
            throw new Error("invalid override keyword for constant " +
                i.getName());
          }
        } else {
          extModule.constants.add(i);
        }
      });
      extModule.enumerations.addAll(m.enumerations);
      extModule.assumptions.addAll(m.assumptions);
      extModule.variables.addAll(m.variables);
      // TODO: Handle override keyword correctly
      m.operations.forEach(i -> {
        if (i.isOverride()) {
          int idx;
          for (idx = 0; idx < extModule.operations.size(); idx++) {
            if (extModule.operations.get(idx).getName().equals(i.getName())) {
              break;
            }
          }
          if (idx < extModule.operations.size()) {
            extModule.operations.set(idx, i);
          } else {
            throw new Error("invalid override keyword for operation " +
                i.getName());
          }
        } else {
          extModule.operations.add(i);
        }
      });
      extModule.shadowed.addAll(m.shadowed);
    }
    return extModule;
  }

  /**
   * @return
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    // Create module header
    writeModuleHeader(builder);

    // Create EXTENDS statement for imports
    if (importModules.size() > 0) {
      importModules.forEach(i -> builder.append(i.getPreComment()));

      List<String> is = new LinkedList<>();
      importModules.forEach(i -> is.add(i.toString()));
      builder.append("EXTENDS ");
      builder.append(String.join(", ", is));
      builder.append("\n");
    }

    // Create enumerations
    enumerations.forEach(builder::append);

    // Create CONSTANT and constant definition statements
    constants.stream().filter(i -> i.getValue() == null)
        .forEach(builder::append);
    // Create constant declarations.
    constants.stream().filter(i -> i.getValue() != null)
        .forEach(builder::append);

    // Create ASSUME statements
    builder.append("\n\\* Begin assumption definitions\n");
    assumptions.forEach(builder::append);
    builder.append("\n\\* End of assumption definitions\n");

    // Create variables
    List<String> vars = variables.stream().map(ExtTLAVariable::getName)
        .collect(Collectors.toList());
    variables.forEach(builder::append);
    // Write vars == <<>> definition
    builder.append("\nvars == <<");
    builder.append(String.join(", ", vars));
    builder.append(">>\n");

    // Write type invariant
    builder.append('\n');
    writeTypeInvariant(builder);
    builder.append("----\n");

    // Write operations
    operations.forEach(i -> {
      if (shadowed.contains(i.getName())) {
        i.setShadowed(true);
      }
      builder.append(i);
      // Append UNCHANGED <<...>>
      builder.append(String.format("\n  /\\ UNCHANGED <<%s>>",
          String.join(", ", i.generateUnchanged(vars, operations))));
    });

    // Write Init/Next operation
    writeInitOperation(builder);
    writeNextOperation(builder);

    // Create instantiation
    if (instanceModules.size() > 0) {
      instanceModules.forEach(builder::append);
    }

    // Create module footer
    writeModuleFooter(builder);

    String tlaSpec = builder.toString();
    // Replace all the enumeration items with corresponding string values
    for (ExtTLAEnumeration e : enumerations) {
      for (String i : e.getItems()) {
        tlaSpec = tlaSpec.replace(e.getName() + "." + i, e.getText(i));
      }
    }
    return tlaSpec;
  }

  private void writeModuleFooter(StringBuilder builder) {
    char[] ending = new char[HeaderLength];
    Arrays.fill(ending, '=');
    builder.append(ending);
    builder.append('\n');
    SimpleDateFormat formater = new SimpleDateFormat(
        "EEE MMM dd HH:mm:ss z yyyy");
    builder.append("\\* Modification History\n");
    builder.append("\\* Generated ");
    builder.append(formater.format(new Date()));
    builder.append(" by ExtTLA Converter\n");
  }

  private void writeModuleHeader(StringBuilder builder) {
    String moduleName = " MODULE " + name + " ";
    int i;
    for (i = 0; i < (HeaderLength - moduleName.length()) / 2; i++) {
      builder.append('-');
    }
    builder.append(moduleName);
    i += moduleName.length();
    for (; i < HeaderLength; i++) {
      builder.append('-');
    }
    builder.append(preComment);
  }

  private void writeTypeInvariant(StringBuilder builder) {
    builder.append("TypeInv ==\n");
    variables.forEach(i -> {
      if (!i.getType().equals("any")) {
        builder.append(String.format("  /\\ %s \\in%s\n",
            i.getName(), i.getType()));
      }
    });
    builder.append('\n');
  }

  private void writeNextOperation(StringBuilder builder) {
    builder.append("Next ==\n");
    operations.forEach(i -> {
      if (Character.isLowerCase(i.getName().charAt(0))) {
        return;
      }
      if (shadowed.contains(i.getName())) {
        return;
      }
      builder.append("  \\/ ");
      i.getArgs().forEach(a -> {
        builder.append(String.format("\\E %s \\in %s: ",
            a.getName(), a.getType()));
      });
      builder.append(i.getName());
      if (i.getArgs().size() > 0) {
        builder.append(String.format("(%s)",
            String.join(", ",
                i.getArgs().stream()
                    .map(ExtTLAOperationArg::getName)
                    .collect(Collectors.toList()))
            )
        );
      }
      builder.append('\n');
    });
    builder.append("\nSpec == Init /\\ [][Next]_vars\n\n");
  }

  private void writeInitOperation(StringBuilder builder) {
    builder.append("Init ==\n");
    variables.forEach(i -> {
      builder.append(String.format("  /\\ %s = %s\n", i.getName(),
          i.getInitValue()));
    });
    builder.append('\n');
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPreComment() {
    return preComment;
  }

  public void setPreComment(String preComment) {
    this.preComment = preComment;
  }

  public List<String> getExtendModules() {
    return extendModules;
  }

  public void setExtendModules(List<String> extendModules) {
    this.extendModules = extendModules;
  }

  public List<ExtTLAImport> getImportModules() {
    return importModules;
  }

  public void setImportModules(List<ExtTLAImport> importModules) {
    this.importModules = importModules;
  }

  public List<ExtTLAInstantiation> getInstanceModules() {
    return instanceModules;
  }

  public void setInstanceModules(List<ExtTLAInstantiation> instanceModules) {
    this.instanceModules = instanceModules;
  }

  public List<ExtTLAConstant> getConstants() {
    return constants;
  }

  public void setConstants(List<ExtTLAConstant> constants) {
    this.constants = constants;
  }

  public List<ExtTLAEnumeration> getEnumerations() {
    return enumerations;
  }

  public void setEnumerations(List<ExtTLAEnumeration> enumerations) {
    this.enumerations = enumerations;
  }

  public List<ExtTLAAssumption> getAssumptions() {
    return assumptions;
  }

  public void setAssumptions(List<ExtTLAAssumption> assumptions) {
    this.assumptions = assumptions;
  }

  public List<ExtTLAVariable> getVariables() {
    return variables;
  }

  public void setVariables(List<ExtTLAVariable> variables) {
    this.variables = variables;
  }

  public List<ExtTLAOperation> getOperations() {
    return operations;
  }

  public void setOperations(List<ExtTLAOperation> operations) {
    this.operations = operations;
  }

  public List<String> getShadowed() {
    return shadowed;
  }

  public void setShadowed(List<String> shadowed) {
    this.shadowed = shadowed;
  }
}

