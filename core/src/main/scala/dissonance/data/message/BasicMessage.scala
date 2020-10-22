package dissonance.data.message

// Most of the time you only care about the id, content, author, and channel
object BasicMessage {
  def unapply(message: Message) = Some((message.id, message.content, message.author, message.channelId))
}
