package com.heartbeat

import co.paralleluniverse.fibers.Suspendable
import com.heartbeat.TransitionCheckContract.Commands.Check
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateRef
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.SchedulableFlow
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import twinkle.agriledger.states.LocationState

/**
 * This is the flow that a Heartbeat state runs when it consumes itself to create a new Heartbeat
 * state on the ledger.
 *
 * @param stateRef the existing Heartbeat state to be updated.
 */
@InitiatingFlow
@SchedulableFlow
class TransitionCheckFlow(private val stateRefLocation: StateRef,
                          private val stateRefScheduled: StateRef) : FlowLogic<SignedTransaction>() {
    companion object {
        object GENERATING_TRANSACTION : Step("Generating a transaction.")
        object SIGNING_TRANSACTION : Step("Signing transaction with our private key.")
        object FINALISING_TRANSACTION : Step("Recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
                GENERATING_TRANSACTION,
                SIGNING_TRANSACTION,
                FINALISING_TRANSACTION
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): SignedTransaction {
        progressTracker.currentStep = GENERATING_TRANSACTION

        val location = serviceHub.vaultService.queryBy<LocationState>(QueryCriteria.VaultQueryCriteria(stateRefs = listOf(stateRefLocation), status = Vault.StateStatus.ALL))



        if (location.statesMetadata.first().status == Vault.StateStatus.UNCONSUMED){
            println("unconsumed")
        } else println("consumed")

        val input = serviceHub.toStateAndRef<TransitionCheckState>(stateRefScheduled)

        val beatCmd = Command(Check(), ourIdentity.owningKey)
        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities.first())
                .addInputState(input)
                .addCommand(beatCmd)

        progressTracker.currentStep = SIGNING_TRANSACTION
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        progressTracker.currentStep = FINALISING_TRANSACTION
        val stx = subFlow(FinalityFlow(signedTx, listOf()))
        return stx
    }
}