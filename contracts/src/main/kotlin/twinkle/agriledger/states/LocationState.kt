package twinkle.agriledger.states

import twinkle.agriledger.contracts.TemplateContract
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

// *********
// * State *
// *********
@BelongsToContract(TemplateContract::class)
data class LocationState(val gps: GpsProperties,
                         val allParticipants: List<Party>,
                         override val linearId: UniqueIdentifier) : LinearState {
    override val participants: List<AbstractParty> = allParticipants
    fun withNewGps(gps: GpsProperties) = copy(gps = gps)
}

@CordaSerializable
data class GpsProperties(val longitude: Float,
                         val latitude: Float)