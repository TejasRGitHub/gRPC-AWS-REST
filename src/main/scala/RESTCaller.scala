package RESTCallerPackage

import akka.actor.ActorSystem
import akka.actor.FSM.Failure
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.StatusCodes.{OK, Success}
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory

import java.util.logging.Logger
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{DurationInt, FiniteDuration}

object RESTCaller {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)
  import system.dispatcher

  private[this] val logger = Logger.getLogger(classOf[RESTCaller.type].getName)
  val config = ConfigFactory.load()
  val timeout: FiniteDuration = config.getInt("HW2_gRPC.WaitTime").seconds


  def sendRequest(date:String, timeStamp: String, dT: String): Future[Future[String]] = {
    val uri = "https://reqres.in/apis/users?page=2"
    val responseFuture: Future[HttpResponse] = Http().singleRequest(Get(uri))

    responseFuture map { res =>
      res.status match {
        case OK => {
          val entityFuture: Future[HttpEntity.Strict] = responseFuture.flatMap(response => response.entity.toStrict(timeout))
          logger.info("Extracting Data from Response")
          entityFuture.map(entity => entity.data.utf8String)
        }
        case _ =>{
           responseFuture.flatMap(response => {
             response.discardEntityBytes()
             Future{"Unable to find log messages with the given date and timestamp"}
           })
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val date = config.getString("HW2_gRPC.Date")
    val timeStamp = config.getString("HW2_gRPC.Timestamp")
    val interval = config.getString("HW2_gRPC.dT")
    val clientRes = Await.result(Await.result(sendRequest(date, timeStamp, interval), timeout), timeout)
    logger.info("Received data -> " + clientRes)

  }

}
