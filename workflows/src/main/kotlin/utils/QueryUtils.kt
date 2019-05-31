package utils

import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import twinkle.agriledger.schema.AssetContainerSchemaV1
import twinkle.agriledger.schema.LocationSchemaV1
import twinkle.agriledger.schema.ObligationSchemaV1
import twinkle.agriledger.states.AssetContainerState
import twinkle.agriledger.states.LocationState
import twinkle.agriledger.states.ObligationState
import java.util.*


val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)


fun getAssetContainerByPhysicalContainerId(physicalContainerUUID: UUID, services: ServiceHub): StateAndRef<AssetContainerState>? {
    val states = getState<AssetContainerState>(services) { generalCriteria ->
        val additionalCriteria = QueryCriteria.VaultCustomQueryCriteria(AssetContainerSchemaV1.PersistentAssetContainer::physicalContainerID.equal(physicalContainerUUID))
        generalCriteria.and(additionalCriteria)
    }
    return states.singleOrNull()
}

fun getLocationByPhysicalContainerId(physicalContainerUUID: UUID, services: ServiceHub): StateAndRef<LocationState>? {
    val states = getState<LocationState>(services) { generalCriteria ->
        val additionalCriteria =
                QueryCriteria.VaultCustomQueryCriteria(LocationSchemaV1.PersistentLocation::physicalContainerID.equal(physicalContainerUUID))
        generalCriteria.and(additionalCriteria)
    }
    return states.singleOrNull()
}

fun getObligationByPhysicalContainerId(physicalContainerUUID: UUID, services: ServiceHub): StateAndRef<ObligationState>? {
    val states = getState<ObligationState>(services) { generalCriteria ->
        val additionalCriteria =
                QueryCriteria.VaultCustomQueryCriteria(ObligationSchemaV1.PersistentObligation::physicalContainerID.equal(physicalContainerUUID))
        generalCriteria.and(additionalCriteria)
    }
    return states.singleOrNull()
}

fun getAssetContainerByLinearId(linearId: String, services: ServiceHub): List<StateAndRef<AssetContainerState>> {
    return getState<AssetContainerState>(services) { generalCriteria ->
        val additionalCriteria =
                QueryCriteria.VaultCustomQueryCriteria(AssetContainerSchemaV1.PersistentAssetContainer::linearId.equal(linearId))
        generalCriteria.and(additionalCriteria)
    }
}

fun getLocationByLinearId(linearId: String, services: ServiceHub): List<StateAndRef<LocationState>> {
    return getState<LocationState>(services) { generalCriteria ->
        val additionalCriteria =
                QueryCriteria.VaultCustomQueryCriteria(LocationSchemaV1.PersistentLocation::linearId.equal(linearId))
        generalCriteria.and(additionalCriteria)
    }
}

fun getObligationByLinearId(linearId: String, services: ServiceHub): List<StateAndRef<ObligationState>> {
    return getState<ObligationState>(services) { generalCriteria ->
        val additionalCriteria =
                QueryCriteria.VaultCustomQueryCriteria(ObligationSchemaV1.PersistentObligation::linearId.equal(linearId))
        generalCriteria.and(additionalCriteria)
    }
}

fun getAssetContainerByLinearId(linearId: String, services: CordaRPCOps): List<StateAndRef<AssetContainerState>> {
    return getState<AssetContainerState>(services) { generalCriteria ->
        val additionalCriteria =
                QueryCriteria.VaultCustomQueryCriteria(AssetContainerSchemaV1.PersistentAssetContainer::linearId.equal(linearId))
        generalCriteria.and(additionalCriteria)
    }
}

fun getLocationByLinearId(linearId: String, services: CordaRPCOps): List<StateAndRef<LocationState>> {
    return getState<LocationState>(services) { generalCriteria ->
        val additionalCriteria =
                QueryCriteria.VaultCustomQueryCriteria(LocationSchemaV1.PersistentLocation::linearId.equal(linearId))
        generalCriteria.and(additionalCriteria)
    }
}

fun getObligationByLinearId(linearId: String, services: CordaRPCOps): List<StateAndRef<ObligationState>> {
    return getState<ObligationState>(services) { generalCriteria ->
        val additionalCriteria =
                QueryCriteria.VaultCustomQueryCriteria(ObligationSchemaV1.PersistentObligation::linearId.equal(linearId))
        generalCriteria.and(additionalCriteria)
    }
}

fun getAssetContainerByLinearIdAll(linearId: String, services: CordaRPCOps): Vault.Page<AssetContainerState> {
    return getAllStates<AssetContainerState>(services) { generalCriteria ->
        val additionalCriteria =
                QueryCriteria.VaultCustomQueryCriteria(AssetContainerSchemaV1.PersistentAssetContainer::linearId.equal(linearId),
                        status = Vault.StateStatus.ALL)
        generalCriteria.and(additionalCriteria)
    }
}

fun getLocationByLinearIdAll(linearId: String, services: CordaRPCOps): Vault.Page<LocationState> {
    return getAllStates<LocationState>(services) { generalCriteria ->
        val additionalCriteria =
                QueryCriteria.VaultCustomQueryCriteria(LocationSchemaV1.PersistentLocation::linearId.equal(linearId),
                        status = Vault.StateStatus.ALL)
        generalCriteria.and(additionalCriteria)
    }
}

fun getObligationByLinearIdAll(linearId: String, services: CordaRPCOps): Vault.Page<ObligationState> {
    return getAllStates<ObligationState>(services) { generalCriteria ->
        val additionalCriteria =
                QueryCriteria.VaultCustomQueryCriteria(ObligationSchemaV1.PersistentObligation::linearId.equal(linearId),
                        status = Vault.StateStatus.ALL)
        generalCriteria.and(additionalCriteria)
    }
}




private inline fun <reified U : ContractState> getState(
        services: ServiceHub,
        block: (generalCriteria: QueryCriteria.VaultQueryCriteria) -> QueryCriteria
): List<StateAndRef<U>> {
    val query = builder { block(generalCriteria) }
    val result = services.vaultService.queryBy<U>(query)
    return result.states
}

private inline fun <reified U : ContractState> getState(
        services: CordaRPCOps,
        block: (generalCriteria: QueryCriteria.VaultQueryCriteria) -> QueryCriteria
): List<StateAndRef<U>> {
    val query = builder {
        block(generalCriteria)
    }
    val result = services.vaultQueryBy<U>(query)
    return result.states
}

private inline fun <reified U : ContractState> getAllStates(
        services: CordaRPCOps,
        block: (generalCriteria: QueryCriteria.VaultQueryCriteria) -> QueryCriteria
): Vault.Page<U> {
    val query = builder {
        val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL)
        block(generalCriteria)
    }
    val result = services.vaultQueryBy<U>(query)
    return result
}
