package com.heartbeat

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

/**
 * A blank contract and command, solely used for building a valid Heartbeat state transaction.
 */
open class TransitionCheckContract : Contract {
    companion object {
        const val contractID = "com.heartbeat.TransitionCheckContract"
    }

    override fun verify(tx: LedgerTransaction) {
        // Omitted for the purpose of this sample.
    }

    interface Commands : CommandData {
        class Check : Commands
    }
}