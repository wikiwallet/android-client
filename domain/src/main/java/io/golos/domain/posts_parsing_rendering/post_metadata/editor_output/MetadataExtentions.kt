package io.golos.domain.posts_parsing_rendering.post_metadata.editor_output

import android.graphics.Typeface
import android.text.style.CharacterStyle
import io.golos.domain.posts_parsing_rendering.post_metadata.TextStyle
import io.golos.domain.posts_parsing_rendering.post_metadata.spans.custom.LinkSpan
import io.golos.domain.posts_parsing_rendering.post_metadata.spans.custom.MentionSpan
import io.golos.domain.posts_parsing_rendering.post_metadata.spans.custom.TagSpan
import io.golos.domain.posts_parsing_rendering.post_metadata.spans_worker.SpansWorkerImpl
import io.golos.utils.helpers.splitToCharSequences

fun CharSequence.getParagraphMetadata(): List<ParagraphMetadata>? =
    this.splitToCharSequences("\n")            // Split to CharSequence
        .filter { it.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.map { paragraph ->
            val spansWorker = SpansWorkerImpl(paragraph)

            val spans = mutableListOf<SpanInfo<*>>()

            spansWorker.getSpansWithIntervals<CharacterStyle>(CharacterStyle::class)
                .forEach {
                    if(it.spanInterval.last != it.spanInterval.first) {
                        val spanInfo =
                            when(it.span) {
                                is LinkSpan -> LinkSpanInfo(it.spanInterval, it.span.value, it.span.value)
                                is TagSpan -> TagSpanInfo(it.spanInterval, it.span.value.toLowerCase(), it.span.value.toLowerCase().removeRange(0..0))
                                is MentionSpan -> MentionSpanInfo(it.spanInterval, it.span.value, it.span.value.removeRange(0..0))
                                else -> null
                            }

                        if(spanInfo != null) {
                            spans.add(spanInfo)
                        }

                    }
                }

            val planText = paragraph.toString()
            ParagraphMetadata(planText, spans)
        }

fun Int.mapTypefaceToEditorTextStyle(): TextStyle =
    when(this) {
        Typeface.ITALIC -> TextStyle.ITALIC
        Typeface.BOLD -> TextStyle.BOLD
        Typeface.BOLD_ITALIC -> TextStyle.BOLD_ITALIC
        else -> throw UnsupportedOperationException("This typeface is not supported: $this")
    }
