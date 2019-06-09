package com.heartbeat

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.SchedulableState
import net.corda.core.contracts.ScheduledActivity
import net.corda.core.contracts.StateRef
import net.corda.core.flows.FlowLogicRefFactory
import net.corda.core.identity.Party
import java.time.Instant

/**
 * @property nextActivityTime When the scheduled activity should be kicked off.
 */
@BelongsToContract(TransitionCheckContract::class)
class TransitionCheckState(
        private val me: Party,
        private val moveStateRef: StateRef,
        private val nextActivityTime: Instant = Instant.now().plusSeconds(10)
) : SchedulableState {

    override val participants get() = listOf(me)

    // Defines the scheduled activity to be conducted by the SchedulableState.
    override fun nextScheduledActivity(thisStateRef: StateRef, flowLogicRefFactory: FlowLogicRefFactory): ScheduledActivity? {
        return ScheduledActivity(flowLogicRefFactory.create(TransitionCheckFlow::class.java, moveStateRef, thisStateRef), nextActivityTime)
    }

}