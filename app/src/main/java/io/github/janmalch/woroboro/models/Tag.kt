package io.github.janmalch.woroboro.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tag(
    val label: String,
    val type: String,
) : Parcelable
