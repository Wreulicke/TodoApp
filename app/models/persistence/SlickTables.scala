package models.persistence

import models.entities.Project
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig
import slick.driver.JdbcProfile
import models.entities.Task

/**
  * The companion object.
  */
object SlickTables extends HasDatabaseConfig[JdbcProfile] {

  protected lazy val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import dbConfig.driver.api._

  abstract class BaseTable[T](tag: Tag, name: String) extends Table[T](tag, name) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  }

  class ProjectTable(tag:Tag) extends BaseTable[Project](tag, "PROJECT") {
    def name=column[String]("NAME")
    def * = (id, name) <> (Project.tupled, Project.unapply)
  }
  implicit val projectTableQ : TableQuery[ProjectTable]=TableQuery[ProjectTable]
  
  class TaskTable(tag:Tag) extends BaseTable[Task](tag, "TASK") {
    def description=column[String]("DESCRIPTION")
    def project=column[Long]("PROJECT")
    def status=column[Long]("STATUS")
    def * = (id, description, project, status) <> (Task.tupled, Task.unapply)
  }
  implicit val taskTableQ : TableQuery[TaskTable]=TableQuery[TaskTable]
  

}
