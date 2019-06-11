package twinkle.agriledger.states

import twinkle.agriledger.contracts.AssetContract
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import twinkle.agriledger.schema.LocationSchemaV1
import java.util.*

// *********
// * State *
// *********
@BelongsToContract(AssetContract::class)
data class LocationState(val gps: GpsProperties,
                         val physicalContainerID: UUID,
                         val allParticipants: List<Party>,
                         val linearId: UniqueIdentifier) : QueryableState {
    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is LocationSchemaV1 -> LocationSchemaV1.PersistentLocation(
                    this.gps.longitude,
                    this.gps.latitude,
                    this.physicalContainerID,
                    this.linearId.toString()
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(LocationSchemaV1)

    override val participants: List<AbstractParty> = allParticipants

    fun withNewGps(gps: GpsProperties) = copy(gps = gps)

    fun withNewPhysicalContainerID(physicalContainerID: UUID) =
            copy(physicalContainerID = physicalContainerID)
}

@CordaSerializable
data class GpsProperties(val longitude: Float = -72.288193,
                         val latitude: Float = 18.513815)