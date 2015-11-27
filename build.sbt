import android.Keys._

android.Plugin.androidBuild

platformTarget in Android := "android-23"

name := "InvisibleWidgetPlus"

scalaVersion := "2.11.7"

javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

scalacOptions ++= Seq("-target:jvm-1.6", "-Xexperimental")

shrinkResources in Android := true

resolvers += Resolver.sonatypeRepo("public")

libraryDependencies += "tk.mygod" %% "mygod-lib-android" % "1.3.3-SNAPSHOT"
