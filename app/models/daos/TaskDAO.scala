package models.daos

import models.entities.Task
import models.persistence.SlickTables
import models.persistence.SlickTables.TaskTable
import slick.lifted.TableQuery

class TaskDAO extends BaseDAO[TaskTable,Task]{
  import dbConfig.driver.api._
  override protected val tableQ = SlickTables.taskTableQ
  def findByProject(project:Long)={
    findByFilter(_.project===project)
  }
}