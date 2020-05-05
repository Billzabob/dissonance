package dissonance.model.message

// Most of the time you only care about the content, author, and channel
object BasicMessage {
  def unapply(message: Message) = Some((message.content, message.author, message.channelId))
}
