package CS441HW2

import LogSearchPayload.LogFinderGrpc.LogFinder
import LogSearchPayload.LogSearchOutput
import com.typesafe.config.{Config, ConfigFactory}

import java.util.logging.Logger
import io.grpc.{Server, ServerBuilder}
import LogSearchPayload.{LogFinderGrpc, LogFormatInput}

import scala.concurrent.{Await, ExecutionContext, Future}
import RESTCallerPackage.RESTCaller
import RESTCallerPackage.RESTCaller.{sendRequest, timeout}


object gRPCServer {
  private val logger = Logger.getLogger(classOf[gRPCServer].getName)
  val config: Config = ConfigFactory.load()

  def main(args: Array[String]): Unit = {
    val server = new gRPCServer(ExecutionContext.global)
    server.start()
    server.blockUntilShutdown()
  }
  private val port = config.getInt("HW2_gRPC.gRPCPort")
}

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

  private class LogFinderProcess extends LogFinder {
    val logger: Logger = Logger.getLogger(classOf[LogFinderProcess].getName)
    override def getLogsFromInput(request: LogFormatInput): Future[LogSearchOutput] = {
      logger.info("Received Date From the gRPC Client -> " + request.date)
      logger.info("Received Timestamp From the gRPC Client -> " + request.timeStamp)
      logger.info("Received interval (dT) From the gRPC Client -> " + request.interval)
      val results = Await.result(Await.result(sendRequest(date = request.date, timeStamp = request.timeStamp, dT = request.timeStamp), timeout), timeout)
      //val results = RESTCaller(date = request.date, timeStamp = request.timeStamp, dT = request.timeStamp)
      val reply = LogSearchOutput("Results after Calling Lambda Functions is " + results)
      Future.successful(reply)
    }
  }
}
