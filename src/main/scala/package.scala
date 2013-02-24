/****************************************************************     
**                  _______        
**                 |__   __|     reqT API  
**   _ __  ___   __ _ | |        (c) 2011-2013, Lund University  
**  |  __|/ _ \ / _  || |        http://reqT.org
**  | |  |  __/| (_| || |   
**  |_|   \___| \__  ||_|   
**                 | |      
**                 |_|      
** reqT is open source, licensed under the BSD 2-clause license: 
** http://opensource.org/licenses/bsd-license.php 
*****************************************************************/

package object reqt {  
  import scala.language.implicitConversions

  lazy val version = "reqT-v2.3.0RC1-SNAPSHOT_2.10.0"
  
  //implicits for constraints.scala
  implicit def attrRefToVar(ref: AttrRef[Int]): Var[AttrRef[Int]] = Var(ref)  
  implicit def constraintsToSeq[T](cs: Constraints[T]): Seq[Constr[T]] = cs.cs
  implicit def seqToConstraints[T](cs: Seq[Constr[T]]): Constraints[T] = Constraints(cs:_*)
  implicit def rangeToInterval(r: Range): Interval = Interval(r.min, r.max)
  implicit class ModelImpose(m: Model) {
    def impose[T](cs: Constraints[T]) = CSP(m, cs)
  }
  implicit class ConstraintsImpose[T](cs: Constraints[T]) {
    def impose(m: Model) = CSP(m, cs)
  }    
  implicit class RangeSeqOps(rs: Seq[Range]) { //to enable > Var("x")::Seq(1 to 10, 12 to 15)
    def ::[T](v: Var[T]): Bounds[T] = Bounds(Seq(v), rs.map(rangeToInterval(_)))
  }
  
  //generator function for variable vectors for constraints:
  def vars[T](vs: T *): Seq[Var[T]] = vs.map(Var(_)).toIndexedSeq

  //conversions functions from Key and NodeSet to scala code string
  def keyNodesToScala(key: Key, nodes: NodeSet): String = "" + key.toScala + ( if (key.edge.isInstanceOf[RelationWithAttribute[_]]) "to " else "") + nodes.toScala
  def keyNodesPairToScala(kns: (Key, NodeSet)): String = "" + keyNodesToScala(kns._1, kns._2)
  
  lazy val nameIndex: Map[String, Int] = elementNames.zipWithIndex.toMap.withDefaultValue(-1)
  lazy val elementNames: List[String] = elementKinds map (_.toString)
  lazy val elementKinds: List[Element] = nodeKinds ++ egdeKinds
  lazy val nodeKinds: List[Element] = entityKinds ++ attributeKinds
  lazy val entityKinds: List[Element] = contextKinds ++ requirementKinds
  lazy val contextKinds: List[Element] = List(Product, Release, Stakeholder, Actor, Resource)
  lazy val scenarioKinds: List[Element] = List(UserStory, UseCase, Task, VividScenario)
  lazy val dataKinds: List[Element] = List(Class, Member)
  lazy val requirementKinds: List[Element] = List(Req, Goal, Feature, Function, Quality, Interface, Design) ++ scenarioKinds ++ dataKinds
  lazy val attributeKinds: List[Element] = List(Gist, Spec, Status, Why, Example, Input, Output, Trigger, Precond, Frequency, Critical, Problem, Prio, Index, Cost, Benefit, Capacity, Urgency, Label, Comment, Image, Deprecated, Submodel)
  lazy val egdeKinds: List[Element] = List(has, owns, requires, excludes, helps, hurts, precedes, inherits, assigns, deprecates)
  lazy val levelIndex: Map[Level, Int] = Map(DROPPED -> 0, ELICITED -> 1, SPECIFIED -> 2, VALIDATED -> 3, POSTPONED -> 4, PLANNED -> 5, FAILED -> 6, IMPLEMENTED -> 7, TESTED -> 8, RELEASED -> 9)
  
  def levelLessThan(l1: Level, l2: Level): Boolean = levelIndex(l1) < levelIndex(l2)
  implicit val levelOrdering = Ordering.fromLessThan[Level](levelLessThan) 
  def statusLessThan(s1: Status, s2: Status): Boolean = levelIndex(s1.value) < levelIndex(s2.value)
  implicit val statusOrdering = Ordering.fromLessThan[Status](statusLessThan) 
  def elementLessThan (me1:Element, me2:Element): Boolean = {
    val (nix1, nix2) = (nameIndex(me1.prefix), nameIndex(me2.prefix))
    if (nix1 == nix2) me1.toScala < me2.toScala
    else nameIndex(me1.prefix) < nameIndex(me2.prefix)
  }  
  implicit val elementOrdering = Ordering.fromLessThan[Element](elementLessThan) 
  def keyLessThan(k1:Key, k2:Key): Boolean = {
    if (k1.entity == k2.entity) elementLessThan(k1.edge, k2.edge)
    else elementLessThan(k1.entity, k2.entity)
  }
  implicit val keyOrdering = Ordering.fromLessThan[Key](keyLessThan) 
  def keyNodeSetLessThan(kns1:(Key, NodeSet), kns2:(Key, NodeSet)): Boolean = keyLessThan(kns1._1, kns2._1)
  implicit val keyNodeSetOrdering = Ordering.fromLessThan[(Key, NodeSet)](keyNodeSetLessThan)
  implicit val entityOrdering = Ordering.fromLessThan[Entity]((e1: Entity, e2: Entity) => elementLessThan(e1,e2)) 
  implicit val nodeOrdering = Ordering.fromLessThan[Node[_]]((n1: Node[_], n2: Node[_]) => elementLessThan(n1,n2)) 
  implicit val edgeOrdering = Ordering.fromLessThan[Edge]((e1: Edge, e2: Edge) => elementLessThan(e1,e2))
  
  implicit def stringToRichString(str: String): RichString = RichString(str)
  implicit def setOfNodesToNodeSet(nodes: Set[Node[_]]):NodeSet = NodeSet(nodes)
  implicit def nodeSetToSetOfNodes(nodes:NodeSet):Set[Node[_]] = nodes.nodes
  implicit def entityToKeyNodeSetPair(e: Entity): (Key,NodeSet) = (Key(e, has()), NodeSet(Spec("???")))
  
  // Implicits objects allowed for attribute External[T]
  implicit object specMaker extends AttrFromString[Spec] { def apply(s: String): Spec = Spec(s) }
  implicit object whyMaker extends AttrFromString[Why] { def apply(s: String): Why = Why(s) }
  implicit object exampleMaker extends AttrFromString[Example] { def apply(s: String): Example = Example(s) }
  implicit object commentMaker extends AttrFromString[Comment] { def apply(s: String): Comment = Comment(s) }
  implicit object modelMaker extends AttrFromString[Submodel] { def apply(s: String): Submodel = Submodel(Model.interpret(s)) }
  
  object defaultHtmlGenerator extends HtmlGenerator 
  lazy val defaultDocumentTemplate = DocumentTemplate(
    "Requirements Document", 
    Text("Generated by reqT", "Date: " + ( new java.util.Date ), "<a href=\"http://reqT.org\">reqT.org</a>"),
    Chapter("Context", Text("A context may include the following external entities: products, releases and stakeholders. "), m => Model()),
    Section("Stakeholders", Text("The following stakeholders have interest in the requirements:"), m => m / Stakeholder),
    Section("Products", Text("The following products are modelled:"), m => m / Product),
    Section("Releases", Text("The following releses are planned:"), m => m / Release),
    Chapter("Features", Text("A feature is a releasable characteristic of a Product."), m => m / Feature), 
    Chapter("Other entities", Text("The following other entities are part of this model."), m => m \ Context \ Feature), 
    Chapter("Undefined destinations", Text("An undefined destination is an entity that is the destination of a relation but is not itself a source, thus having no attributes or relations."), 
      m => Model.fromEntitySet(m.undefined)) 
  ) 
  def longName(s: String) = s match {
    case "Spec" => "Specification"
    case "Prio" => "Priority"
    case "Why" => "Rationale"
    case _ => s
  }
  
  //fileutils
  def fileSep = System.getProperty("file.separator")
  def slashify(s:String) = s.replaceAllLiterally(fileSep, "/")
  def pwd = slashify(System.getProperty("user.dir"))
  def listFiles(dir: String) = new java.io.File(dir).listFiles.toList
  def ls(d: String) { println(listFiles(d) map { f => f.getName + ( if (f.isDirectory) "/" else "") }  mkString("\n")) }
  def ls { ls(pwd) }
  def dir { ls } 
  def dir(d: String)  = ls(d)
  var defaultPath = ""
  def saveString(s:String, fileName:String) = {
    val fn = defaultPath + fileName
    val outFile = new java.io.FileOutputStream(fn)
    val outStream = new java.io.PrintStream(outFile)
    try { outStream.println(s.toString) } finally { outStream.close }
    println("Saved to file: "+fn) 
  }
  def loadLines(fileName:String) = {
    val fn = defaultPath + fileName
    val source = scala.io.Source.fromFile(fn)
    val lines = source.getLines.toList
    source.close
    lines
  }
  def load(fileName:String): String = {
    val fn = defaultPath + fileName
    try  { loadLines(fn).mkString("\n") } catch  { case e: Throwable => "ERROR " + e }
  }
  //dbg utils to be used in REPL> :wrap timedSec
  def timedSec[T](body: => T): T = {
    val start = System.nanoTime
    try body
    finally println((System.nanoTime - start)/1e9 + " seconds elapsed.")
  }  

}  //end package object reqT