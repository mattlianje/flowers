package commons

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.io.Source

class FileServiceTest extends AnyFlatSpec with Matchers with FileService {

  "FileService" should "load default English content successfully" in {
    val englishFileContent = ENGLISH_FILE
    englishFileContent should not be empty
    englishFileContent.head should be(
      "To OKH OP ABT and to OKH Foreign Armies East from Army Group South IA 01 No 411/43, signed von Weich, General Feldsmarchall,"
    )
  }

  it should "load default German content successfully" in {
    val germanFileContent = GERMAN_FILE
    germanFileContent should not be empty
    germanFileContent.head should be(
      "An OKH OP ABT und an OKH Foreign Armies East der Heeresgruppe Sud IA 01 Nr. 411/43, unterzeichnet von Weich, General Feldsmarchall,"
    )
  }
}
