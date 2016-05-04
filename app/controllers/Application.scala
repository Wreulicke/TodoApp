package controllers

import scala.concurrent.Future

import javax.inject.Inject
import models.daos.AbstractBaseDAO
import models.daos.TaskDAO
import models.entities.Project
import models.entities.Task
import models.persistence.SlickTables.ProjectTable
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Action
import play.api.mvc.Controller
import spray.json._
import play.api.libs.json.JsResult
import play.api.libs.json.Format
import play.api.libs.json.Json
import models.daos.ProjectDAO

class Application @Inject()(projects: ProjectDAO, tasks: TaskDAO)
                           extends Controller with DefaultJsonProtocol{
  
  implicit val format=jsonFormat4(Task)
  implicit val pformat=jsonFormat2(Project)
  
  def index = Action.async{
    projects.findById(0).map { f => f.fold(NoContent)(projects => Ok(projects.id.toString())) }
  }
  
  def addTask=Action.async{implicit rs=>
    rs.body.asJson.map { x => new Task(project=x.\("project").as[Long],description=x.\("desc").asOpt[String].getOrElse("")) } match {
      case Some(task) => 
        val result=for {
         id <- tasks.insert(task)
        } yield tasks.findById(id)
        result.flatMap {
          f =>
            f.collect{
                case Some(x) => Ok(x.toJson.toString())
                case None => BadRequest("error")
            }
        }
      case None => Future{BadRequest("error")}
    }

  }
  implicit def toJsonStr[T](instance:T)(implicit write:JsonWriter[T])={
    instance.toJson(write).toString()
  }
  
  def allProject=Action.async{
    projects.all.map { toJsonStr(_) }.map { json => Ok(json) }
  }
  
  def project(project:Long)=Action.async{
    projects.findById(project).map { toJsonStr(_) }.map { json => Ok(json) }
  }
  
  def taskList(project:Long)=Action.async{implicit rs=>
    val list=tasks.findByProject(project)
    list.map { list => Ok(list.toJson.toString()) }
  }
}


