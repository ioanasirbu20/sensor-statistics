package model

import zio.{Task, UIO, ZIO}

case class Measurement(id: String, humidity: Option[Int])

object Measurement {
  def apply(raw: String): Task[Measurement] =
    raw.split(",").toList match {
      case id :: values =>
        UIO(Measurement(id, values.headOption.flatMap(_.toIntOption)))
      case _ => ZIO.fail(new Exception(s"Failed to parse: $raw"))
    }
}
