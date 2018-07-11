package edu.cmu.isr.module;

public class ExtTLAInvariant {
  private String name;
  private String exp;
  private String precomment;

  public ExtTLAInvariant(String name, String exp) {
    this.name = name;
    this.exp = exp;
  }

  public String getText() {
    return String.format("%sInv ==%s\n", name, exp);
  }

  public String getName() {
    return name;
  }

  public String getExp() {
    return exp;
  }

  public String getPrecomment() {
    return precomment;
  }

  public void setPrecomment(String precomment) {
    this.precomment = precomment;
  }
}
