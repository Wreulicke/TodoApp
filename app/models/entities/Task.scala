package models.entities

case class Task(id: Long=0, var description: String="", project:Long,var status:Long=0) extends BaseEntity
