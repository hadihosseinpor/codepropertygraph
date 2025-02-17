package io.shiftleft.passes

import java.lang.{Long => JLong}

import gnu.trove.set.hash.TCustomHashSet
import gnu.trove.strategy.IdentityHashingStrategy
import gremlin.scala.Edge
import io.shiftleft.codepropertygraph.generated.nodes.{NewNode, StoredNode}
import org.apache.logging.log4j.LogManager

import scala.collection.mutable
import scala.collection.JavaConverters._

/**
  * A lightweight write-only graph used for creation of CPG graph overlays
  *
  * The graph can store edges to/from nodes that do not exist in the base graph.
  * It doesn't assign ids for these nodes until the diff graph is serialized.
  * Ids of new nodes may collide with ids of nodes in the base graph, which are
  * not sources or destinations of edges of the diff graph. When the CPG loader
  * adds nodes of the overlay, it therefor needs to reassign ids for nodes if they
  * are already used in the original CPG.
  *
  * TODO Michael: make DiffGraph extend tinkerpop.Graph to simplify and foolproof the model
  * */
class DiffGraph {
  import DiffGraph._

  private val _edges = mutable.ArrayBuffer.empty[EdgeInDiffGraph]
  private val _edgesToOriginal = mutable.ArrayBuffer.empty[EdgeToOriginal]
  private val _edgesFromOriginal = mutable.ArrayBuffer.empty[EdgeFromOriginal]
  private val _edgesInOriginal = mutable.ArrayBuffer.empty[EdgeInOriginal]
  private val _nodeProperties = mutable.ArrayBuffer.empty[NodeProperty]
  private val _edgeProperties = mutable.ArrayBuffer.empty[EdgeProperty]

  private var _nodes = new TCustomHashSet[NewNode](IdentityHashingStrategy.INSTANCE)

  def nodes: Iterator[NewNode] = _nodes.iterator().asScala
  def edges: Vector[EdgeInDiffGraph] = _edges.toVector
  def edgesToOriginal: Vector[EdgeToOriginal] = _edgesToOriginal.toVector
  def edgesFromOriginal: Vector[EdgeFromOriginal] = _edgesFromOriginal.toVector
  def edgesInOriginal: Vector[EdgeInOriginal] = _edgesInOriginal.toVector
  def nodeProperties: Vector[NodeProperty] = _nodeProperties.toVector
  def edgeProperties: Vector[EdgeProperty] = _edgeProperties.toVector

  def addNode(node: NewNode): Unit = {
    _nodes.add(node)
    incrementNodeCreatedCount()
  }

  def mergeFrom(other: DiffGraph): Unit = {
    other.nodes.foreach(addNode)
    _edges.appendAll(other._edges)
    _edgesToOriginal.appendAll(other._edgesToOriginal)
    _edgesInOriginal.appendAll(other._edgesInOriginal)
    _nodeProperties.appendAll(other._nodeProperties)
    _edgeProperties.appendAll(other._edgeProperties)
  }

  /**
    * Add edge between nodes present in the diff graph
    * */
  def addEdge(srcNode: NewNode,
              dstNode: NewNode,
              edgeLabel: String,
              properties: Seq[(String, AnyRef)] = List()): Unit = {
    _edges += new EdgeInDiffGraph(srcNode, dstNode, edgeLabel, properties)
    incrementEdgeCreatedCount()
  }

  /**
    * Add edge from a node in the diff graph to a node in the original graph
    * */
  def addEdgeToOriginal(srcNode: NewNode,
                        dstNode: StoredNode,
                        edgeLabel: String,
                        properties: Seq[(String, AnyRef)] = List()): Unit = {
    _edgesToOriginal += new EdgeToOriginal(srcNode, dstNode, edgeLabel, properties)
    incrementEdgeCreatedCount()
  }

  /**
    * Add edge from a node in the original graph to a node in the diff graph
    * */
  def addEdgeFromOriginal(srcNode: StoredNode,
                          dstNode: NewNode,
                          edgeLabel: String,
                          properties: Seq[(String, AnyRef)] = List()): Unit = {
    _edgesFromOriginal += new EdgeFromOriginal(srcNode, dstNode, edgeLabel, properties)
    incrementEdgeCreatedCount()
  }

  /**
    * Add edge between nodes of the original graph
    * */
  def addEdgeInOriginal(srcNode: StoredNode,
                        dstNode: StoredNode,
                        edgeLabel: String,
                        properties: Seq[(String, AnyRef)] = List()): Unit = {
    _edgesInOriginal += new EdgeInOriginal(srcNode, dstNode, edgeLabel, properties)
    incrementEdgeCreatedCount()
  }

  /**
    * Add a property to an existing node
    * */
  def addNodeProperty(node: StoredNode, key: String, value: AnyRef) =
    _nodeProperties += new NodeProperty(node, key, value)

  /**
    * Add a property to an existing edge
    * */
  def addEdgeProperty(edge: Edge, key: String, value: AnyRef) =
    _edgeProperties += new EdgeProperty(edge, key, value)

  override def toString() = {
    val nodeCount = _nodes.size
    val edgeCount = _edges.size + _edgesToOriginal.size + _edgesFromOriginal.size + _edgesInOriginal.size
    val propertyCount = _nodeProperties.size + _edgeProperties.size
    s"DiffGraph[nodes: $nodeCount, edges: $edgeCount, properties: $propertyCount]"
  }
}

object DiffGraph {
  private val logger = LogManager.getLogger(getClass)
  private var nodesCreatedCount = 0
  private var edgesCreatedCount = 0

  def incrementNodeCreatedCount(): Unit = {
    nodesCreatedCount += 1
    if (nodesCreatedCount % 100000 == 0)
      logger.debug(s"added $nodesCreatedCount nodes (total across all DiffGraphs)")
  }

  def incrementEdgeCreatedCount(): Unit = {
    edgesCreatedCount += 1
    if (edgesCreatedCount % 100000 == 0)
      logger.debug(s"added $edgesCreatedCount edges (total across all DiffGraphs)")
  }

  abstract class DiffEdge {
    def label: String
    def properties: Seq[(String, AnyRef)]
  }
  case class NodeProperty(node: StoredNode, propertyKey: String, propertyValue: AnyRef)
  case class EdgeProperty(edge: Edge, propertyKey: String, propertyValue: AnyRef)
  type Properties = Seq[(String, AnyRef)]

  case class EdgeInDiffGraph(src: NewNode, dst: NewNode, label: String, properties: Properties) extends DiffEdge
  case class EdgeToOriginal(src: NewNode, dst: StoredNode, label: String, properties: Properties) extends DiffEdge
  case class EdgeFromOriginal(src: StoredNode, dst: NewNode, label: String, properties: Properties) extends DiffEdge
  case class EdgeInOriginal(src: StoredNode, dst: StoredNode, label: String, properties: Properties) extends DiffEdge
}
