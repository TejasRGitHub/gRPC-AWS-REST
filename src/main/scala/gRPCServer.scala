package CS441HW2

import LogSearchPayload.LogFinderGrpc.LogFinder
import LogSearchPayload.LogSearchOutput
import com.typesafe.config.{Config, ConfigFactory}

import java.util.logging.Logger
import io.grpc.{Server, ServerBuilder}
import LogSearchPayload.{LogFinderGrpc, LogFormatInput}

import scala.concurrent.{Await, ExecutionContext, Future}
import RESTCallerPackage.RESTCaller.{config, sendRequest, timeout}


object gRPCServer {
  // Initialize the logger and configs
  private val logger = Logger.getLogger(classOf[gRPCServer].getName)
  val config: Config = ConfigFactory.load()

  // Set the port from the config file
  private val port = config.getInt("HW2_gRPC.gRPCPort")

  // This is the entry point for setting up a gRPC server
  def main(args: Array[String]): Unit = {
    val server = new gRPCServer(ExecutionContext.global)
    server.start()
    server.blockUntilShutdown()
  }

}

// gRPC Server contains methods to
// start the server
// gracefully shutdown the server when interrupted by sys.exit
// LogFinder Class containing methods for receiving gRPC call and sending a GET HTTP request for the input params
class gRPCServer(executionContext: ExecutionContext) { self =>
  // Change this var variable
  private[this] var server: Server = null

  private def start(): Unit = {
    server = ServerBuilder.forPort(gRPCServer.port).addService(LogFinderGrpc.bindService(new LogFinderProcess, executionContext)).build.start
    gRPCServer.logger.info("Server started, listening on " + gRPCServer.port)
    sys.addShutdownHook {
      System.err.println("*** shutting down gRPC server since JVM is shutting down")
      self.stop()
      System.err.println("*** server shut down")
    }
  }

  private def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

  private def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }

  // LogFinderProcess class overriders the getLogsFromInput methods written in the LogSearchPayload.proto file
  private class LogFinderProcess extends LogFinder {
    // Initializing separate logger for LogFinderProcess
    val logger: Logger = Logger.getLogger(classOf[LogFinderProcess].getName)

    // Functions to perform log searching
    override def getLogsFromInput(request: LogFormatInput): Future[LogSearchOutput] = {
      logger.info("Received Date From the gRPC Client -> " + request.date)
      logger.info("Received Timestamp From the gRPC Client -> " + request.timeStamp)
      logger.info("Received Interval (dT) From the gRPC Client -> " + request.interval)
      val url = config.getString("HW2_gRPC.LOG_STATUS_URL")
      logger.info("Calling REST URL -> " + url)
      val results = Await.result(Await.result(sendRequest(url = url, date = request.date, timeStamp = request.timeStamp, dT = request.interval), timeout), timeout)
      val reply = LogSearchOutput("Results after Calling Lambda Functions is " + results)
      Future.successful(reply)
    }
  }
}
