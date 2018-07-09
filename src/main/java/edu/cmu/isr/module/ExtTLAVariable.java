package edu.cmu.isr.module;

/**
 *
 */
public class ExtTLAVariable {
  private String name;
  private String type;
  private String initValue;
  private String preComment = "";

  public ExtTLAVariable(String name, String type, String initValue) {
    this.name = name;
    this.type = type;
    this.initValue = initValue;
  }

  @Override
  public String toString() {
    return String.format("%sVARIABLE %s\n", preComment, name);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getInitValue() {
    return initValue;
  }

  public void setInitValue(String initValue) {
    this.initValue = initValue;
  }

  public String getPreComment() {
    return preComment;
  }

  public void setPreComment(String preComment) {
    this.preComment = preComment;
  }
}
