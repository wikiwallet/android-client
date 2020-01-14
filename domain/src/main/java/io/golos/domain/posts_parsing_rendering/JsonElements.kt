package io.golos.domain.posts_parsing_rendering

enum class BlockType(val value: String) {
    POST("post"),
    PARAGRAPH("paragraph"),
    TEXT("text"),
    TAG("tag"),
    MENTION("mention"),
    LINK("link"),
    IMAGE("image"),
    VIDEO("video"),
    WEBSITE("website"),
    ATTACHMENTS("attachments"),
    RICH("rich"),
    EMBED("embed"),
    UNDEFINED("undefined")
}

enum class Attribute(val value: String) {
    VERSION("version"),
    TITLE("title"),
    STYLE("style"),
    TEXT_COLOR("text_color"),
    URL("url"),
    TYPE("type"),
    THUMBNAIL_URL("thumbnailUrl"),
    THUMBNAIL_SIZE("thumbnail_size"),
    THUMBNAIL_WIDTH("thumbnailWidth"),
    THUMBNAIL_HEIGHT("thumbnailHeight"),
    WIDTH("width"),
    HEIGHT("height"),
    DESCRIPTION("description"),
    PROVIDER_NAME("providerName"),
    HTML("html"),
    AUTHOR("author"),
    AUTHOR_URL("authorUrl")
}

enum class CommonType(val value: String) {
    ID("id"),
    TYPE("type"),
}

object PostTypeJson {
    const val BASIC = "basic"
    const val ARTICLE = "article"
    const val COMMENT = "comment"
}