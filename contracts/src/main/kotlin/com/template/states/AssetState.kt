package com.template.states

import com.template.contracts.TemplateContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.time.Instant

// *********
// * State *
// *********
@BelongsToContract(TemplateContract::class)
data class AssetState(val data: String,
                      val owner: Party,
                      val type: String,
                      val dts: Instant,
                      override val linearId: UniqueIdentifier) : LinearState {
    override val participants: List<AbstractParty> = listOf(owner)

    fun withNewDts() = copy(dts = Instant.now())
}

