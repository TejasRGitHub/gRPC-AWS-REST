import com.typesafe.config.ConfigFactory
import org.scalatest.flatspec.AnyFlatSpec

class CommomUtilsUnitTests extends AnyFlatSpec{
  // Unit tests to check if the values from application.conf
  private val config = ConfigFactory.load()

  it should "Test to confirm if Time Stamp exits and is a string" in {
    val timeStamp = config.getString("HW2_gRPC.Timestamp")
    assert(timeStamp.nonEmpty)
  }

  it should "Test to confirm if Date exits and is a string" in {
    val date = config.getString("HW2_gRPC.Date")
    assert(date.nonEmpty)
  }

  it should "Test to confirm if dT exits and is a string" in {
    val dT = config.getString("HW2_gRPC.dT")
    assert(dT.nonEmpty)
  }

  it should "Test to confirm if gRPCPort exits and is a int" in {
    val gRPCPort = config.getInt("HW2_gRPC.gRPCPort")
    assert(gRPCPort.isValidInt)
  }

  it should "Test to confirm if WaitTime exits and is a int" in {
    val WaitTime = config.getInt("HW2_gRPC.WaitTime")
    assert(WaitTime.isValidInt)
  }

  it should "Test to confirm if GET_MSGS_URL exits and is a string" in {
    val msgURL = config.getString("HW2_gRPC.GET_MSGS_URL")
    assert(msgURL.nonEmpty)
  }

  it should "Test to confirm if LOG_STATUS_URL exits and is a string" in {
    val validateTimeRangeURL = config.getString("HW2_gRPC.LOG_STATUS_URL")
    assert(validateTimeRangeURL.nonEmpty)
  }


}
