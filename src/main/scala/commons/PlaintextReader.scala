package commons

import commons.Baudot.{BitString, stringToBaudotChunks}

object PlaintextReader {
  import Language._
  private def fileNameForLanguage(language: Language.Value): String =
    language match {
      case English => "/plaintext-zitadelle-1943-04-25.txt"
      case German  => "/plaintext-zitadelle-de-1943-04-25.txt"
    }

  def loadLorenzPlaintext(language: Language.Value): String = {
    val fileName = fileNameForLanguage(language)
    val source =
      scala.io.Source.fromInputStream(getClass.getResourceAsStream(fileName))
    try {
      // Not strictly needed, but for now we are ingesting the plaintext without carriage returns etc ...
      source.mkString.replaceAll("\r\n|\n|\r", "")
    } finally {
      source.close()
    }
  }

  def loadLorenzBitStream(language: Language.Value): Option[Seq[BitString]] = {
    val p = loadLorenzPlaintext(language)
    stringToBaudotChunks(p) match {
      case Some(value: Seq[BitString]) => Some(value)
      case None        => None
    }
  }
}
