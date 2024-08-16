package commons

import scala.io.Source
import scala.util.{Try, Success, Failure}


trait FileService {

  final def ENGLISH_FILE: List[String] = {
    Source.fromResource("zitadelle-en.txt").getLines().toList
  }

  final def GERMAN_FILE: List[String] = {
    Source.fromResource("zitadelle-de.txt").getLines().toList
  }
}
