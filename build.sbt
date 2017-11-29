import sbt.Resolver.bintrayRepo

scalaVersion in ThisBuild := "2.11.8"

lazy val `url-shortener-api` = (project in file("url-shortener-api"))
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies += lagomScaladslApi
  )

lazy val `url-shortener-impl` = (project in file("url-shortener-impl"))
  .enablePlugins(LagomScala)
  .settings(
    version := "1.0-SNAPSHOT",
    resolvers += bintrayRepo("hajile", "maven"),
    libraryDependencies ++= Seq(
      "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided",
      lagomScaladslPersistenceCassandra,
      "com.lightbend" %% "lagom13-scala-service-locator-dns" % "2.2.2"
    )
  )
  .dependsOn(`url-shortener-api`)
