package edu.cmu.isr.module;

/**
 *
 */
public class ExtTLAImport {
  private String name;
  private String preComment = "";

  public ExtTLAImport(String name) {
    this.name = name;
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

  public String getPreComment() {
    return preComment;
  }

  public void setPreComment(String preComment) {
    this.preComment = preComment;
  }
}
