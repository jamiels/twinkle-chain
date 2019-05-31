package twinkle.agriledger.states

import twinkle.agriledger.contracts.AssetContract
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import twinkle.agriledger.schema.ObligationSchemaV1
import java.util.*

// *********
// * State *
// *********
@BelongsToContract(AssetContract::class)
data class ObligationState(//val asset: StaticPointer<AssetContainerState>,
        val obligation: ObligationProperties,
        val physicalContainerID: UUID,
        val linearId: UniqueIdentifier) : QueryableState {
    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is ObligationSchemaV1 -> ObligationSchemaV1.PersistentObligation(
                    this.physicalContainerID,
                    this.linearId.toString()
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(ObligationSchemaV1)

    override val participants: List<AbstractParty> = listOf(obligation.owner, obligation.beneficiary)

    fun withNewPhysicalContainerID(physicalContainerID: UUID) =
            copy(physicalContainerID = physicalContainerID)
}

@CordaSerializable
data class ObligationProperties(val owner: Party,
                                val beneficiary: Party,
                                val amount: Amount<Currency>)
