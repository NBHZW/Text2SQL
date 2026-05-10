package com.zealsinger.kotlin.agent.server.a2a

import io.a2a.server.TransportMetadata
import io.a2a.spec.TransportProtocol

class JsonRpcTransportMetadata : TransportMetadata {
    override fun getTransportProtocol(): String {
        return TransportProtocol.JSONRPC.toString()
    }
}