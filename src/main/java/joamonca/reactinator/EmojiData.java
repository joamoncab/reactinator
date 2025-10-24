package joamonca.reactinator;

public class EmojiData {
    private String emojiName;
    private long emojiID;
    private boolean isAnimated;
    public EmojiData(String emojiName, long emojiID, boolean isAnimated) {
        this.emojiName = emojiName;
        this.emojiID = emojiID;
        this.isAnimated = isAnimated;
    }

    public long getEmojiID() {
        return emojiID;
    }

    public void setEmojiID(long emojiID) {
        this.emojiID = emojiID;
    }

    public String getEmojiName() {
        return emojiName;
    }

    public void setEmojiName(String emojiName) {
        this.emojiName = emojiName;
    }

    public boolean isAnimated() {
        return isAnimated;
    }

    public void setAnimated(boolean animated) {
        isAnimated = animated;
    }
}
