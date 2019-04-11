package com.template.states

import com.template.contracts.TemplateContract
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.util.*

// *********
// * State *
// *********
@BelongsToContract(TemplateContract::class)
data class ObligationState(//val asset: StaticPointer<AssetContainerState>,
        val obligation: ObligationProperties,
        override val linearId: UniqueIdentifier) : LinearState{
    override val participants: List<AbstractParty> = listOf(obligation.owner)
}

@CordaSerializable
data class ObligationProperties(val owner: Party,
                                val beneficiary: Party,
                                val amount: Amount<Currency>)
