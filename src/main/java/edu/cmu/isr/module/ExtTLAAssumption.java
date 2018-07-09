package edu.cmu.isr.module;

/**
 *
 */
public class ExtTLAAssumption {
  private String exp;
  private String preComment = "";

  public ExtTLAAssumption(String exp) {
    this.exp = exp;
  }

  @Override
  public String toString() {
    return String.format("%sASSUME%s\n", preComment, exp);
  }

  public String getExp() {
    return exp;
  }

  public void setExp(String exp) {
    this.exp = exp;
  }

  public String getPreComment() {
    return preComment;
  }

  public void setPreComment(String preComment) {
    this.preComment = preComment;
  }
}
