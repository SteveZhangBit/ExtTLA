package edu.cmu.isr.exttla

import java.text.SimpleDateFormat
import java.util.*

fun formalizeVars(s: String, indent: String = "  "): String {
  if (s.length <= 80) {
    return s
  }
  val idx = s.substring(0, 80).lastIndexOf(',')
  return s.substring(0, idx+1) + "\n" + indent + formalizeVars(s.substring(idx+1), indent).trimStart()
}

class Module(val name: String) : Element {
  private val HEADER_LEN = 77

  var preComment: String = ""
  val extendModules: MutableList<String> = mutableListOf()
  val importModules: MutableList<Import> = mutableListOf()
  val instanceModules: MutableList<Instantiation> = mutableListOf()
  val constants: MutableList<Constant> = mutableListOf()
  val enumerations: MutableList<Enumeration> = mutableListOf()
  val assumptions: MutableList<Assumption> = mutableListOf()
  val variables: MutableList<Variable> = mutableListOf()
  val operations: MutableList<Operation> = mutableListOf()
  val hidden: MutableList<String> = mutableListOf()
  val invariants: MutableList<Invariant> = mutableListOf()
  val properties: MutableList<Property> = mutableListOf()

  fun extendWith(modules: MutableList<Module>): Module {
    if (modules.isEmpty()) {
      return this
    }
    modules.add(this)

    val extModule = Module(name)
    extModule.preComment = preComment
    modules.forEach { m ->
      extModule.importModules.addAll(m.importModules)
      extModule.instanceModules.addAll(m.instanceModules)
      m.constants.forEach {
        if (it.override) {
          val idx = extModule.constants.indexOfFirst { x -> x.name == it.name }
          if (idx != -1) {
            extModule.constants[idx] = it
          } else {
            throw Error("invalid override keyword for constant ${it.name}")
          }
        } else {
          extModule.constants.add(it)
        }
      }
      m.enumerations.forEach {
        if (it.override) {
          val idx =
            extModule.enumerations.indexOfFirst { x -> x.name == it.name }
          if (idx != -1) {
            extModule.enumerations[idx] = it
          } else {
            throw Error("invalid override keyword for enumeration ${it.name}")
          }
        } else {
          extModule.enumerations.add(it)
        }
      }
      extModule.assumptions.addAll(m.assumptions)
      extModule.variables.addAll(m.variables)
      m.operations.forEach {
        if (it.override) {
          val idx = extModule.operations.indexOfFirst { x -> x.name == it.name }
          if (idx != -1) {
            // Add super_<op> to the module
            val tmp = extModule.operations[idx]
            val copy = tmp.copy(name="super_${it.name}")
            copy.preComment = "\n\\* Extend from ${m.name}" + tmp.preComment
            copy.args.addAll(tmp.args)

            extModule.operations[idx] = it
            extModule.operations.add(idx, copy)
          } else {
            throw Error("invalid override keyword for operation ${it.name}")
          }
        } else {
          extModule.operations.add(it)
        }
      }
      extModule.hidden.addAll(m.hidden)
      extModule.invariants.addAll(m.invariants)
      extModule.properties.addAll(m.properties)
    }
    return extModule
  }

  override fun getText(): String {
    val builder = StringBuilder()

    // Create module header
    writeModuleHeader(builder)

    // Create EXTENDS statement for imports
    if (importModules.size > 0) {
      importModules.forEach { builder.append(it.preComment) }
      builder.append(importModules.joinToString(", ", "EXTENDS ", "\n") { it.name })
    }

    // Create enumerations
    enumerations.forEach { builder.append(it.getText()) }

    // Create CONSTANT and constant definition statements
    constants.filter { it.value == null }.forEach { builder.append(it.getText()) }
    // Create constant declarations
    constants.filter { it.value != null }.forEach { builder.append(it.getText()) }

    // Create ASSUME statements
    assumptions.forEach { builder.append(it.getText()) }
    builder.append('\n')

    // Create variables
    variables.forEach { builder.append(it.getText()) }
    builder.append(formalizeVars(
      variables.joinToString(", ", "\nvars == <<", ">>\n") { it.name }))

    // Create instantiation
    if (instanceModules.isNotEmpty()) instanceModules.forEach { builder.append(it.getText()) }

    // Write type invariant
    builder.append('\n')
    writeTypeInvariant(builder)
    builder.append("----\n")

    // Write operations
    val sortedOps: MutableList<Operation> = mutableListOf()
    operations.forEach {
      it.generateSubOps(operations).forEach { subop ->
        if (sortedOps.indexOf(subop) == -1) {
          sortedOps.add(subop)
        }
      }
      if (sortedOps.indexOf(it) == -1) {
        sortedOps.add(it)
      }
    }

    sortedOps.forEach {
      builder.append(it.getText())
      // Find the unchanged variables.
      if (it.name[0].isUpperCase() && !hidden.contains(it.name)) {
        val unchanged = it.generateUnchanged(variables.map { it.name }, operations)
        // Append UNCHANGED <<...>>
        if (unchanged.isNotEmpty()) {
          builder.append(formalizeVars(
            "  /\\ UNCHANGED <<${unchanged.joinToString(", ")}>>\n",
            "      "))
        }
      }
    }
    builder.append("\n----\n\n")

    // Write Init/Next operation
    writeInit(builder)
    writeNext(builder)
    builder.append("\n----\n")

    // Write THEOREMS
    invariants.forEach { builder.append(it.getText()) }
    properties.forEach { builder.append(it.getText()) }
    builder.append('\n')

    // Create module footer
    writeModuleFooter(builder)

    var tlaSpec = builder.toString()
    // Replace all the enumeration items with corresponding string values
    enumerations.forEach { e ->
      e.items.forEach { tlaSpec = tlaSpec.replace("${e.name}.$it", e.getItemString(it)) }
    }
    return tlaSpec
  }

  private fun writeModuleHeader(builder: StringBuilder) {
    val mName = " MODULE $name "
    val len = (HEADER_LEN - mName.length) / 2
    for (i in 0 until len) {
      builder.append('-')
    }
    builder.append(mName)
    for (i in len + mName.length until HEADER_LEN) {
      builder.append('-')
    }
    builder.append(preComment)
  }

  private fun writeModuleFooter(builder: StringBuilder) {
    for (i in 0 until HEADER_LEN) {
      builder.append('=')
    }
    builder.append('\n')
    val formatter = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy")
    builder.append("\\* Modification History\n\\* Generated ")
    builder.append(formatter.format(Date()))
    builder.append(" by ExtTLA Converter\n")
  }

  private fun writeTypeInvariant(builder: StringBuilder) {
    builder.append("TypeInv ==\n")
    variables.forEach {
      if (it.type != "any") {
        builder.append("  /\\ ${it.name} \\in${it.type}\n")
      }
    }
    builder.append('\n')
  }

  private fun writeInit(builder: StringBuilder) {
    builder.append("Init ==\n")
    variables.forEach {
      builder.append("  /\\ ${it.name} = ${it.initValue}\n")
    }
    builder.append('\n')
  }

  private fun writeNext(builder: StringBuilder) {
    builder.append("Next ==\n")

    val nextOps = operations.filter { Character.isUpperCase(it.name[0]) && !hidden.contains(it.name) }
    nextOps.forEach {
      builder.append("  \\/ ")
      it.args.forEach { builder.append("\\E ${it.name} \\in ${it.type}: ") }
      builder.append(it.name)
      if (it.args.isNotEmpty()) {
        builder.append(it.args.joinToString(", ", "(", ")") {
          it.name
        })
      }
      builder.append('\n')
    }
    builder.append("\nSpec ==\n  /\\ Init /\\ [][Next]_vars /\\ WF_vars(Next)\n")
    // Append fairness
    nextOps.filter { it.fairness != null }.forEach {
      if (it.fairness == "WF" || it.fairness == "SF") {
        builder.append("  /\\ ${it.fairness}_vars(")
        it.args.forEach { builder.append("\\E ${it.name} \\in ${it.type}: ") }
        builder.append(it.name)
        if (it.args.isNotEmpty()) {
          builder.append(it.args.joinToString(", ", "(", ")") {
            it.name
          })
        }
        builder.append(")\n")
      } else {
        builder.append("  /\\ ${it.fairness}\n")
      }
    }
  }

}