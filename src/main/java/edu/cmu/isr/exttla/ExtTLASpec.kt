package edu.cmu.isr.exttla

class ExtTLASpec {

  val modules: MutableMap<String, ExtTLAModule> = mutableMapOf()

  fun addNewModule(name: String): ExtTLAModule {
    val module = ExtTLAModule(name)
    modules[name] = module
    return module
  }

  fun extendModule(m: ExtTLAModule): ExtTLAModule {
    val extModules: MutableList<ExtTLAModule> = m.extendModules
      .fold(mutableListOf()) { l, it ->
        if (it in modules) {
          // Extend the module first
          l.add(extendModule(modules[it]!!))
          l
        } else {
          throw Error("no such module $it")
        }
      }
    return m.extendWith(extModules)
  }
}