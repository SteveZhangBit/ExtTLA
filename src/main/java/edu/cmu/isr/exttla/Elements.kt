package edu.cmu.isr.exttla

import java.util.regex.Pattern

interface Element {
  fun getText(): String
}

/**
 *
 */
data class Assumption(val exp: String) : Element {
  var preComment: String = ""

  override fun getText(): String {
    return "${preComment}ASSUME$exp"
  }
}

/**
 *
 */
data class Constant(
  val name: String,
  val value: String? = null,
  var override: Boolean = false
) : Element {
  var preComment: String = ""

  override fun getText(): String {
    return if (value == null) {
      "${preComment}CONSTANT $name\n"
    } else {
      "$preComment$name == ${value.trim()}\n"
    }
  }
}

/**
 *
 */
data class Enumeration(
  val name: String,
  var override: Boolean = false
) : Element {
  val items: MutableList<String> = mutableListOf()
  var preComment: String = ""

  fun getItemString(i: String): String {
    return "\"${name}_$i\""
  }

  override fun getText(): String {
    val strItems = items.joinToString(",\n  ") { getItemString(it) }
    return "$preComment$name == {\n  $strItems\n}\n"
  }
}

/**
 *
 */
data class Import(val name: String) : Element {
  var preComment: String = ""

  override fun getText(): String {
    return name
  }
}

/**
 *
 */
data class Instantiation(
  val name: String,
  val mapping: String? = null
) : Element {
  var preComment: String = ""

  override fun getText(): String {
    return if (mapping == null) {
      "$preComment$name == INSTANCE $name\n"
    } else {
      "$preComment$name == INSTANCE $name WITH $mapping\n"
    }
  }
}

/**
 *
 */
data class Operation(
  val name: String,
  val exp: String,
  val override: Boolean = false,
  val recursive: Boolean = false,
  val fairness: String?
) : Element {
  val args: MutableList<OperationArg> = mutableListOf()
  var preComment: String = ""
  private val changed: MutableSet<String> = mutableSetOf()

  init {
    // Find all the changed variables in this operation
    val r = Pattern.compile("""(\w+)'""")
    val m = r.matcher(exp)
    while (m.find()) {
      changed.add(m.group(1))
    }
  }

  fun addArg(name: String, type: String) {
    args.add(OperationArg(name, type))
  }

  private fun matchSubops(opName: String): Boolean {
    val p =
      """(?s).*(\\/|/\\|THEN|ELSE)\s+(/\\\s+|\\/\s+)?$opName.*""".toRegex()
    return exp.matches(p)
  }

  fun generateSubOps(ops: List<Operation>): List<Operation> {
    return ops.fold(mutableListOf()) { l, it ->
      if (it.name != name && matchSubops(it.name)) {
        l.add(it)
      }
      l
    }
  }

  fun generateUnchanged(vars: List<String>, ops: List<Operation>): List<String> {
    val s = vars.toMutableSet()
    ops.forEach {
      if (it.name != name && matchSubops(it.name)) {
        s.retainAll(it.generateUnchanged(vars, ops))
      }
    }
    s.removeAll(changed)
    return s.toList()
  }

  override fun getText(): String {
    return if (args.size > 0) {
      "$preComment$name(${args.joinToString(", ") { it.getText() }}) ==$exp\n"
    } else {
      "$preComment$name ==$exp\n"
    }
  }
}

/**
 *
 */
data class OperationArg(
  val name: String,
  val type: String
) : Element {

  override fun getText(): String {
    return name
  }
}

/**
 *
 */
data class Variable(
  val name: String,
  val type: String,
  val initValue: String
) : Element {
  var preComment: String = ""

  override fun getText(): String {
    return "${preComment}VARIABLE $name\n"
  }
}

/**
 *
 */
data class Invariant(val name: String, val exp: String) :
  Element {
  var preComment: String = ""

  override fun getText(): String {
    return "$preComment${name}Inv ==$exp\n"
  }
}

/**
 *
 */
data class Property(val name: String, val exp: String) :
  Element {
  var preComment: String = ""

  override fun getText(): String {
    return "$preComment${name}Prop ==$exp\n"
  }
}
