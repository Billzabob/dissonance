package dissonance.data

// Most of the time you only care about the name
object BasicEmoji {
  def unapply(emoji: Emoji) = emoji.name
}
