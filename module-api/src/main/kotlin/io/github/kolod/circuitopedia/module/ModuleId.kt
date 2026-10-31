package io.github.kolod.circuitopedia.module

/**
 * Stable identifier of a module, used for navigation and saved state. It is never shown to
 * users and must never change once a module has shipped.
 */
@JvmInline
value class ModuleId(val value: String) {
    init {
        require(value.length in 1..MAX_LENGTH && PATTERN.matches(value)) {
            "Module id must be kebab-case [a-z0-9]+(-[a-z0-9]+)*, 1..$MAX_LENGTH chars: \"$value\""
        }
    }

    override fun toString(): String = value

    private companion object {
        const val MAX_LENGTH = 64
        val PATTERN = Regex("[a-z0-9]+(-[a-z0-9]+)*")
    }
}
