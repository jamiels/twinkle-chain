package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.TemplateContract
import com.template.states.AssetState
import com.template.states.Gps
import com.template.states.LocationState
import com.template.states.ObligationState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.StaticPointer
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.*

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class OriginateAssetFlowInitiator(val assetState: AssetState,
                                  val gps: Gps,
                                  val beneficiary: Party,
                                  val amount: Amount<Currency>) : FlowLogic<List<SignedTransaction>>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): List<SignedTransaction> {
        // Step 1. Get a reference to the notary service on our network and our key pair.
        // Note: ongoing work to support multiple notary identities is still in progress.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        var signedTransactions = mutableListOf<SignedTransaction>()

        // Step 2. Create a new issue command.
        // Remember that a command is a CommandData object and a list of CompositeKeys
        val issueCommand = Command(TemplateContract.Commands.Issue(), assetState.participants.map { it.owningKey })

        // Step 3. Create a new TransactionBuilder object.
        val builder = TransactionBuilder(notary = notary)

        // Step 4. Add the iou as an output state, as well as a command to the transaction builder.
        builder.addOutputState(assetState, TemplateContract.ID)
        builder.addCommand(issueCommand)

        // Step 5. Verify and sign parent state with our KeyPair.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)
        signedTransactions.add(ptx)

        // Create child states


        // * I am not catch for what we need Static Pointer
        // ****************************** //
        //val stateRef = StateRef(ptx.id, 0)
        //val staticPointer = StaticPointer(stateRef, AssetState::class.java)
        //val locationState = LocationState(staticPointer, gps)
        //val obligationState = ObligationState(staticPointer, assetState.owner, beneficiary, amount)
        // ****************************** //

        val locationState = LocationState(gps, assetState.linearId)
        val obligationState = ObligationState(assetState.owner, beneficiary, amount, assetState.linearId)

        // build child transaction
        val builderChild = TransactionBuilder(notary = notary)
        builderChild.addOutputState(locationState, TemplateContract.ID)
        builderChild.addOutputState(obligationState, TemplateContract.ID)
        builderChild.addCommand(issueCommand)
        // Verify and sign child transaction with our KeyPair.
        builder.verify(serviceHub)
        signedTransactions.add(serviceHub.signInitialTransaction(builder))

        val sessions = listOf(initiateFlow(ourIdentity))
        // Step 6. Collect the other party's signature using the SignTransactionFlow.
        return signedTransactions.map{ ptx ->
            val stx = subFlow(CollectSignaturesInitiatingFlow(
                    ptx, sessions))
            subFlow(FinalityFlow(stx, sessions))
        }
    }
}

@InitiatedBy(Initiator::class)
class OriginateAssetFlowResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        // Responder flow logic goes here.
    }
}

@InitiatingFlow
class CollectSignaturesInitiatingFlow(val signedTransaction: SignedTransaction, val sessions: List<FlowSession>): FlowLogic<SignedTransaction>() {
    override fun call(): SignedTransaction {
        return subFlow(CollectSignaturesFlow(signedTransaction, sessions))
    }
}

@InitiatedBy(CollectSignaturesInitiatingFlow::class)
class CollectSignaturesInitiatingFlowResponder(val otherPartyFlow: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(otherPartyFlow) {
            override fun checkTransaction(stx: SignedTransaction) {
                TODO("Check the transaction here.")
            }
        }

        return subFlow(signTransactionFlow)
    }
}
