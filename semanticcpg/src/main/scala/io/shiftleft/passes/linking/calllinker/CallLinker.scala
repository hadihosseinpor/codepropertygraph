package io.shiftleft.passes.linking.calllinker

import gremlin.scala._
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.{EdgeTypes, NodeTypes, nodes}
import io.shiftleft.passes.{CpgPass, DiffGraph}
import io.shiftleft.Implicits.JavaIteratorDeco
import org.apache.tinkerpop.gremlin.structure.Direction
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

class CallLinker(cpg: Cpg) extends CpgPass(cpg){
  import CallLinker._
  private var methodFullNameToNode = Map.empty[String, nodes.StoredNode]
  /**
    * Main method of enhancement - to be implemented by child class
    **/
  override def run(): Iterator[DiffGraph] = {
    val dstGraph = new DiffGraph()

    cpg.graph.V.hasLabel(NodeTypes.METHOD).sideEffectWithTraverser { traverser =>
      val method = traverser.get.asInstanceOf[nodes.Method]
      methodFullNameToNode += (method.fullName -> method)
    }.iterate()

    cpg.call.sideEffect { call =>
      try {
        linkCall(call, dstGraph)
      } catch {
        case exception: Exception =>
          throw new RuntimeException(exception)
      }
    }.exec()

    Iterator(dstGraph)
  }

  private def linkCall(call: nodes.Call, dstGraph: DiffGraph): Unit = {
    call.vertices(Direction.OUT, EdgeTypes.RECEIVER).nextOption.foreach {
      case methodRef: nodes.MethodRef =>
        methodFullNameToNode.get(methodRef.methodInstFullName) match {
          case Some(method) =>
            dstGraph.addEdgeInOriginal(call, method, EdgeTypes.CALL)
          case None =>
            logger.warn(s"Unable to link METHOD_REF with METHOD_FULL_NAME ${methodRef.methodInstFullName}.")
        }
      case receiver =>
        val receiverTypeDecl = receiver.vertices(Direction.OUT, EdgeTypes.EVAL_TYPE).nextChecked
          .vertices(Direction.OUT, EdgeTypes.REF).nextChecked

        val resolvedMethodOption =
          receiverTypeDecl.vertices(Direction.OUT, EdgeTypes.BINDS).asScala.collectFirst {
            case binding: nodes.Binding if binding.name == call.name && binding.signature == call.signature =>
              binding.vertices(Direction.OUT, EdgeTypes.REF).nextChecked.asInstanceOf[nodes.Method]
          }

        resolvedMethodOption.foreach { method =>
          dstGraph.addEdgeInOriginal(call, method, EdgeTypes.CALL)
        }
    }
  }
}

object CallLinker {
  private val logger = LoggerFactory.getLogger(getClass)
}