import model.{Computed, Result}

object Rendered {

  def render(result: Result): String =
    s"""
       |Num of processed files: ${result.processedFiles}
       |Num of processed measurements: ${result.partialResult.processedMeasurements}
       |Num of failed measurements: ${result.partialResult.failedMeasurements}
       |
       |Sensors with highest avg humidity:
       |
       |sensor-id,min,avg,max
       |${renderComputed(result.partialResult.computed)}
       |""".stripMargin

  private def renderComputed(list: List[(String, Computed)]): String =
    list.map {
      x => s"${x._1},${get(x._2.min)},${get(x._2.avg)},${get(x._2.max)}"
    }.mkString("\n")

  private def get(optionValue: Option[Int]) = optionValue.getOrElse("NaN")

}
