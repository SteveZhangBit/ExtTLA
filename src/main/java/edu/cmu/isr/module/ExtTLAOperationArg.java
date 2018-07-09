package edu.cmu.isr.module;

public class ExtTLAOperationArg {
  private String name;
  private String type;

  public ExtTLAOperationArg(String name, String type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public String toString() {
    return name;
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
}
