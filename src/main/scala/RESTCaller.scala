package RESTCallerPackage

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.StatusCodes.{OK, Success}
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import akka.stream.Materializer
import com.typesafe.config.{Config, ConfigFactory}

import java.util.logging.Logger
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{DurationInt, FiniteDuration}

/* REST Caller is used to make rest calls to the lambda functions to invoke the GET apis
   This object contains generic methods which takes in -
   URL - Endpoint where the REST service is hosted
   date - Date component of the log file. This is used to get the log file stored on S3
   timeStamp - Timestamp for which logs messages have to be found out
   dT - Small interval which is used to create range time range
*/
object RESTCaller {
  //Initializing the Akka system
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)
  import system.dispatcher

  // Initialise Logger and configs
  private[this] val logger = Logger.getLogger(classOf[RESTCaller.type].getName)
  val config: Config = ConfigFactory.load()

  // This config is used as timeout for any REST call unmarshalling to take place
  val timeout: FiniteDuration = config.getInt("HW2_gRPC.WaitTime").seconds

  // Send Request method to create a HTTP Rest GET call
  def sendRequest(url: String, date:String, timeStamp: String, dT: String): Future[Future[String]] = {
    // Construct a GET URL
    logger.info("Calling Rest for input --->>>>")
    logger.info(url)
    logger.info(date)
    logger.info(timeStamp)
    logger.info(dT)

    val uri = url + "?date=" + date + "&timestamp=" + timeStamp + "&interval=" + dT
    val responseFuture: Future[HttpResponse] = Http().singleRequest(Get(uri))

    // Get the status of the response
    // If 200 then the logs records were fetched or log finding ( for gRPC client ) was completed
    responseFuture.map((res) =>
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
    )
  }

  // main function to invoke REST Client
  def main(args: Array[String]): Unit = {
    val date = config.getString("HW2_gRPC.Date")
    val timeStamp = config.getString("HW2_gRPC.Timestamp")
    val interval = config.getString("HW2_gRPC.dT")
    val getMessageURL = config.getString("HW2_gRPC.GET_MSGS_URL")
    val clientRes = Await.result(Await.result(sendRequest(getMessageURL, date, timeStamp, interval), timeout), timeout)
    logger.info("Received data -> " + clientRes)

  }

}
