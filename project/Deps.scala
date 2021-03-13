import sbt._

object Deps {
  object vsn {
    val elastic4sVersion = "7.10.0"
    val cats = "2.1.1"
    val zio = "1.0.3"
    val interopCats = "2.2.0.1"
    val `zio-config` = "1.0.0-RC27"
    val `zio-logging` = "0.5.2"
    val http4s = "0.21.11"
    val testcontainers = "1.15.0-rc2"
    val pureConfigVersion = "0.14.0"
    val slf4j = "1.7.30"
    val logback = "1.2.3"
    val tapir = "0.17.12"
  }

  val testcontainers = List(
    "org.testcontainers" % "testcontainers" % "1.15.0-rc2" % Test,
    "org.testcontainers" % "elasticsearch" % "1.15.0-rc2" % Test
  )

  val catsDeps = List("org.typelevel" %% "cats-core" % vsn.cats)

  val configDeps = List(
    "com.github.pureconfig" %% "pureconfig" % vsn.pureConfigVersion
  )

  val elastic4S = List(
    "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % vsn.elastic4sVersion,
    "com.sksamuel.elastic4s" %% "elastic4s-effect-zio" % vsn.elastic4sVersion,
    "com.sksamuel.elastic4s" %% "elastic4s-testkit" % vsn.elastic4sVersion % "test"
  )

  val logDeps = Seq(
    "ch.qos.logback" % "logback-classic" % vsn.logback,
    "org.slf4j" % "slf4j-api" % vsn.slf4j
  )

  val http4sDeps = List(
    "org.http4s" %% "http4s-blaze-server" % vsn.http4s,
    "org.http4s" %% "http4s-circe" % vsn.http4s,
    "org.http4s" %% "http4s-dsl" % vsn.http4s,
  )

  val zioDeps = List(
    "dev.zio" %% "zio" % vsn.zio,
    "dev.zio" %% "zio-macros" % vsn.zio,
    "dev.zio" %% "zio-interop-cats" % vsn.interopCats,
    "dev.zio" %% "zio-test" % vsn.zio % Test,
    "dev.zio" %% "zio-test-sbt" % vsn.zio % Test,
    "dev.zio" %% "zio-test-magnolia" % vsn.zio % Test
  )

  val tapirDeps = Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-zio" % vsn.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-zio-http4s-server" % vsn.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % vsn.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-core" % vsn.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-json-play" % vsn.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % vsn.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % vsn.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s" % vsn.tapir
  )

  val deps =
    elastic4S ++ configDeps ++ catsDeps ++ logDeps ++ http4sDeps ++ zioDeps ++ tapirDeps ++
      testcontainers
}
