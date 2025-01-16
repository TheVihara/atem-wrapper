package me.vihara.atemwrapper.core

import me.vihara.atemwrapper.api.parser.AtemParser
import me.vihara.atemwrapper.api.parser.impl.ConfigurationAtemParser
import me.vihara.atemwrapper.api.parser.impl.InputLabelsAtemParser
import me.vihara.atemwrapper.api.parser.impl.OutputLabelsAtemParser
import me.vihara.atemwrapper.api.parser.impl.ProtocolPreambleAtemParser
import me.vihara.atemwrapper.api.parser.impl.VideoHubDeviceAtemParser
import me.vihara.atemwrapper.api.parser.impl.VideoInputStatusAtemParser
import me.vihara.atemwrapper.api.parser.impl.VideoOutputLocksAtemParser
import me.vihara.atemwrapper.api.parser.impl.VideoOutputRoutingAtemParser

object ParserRegistry {
    private val parsers: Map<String, AtemParser<*>> = mapOf(
        "VIDEOHUB DEVICE" to VideoHubDeviceAtemParser(),
        "INPUT LABELS" to InputLabelsAtemParser(),
        "VIDEO INPUT STATUS" to VideoInputStatusAtemParser(),
        "OUTPUT LABELS" to OutputLabelsAtemParser(),
        "VIDEO OUTPUT ROUTING" to VideoOutputRoutingAtemParser(),
        "CONFIGURATION" to ConfigurationAtemParser(),
        "VIDEO OUTPUT LOCKS" to VideoOutputLocksAtemParser(),
        "PROTOCOL PREAMBLE" to ProtocolPreambleAtemParser()
    )

    fun <T> getParser(label: String): AtemParser<T>? {
        @Suppress("UNCHECKED_CAST")
        return parsers[label] as? AtemParser<T>
    }
}