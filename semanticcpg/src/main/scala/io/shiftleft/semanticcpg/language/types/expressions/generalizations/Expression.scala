package io.shiftleft.semanticcpg.language.types.expressions.generalizations

import gremlin.scala._
import io.shiftleft.codepropertygraph.generated.{EdgeTypes, NodeKeys, NodeTypes}
import io.shiftleft.codepropertygraph.generated.nodes
import io.shiftleft.semanticcpg.language._
import io.shiftleft.semanticcpg.language.types.expressions.Call
import io.shiftleft.semanticcpg.language.types.propertyaccessors._
import io.shiftleft.semanticcpg.language.types.structure.{Method, MethodParameter, Type}

/**
  An expression (base type)
  */
class Expression(raw: GremlinScala[nodes.Expression])
    extends NodeSteps[nodes.Expression](raw)
    with ExpressionBase[nodes.Expression] {}

trait ExpressionBase[NodeType <: nodes.Expression]
    extends OrderAccessors[NodeType]
    with ArgumentIndexAccessors[NodeType]
    with EvalTypeAccessors[NodeType]
    with CodeAccessors[NodeType]
    with LineNumberAccessors[NodeType]
    with AstNodeBase[NodeType] { this: NodeSteps[NodeType] =>

  /**
    Traverse to enclosing expression
    */
  def expressionUp: Expression =
    new Expression(raw.in(EdgeTypes.AST).not(_.hasLabel(NodeTypes.LOCAL)).cast[nodes.Expression])

  /**
    Traverse to sub expressions
    */
  def expressionDown: Expression =
    new Expression(
      raw
        .out(EdgeTypes.AST)
        .not(_.hasLabel(NodeTypes.LOCAL))
        .cast[nodes.Expression])

  /**
    If the expression is used as receiver for a call, this traverses to the call.
    */
  def receivedCall: Call =
    new Call(
      raw
        .in(EdgeTypes.RECEIVER)
        .cast[nodes.Call]
    )

  /**
    Traverse to related parameter
    */
  def toParameter: MethodParameter = {
    new MethodParameter(
      raw
        .sack((sack: Integer, node: nodes.Expression) => node.value2(NodeKeys.ARGUMENT_INDEX))
        .in(EdgeTypes.AST)
        .out(EdgeTypes.CALL)
        .out(EdgeTypes.REF)
        .out(EdgeTypes.AST)
        .hasLabel(NodeTypes.METHOD_PARAMETER_IN)
        .filterWithTraverser { traverser =>
          val parameterIndex = traverser.sack[Integer]
          traverser.get.value2(NodeKeys.ORDER) == parameterIndex
        }
        .cast[nodes.MethodParameterIn]
    )
  }

  /**
    Traverse to enclosing method
    */
  def method: Method =
    new Method(raw.in(EdgeTypes.CONTAINS).cast[nodes.Method])

  /**
    * Traverse to next expression in CFG.
    */
  def cfgNext: Expression =
    new Expression(
      raw
        .out(EdgeTypes.CFG)
        .filterNot(_.hasLabel(NodeTypes.METHOD_RETURN))
        .cast[nodes.Expression]
    )

  /**
    * Traverse to previous expression in CFG.
    */
  def cfgPrev: Expression =
    new Expression(
      raw
        .in(EdgeTypes.CFG)
        .filterNot(_.hasLabel(NodeTypes.METHOD))
        .cast[nodes.Expression]
    )

  /**
    * Traverse to expression evaluation type
    * */
  def typ: Type =
    new Type(raw.out(EdgeTypes.EVAL_TYPE).cast[nodes.Type])
}
