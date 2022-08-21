package service

import model.{Computed, PartialResult, Measurement, Result}
import zio.{Task, ZIO}
import zio.blocking.Blocking
import zio.stream.{ZStream, ZTransducer}

import java.io.File

trait StatisticsService {
  def compute(path: String): ZIO[Blocking, Throwable, Result]
}

object StatisticsServiceImplementation extends StatisticsService {
  override def compute(path: String): ZIO[Blocking, Throwable, Result] = {

    val partialResult =
      getMeasurements(path).broadcast(3, 10).use {
        case s1 :: s2 :: s3 :: Nil => getPartialResults(s1, s2, s3)
        case _ => ZIO.fail(new Exception("Failure"))
      }

    (listFiles(path).runCount <&> partialResult).map {
      case (noOfProcessedFiles, partialResult) => Result(noOfProcessedFiles, partialResult)
    }
  }

  private def getMeasurements(path: String) =
    for {
      file <- listFiles(path)
      measurement <- readFile(file)
    } yield measurement

  private def getPartialResults(
                                 stream1: ZStream[Any, Throwable, Measurement],
                                 stream2: ZStream[Any, Throwable, Measurement],
                                 stream3: ZStream[Any, Throwable, Measurement]
                               ) =
    for {
      noOfMeasurements <- stream1.runCount.fork
      noOfFailedMeasurements <- stream2.filter(_.humidity.isEmpty).runCount.fork
      measurementsBySensor <- stream3.runCollect
        .map { measurements =>
          measurements
            .groupBy(_.id)
            .map { case (sensorId, measurementsForOneSensor) =>
              val humidityValues = measurementsForOneSensor.flatMap(_.humidity)
              val computed =
                if (humidityValues.isEmpty) Computed(None, None, None)
                else
                  Computed(
                    Some(humidityValues.min),
                    Some(humidityValues.foldLeft(0)(_ + _) / humidityValues.length),
                    Some(humidityValues.max)
                  )
              sensorId -> computed
            }
            .toList
            .sortBy(_._2.avg)
            .reverse
        }
        .fork
      ((noOfMeasurements, noOfFailedMeasurements), measurementsBySensor) <- noOfMeasurements.join <&> noOfFailedMeasurements.join <&> measurementsBySensor.join
    } yield PartialResult(noOfMeasurements, noOfFailedMeasurements, measurementsBySensor)

  private def readFile(file: File) =
    ZStream
      .fromFile(file.toPath)
      .transduce(ZTransducer.utf8Decode >>> ZTransducer.splitLines)
      .drop(1)
      .mapM(Measurement(_))

  private def listFiles(path: String) =
    ZStream
      .fromEffect(Task {
        val directory = new File(path)

        if (directory.exists && directory.isDirectory)
          directory.listFiles.filter(_.isFile).toList
        else
          List.empty
      })
      .flatMap(files => ZStream.fromIterable(files))
}