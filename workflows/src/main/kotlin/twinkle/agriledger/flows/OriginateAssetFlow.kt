package twinkle.agriledger.flows

import co.paralleluniverse.fibers.Suspendable
import twinkle.agriledger.contracts.TemplateContract
//import agriledger.twinkle.firebase.FirebaseRepository
import twinkle.agriledger.states.*
import net.corda.core.contracts.*
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.time.Instant


// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class OriginateAssetFlowInitiator(val assetContainer: AssetContainerProperties,
                                  val gps: GpsProperties,
                                  val obligation: ObligationProperties) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        // Step 1. Get a reference to the notary service on our network and our key pair.
        // Note: ongoing work to support multiple notary identities is still in progress.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val participants = listOf(assetContainer.owner, obligation.beneficiary)

        //create state and additional data for child states
        val dts = Instant.now()
        val assetState = AssetContainerState(assetContainer, participants = participants)


        // Step 2. Create a new issue command.
        // Remember that a command is a CommandData object and a list of CompositeKeys
        val issueCommand = Command(TemplateContract.Commands.Issue(),
                participants.map { it.owningKey })

        // Step 3. Create a new TransactionBuilder object.
        val builder = TransactionBuilder(notary = notary)

        // Step 4. Add the parent output state, as well as a command to the transaction builder.
        builder.addOutputState(assetState, TemplateContract.ID)


        // Create child states
        val locationState = LocationState(gps, participants, assetState.linearId)
        val obligationState = ObligationState(obligation, assetState.linearId)

        builder.addOutputState(locationState, TemplateContract.ID)
        builder.addOutputState(obligationState, TemplateContract.ID)
        builder.addCommand(issueCommand)

        // Step 5. Verify and sign parent state with our KeyPair.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        // Stage 6. Collect signature and add it to the transaction.
        // This also verifies the transaction and checks the signatures.
        val sessions = listOf(initiateFlow(obligation.beneficiary))
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        // Stage 9. Notarise and record the transaction in our vaults.
        val notarizedTx= subFlow(FinalityFlow(stx, sessions))

        // Stage 10 cache data into firebase
        //Todo clean this do to cache moved to observable
//        FirebaseRepository().cacheAsset(assetState.linearId.toString(),
//                assetContainer.data, assetContainer.owner.toString(), assetContainer.type, dts, gps.latitude, gps.longitude)

        return notarizedTx
    }


}

@InitiatedBy(OriginateAssetFlowInitiator::class)
class OriginateAssetFlowResponder(val flowSession: FlowSession) : FlowLogic<SignedTransaction>() {
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



