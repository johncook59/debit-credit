import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder

object GatlingRunner {

  def main(args: Array[String]): Unit = {

    val simClass: String = "DebitCreditSimulation"

    val props = new GatlingPropertiesBuilder().
      simulationClass(simClass)

    Gatling.fromMap(props.build)
  }
}
