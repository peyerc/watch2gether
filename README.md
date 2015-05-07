# watch2gether

## Installation and Deployment

### Install
1. git clone git@github.com:mmz-srf/srf-peyerc/watch2gether.git
2. For OS X: brew install sbt. For other systems check [http://www.scala-sbt.org/download.html](http://www.scala-sbt.org/download.html)
3. Import project in IntellJ: File->Import Project select the build.sbt file. IntellJ will then import and download everything. (Prerequisite: scala plugin for IntelliJ)

### Deployment
1. sbt dist
2. cf push watch2gether -p target/universal/watch2gether-1.0-SNAPSHOT.zip