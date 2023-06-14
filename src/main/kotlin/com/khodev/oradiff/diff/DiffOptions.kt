package com.khodev.oradiff.diff

data class DiffOptions (
    val withTablespace: Boolean,
    val ignoreSourceComments: Boolean,
    val ignoreObjectComments: Boolean,
    val ignoreGrantChanges: Boolean
)