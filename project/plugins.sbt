addSbtPlugin("ch.epfl.scala"             % "sbt-missinglink"        % "0.3.1")      // Check for binary incompatibility in dependencies with `sbt missinglinkCheck`
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat"           % "0.1.11")     // Add extra compiler flags to prevent common mistakes
addSbtPlugin("net.virtual-void"          % "sbt-dependency-graph"   % "0.10.0-RC1") // Get a dependency graph with `sbt dependencyTree`
addSbtPlugin("org.jmotor.sbt"            % "sbt-dependency-updates" % "1.2.1")      // Check for dependency updates with `sbt dependencyUpdates`
addSbtPlugin("org.scalameta"             % "sbt-scalafmt"           % "2.2.1")      // Format code with `sbt scalafmt`
addSbtPlugin("org.scoverage"             % "sbt-scoverage"          % "1.6.1")      // Runs code coverage for the project `sbt coverage` https://github.com/scoverage/sbt-scoverage