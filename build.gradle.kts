plugins {
   id("us.ihmc.ihmc-build") version "0.23.0"
   id("us.ihmc.ihmc-ci") version "7.3"
   id("us.ihmc.ihmc-cd") version "1.17"
}

ihmc {
   group = "us.ihmc"
   version = "2.1.5"
   vcsUrl = "https://github.com/ihmcrobotics/ihmc-video-codecs"
   openSource = true

   configureDependencyResolution()
   javaDirectory("main", "examples")
   javaDirectory("main", "swig/generated")
   configurePublications()
}

mainDependencies {
	api("org.apache.commons:commons-compress:1.8.1")
}
