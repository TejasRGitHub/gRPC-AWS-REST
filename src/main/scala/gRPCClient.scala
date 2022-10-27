package CS441HW2

import LogSearchPayload.{LogFinderGrpc, LogFormatInput}
import com.typesafe.config.ConfigFactory
import java.util.concurrent.TimeUnit
import java.util.logging.{Level, Logger}
import io.grpc.{ManagedChannelBuilder, StatusRuntimeException}

class gRPCClient{}

// gRPC Client using protobuf to talk to gRPC server
// This client establishes connection and then calls the gRPC server with parameters
object gRPCClient {

  def main(args: Array[String]): Unit = {
    // Initialize the configs
    val config = ConfigFactory.load()
    val logger = Logger.getLogger(classOf[gRPCClient].getName)
    logger.info("Starting the gRPC Client and sending request to the server")
    val port = config.getInt("HW2_gRPC.gRPCPort")
    logger.info("Calling RPC on port " + port)
    val channel = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build
    val blockingStub = LogFinderGrpc.blockingStub(channel)

    // get the inputs from the config file
    val date = config.getString("HW2_gRPC.Date")
    val timeStamp = config.getString("HW2_gRPC.Timestamp")
    val interval = config.getString("HW2_gRPC.dT")
    val request = LogFormatInput(date= date, timeStamp = timeStamp, interval = interval)
    try {
      val response = blockingStub.getLogsFromInput(request)
      logger.info("Response After Sending to gRPC Server -> " + response.outputString)
    }
    catch {
      case e: StatusRuntimeException =>
        logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus)
    }
    finally {
      channel.shutdown.awaitTermination(config.getInt("HW2_gRPC.Shutdown"), TimeUnit.SECONDS)
    }
  }
}

