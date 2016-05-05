package controllers

import scala.concurrent.Future

import javax.inject.Inject
import models.daos.AbstractBaseDAO
import models.daos.ProjectDAO
import models.daos.TaskDAO
import models.entities.Project
import models.entities.Task
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Action
import play.api.mvc.Controller
import spray.json._
import play.mvc.Http.Response
import models.response.ResponseMessage
import models.response.ResponseFormat

class Application @Inject()(projects: ProjectDAO, tasks: TaskDAO)
                           extends Controller with ResponseFormat{
  
  def index = Action.async{
    projects.findById(0).map { f => f.fold(NoContent)(projects => Ok(projects.id.toString())) }
  }
  
  def addTask=Action.async{implicit rs=>
    rs.body.asJson.map { x => new Task(project=x.\("project").as[Long],description=x.\("description").asOpt[String].getOrElse("")) } match {
      case Some(task) => 
        val result=for {
         id <- tasks.insert(task)
        } yield tasks.findById(id)
        result.flatMap {
          f =>
            f.collect{
                case Some(task) => Ok(task.toJsonStr)
                case None => BadRequest("error")
            }
        }
      case None => Future{BadRequest("error")}
    }
  }
  
  implicit def toJsonStr[T](instance:T)(implicit write:JsonWriter[T])={
    instance.toJson(write).toString()
  }
  
  def newProject=Action.async{implicit rs=>
    rs.body.asJson.map { json => new Project(name=json.\("name").asOpt[String].getOrElse("")) } match {
      case Some(project) =>
        projects.insert(project).flatMap {id =>  
          projects.findById(id).collect {
              case Some(p) => p.toJsonResponse
              case None => BadGateway(ResponseMessage("failed to regist a new project or occured internal server error").toJsonStr)
          }
        }
        
      case None => Future{
        BadRequest(ResponseMessage("invalid request").toJsonStr)
      }
    }
  }
  
  def allProject=Action.async{
    projects.all.map(_.toJsonResponse)
  }
  
  def project(project:Long)=Action.async{
    projects.findById(project).map {_.toJsonResponse}
  }
  
  def taskList(project:Long)=Action.async{implicit rs=>
    tasks.findByProject(project).map {_.toJsonResponse}
  }
}


