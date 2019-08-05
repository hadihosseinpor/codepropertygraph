package io.shiftleft.queryprimitives.steps.types.structure

import gremlin.scala.GremlinScala
import io.shiftleft.codepropertygraph.generated.{EdgeTypes, nodes}
import io.shiftleft.queryprimitives.steps.NodeSteps
import io.shiftleft.queryprimitives.steps.types.propertyaccessors.{NameAccessors, SignatureAccessors}
import io.shiftleft.queryprimitives.steps.Implicits.GremlinScalaDeco
import shapeless.HList

class Binding[Labels <: HList](override val raw: GremlinScala.Aux[nodes.Binding, Labels])
  extends NodeSteps[nodes.Binding, Labels](raw)
    with NameAccessors[nodes.Binding, Labels]
    with SignatureAccessors[nodes.Binding, Labels] {

  /**
    * Traverse to the method bound by this method binding.
    */
  def boundMethod: Method[Labels] = {
    new Method[Labels](
      raw.out(EdgeTypes.REF).cast[nodes.Method]
    )
  }

  /**
    * Traverse to the method bound by this method binding.
    */
  def bindingTypeDecl: TypeDecl[Labels] = {
    new TypeDecl[Labels](
      raw.in(EdgeTypes.BINDS).cast[nodes.TypeDecl]
    )
  }
}
