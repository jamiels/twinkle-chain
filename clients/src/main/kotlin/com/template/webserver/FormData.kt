package com.template.webserver

import com.template.states.AssetContainerProperties
import com.template.states.GpsProperties
import com.template.states.ObligationProperties
import net.corda.core.contracts.Amount
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import java.time.Instant
import java.util.*


data class AssetContainerData(
        val data: String,
        val owner: String,
        val type: String,
        val longitude: Float,
        val latitude: Float,
        val beneficiary: String,
        val amount: Long){

    fun toAssetContainerProperties(proxy: CordaRPCOps) = AssetContainerProperties(
            data = data,
            owner = partyFromString(owner, proxy),
            type = type,
            dts = Instant.now()
    )

    fun toGpsProperties() = GpsProperties(
            longitude = longitude,
            latitude = latitude)

    fun  toObligationProperties(proxy: CordaRPCOps) = ObligationProperties(
            owner = partyFromString(owner, proxy),
            beneficiary = partyFromString(beneficiary, proxy),
            amount = Amount(amount * 100, Currency.getInstance("USD"))
    )
}

data class MoveData(
        val longitude: Float,
        val latitude: Float,
        val linearId: String){

    fun toGpsProperties() = GpsProperties(
            longitude = longitude,
            latitude = latitude)

}

data class ObligationData(val owner: String,
                                val beneficiary: String,
                                val amount: Long){

    fun  toObligationProperties(proxy: CordaRPCOps) = ObligationProperties(
            owner = partyFromString(owner, proxy),
            beneficiary = partyFromString(beneficiary, proxy),
            amount = Amount(amount * 100, Currency.getInstance("USD"))
    )
}

fun partyFromString(party: String, proxy: CordaRPCOps): Party{
    return proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(party))?: throw RuntimeException("Unknown Party: $party")
}