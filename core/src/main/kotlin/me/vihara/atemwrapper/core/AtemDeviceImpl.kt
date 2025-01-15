package me.vihara.atemwrapper.core

import me.vihara.atemwrapper.api.device.AtemDevice
import me.vihara.atemwrapper.protocol.ProtocolClient

class AtemDeviceImpl(
    hostName: String,
    port: Int
) : ProtocolClient(hostName, port), AtemDevice {

    private var protocolVersion: Float = 1.0f
    private var modelName: String = "Default Model"
    private var videoInputs: Int = 4
    private var videoOutputs: Int = 2
    private var extraData: Map<String, Any> = emptyMap()
    private var routing: Array<Int?> = arrayOfNulls(videoOutputs)
    private var outputLocks: Array<Boolean> = Array(videoOutputs) { false }

    override fun getProtocolVersion(): Float = protocolVersion
    override fun getModelName(): String = modelName
    override fun getVideoInputs(): Int = videoInputs
    override fun getVideoOutputs(): Int = videoOutputs
    override fun getExtraData(): Map<String, Any> = extraData
    override fun getRouting(): Array<Int?> = routing
    override fun getOutputLocks(): Array<Boolean> = outputLocks

    override fun setRoute(output: Int, input: Int) {
        if (output in routing.indices && input in 0 until videoInputs) {
            routing[output] = input
            println("Route set: Output $output -> Input $input")
        } else {
            println("Invalid route: Output $output or Input $input is out of bounds")
        }
    }

    override fun setOutputLock(output: Int) {
        if (output in outputLocks.indices) {
            outputLocks[output] = true
            println("Output $output locked")
        } else {
            println("Invalid output: $output is out of bounds")
        }
    }

    override fun handleData(data: ByteArray) {
        val received = data.decodeToString()
        println("Received data: $received")
    }

    override fun handleError(e: Throwable) {
        println("Protocol error: ${e.message}")
    }
}
