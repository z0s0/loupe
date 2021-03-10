import service.Search

object DI {
  val live = Config.live >>> ElasticClient.live >>> Search.live
}
