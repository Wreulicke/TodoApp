package models.response

import spray.json.DefaultJsonProtocol
import models.entities.Project
import models.entities.Task
import spray.json.JsonWriter
import play.api.mvc.Controller
import play.api.mvc.Results._
import spray.json._


case class ResponseMessage(message:String){
  def toBadRequest(implicit writer:JsonWriter[ResponseMessage])={
    BadRequest(this.toJson.toString())
  }
}

trait ResponseFormat extends DefaultJsonProtocol{
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
