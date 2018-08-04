package edu.cmu.isr.exttla

class Spec {

  val modules: MutableMap<String, Module> = mutableMapOf()

  fun addNewModule(name: String): Module {
    val module = Module(name)
    modules[name] = module
    return module
  }

  fun extendModule(m: Module): Module {
    return m.extendWith(findParentModules(m))
  }

  private fun findParentModules(m: Module): MutableList<Module> {
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