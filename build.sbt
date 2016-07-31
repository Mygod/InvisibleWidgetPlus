android.Plugin.androidBuild

platformTarget := "android-24"

name := "InvisibleWidgetPlus"

scalaVersion := "2.11.8"

javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

scalacOptions ++= Seq("-target:jvm-1.6", "-Xexperimental")

typedResources := false

typedViewHolders := false

shrinkResources := true

resConfigs := Seq("zh")

resolvers += Resolver.sonatypeRepo("public")

libraryDependencies += "tk.mygod" %% "mygod-lib-android" % "2.0.2"

useSupportVectors

proguardVersion in Android := "5.2.1"

proguardCache := Seq()

proguardConfig in Android := List("-dontobfuscate",
  "-dontoptimize",
  "-renamesourcefileattribute SourceFile",
  "-keepattributes SourceFile,LineNumberTable",
  "-verbose",
  "-flattenpackagehierarchy",
  "-dontusemixedcaseclassnames",
  "-dontpreverify",
  "-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*,!code/allocation/variable",
  "-keepattributes *Annotation*",
  "-dontnote android.annotation.**",
  "-dontwarn android.support.**",
  "-dontnote android.support.**",
  "-dontnote scala.ScalaObject",
  "-dontnote org.xml.sax.EntityResolver",
  "-dontnote scala.concurrent.forkjoin.**",
  "-dontwarn scala.beans.ScalaBeanInfo",
  "-dontwarn scala.concurrent.**",
  "-dontnote scala.reflect.**",
  "-dontwarn scala.reflect.**",
  "-dontwarn scala.sys.process.package$",
  "-dontwarn **$$anonfun$*",
  "-dontwarn scala.collection.immutable.RedBlack$Empty",
  "-dontwarn scala.tools.**,plugintemplate.**",

  "-keep class scala.collection.SeqLike { public java.lang.String toString(); }",

  "-keep class android.support.v7.view.menu.ListMenuItemView { <init>(...); }",
  "-keep class android.support.v7.widget.FitWindowsLinearLayout { <init>(...); }",
  "-keep class android.support.v7.widget.Toolbar { <init>(...); }",
  "-keep class android.support.v7.widget.ViewStubCompat { <init>(...); }",

  "-keep class tk.mygod.invisibleWidgetPlus.ActivitiesShortcut { <init>(...); }",
  "-keep class tk.mygod.invisibleWidgetPlus.DoNothingShortcut { <init>(...); }",
  "-keep class tk.mygod.invisibleWidgetPlus.InvisibleWidget { <init>(...); }",
  "-keep class tk.mygod.invisibleWidgetPlus.PackageListener { <init>(...); }",
  "-keep class tk.mygod.invisibleWidgetPlus.ShortcutsChooser { <init>(...); }")
