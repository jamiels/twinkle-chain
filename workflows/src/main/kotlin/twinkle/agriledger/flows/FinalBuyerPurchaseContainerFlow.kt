package twinkle.agriledger.flows

import co.paralleluniverse.fibers.Suspendable
import com.heartbeat.StartTransitionCheckFlow
import net.corda.core.CordaRuntimeException
import twinkle.agriledger.contracts.AssetContract
import twinkle.agriledger.states.GpsProperties
import twinkle.agriledger.states.LocationState
import twinkle.agriledger.states.ObligationState
import net.corda.core.contracts.*
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import twinkle.agriledger.states.AssetContainerState
import utils.getAssetContainerByPhysicalContainerId
import java.util.*

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class FinalBuyerPurchaseContainerFlow(val linearId: UniqueIdentifier) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        // Stage 1. Retrieve States specified by linearId from the vault.
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val assetStateAndRef =  serviceHub.vaultService.queryBy<AssetContainerState>(queryCriteria).states.single()
        val inputAsset = assetStateAndRef.state.data
        val locationStateAndRef =  serviceHub.vaultService.queryBy<LocationState>(queryCriteria).states.single()
        val inputLocation = locationStateAndRef.state.data
        val obligationStateAndRef =  serviceHub.vaultService.queryBy<ObligationState>(queryCriteria).states.single()

        // Stage 2. Create finalize command.
        val signers = inputAsset.participants.map { it.owningKey }
        val transferCommand = Command(AssetContract.Commands.Finalize(), signers)

        // Stage 3. Get a reference to a transaction builder.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val builder = TransactionBuilder(notary = notary)

        // Stage 4. Create the transaction which comprises 3 inputs, 0 outputs and one command.
        builder.withItems(assetStateAndRef,
                locationStateAndRef, obligationStateAndRef,
                transferCommand)

        // Stage 5. Verify and sign the transaction.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        // Stage 6. Collect signature and add it to the transaction.
        // This also verifies the transaction and checks the signatures.
        val inputObligation = obligationStateAndRef.state.data
        val counterparty = if (serviceHub.myInfo.legalIdentities.first() == inputObligation.obligation.beneficiary){
            inputObligation.obligation.owner
        } else {
            inputObligation.obligation.beneficiary
        }
        val sessions = listOf(initiateFlow(counterparty))
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        // Stage 8. Notarise and record the transaction in our vaults.
        val norarizedTx = subFlow(FinalityFlow(stx, sessions))

        return norarizedTx
    }
}

@InitiatedBy(FinalBuyerPurchaseContainerFlow::class)
class FinalBuyerPurchaseContainerFlowResponder(val flowSession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                // some requirements should be here
            }
        }

        val txWeJustSignedId = subFlow(signedTransactionFlow)

        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txWeJustSignedId.id))
    }
}