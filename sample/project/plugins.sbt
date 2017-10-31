// Comment to get more information during initialization
logLevel := Level.Warn

resolvers := Seq(
  "TIM Group Repo" at "http://repo.youdevise.com/nexus/content/groups/public",
  Resolver.url("TIMGroup Ivy", url("http://repo.youdevise.com/nexus/content/groups/public_ivy/"))(Resolver.ivyStylePatterns)
)

// Use the Play sbt plugin for Play projects
addSbtPlugin("play" % "sbt-plugin" % "2.0.8")

// Visualize all dependencies of this project
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")
