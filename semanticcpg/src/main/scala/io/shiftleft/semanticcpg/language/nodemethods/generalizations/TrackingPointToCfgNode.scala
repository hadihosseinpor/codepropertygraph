package io.shiftleft.semanticcpg.language.nodemethods.generalizations

import io.shiftleft.codepropertygraph.generated.nodes
import io.shiftleft.codepropertygraph.generated.nodes.NodeVisitor
import io.shiftleft.semanticcpg.utils.{ExpandTo, MemberAccess}

object TrackingPointToCfgNode extends TrackingPointToCfgNode

trait TrackingPointToCfgNode extends NodeVisitor[nodes.CfgNode] with ExpressionGeneralization[nodes.CfgNode] {

  override def visit(node: nodes.MethodParameterIn): nodes.CfgNode = {
    ExpandTo.parameterInToMethod(node).asInstanceOf[nodes.CfgNode]
  }

  override def visit(node: nodes.MethodParameterOut): nodes.CfgNode = {
    val method = ExpandTo.parameterInToMethod(node)
    val methodReturn = ExpandTo.methodToFormalReturn(method)
    methodReturn.asInstanceOf[nodes.CfgNode]
  }

  override def visit(node: nodes.MethodReturn): nodes.CfgNode = {
    node
  }

  override def visit(node: nodes.Call): nodes.CfgNode = {
    if (MemberAccess.isGenericMemberAccessName(node.name)) {
      ExpandTo.argumentToCallOrReturn(node)
    } else {
      node
    }
  }

  override def visit(node: nodes.ImplicitCall): nodes.CfgNode = {
    node
  }

  override def visit(node: nodes.Identifier): nodes.CfgNode = {
    ExpandTo.argumentToCallOrReturn(node)
  }

  override def visit(node: nodes.MethodRef): nodes.CfgNode = {
    ExpandTo.argumentToCallOrReturn(node)
  }

  override def visit(node: nodes.Literal): nodes.CfgNode = {
    ExpandTo.argumentToCallOrReturn(node)
  }

  override def visit(node: nodes.Expression): nodes.CfgNode = {
    node
  }
}
