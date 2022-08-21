import service.StatisticsServiceImplementation.compute
import zio._
import zio.console._

object Main extends App {

  def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    ZIO.foreach(args) { path =>
      compute(path).tap(r => putStrLn(Rendered.render(r)))
    }.exitCode
}