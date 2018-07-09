package edu.cmu.isr.module;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class ExtTLAEnumeration {
  private String name;
  private List<String> items = new LinkedList<>();
  private String preComment = "";

  public ExtTLAEnumeration(String name) {
    this.name = name;
  }

  public void addItem(String v) {
    items.add(v);
  }

  public String getText(String i) {
    return String.format("\"%s_%s\"", name, i);
  }

  @Override
  public String toString() {
    List<String> strItems = items.stream()
        .map(this::getText)
        .collect(Collectors.toList());
    return String.format("%s%s == {\n  %s\n}\n", preComment, name,
        String.join(",\n  ", strItems));
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getItems() {
    return items;
  }

  public void setItems(List<String> items) {
    this.items = items;
  }

  public String getPreComment() {
    return preComment;
  }

  public void setPreComment(String preComment) {
    this.preComment = preComment;
  }
}
