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
import play.api.mvc.Result

class Application @Inject() (projects: ProjectDAO, tasks: TaskDAO)
    extends Controller with ResponseFormat {

  def index = Action.async {
    projects.findById(0).map { f => f.fold(NoContent)(projects => Ok(projects.id.toString())) }
  }
  implicit class Res(message:String){
    def toBadRequest={
      ResponseMessage(message).toBadRequest
    }
  }
  def modifyTask = Action.async { implicit rs =>
    rs.body.asJson match {
      case Some(json) =>
        val id = json.\("id").asOpt[Long]
        id.map { id =>
          tasks.findById(id).flatMap {
            taskOpt =>
              taskOpt.map { task =>
                if (task.project != json.\("project").as[Int])
                  Future {
                    "project differ".toBadRequest
                  }
                else {
                  task.description=(json.\("description").asOpt[String]).getOrElse(task.description)
                  task.status=(json.\("status").asOpt[Long]).getOrElse(task.status)
                  tasks.update(task).map { size =>
                    if (size > 0) task.toJsonResponse
                    else "not found task".toBadRequest
                  }
                }
              }.getOrElse(Future { "update target is missed".toBadRequest })
          }
        }.getOrElse(Future { "malformed request:id is not found".toBadRequest })
      case None => Future { "malformed request".toBadRequest }
    }

  }
  def addTask = Action.async { implicit rs =>
    rs.body.asJson.map { x => new Task(project = x.\("project").as[Long], description = x.\("description").asOpt[String].getOrElse("")) } match {
      case Some(task) =>
        val result = for {
          id <- tasks.insert(task)
        } yield tasks.findById(id)
        result.flatMap {
          f =>
            f.collect {
              case Some(task) => Ok(task.toJsonStr)
              case None       => "error".toBadRequest
            }
        }
      case None => Future { "error".toBadRequest }
    }
  }

  def newProject = Action.async { implicit rs =>
    rs.body.asJson.map { json => new Project(name = json.\("name").asOpt[String].getOrElse("")) } match {
      case Some(project) =>
        projects.insert(project).flatMap { id =>
          projects.findById(id).collect {
            case Some(p) => p.toJsonResponse
            case None    => "failed to regist a new project or occured internal server error".toBadRequest
          }
        }

      case None => Future {
        "invalid request".toBadRequest
      }
    }
  }

  def allProject = Action.async {
    projects.all.map(_.toJsonResponse)
  }

  def project(project: Long) = Action.async {
    projects.findById(project).map { _.toJsonResponse }
  }

  def taskList(project: Long) = Action.async { implicit rs =>
    tasks.findByProject(project).map { _.toJsonResponse }
  }
}


