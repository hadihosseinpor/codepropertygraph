package io.shiftleft.semanticcpg.language.types.expressions.generalizations

import gremlin.scala.GremlinScala
import io.shiftleft.codepropertygraph.generated.{EdgeTypes, NodeTypes, nodes}
import io.shiftleft.semanticcpg.language.{ICallResolver, NodeSteps}
import io.shiftleft.semanticcpg.language.types.structure.Block
import io.shiftleft.semanticcpg.language._
import io.shiftleft.semanticcpg.language.types.expressions._
import io.shiftleft.semanticcpg.language.types.propertyaccessors.OrderAccessors

class AstNode(raw: GremlinScala[nodes.AstNode])
    extends NodeSteps[nodes.AstNode](raw)
    with AstNodeBase[nodes.AstNode]
    with OrderAccessors[nodes.AstNode] {}

trait AstNodeBase[NodeType <: nodes.AstNode] { this: NodeSteps[NodeType] =>

  /**
    * Nodes of the AST rooted in this node, including the node itself.
    * */
  def ast: AstNode = new AstNode(raw.emit.repeat(_.out(EdgeTypes.AST)).cast[nodes.AstNode])

  /**
    * Nodes of the AST rooted in this node, minus the node itself
    * */
  def astMinusRoot: AstNode = new AstNode(raw.repeat(_.out(EdgeTypes.AST)).emit.cast[nodes.AstNode])

  /**
    * Direct children of node in the AST
    * */
  def astChildren: AstNode = new AstNode(raw.out(EdgeTypes.AST).cast[nodes.AstNode])

  /**
    * Parent AST node
    * */
  def astParent: AstNode = new AstNode(raw.in(EdgeTypes.AST).cast[nodes.AstNode])

  /**
    * Nodes of the AST obtained by expanding AST edges backwards until the method root is reached
    * */
  def inAst: AstNode =
    new AstNode(
      raw.emit
        .until(_.hasLabel(NodeTypes.METHOD))
        .repeat(_.in(EdgeTypes.AST))
        .cast[nodes.AstNode])

  /**
    * Nodes of the AST obtained by expanding AST edges backwards until the method root is reached, minus this node
    * */
  def inAstMinusLeaf: AstNode =
    new AstNode(
      raw
        .until(_.hasLabel(NodeTypes.METHOD))
        .repeat(_.in(EdgeTypes.AST))
        .emit
        .cast[nodes.AstNode])

  /**
    * Traverse only to those AST nodes that are also control flow graph nodes
    * */
  def isCfgNode: CfgNode =
    new CfgNode(raw.filterOnEnd(_.isInstanceOf[nodes.CfgNode]).cast[nodes.CfgNode])

  /**
    * Traverse only to those AST nodes that are blocks
    * */
  def isBlock: Block = new Block(
    raw.hasLabel(NodeTypes.BLOCK).cast[nodes.Block]
  )

  /**
    * Traverse only to those AST nodes that are control structures
    * */
  def isControlStructure: ControlStructure =
    new ControlStructure(raw.hasLabel(NodeTypes.CONTROL_STRUCTURE).cast[nodes.ControlStructure])

  /**
    * Traverse only to AST nodes that are expressions
    * */
  def isExpression: Expression = new Expression(
    raw.filterOnEnd(_.isInstanceOf[nodes.Expression]).cast[nodes.Expression]
  )
  @deprecated("replaced by isCall", "July 19")
  def call: Call = isCall

  @deprecated("replaced by isLiteral", "July 19")
  def literal: Literal = isLiteral

  /**
    * Traverse only to AST nodes that are calls
    * */
  def isCall: Call = new Call(
    raw.hasLabel(NodeTypes.CALL).cast[nodes.Call]
  )

  /**
  Cast to call if applicable and filter for callee fullName `calleeRegex`
    */
  def isCall(calleeRegex: String)(implicit callResolver: ICallResolver): Call =
    isCall.filter(_.calledMethod.fullName(calleeRegex))

  /**
    * Traverse only to AST nodes that are literals
    * */
  def isLiteral: Literal = new Literal(
    raw.hasLabel(NodeTypes.LITERAL).cast[nodes.Literal]
  )

  /**
    * Traverse only to AST nodes that are identifier
    * */
  def isIdentifier: Identifier = new Identifier(
    raw.hasLabel(NodeTypes.IDENTIFIER).cast[nodes.Identifier]
  )

  /**
    * Traverse only to AST nodes that are return nodes
    * */
  def isReturnNode: Return = new Return(
    raw.hasLabel(NodeTypes.RETURN).cast[nodes.Return]
  )
}
