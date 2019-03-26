package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.TemplateContract
import com.template.states.AssetState
import com.template.states.Gps
import com.template.states.LocationState
import com.template.states.ObligationState
import net.corda.core.contracts.*
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class MoveFlowInitiator(val linearId: UniqueIdentifier,
                        val longitude: Float,
                        val latitude: Float) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        // Stage 1. Retrieve States specified by linearId from the vault.
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val assetStateAndRef =  serviceHub.vaultService.queryBy<AssetState>(queryCriteria).states.single()
        val inputAsset = assetStateAndRef.state.data
        val locationStateAndRef =  serviceHub.vaultService.queryBy<LocationState>(queryCriteria).states.single()
        val inputLocation = locationStateAndRef.state.data
        val inputObligation =  serviceHub.vaultService.queryBy<ObligationState>(queryCriteria).states.single().state.data

        // Stage 2. Create the new Parent and Child state reflecting a new gps.
        val gps = Gps(longitude, latitude)
        val outputAsset = inputAsset.withNewDts()
        val outputLocation = inputLocation.withNewGps(gps)


        // Stage 3. Create the transfer command.
        val signers = listOf(inputAsset.owner.owningKey)
        val transferCommand = Command(TemplateContract.Commands.Transfer(), signers)

        // Stage 4. Get a reference to a transaction builder.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val builder = TransactionBuilder(notary = notary)

        // Stage 5. Create the transaction which comprises inputs, outputs and one command.
        builder.withItems(assetStateAndRef,
                locationStateAndRef,
                StateAndContract(outputAsset, TemplateContract.ID),
                StateAndContract(outputLocation, TemplateContract.ID),
                transferCommand)

        // Stage 6. Verify and sign the transaction.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        // Stage 7. Collect signature and add it to the transaction.
        // This also verifies the transaction and checks the signatures.
        val sessions = listOf(initiateFlow(inputObligation.beneficiary))
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        // Stage 8. Notarise and record the transaction in our vaults.
        return subFlow(FinalityFlow(stx, sessions))
    }
}

@InitiatedBy(MoveFlowInitiator::class)
class MoveFlowResponder(val flowSession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                // some requirements
            }
        }

        val txWeJustSignedId = subFlow(signedTransactionFlow)

        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txWeJustSignedId.id))
    }
}
