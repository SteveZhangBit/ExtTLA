package edu.cmu.isr.module;

/**
 *
 */
public class ExtTLAInstantiation {
  private String name;
  private String mapping;
  private String preComment = "";

  public ExtTLAInstantiation(String name) {
    this.name = name;
  }

  public ExtTLAInstantiation(String name, String mapping) {
    this.name = name;
    this.mapping = mapping;
  }

  @Override
  public String toString() {
    if (mapping == null) {
      return String.format("%s%s == INSTANCE %s\n", preComment, name, name);
    } else {
      return String.format("%s%s == INSTANCE %s WITH %s\n", preComment,
          name, name, mapping);
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMapping() {
    return mapping;
  }

  public void setMapping(String mapping) {
    this.mapping = mapping;
  }

  public String getPreComment() {
    return preComment;
  }

  public void setPreComment(String preComment) {
    this.preComment = preComment;
  }
}
