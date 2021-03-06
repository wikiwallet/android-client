package io.golos.domain.posts_parsing_rendering.mappers.editor_output_to_json

import io.golos.domain.posts_parsing_rendering.Attribute
import io.golos.domain.posts_parsing_rendering.BlockType
import io.golos.domain.posts_parsing_rendering.DocumentType
import io.golos.domain.posts_parsing_rendering.PostGlobalConstants
import io.golos.domain.posts_parsing_rendering.json_builder.JsonBuilderBlocks
import io.golos.domain.posts_parsing_rendering.json_builder.JsonBuilderImpl
import io.golos.domain.posts_parsing_rendering.json_builder.PostAttribute
import io.golos.domain.posts_parsing_rendering.mappers.editor_output_to_json.spans_splitter.*
import io.golos.domain.posts_parsing_rendering.post_metadata.editor_output.ControlMetadata
import io.golos.domain.posts_parsing_rendering.post_metadata.editor_output.EmbedMetadata
import io.golos.domain.posts_parsing_rendering.post_metadata.editor_output.EmbedType
import io.golos.domain.posts_parsing_rendering.post_metadata.editor_output.ParagraphMetadata

object EditorOutputToJsonMapper {
    fun mapPost(title:String,metadata: List<ControlMetadata>, localImagesUri: List<String>, deviceInfoEntity: String) =
        map(metadata, localImagesUri, BlockType.POST, DocumentType.BASIC, deviceInfoEntity,title)

    fun mapComment(metadata: List<ControlMetadata>, localImagesUri: List<String>, deviceInfoEntity: String) =
        map(metadata, localImagesUri, BlockType.DOCUMENT, DocumentType.COMMENT, deviceInfoEntity)

    @Suppress("NestedLambdaShadowedImplicitParameter")
    private fun map(
        metadata: List<ControlMetadata>,
        localImagesUri: List<String>,
        rootBlockType: BlockType,
        documentType: DocumentType,
        deviceInfoEntity: String,
        title: String? = null
    ): String {
        val builder = JsonBuilderImpl.create(deviceInfoEntity)
        val spansSplitter = SpansSplitter()

        builder.putBlock(
            rootBlockType,
            true,
            PostAttribute(
                Attribute.TITLE,
                title.toString()
            ),
            PostAttribute(
                Attribute.VERSION,
                PostGlobalConstants.postFormatVersion.toString()
            ),
            PostAttribute(
                Attribute.TYPE,
                documentType.value
            )
        ) {

            val paragraphs = metadata.filterIsInstance<ParagraphMetadata>()
            val embeds = metadata.filterIsInstance<EmbedMetadata>()

            paragraphs.forEachIndexed { index, paragraph ->
                val isLast = index == paragraphs.lastIndex && embeds.isEmpty()
                putParagraph(
                    paragraph,
                    it,
                    isLast,
                    spansSplitter
                )
            }

            if(embeds.isNotEmpty()) {
                it.putBlock(BlockType.ATTACHMENTS, true) {
                    putEmbed(
                        embeds.first(),
                        it,
                        localImagesUri
                    )
                }
            }
        }

        return builder.build()
    }

    private fun putEmbed(metadataBlock: EmbedMetadata, builder: JsonBuilderBlocks, localImagesUri: List<String>) {
        val uri = if(metadataBlock.type == EmbedType.LOCAL_IMAGE) localImagesUri[0] else metadataBlock.sourceUri.toString()

        builder.putBlock(metadataBlock.type.mapToBlockType(), true, uri)
    }

    private fun EmbedType.mapToBlockType(): BlockType =
        when(this) {
            EmbedType.LOCAL_IMAGE,
            EmbedType.EXTERNAL_IMAGE -> BlockType.IMAGE
            EmbedType.EXTERNAL_VIDEO -> BlockType.VIDEO
            EmbedType.EXTERNAL_WEBSITE -> BlockType.WEBSITE
        }

    private fun putParagraph(metadataBlock: ParagraphMetadata, builder: JsonBuilderBlocks, isLast: Boolean, splitter: SpansSplitter) {
        if(metadataBlock.plainText.isEmpty()) {
            return
        }

        builder.putBlock(BlockType.PARAGRAPH, isLast) {
            val textItems = splitter.split(metadataBlock)

            textItems.forEachIndexed { index, split ->
                val isLastSplit = index == textItems.indices.last

                when(split) {
                    is SplittedText -> builder.putBlock(
                        BlockType.TEXT, isLastSplit, split.content.replace("\"", "\\\""))

                    is SplittedTag -> builder.putBlock(
                        BlockType.TAG, isLastSplit, split.content)

                    is SplittedMention -> builder.putBlock(
                        BlockType.MENTION, isLastSplit, split.content)

                    is SplittedLink -> builder.putBlock(
                        BlockType.LINK,
                        isLastSplit,
                        split.content,
                        PostAttribute(
                            Attribute.URL,
                            split.uri.toString()
                        )
                    )
                }
            }
        }
    }
}