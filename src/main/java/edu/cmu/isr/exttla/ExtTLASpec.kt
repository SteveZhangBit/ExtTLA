package edu.cmu.isr.exttla

class ExtTLASpec {

  val modules: MutableMap<String, ExtTLAModule> = mutableMapOf()

  fun addNewModule(name: String): ExtTLAModule {
    val module = ExtTLAModule(name)
    modules[name] = module
    return module
  }

  fun extendModule(m: ExtTLAModule): ExtTLAModule {
    return m.extendWith(findParentModules(m))
  }

  private fun findParentModules(m: ExtTLAModule): MutableList<ExtTLAModule> {
    return m.extendModules
      .map {
        if (it in modules) {
          val parents = findParentModules(modules[it]!!)
          parents.add(modules[it]!!)
          parents
        } else {
          throw Error("no such module $it")
        }
      }.fold(mutableListOf()) { l, it ->
        it.forEach { mm ->
          if (!l.contains(mm)) {
            l.add(mm)
          }
        }
        l
      }
  }
}