package model

case class PartialResult(
                          processedMeasurements: Long,
                          failedMeasurements: Long,
                          computed: List[(String, Computed)]
                        )