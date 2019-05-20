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
import utils.getAssetContainerByPhysicalContainerId
import java.util.*

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class MoveFlowInitiator(val physicalContainerID: String,
                        val gps: GpsProperties) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        // Stage 1. Retrieve States specified by linearId from the vault.
        val physicalContainerUUID = UUID.fromString(physicalContainerID)

        val assetStateAndRef = getAssetContainerByPhysicalContainerId(physicalContainerUUID, serviceHub) ?:
        throw CordaRuntimeException("state with such physicalContainerID does not exists")

        val linearId = assetStateAndRef.state.data.linearId

        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        //val assetStateAndRef =  serviceHub.vaultService.queryBy<AssetContainerState>(queryCriteria).states.single()
        val inputAsset = assetStateAndRef.state.data
        val locationStateAndRef =  serviceHub.vaultService.queryBy<LocationState>(queryCriteria).states.single()
        val inputLocation = locationStateAndRef.state.data
        val inputObligation =  serviceHub.vaultService.queryBy<ObligationState>(queryCriteria).states.single().state.data

        // Stage 2. Create the new Parent and Child state reflecting a new gps.
        val outputLocation = inputLocation.withNewGps(gps)

        // Stage 3. Create the transfer command.
        val signers = inputAsset.participants.map { it.owningKey }
        val transferCommand = Command(AssetContract.Commands.Transfer(), signers)

        // Stage 4. Get a reference to a transaction builder.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val builder = TransactionBuilder(notary = notary)

        // Stage 5. Create the transaction which comprises inputs, outputs and one command.
        builder.withItems(
                locationStateAndRef,
                StateAndContract(outputLocation, AssetContract.ID),
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
        val norarizedTx = subFlow(FinalityFlow(stx, sessions))

        // Stage 9 cashe data in firebase
        //Todo clean this do to cache moved to observable
        //FirebaseRepository().cacheMove(linearId.toString(), gps.latitude, gps.longitude)

        // Run transaction check
        subFlow(StartTransitionCheckFlow(StateRef(stx.tx.id, 0)))

        return norarizedTx
    }
}

@InitiatedBy(MoveFlowInitiator::class)
class MoveFlowResponder(val flowSession: FlowSession) : FlowLogic<SignedTransaction>() {
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
