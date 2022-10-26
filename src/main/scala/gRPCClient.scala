package CS441HW2

import LogSearchPayload.{LogFinderGrpc, LogFormatInput}
import com.typesafe.config.ConfigFactory
import java.util.concurrent.TimeUnit
import java.util.logging.{Level, Logger}
import io.grpc.{ManagedChannelBuilder, StatusRuntimeException}

class gRPCClient{}

object gRPCClient {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val logger = Logger.getLogger(classOf[gRPCClient].getName)
    val port = config.getInt("HW2_gRPC.gRPCPort")
    val channel = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build
    val blockingStub = LogFinderGrpc.blockingStub(channel)
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

