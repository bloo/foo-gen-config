import sbt._
import Keys._
import java.io.File
import com.typesafe.config.ConfigFactory

object FooConfigPlugin extends sbt.Plugin {

	object FooKeys {
	    val fooGen = TaskKey[Seq[File]]("foo-gen", "Generate the resources")
 	}

	System.setProperty("config.trace", "loads")
	import FooKeys._

	val fooGenSettings = Seq(	
	    resourceGenerators in Compile <+= fooGen in Compile,
	    fooGen in Compile := Def.task {

		// need to have src/main/resources in Classpath
		// THIS DOES NOT WORK!
		//fullClasspath += Attributed.blank( (resourceDirectory in Compile).value )
		val cl = new java.net.URLClassLoader(Array((resourceDirectory in Compile).value.toURI.toURL))

	    	// load src/main/resources/application.conf
	    	val cfg = com.typesafe.config.ConfigFactory.load(cl)
		val barConfString = cfg getString "foo.bar"

		// http://www.scala-sbt.org/release/docs/Howto/generatefiles.html
		val out = (resourceManaged in Compile).value / "foo" / (version.value + ".out")
		val content = "foo.bar ~~> " + barConfString
	        IO.write(out, content)

		// return seq of generated files
	        Seq(out)
	    }.value
	)
}
