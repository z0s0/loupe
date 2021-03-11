name := "loupe"

version := "0.1"

scalaVersion := "2.13.5"
libraryDependencies ++= Deps.deps
addCompilerPlugin(
  "org.typelevel" %% "kind-projector" % "0.11.3" cross CrossVersion.full
)
