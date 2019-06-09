package twinkle.agriledger.flows

//import agriledger.twinkle.firebase.FirebaseRepository
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
import utils.getObligationByPhysicalContainerId
import java.util.*

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class AssetNewStageFlow(val physicalContainerId: String,
                        val stage: String) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        // Stage 1. Retrieve States specified by linearId from the vault.
        //val assetStateAndRef =  serviceHub.vaultService.queryBy<AssetContainerState>(queryCriteria).states.single()
        val containerId = UUID.fromString(physicalContainerId)
        val assetStateAndRef =  getAssetContainerByPhysicalContainerId(containerId, serviceHub) ?:
                throw RuntimeException("Container with phesical container id not found")
        val inputAsset = assetStateAndRef.state.data
        //val inputObligation =  serviceHub.vaultService.queryBy<ObligationState>(queryCriteria).states.single().state.data
        val inputObligation =  getObligationByPhysicalContainerId(containerId, serviceHub)!!.state.data

        // Stage 2. Create the new Parent and Child state reflecting a new stage.
        val outputAsset = inputAsset.withNewStage(stage)

        // Stage 3. Create the transfer command.
        val signers = inputAsset.participants.map { it.owningKey }
        val transferCommand = Command(AssetContract.Commands.NewStage(), signers)

        // Stage 4. Get a reference to a transaction builder.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val builder = TransactionBuilder(notary = notary)

        // Stage 5. Create the transaction which comprises inputs, outputs and one command.
        builder.withItems(
                assetStateAndRef,
                StateAndContract(outputAsset, AssetContract.ID),
                transferCommand)

        // Stage 6. Verify and sign the transaction.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        // Stage 7. Collect signature and add it to the transaction.
        // This also verifies the transaction and checks the signatures.
        val counterparty = if (serviceHub.myInfo.legalIdentities.first() == inputObligation.obligation.beneficiary){
            inputObligation.obligation.owner
        } else {
            inputObligation.obligation.beneficiary
        }
        val sessions = listOf(initiateFlow(counterparty))
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        // Stage 8. Notarise and record the transaction in our vaults.
        return subFlow(FinalityFlow(stx, sessions))
    }
}

@InitiatedBy(AssetNewStageFlow::class)
class AssetNewStageFlowResponder(val flowSession: FlowSession) : FlowLogic<SignedTransaction>() {
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
