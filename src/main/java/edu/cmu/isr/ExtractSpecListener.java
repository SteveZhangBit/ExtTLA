package edu.cmu.isr;

import edu.cmu.isr.module.ExtTLAConstant;
import edu.cmu.isr.module.ExtTLAEnumeration;
import edu.cmu.isr.module.ExtTLAOperation;
import edu.cmu.isr.module.ExtTLAVariable;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

public class ExtractSpecListener extends edu.cmu.isr.ExtTLABaseListener {

  private BufferedTokenStream tokens;
  /**
   *
   */
  private ExtTLASpec specObj = new ExtTLASpec();
  private ExtTLAModule curModule;

  public ExtractSpecListener(BufferedTokenStream tokens) {
    this.tokens = tokens;
  }

  private String getCommentBefore(Token t) {
    List<Token> cmtChannel = tokens.getHiddenTokensToLeft(t.getTokenIndex(),
        edu.cmu.isr.ExtTLALexer.COMMENTS_CHANNEL);
    if (cmtChannel != null) {
      StringBuilder builder = new StringBuilder();
      cmtChannel.forEach(i -> builder.append(i.getText()));
      return builder
          .toString()
          .replaceAll("\n\\s*\\*", "\n \\*")
          .replaceFirst("^/\\*", "\n(*")
          .replaceFirst("\\*/$", "*)\n")
          .replaceFirst("^//", "\\*");
    }
    return "\n";
  }

  @Override
  public void enterModule(edu.cmu.isr.ExtTLAParser.ModuleContext ctx) {
    curModule = specObj.addNewModule(ctx.IDENT().toString());
    curModule.setPreComment(getCommentBefore(ctx.getStart()));
  }

  @Override
  public void enterExtend(edu.cmu.isr.ExtTLAParser.ExtendContext ctx) {
    getCommentBefore(ctx.getStart());

    ctx.IDENT().forEach(i -> curModule.addExtend(i.toString()));
  }

  @Override
  public void enterImplement(edu.cmu.isr.ExtTLAParser.ImplementContext ctx) {
    String cmt = getCommentBefore(ctx.getStart());

    if (ctx.TLA_EXP() == null) {
      curModule
          .addInstance(ctx.IDENT().toString())
          .setPreComment(cmt);
    } else {
      String tla = extractTLAExpression(ctx.TLA_EXP());
      curModule
          .addInstance(ctx.IDENT().toString(), tla)
          .setPreComment(cmt);
    }
  }

  @Override
  public void enterImports(edu.cmu.isr.ExtTLAParser.ImportsContext ctx) {
    ctx.IDENT().forEach(i -> curModule
        .addImport(i.toString())
        .setPreComment(getCommentBefore(ctx.getStart())));
  }

  @Override
  public void enterConstants(edu.cmu.isr.ExtTLAParser.ConstantsContext ctx) {
    // Get override keyword if any
    boolean isOverride = ctx.getChild(0).getText().equals("override");
    for (int ci = 0; ci < ctx.const_decl().size(); ci++) {
      edu.cmu.isr.ExtTLAParser.Const_declContext i = ctx.const_decl(ci);

      String name = i.IDENT().toString();
      ExtTLAConstant c;
      if (i.literal() == null && i.TLA_EXP() == null) {
        c = curModule.addConstant(new ExtTLAConstant(name));
      } else {
        String value;
        if (i.literal() != null) {
          value = i.literal().getText();
        } else {
          value = extractTLAExpression(i.TLA_EXP());
        }
        // 'override' keyword is only valid in 'override const a = v'
        c = curModule.addConstant(new ExtTLAConstant(name, value, isOverride));
      }
      // Add pre comment
      if (ci == 0) {
        c.setPreComment(getCommentBefore(ctx.getStart()));
      }
    }
  }

  @Override
  public void enterEnumerations(edu.cmu.isr.ExtTLAParser.EnumerationsContext ctx) {
    ExtTLAEnumeration e = new ExtTLAEnumeration(ctx.IDENT(0).toString());
    ctx.IDENT().subList(1, ctx.IDENT().size()).forEach(i ->
        e.addItem(i.toString()));
    curModule
        .addEnumeration(e)
        .setPreComment(getCommentBefore(ctx.getStart()));
  }

  @Override
  public void enterAssumes(edu.cmu.isr.ExtTLAParser.AssumesContext ctx) {
    String tla = extractTLAExpression(ctx.TLA_EXP());
    curModule
        .addAssumption(tla)
        .setPreComment(getCommentBefore(ctx.getStart()));
  }

  @Override
  public void enterVariables(edu.cmu.isr.ExtTLAParser.VariablesContext ctx) {
    String cmt = getCommentBefore(ctx.getStart());

    String name = ctx.IDENT(0).toString();
    String initValue;
    if (ctx.var_init_val().TLA_EXP() == null) {
      initValue = ctx.var_init_val().IDENT().toString();
    } else {
      initValue = extractTLAExpression(ctx.var_init_val().TLA_EXP());
    }
    if (ctx.TLA_EXP() == null) {
      ExtTLAVariable v = new ExtTLAVariable(name, ctx.IDENT(1).toString(),
          initValue);
      curModule.addVariable(v).setPreComment(cmt);
    } else {
      String tla = extractTLAExpression(ctx.TLA_EXP());
      ExtTLAVariable v = new ExtTLAVariable(name, tla, initValue);
      curModule.addVariable(v).setPreComment(cmt);
    }
  }

  @Override
  public void enterOperations(edu.cmu.isr.ExtTLAParser.OperationsContext ctx) {
    // Read 'override' key if any
    boolean isOverride = ctx.getChild(0).getText().equals("override");

    String name = ctx.IDENT().toString();
    String tla = extractTLAExpression(ctx.TLA_EXP());
    ExtTLAOperation op = new ExtTLAOperation(name, tla, isOverride);

    if (ctx.arguments() != null) {
      ctx.arguments().arg().forEach(i -> {
        if (i.TLA_EXP() == null) {
          op.addArg(i.IDENT(0).toString(), i.IDENT(1).toString());
        } else {
          String type = extractTLAExpression(i.TLA_EXP());
          op.addArg(i.IDENT(0).toString(), type);
        }
      });
    }

    curModule
        .addOperation(op)
        .setPreComment(getCommentBefore(ctx.getStart()));
  }

  @Override
  public void enterShadow(edu.cmu.isr.ExtTLAParser.ShadowContext ctx) {
    curModule.addShadow(ctx.IDENT().toString());
  }

  @Override
  public void enterInvariants(edu.cmu.isr.ExtTLAParser.InvariantsContext ctx) {
    curModule
        .addInvariant(ctx.IDENT().toString(),
            extractTLAExpression(ctx.TLA_EXP()))
        .setPrecomment(getCommentBefore(ctx.getStart()));
  }

  public ExtTLASpec getSpecObj() {
    return specObj;
  }

  private String extractTLAExpression(TerminalNode n) {
    String tla = n.toString();
    return tla.substring(0, tla.length() - 2)
        .replaceAll("\\s+$", "")
        .substring(2)
        .replace("\n  ", "\n")
        .replace("/*", "(*")
        .replace("*/", "*)")
        .replace("//", "\\*");
  }
}
