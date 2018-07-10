package edu.cmu.isr.module;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ExtTLAOperation {
  private String name;
  private List<ExtTLAOperationArg> args = new LinkedList<>();
  private String tla;
  private boolean isOverride = false;
  private String preComment = "";

  public ExtTLAOperation(String name, String tla) {
    this.name = name;
    this.tla = tla;
  }

  public ExtTLAOperation(String name, String tla, boolean isOverride) {
    this.name = name;
    this.tla = tla;
    this.isOverride = isOverride;
  }

  public void addArg(String name, String type) {
    args.add(new ExtTLAOperationArg(name, type));
  }

  public List<String> generateUnchanged(List<String> vars, List<ExtTLAOperation> ops) {
    List<String> unchanged = new LinkedList<>();

    // I assume that operation starts with an upper case is the main operation
    // which would be a valid next step
    if (Character.isLowerCase(name.charAt(0))) {
      return unchanged;
    }

    Set<String> rest = new HashSet<>(vars);
    // Find the changed variables in sub-operations
    ops.forEach(i -> {
      // Skip this operation itself
      if (i.name.equals(name)) {
        return;
      }
      // If this operation contains a sub-operation
      if (tla.matches("(?s).*/\\\\\\s+" + i.name + "\\(.*\\).*")) {
        rest.retainAll(i.generateUnchanged(vars, ops));
      }
    });
    rest.forEach(i -> {
      if (!tla.matches("(?s).*" + i + "'\\s*=.*")) {
        unchanged.add(i);
      }
    });

    return unchanged;
  }

  @Override
  public String toString() {
    if (args.size() > 0) {
      String[] argNames = new String[args.size()];
      for (int i = 0; i < args.size(); i++) {
        argNames[i] = args.get(i).toString();
      }
      return String.format("%s%s(%s) ==%s\n", preComment,
          name, String.join(", ", argNames), tla);
    }
    return String.format("%s%s ==%s\n", preComment, name, tla);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<ExtTLAOperationArg> getArgs() {
    return args;
  }

  public void setArgs(List<ExtTLAOperationArg> args) {
    this.args = args;
  }

  public String getTla() {
    return tla;
  }

  public void setTla(String tla) {
    this.tla = tla;
  }

  public boolean isOverride() {
    return isOverride;
  }

  public void setOverride(boolean override) {
    isOverride = override;
  }

  public String getPreComment() {
    return preComment;
  }

  public void setPreComment(String preComment) {
    this.preComment = preComment;
  }

}
