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
                               val linearIdHash: SecureHash = SecureHash.sha256(linearId.toString())) : LinearState {
    override val participants: List<AbstractParty> = listOf(assetContainer.owner)
}

@CordaSerializable
data class AssetContainerProperties(val data: String,
                                    val owner: Party,
                                    val producerID: Int,
                                    val type: String,
                                    val dts: Instant = Instant.now())

@CordaSerializable
enum class ProductTypeEnum {
    MANGO,
    AVOCADO,
    PINEAPPLE
}

