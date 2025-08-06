package dev.turingcomplete.intellijgradleutilitiesplugin.other

inline fun <reified T> Any.safeCastTo(): T? = if (this is T) this else null
