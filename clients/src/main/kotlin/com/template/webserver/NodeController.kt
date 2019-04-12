package com.template.webserver

import com.template.flows.MoveFlowInitiator
import net.corda.core.messaging.startFlow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import com.template.flows.OriginateAssetFlowInitiator
import com.template.states.AssetContainerState
import com.template.states.LocationState
import com.template.webserver.servises.NodeRPCConnection
import com.template.webserver.servises.NodeService
import net.corda.core.concurrent.CordaFuture
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import org.springframework.web.bind.annotation.*
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("node") // The paths for HTTP requests are relative to this base path.
class NodeController(val service: NodeService) {

    @GetMapping("me")
    fun whoami() = service.whoami()

    /**
     * Returns all parties registered with the network map.
     */
    @GetMapping("peers")
    fun getPeers() = service.getPeers()

    @GetMapping("vault")
    fun getVault(): Pair<List<StateAndRef<ContractState>>, List<StateAndRef<ContractState>>> {
        val unconsumedStates = service.proxy.vaultQueryBy<ContractState>(QueryCriteria.VaultQueryCriteria()).states
        val consumedStates = service.proxy.vaultQueryBy<ContractState>(QueryCriteria.VaultQueryCriteria(Vault.StateStatus.CONSUMED)).states
        return Pair(unconsumedStates, consumedStates)
    }




}