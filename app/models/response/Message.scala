package models.response

import spray.json.DefaultJsonProtocol
import models.entities.Project
import models.entities.Task
import spray.json.JsonWriter
import play.api.mvc.Controller

case class ResponseMessage(message:String)

trait ResponseFormat extends Controller with DefaultJsonProtocol{
  implicit val responseFormat=jsonFormat1(ResponseMessage)
  implicit val taskFormat=jsonFormat4(Task)
  implicit val projectFormat=jsonFormat2(Project)
  
  implicit class JsonData[T](instance:T){
    def toJsonStr(implicit writer:JsonWriter[T])={
      writer.write(instance).toString()
    }
    def toJsonResponse(implicit writer:JsonWriter[T])={
      Ok(instance.toJsonStr)
    }
  } 
}
