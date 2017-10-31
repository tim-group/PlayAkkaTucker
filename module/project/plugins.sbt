resolvers := Seq(
  "TIM Group Repo" at "http://repo.net.local/nexus/content/groups/public",
  Resolver.url("TIMGroup Ivy", url("http://repo.youdevise.com/nexus/content/groups/public_ivy/"))(Resolver.ivyStylePatterns)
)
