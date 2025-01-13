package me.vihara.atemwrapper.core

import io.ktor.network.sockets.*
import me.vihara.atemwrapper.api.device.AtemDevice
import me.vihara.atemwrapper.protocol.ProtocolClient

class AtemDeviceImpl : AtemDevice(), ProtocolClient {
    override var socket: Socket? = null

    override fun setRoute(output: Int, input: Int) {

    }

    override fun setOutputLock(output: Int) {

    }
}