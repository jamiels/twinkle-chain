package com.template.states

import com.template.contracts.TemplateContract
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

// *********
// * State *
// *********
@BelongsToContract(TemplateContract::class)
data class LocationState(//val asset: StaticPointer<AssetState>,
        val gps: Gps,
        override val linearId: UniqueIdentifier) : LinearState{
    override val participants: List<AbstractParty> = listOf()
    fun withNewGps(gps: Gps) = copy(gps = gps)
}

@CordaSerializable
data class Gps(val longitude: Float,
               val latitude: Float)