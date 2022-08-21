package service

import model.{Computed, PartialResult}
import zio.test._
import model.Result

object StatisticsServiceSpec extends DefaultRunnableSpec {

  val computedMeasurements: List[(String, Computed)] = List(
    ("s2", Computed(Some(78), Some(83), Some(88))),
    ("s1", Computed(Some(10), Some(54), Some(98))),
    ("s3", Computed(None, None, None))
  )

  val expected: Result = Result(2, PartialResult(5, 1, computedMeasurements))
  val path = s"${System.getProperty("user.dir")}/src/main/scala/files"

  override def spec = suite("StatisticsServiceSpec")(
    testM("returns the right calculations for the measurements found in the files from the directory") {
      StatisticsServiceImplementation.compute(path).map(x => assertTrue(x == expected))
    }


  )
}
