package edu.cmu.isr.module;

/**
 *
 */
public class ExtTLAConstant {
  private String name;
  private boolean isOverride = false;
  private String value;
  private String preComment = "";

  public ExtTLAConstant(String name) {
    this.name = name;
  }

  public ExtTLAConstant(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public ExtTLAConstant(String name, String value, boolean isOverride) {
    this.name = name;
    this.value = value;
    this.isOverride = isOverride;
  }

  @Override
  public String toString() {
    if (value == null) {
      return String.format("%sCONSTANT %s\n", preComment, name);
    } else {
      return String.format("%s%s == %s\n", preComment, name, value.trim());
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isOverride() {
    return isOverride;
  }

  public void setOverride(boolean override) {
    isOverride = override;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getPreComment() {
    return preComment;
  }

  public void setPreComment(String preComment) {
    this.preComment = preComment;
  }
}
