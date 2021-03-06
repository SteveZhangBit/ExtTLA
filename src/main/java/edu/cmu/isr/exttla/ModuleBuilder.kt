package edu.cmu.isr.exttla

import edu.cmu.isr.exttla.grammar.ExtTLABaseListener
import edu.cmu.isr.exttla.grammar.ExtTLALexer
import edu.cmu.isr.exttla.grammar.ExtTLAParser
import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.TerminalNode

/**
 *
 */
class ModuleBuilder(
  private val tokens: BufferedTokenStream
) : ExtTLABaseListener() {

  val specObj: Spec = Spec()

  private var curModule: Module? = null

  private fun getCommentBefore(t: Token): String {
    val cmtChannel = tokens.getHiddenTokensToLeft(
      t.tokenIndex,
      ExtTLALexer.COMMENTS_CHANNEL
    )
    if (cmtChannel != null) {
      val builder = StringBuilder()
      cmtChannel.forEach { builder.append(it.text) }
      return builder
        .toString()
        .replace("""\n\s*\*""".toRegex(), "\n \\*")
        .replaceFirst("""^/\*""".toRegex(), "\n(*")
        .replaceFirst("""\*/$""".toRegex(), "*)\n")
        .replaceFirst("""^//""".toRegex(), "\\*")
    }
    return "\n"
  }

  override fun enterModule(ctx: ExtTLAParser.ModuleContext?) {
    curModule = specObj.addNewModule(ctx!!.IDENT().toString())
    curModule!!.preComment = getCommentBefore(ctx.getStart())
  }

  override fun enterExtend(ctx: ExtTLAParser.ExtendContext?) {
    ctx!!.IDENT().forEach { curModule!!.extendModules.add(it.toString()) }
  }

  override fun enterImplement(ctx: ExtTLAParser.ImplementContext?) {
    val cmt = getCommentBefore(ctx!!.getStart())
    if (ctx.TLA_EXP() == null) {
      val i = Instantiation(ctx.IDENT().toString())
      i.preComment = cmt
      curModule!!.instanceModules.add(i)
    } else {
      val tla = extractTLAExpression(ctx.TLA_EXP())
      val i = Instantiation(ctx.IDENT().toString(), tla)
      i.preComment = cmt
      curModule!!.instanceModules.add(i)
    }
  }

  override fun enterImports(ctx: ExtTLAParser.ImportsContext?) {
    ctx!!.IDENT().forEach {
      val i = Import(it.toString())
      i.preComment = getCommentBefore(ctx.getStart())
      curModule!!.importModules.add(i)
    }
  }

  override fun enterConstants(ctx: ExtTLAParser.ConstantsContext?) {
    val isOverride = ctx!!.getChild(0).text == "override"
    ctx.const_decl().forEachIndexed { idx, it ->
      val name = it.IDENT().toString()
      val c = if (it.literal() == null && it.TLA_EXP() == null) {
        Constant(name)
      } else {
        val value = if (it.literal() != null) {
          it.literal().text
        } else {
          extractTLAExpression(it.TLA_EXP())
        }
        // 'override' keyword is only valid in 'override const a = v'
        Constant(name, value, isOverride)
      }
      // Add pre comment
      if (idx == 0) {
        c.preComment = getCommentBefore(ctx.getStart())
      }
      curModule!!.constants.add(c)
    }
  }

  override fun enterEnumerations(ctx: ExtTLAParser.EnumerationsContext?) {
    // Read 'override' key if any
    val isOverride = ctx!!.getChild(0).text == "override"

    val e = Enumeration(ctx.IDENT(0).toString(), isOverride)
    ctx.IDENT().subList(1, ctx.IDENT().size).forEach {
      e.items.add(it.toString())
    }
    e.preComment = getCommentBefore(ctx.getStart())
    curModule!!.enumerations.add(e)
  }

  override fun enterAssumes(ctx: ExtTLAParser.AssumesContext?) {
    val a = Assumption(extractTLAExpression(ctx!!.TLA_EXP()))
    a.preComment = getCommentBefore(ctx.getStart())
    curModule!!.assumptions.add(a)
  }

  override fun enterVariables(ctx: ExtTLAParser.VariablesContext?) {
    val name = ctx!!.IDENT(0).toString()
    val initValue = if (ctx.var_init_val().TLA_EXP() == null) {
      ctx.var_init_val().IDENT().toString()
    } else {
      extractTLAExpression(ctx.var_init_val().TLA_EXP())
    }
    val v = if (ctx.TLA_EXP() == null) {
      Variable(name, ctx.IDENT(1).toString(), initValue)
    } else {
      Variable(name, extractTLAExpression(ctx.TLA_EXP()), initValue)
    }
    v.preComment = getCommentBefore(ctx.getStart())
    curModule!!.variables.add(v)
  }

  override fun enterOperations(ctx: ExtTLAParser.OperationsContext?) {
    // Read 'override' key if any
    val isOverride = ctx!!.children.find { it.text == "override" } != null
    val isRecursive = ctx.children.find { it.text == "recursive" } != null
    val fairness = ctx.children.find { it.text == "SF" || it.text == "WF" || it.text == "fairness" }

    var fairnessStr: String? = fairness?.text
    if (fairnessStr != null && fairnessStr != "SF" && fairnessStr != "WF") {
      fairnessStr = extractTLAExpression(ctx.TLA_EXP(1)).trim()
    }

    val name = ctx.IDENT().toString()
    val exp = extractTLAExpression(ctx.TLA_EXP(0))
    val op = Operation(name, exp, isOverride, isRecursive, fairnessStr)

    if (ctx.arguments() != null) {
      ctx.arguments().arg().forEach {
        if (it.TLA_EXP() == null) {
          op.addArg(it.IDENT(0).toString(), it.IDENT(1).toString())
        } else {
          op.addArg(it.IDENT(0).toString(), extractTLAExpression(it.TLA_EXP()))
        }
      }
    }
    op.preComment = getCommentBefore(ctx.getStart())
    curModule!!.operations.add(op)
  }

  override fun enterHide(ctx: ExtTLAParser.HideContext?) {
    curModule!!.hidden.add(ctx!!.IDENT().toString())
  }

  override fun enterInvariants(ctx: ExtTLAParser.InvariantsContext?) {
    val i = Invariant(
      ctx!!.IDENT().toString(),
      extractTLAExpression(ctx.TLA_EXP())
    )
    i.preComment = getCommentBefore(ctx.getStart())
    curModule!!.invariants.add(i)
  }

  override fun enterProperties(ctx: ExtTLAParser.PropertiesContext?) {
    val i = Property(
      ctx!!.IDENT().toString(),
      extractTLAExpression(ctx.TLA_EXP())
    )
    i.preComment = getCommentBefore(ctx.getStart())
    curModule!!.properties.add(i)
  }

  private fun extractTLAExpression(n: TerminalNode): String {
    val tla = n.toString()
    return tla.substring(0, tla.length - 2)
      .replace("""\s+$""".toRegex(), "")
      .substring(2)
      .replace("\n  ", "\n")
      .replace("/*", "(*")
      .replace("*/", "*)")
      .replace("//", "\\*")
  }
}