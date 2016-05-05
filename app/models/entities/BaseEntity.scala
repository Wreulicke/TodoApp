package models.entities

import play.api.libs.json.JsValue

trait BaseEntity {
  val id : Long
  def isValid = true
  def update[T>:BaseEntity](key:String, value:Option[Any]):T={
    value match {
      case Some(v) => 
        this.getClass()
        .getDeclaredFields
        .filter { _.getName==key }
        .foreach(_.set(this, value))
      case None => ???
    }
    this
  }
}