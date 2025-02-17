package io.shiftleft.semanticcpg.language.nodemethods

import io.shiftleft.codepropertygraph.generated.nodes.NodeVisitor
import io.shiftleft.codepropertygraph.generated.{EdgeTypes, nodes}
import io.shiftleft.Implicits.JavaIteratorDeco
import io.shiftleft.semanticcpg.language.nodemethods.generalizations.ExpressionGeneralization
import org.apache.tinkerpop.gremlin.structure.Direction

class WithinMethodMethods(val node: nodes.WithinMethod) extends AnyVal {
  def method: nodes.Method = node.accept(WithinMethodToMethod)
}

private object WithinMethodToMethod extends NodeVisitor[nodes.Method] with ExpressionGeneralization[nodes.Method] {

  override def visit(node: nodes.Method): nodes.Method = node.asInstanceOf[nodes.Method]

  override def visit(node: nodes.MethodParameterIn): nodes.Method = walkUpAst(node)

  override def visit(node: nodes.MethodParameterOut): nodes.Method = walkUpAst(node)

  override def visit(node: nodes.MethodReturn): nodes.Method = walkUpAst(node)

  override def visit(node: nodes.ImplicitCall): nodes.Method = walkUpAst(node)

  override def visit(node: nodes.Expression): nodes.Method = walkUpContainsEdges(node)

  private def walkUpContainsEdges(node: nodes.WithinMethod): nodes.Method =
    node.vertices(Direction.IN, EdgeTypes.CONTAINS).nextChecked.asInstanceOf[nodes.Method]

  private def walkUpAst(node: nodes.WithinMethod): nodes.Method =
    node.vertices(Direction.IN, EdgeTypes.AST).nextChecked.asInstanceOf[nodes.Method]
}
