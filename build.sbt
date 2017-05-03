name := "huit-api"

version := "1.0"

lazy val `huit-api` = (project in file(".")).enablePlugins(PlayJava, PlayEbean, DebianPlugin)

maintainer in Linux := "Abdoul Boubacar <arbmainassara@gmail.com>"

packageSummary in Linux := "API REST pour la gestion de la table du huit américain"

packageDescription := "API REST pour la gestion de la table du huit américain"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  filters,
  "postgresql" % "postgresql" % "9.1-903.jdbc4",
  "org.mindrot" % "jbcrypt" % "0.3m"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
