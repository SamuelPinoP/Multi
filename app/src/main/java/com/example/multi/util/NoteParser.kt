package com.example.multi.util

import android.net.Uri

/** Represents either a text segment or an image URI within a note. */
sealed class NoteBlock {
    data class TextBlock(val text: String) : NoteBlock()
    data class ImageBlock(val uri: String) : NoteBlock()
}

private val imageRegex = Regex("\\[image:(.+?)\\]")

/** Parse this string into a list of [NoteBlock]s using [imageRegex]. */
fun String.parseNoteBlocks(): List<NoteBlock> {
    val result = mutableListOf<NoteBlock>()
    var index = 0
    for (match in imageRegex.findAll(this)) {
        val start = match.range.first
        if (start > index) {
            result.add(NoteBlock.TextBlock(substring(index, start)))
        }
        val uri = match.groupValues[1]
        result.add(NoteBlock.ImageBlock(uri))
        index = match.range.last + 1
    }
    if (index < length) {
        result.add(NoteBlock.TextBlock(substring(index)))
    }
    return result
}
