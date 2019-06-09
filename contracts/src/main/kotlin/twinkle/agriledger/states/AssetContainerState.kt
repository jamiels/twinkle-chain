package twinkle.agriledger.states

import twinkle.agriledger.contracts.AssetContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import twinkle.agriledger.schema.AssetContainerSchemaV1

import java.time.Instant
import java.util.*

// *********
// * State *
// *********
@BelongsToContract(AssetContract::class)
data class AssetContainerState(val assetContainer: AssetContainerProperties,
                               val linearId: UniqueIdentifier = UniqueIdentifier(),
                               val physicalContainerID: UUID = linearId.id,
                               val linearIdHash: SecureHash = SecureHash.sha256(linearId.toString()),
                               override val participants: List<AbstractParty> = listOf()) : QueryableState {

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is AssetContainerSchemaV1 -> AssetContainerSchemaV1.PersistentAssetContainer(
                    this.assetContainer.owner.name.toString(),
                    this.assetContainer.producerID,
                    this.assetContainer.prId,
                    this.assetContainer.stage,
                    this.assetContainer.type,
                    this.physicalContainerID,
                    this.assetContainer.dts,
                    this.linearId.toString()
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(AssetContainerSchemaV1)

    fun withNewStage(stage: String) = copy(assetContainer = assetContainer.copy(stage = stage))

    fun withNewPhysicalContainerID(physicalContainerID: UUID) =
            copy(physicalContainerID = physicalContainerID)

}

@CordaSerializable
data class AssetContainerProperties(val owner: Party,
                                    val producerID: Int,
                                    val prId: String,
                                    val stage: String,
                                    val type: String,
                                    val dts: Instant = Instant.now())


