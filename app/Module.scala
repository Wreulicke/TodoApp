import java.time.Clock

import com.google.inject.AbstractModule
import com.google.inject.Provides

import models.daos.AbstractBaseDAO
import models.daos.BaseDAO
import models.entities.Project
import models.persistence.SlickTables
import models.persistence.SlickTables.ProjectTable
import models.persistence.SlickTables.TaskTable
import slick.lifted.TableQuery
import models.entities.Task
import models.daos.TaskDAO
import models.daos.ProjectDAO


class Module extends AbstractModule {

  override def configure() = {
    // Use the system clock as the default implementation of Clock
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
  }

  @Provides
  def projects=new ProjectDAO
  
  @Provides
  def tasks = new TaskDAO
  

}



