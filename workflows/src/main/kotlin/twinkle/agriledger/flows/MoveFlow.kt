package twinkle.agriledger.flows

//import agriledger.twinkle.firebase.FirebaseRepository
import co.paralleluniverse.fibers.Suspendable
import twinkle.agriledger.contracts.TemplateContract
import twinkle.agriledger.states.AssetContainerState
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

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class MoveFlowInitiator(val linearId: UniqueIdentifier,
                        val gps: GpsProperties) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        // Stage 1. Retrieve States specified by linearId from the vault.
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val assetStateAndRef =  serviceHub.vaultService.queryBy<AssetContainerState>(queryCriteria).states.single()
        val inputAsset = assetStateAndRef.state.data
        val locationStateAndRef =  serviceHub.vaultService.queryBy<LocationState>(queryCriteria).states.single()
        val inputLocation = locationStateAndRef.state.data
        val inputObligation =  serviceHub.vaultService.queryBy<ObligationState>(queryCriteria).states.single().state.data

        // Stage 2. Create the new Parent and Child state reflecting a new gps.
        val outputLocation = inputLocation.withNewGps(gps)


        // Stage 3. Create the transfer command.
        val signers = listOf(inputAsset.assetContainer.owner.owningKey)
        val transferCommand = Command(TemplateContract.Commands.Transfer(), signers)

        // Stage 4. Get a reference to a transaction builder.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val builder = TransactionBuilder(notary = notary)

        // Stage 5. Create the transaction which comprises inputs, outputs and one command.
        builder.withItems(
                locationStateAndRef,
                StateAndContract(outputLocation, TemplateContract.ID),
                transferCommand)

        // Stage 6. Verify and sign the transaction.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        // Stage 7. Collect signature and add it to the transaction.
        // This also verifies the transaction and checks the signatures.
        val sessions = listOf(initiateFlow(inputObligation.obligation.beneficiary))
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        // Stage 8. Notarise and record the transaction in our vaults.
        val norarizedTx = subFlow(FinalityFlow(stx, sessions))

        // Stage 9 cashe data in firebase
        //Todo clean this do to cache moved to observable
        //FirebaseRepository().cacheMove(linearId.toString(), gps.latitude, gps.longitude)

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
