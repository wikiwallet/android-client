package io.golos.posts_parsing_rendering.json_to_dto.mappers

import io.golos.domain.post.post_dto.TagBlock
import org.json.JSONObject

class TagMapper(mappersFactory: MappersFactory): MapperBase<TagBlock>(mappersFactory) {
    override fun map(source: JSONObject): TagBlock = TagBlock(source.getContentAsString())
}