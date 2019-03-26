package com.template.states

import com.template.contracts.TemplateContract
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import java.util.*

// *********
// * State *
// *********
@BelongsToContract(TemplateContract::class)
data class ObligationState(//val asset: StaticPointer<AssetState>,
        val owner: Party,
        val beneficiary: Party,
        val amount: Amount<Currency>,
        override val linearId: UniqueIdentifier) : LinearState{
    override val participants: List<AbstractParty> = listOf(owner)
}
