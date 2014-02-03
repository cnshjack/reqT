/*     
**                  _______        
**                 |__   __|     reqT API  
**   _ __  ___   __ _ | |        (c) 2011-2014, Lund University  
**  |  __|/ _ \ / _  || |        http://reqT.org
**  | |  |  __/| (_| || |   
**  |_|   \___| \__  ||_|   
**                 | |      
**                 |_|      
** reqT is open source, licensed under the BSD 2-clause license: 
** http://opensource.org/licenses/bsd-license.php 
*****************************************************************/

package reqT 

/** A base trait for the reqT DSL.
*/
sealed trait Base
  
/** A marker trait for parameters to separation operators on class Model.
*/
trait Selector 

trait HasType { def myType: Type } 
trait HasValue[T] { def value: T }
trait HasDefault[T] { def default: T }
trait CanMakePair {
  def toPair: (Key, MapTo)  = (key, mapTo)
  def key: Key
  def mapTo: MapTo
}

sealed trait Elem extends Base with HasType with CanMakePair with Selector { 
  def isNode: Boolean
  def isAttribute: Boolean
  def isEntity: Boolean = !isAttribute
  def isRelation: Boolean = !isNode
}

sealed trait Node extends Elem {
  override def isNode: Boolean = true
}

sealed trait MapTo extends Base with HasType  

trait Attribute[T] extends Node with MapTo with HasValue[T] with CanMakePair {
  override def myType: AttributeType[T]
  override def key: AttributeType[T] = myType
  override def mapTo: Attribute[T] = this
  override def isAttribute: Boolean = true
}

trait Model extends MapTo with ModelImplementation
object Model extends Type with ModelCompanion with ModelFromMap

sealed trait Key extends Base with Selector

trait Entity extends Node with HeadFactory with RelationFactory {
  def id: String
  override def key: Head = Head( this , reqT.has) 
  override def mapTo: Model = Model() 
  override def myType: EntityType
  override def isAttribute: Boolean = false
}

trait Type extends Selector   
trait AttributeType[T] extends Key with Type with HasDefault[T] { 
  val default: T 
}

trait EntityType extends Type {
  def apply(id: String): Entity
  def apply(): Entity = apply(nextId)
  def apply(i: Int): Entity = apply(i.toString)
}

trait RelationType extends Type

case class Head(entity: Entity, link: RelationType) extends Key 

case class Relation(entity: Entity, link: RelationType, tail: Model) extends Elem {
  val myType = Relation
  override def key: Head = Head(entity, link)
  override def mapTo: Model = tail
  override lazy val toString = s"$entity $link ${tail.toStringBody}"
  override def isNode: Boolean = false
  override def isAttribute: Boolean = false
}
case object Relation extends Type {
  def apply(h: Head, tail: Model): Relation = new Relation(h.entity, h.link, tail) 
}  
  







