organization in ThisBuild := "cbotek"

version in ThisBuild := "0.0.1"

scalaVersion in ThisBuild := "2.12.6"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test

lazy val `item-inventory-lagom` = (project in file("."))
  .aggregate(`item-api`, `item-impl`, `shared`)

lazy val `shared` = (project in file("shared"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      scalaTest
    )
  )

lazy val `item-api` = (project in file("item-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  ).dependsOn(`shared`)

lazy val `item-impl` = (project in file("item-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`item-api`)
