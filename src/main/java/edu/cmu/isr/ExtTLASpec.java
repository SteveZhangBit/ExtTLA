package edu.cmu.isr;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ExtTLASpec {

  /**
   *
   */
  private Map<String, ExtTLAModule> modules = new HashMap<>();

  public ExtTLAModule addNewModule(String name) {
    ExtTLAModule module = new ExtTLAModule(name);
    modules.put(name, module);
    return module;
  }

  public ExtTLAModule extendModule(ExtTLAModule m) {
    List<ExtTLAModule> extModules = new LinkedList<>();
    m.getExtendModules().forEach(i -> {
      if (modules.containsKey(i)) {
        // Extend the module first
        extModules.add(extendModule(modules.get(i)));
      } else {
        throw new Error("no such module " + i);
      }
    });
    return m.extendWith(extModules);
  }

  public Map<String, ExtTLAModule> getModules() {
    return modules;
  }
}
