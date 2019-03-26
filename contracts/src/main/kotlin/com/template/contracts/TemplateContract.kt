package com.template.contracts

import net.corda.core.contracts.*
import net.corda.core.contracts.Requirements.using
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class TemplateContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.TemplateContract"
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Issue : Commands
        class Transfer : Commands
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<TemplateContract.Commands>()
        when (command.value) {
            is Commands.Issue -> requireThat {

            }
            is Commands.Transfer -> requireThat {

            }

        }
    }
}