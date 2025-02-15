package twinkle.agriledger.flows

import co.paralleluniverse.fibers.Suspendable
import com.heartbeat.StartTransitionCheckFlow
import net.corda.core.CordaRuntimeException
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import twinkle.agriledger.contracts.AssetContract
import twinkle.agriledger.states.GpsProperties
import twinkle.agriledger.states.LocationState
import twinkle.agriledger.states.ObligationState
import utils.getAssetContainerByPhysicalContainerId
import utils.getLocationByPhysicalContainerId
import utils.getObligationByPhysicalContainerId
import java.util.*

@InitiatingFlow
@StartableByRPC
class SplitAssetContainerFlow(val physicalContainerID: String,
                              val splitNumber: Int) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        // Stage 1. Retrieve States specified by linearId from the vault.
        val physicalContainerUUID = UUID.fromString(physicalContainerID)

        val assetStateAndRef = getAssetContainerByPhysicalContainerId(physicalContainerUUID, serviceHub)
                ?: throw CordaRuntimeException("state with such physicalContainerID does not exists")
        val inputAsset = assetStateAndRef.state.data

        val locationStateAndRef = getLocationByPhysicalContainerId(physicalContainerUUID, serviceHub)
                ?: throw CordaRuntimeException("state with such physicalContainerID does not exists")
        val inputLocation = locationStateAndRef.state.data

        val obligationStateAndRef = getObligationByPhysicalContainerId(physicalContainerUUID, serviceHub)
                ?: throw CordaRuntimeException("state with such physicalContainerID does not exists")
        val inputObligation = obligationStateAndRef.state.data

        // Stage 3. Create the transfer command.
        val signers = inputAsset.participants.map { it.owningKey }
        val transferCommand = Command(AssetContract.Commands.Transfer(), signers)

        // Stage 4. Get a reference to a transaction builder.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val builder = TransactionBuilder(notary = notary)
        builder.withItems(assetStateAndRef, locationStateAndRef, obligationStateAndRef)

        // Stage 2. Create the new Parent and Child states.
        (0 until splitNumber).forEach {
            val physicalContainerID = UUID.randomUUID()
            builder.withItems(
                    StateAndContract(inputAsset.withNewPhysicalContainerID(physicalContainerID), AssetContract.ID),
                    StateAndContract(inputLocation.withNewPhysicalContainerID(physicalContainerID), AssetContract.ID),
                    StateAndContract(inputObligation.withNewPhysicalContainerID(physicalContainerID), AssetContract.ID)
            )
        }

        // Stage 5. Create the transaction which comprises inputs, outputs and one command.
        builder.withItems(transferCommand)

        // Stage 6. Verify and sign the transaction.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        // Stage 7. Collect signature and add it to the transaction.
        // This also verifies the transaction and checks the signatures.
        val counterparty = if (serviceHub.myInfo.legalIdentities.first() == inputObligation.obligation.beneficiary) {
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

@InitiatedBy(SplitAssetContainerFlow::class)
class SplitAssetContainerFlowResponder(val flowSession: FlowSession) : FlowLogic<SignedTransaction>() {
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
