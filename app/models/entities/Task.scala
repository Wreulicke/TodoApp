package models.entities

case class Task(id: Long=0, description: String="", project:Long, status:Long=0) extends BaseEntity