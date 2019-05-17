package twinkle.agriledger.states

import twinkle.agriledger.contracts.TemplateContract
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
@BelongsToContract(TemplateContract::class)
data class AssetContainerState(val assetContainer: AssetContainerProperties,
                               override val linearId: UniqueIdentifier = UniqueIdentifier(),
                               val linearIdHash: SecureHash = SecureHash.sha256(linearId.toString()),
                               override val participants: List<AbstractParty> = listOf()) : LinearState, QueryableState {

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is AssetContainerSchemaV1 -> AssetContainerSchemaV1.PersistentAssetContainer(
                    this.assetContainer.owner.name.toString(),
                    this.assetContainer.producerID,
                    this.assetContainer.type,
                    this.assetContainer.physicalContainerID,
                    this.assetContainer.dts
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(AssetContainerSchemaV1)

}

@CordaSerializable
data class AssetContainerProperties(val owner: Party,
                                    val producerID: Int,
                                    val type: String,
                                    val physicalContainerID: UUID,
                                    val dts: Instant = Instant.now())


