package edu.cmu.isr.exttla

interface ExtTLAElement {
  fun getText(): String
}

/**
 *
 */
data class ExtTLAAssumption(val exp: String) : ExtTLAElement {
  var preComment: String = ""

  override fun getText(): String {
    return "${preComment}ASSUME$exp"
  }
}

/**
 *
 */
data class ExtTLAConstant(
  val name: String,
  val value: String? = null,
  var override: Boolean = false
) : ExtTLAElement {
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
data class ExtTLAEnumeration(val name: String) :
  ExtTLAElement {
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
data class ExtTLAImport(val name: String) : ExtTLAElement {
  var preComment: String = ""

  override fun getText(): String {
    return name
  }
}

/**
 *
 */
data class ExtTLAInstantiation(
  val name: String,
  val mapping: String? = null
) : ExtTLAElement {
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
data class ExtTLAInvariant(val name: String, val exp: String) :
  ExtTLAElement {
  var preComment: String = ""

  override fun getText(): String {
    return "$preComment${name}Inv ==$exp\n"
  }
}

/**
 *
 */
data class ExtTLAOperation(
  val name: String,
  val exp: String,
  var override: Boolean = false
) : ExtTLAElement {
  val args: MutableList<ExtTLAOperationArg> = mutableListOf()
  var preComment: String = ""

  fun addArg(name: String, type: String) {
    args.add(ExtTLAOperationArg(name, type))
  }

  fun generateUnchanged(
    vars: List<String>,
    ops: List<ExtTLAOperation>
  ): List<String> {
    // I assume that operation starts with an upper case is the main operation
    // which would be a valid next step
    if (Character.isLowerCase(name[0])) {
      return listOf()
    }

    // Find the changed variables in sub-operations
    val rest: Set<String> = ops.fold(vars.toMutableSet()) { s, i ->
      // Skip this operation itself
      if (i.name != name) {
        val p = """(?s).*/\\\s+${i.name}\(.*\).*""".toRegex()
        // If this operation contains a sub-operation
        if (exp.matches(p)) {
          s.retainAll(i.generateUnchanged(vars, ops))
        }
      }
      s
    }

    return rest.toList().filter {
      val p = """(?s).*$it'\s*=.*""".toRegex()
      !exp.matches(p)
    }
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
data class ExtTLAOperationArg(
  val name: String,
  val type: String
) : ExtTLAElement {

  override fun getText(): String {
    return name
  }
}

data class ExtTLAVariable(
  val name: String,
  val type: String,
  val initValue: String
) : ExtTLAElement {
  var preComment: String = ""

  override fun getText(): String {
    return "${preComment}VARIABLE $name\n"
  }
}