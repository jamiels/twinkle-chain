package twinkle.agriledger.states

import twinkle.agriledger.contracts.TemplateContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

import java.time.Instant

// *********
// * State *
// *********
@BelongsToContract(TemplateContract::class)
data class AssetContainerState(val assetContainer: AssetContainerProperties,
                               override val linearId: UniqueIdentifier = UniqueIdentifier(),
                               val linearIdHash: SecureHash = SecureHash.sha256(linearId.toString()),
                               override val participants: List<AbstractParty> = listOf()) : LinearState {

}

@CordaSerializable
data class AssetContainerProperties(val owner: Party,
                                    val producerID: Int,
                                    val type: String,
                                    val physicalContainerID: UniqueIdentifier,
                                    val dts: Instant = Instant.now())


