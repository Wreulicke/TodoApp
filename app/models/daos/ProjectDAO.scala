package models.daos
import models.entities.Project
import models.persistence.SlickTables.ProjectTable
import models.persistence.SlickTables

class ProjectDAO extends BaseDAO[ProjectTable,Project]{
  import dbConfig.driver.api._
  override protected val tableQ = SlickTables.projectTableQ
  def all=db.run(tableQ.to[List].result)
}