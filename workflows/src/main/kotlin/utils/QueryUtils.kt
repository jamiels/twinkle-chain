package utils

import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import twinkle.agriledger.schema.AssetContainerSchemaV1
import twinkle.agriledger.states.AssetContainerState
import java.util.*


val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL)


    fun getAssetContainerByPhysicalContainerId(physicalContainerUUID: UUID, services: ServiceHub): StateAndRef<AssetContainerState>? {
        val states = getState<AssetContainerState>(services) { generalCriteria ->
            val additionalCriteria = QueryCriteria.VaultCustomQueryCriteria(AssetContainerSchemaV1.PersistentAssetContainer::physicalContainerID.equal(physicalContainerUUID))
            generalCriteria.and(additionalCriteria)
        }
        return states.singleOrNull()
    }

    private inline fun <reified U : ContractState> getState(
            services: ServiceHub,
            block: (generalCriteria: QueryCriteria.VaultQueryCriteria) -> QueryCriteria
    ): List<StateAndRef<U>> {
        val query = builder {
            val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
            block(generalCriteria)
        }
        val result = services.vaultService.queryBy<U>(query)
        return result.states
    }
