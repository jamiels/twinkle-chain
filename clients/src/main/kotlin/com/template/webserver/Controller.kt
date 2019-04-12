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

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy
    private val me = proxy.nodeInfo().legalIdentities.first()
    private val myLegalName = me.name

    @GetMapping(value = "/templateendpoint", produces = arrayOf("text/plain"))
    private fun templateendpoint(): String {
        return "Define an endpoint here."
    }

    @GetMapping("me")
    fun whoami() = mapOf("me" to myLegalName)

    /**
     * Returns all parties registered with the network map.
     */
    @GetMapping("peers")
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = proxy.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentities.first().name }
                .filter { it !in listOf(myLegalName) })
    }

    @PostMapping("create-asset")
    fun createAsset(@RequestBody assetData: AssetContainerData): ResponseEntity<String> {
        val flowFuture = proxy.startFlow(::OriginateAssetFlowInitiator,
                assetData.toAssetContainerProperties(proxy),
                assetData.toGpsProperties(),
                assetData.toObligationProperties(proxy)).returnValue
        return executeTx(flowFuture)
    }

    @PostMapping("move-asset")
    fun moveAsset(@RequestBody moveData: MoveData): ResponseEntity<String> {
        val flowFuture = proxy.startFlow(::MoveFlowInitiator,
                UniqueIdentifier.fromString(moveData.linearId),
                moveData.toGpsProperties()).returnValue
        return executeTx(flowFuture)
    }

    @GetMapping("vault")
    fun getVault(): Pair<List<StateAndRef<ContractState>>, List<StateAndRef<ContractState>>> {
        val unconsumedStates = proxy.vaultQueryBy<ContractState>(QueryCriteria.VaultQueryCriteria()).states
        val consumedStates = proxy.vaultQueryBy<ContractState>(QueryCriteria.VaultQueryCriteria(Vault.StateStatus.CONSUMED)).states
        return Pair(unconsumedStates, consumedStates)
    }

    @GetMapping("assets")
    fun getAssets() = proxy.vaultQueryBy<AssetContainerState>(QueryCriteria.VaultQueryCriteria()).states

    @GetMapping("asset-trace")
    fun geAssetTrace(linearIdString: String) =
            proxy.vaultQueryBy<LocationState>(QueryCriteria.LinearStateQueryCriteria(linearId = listOf(UniqueIdentifier.fromString(linearIdString))))


    private fun executeTx(flowFuture: CordaFuture<SignedTransaction>): ResponseEntity<String> {
        val result = try {
            flowFuture.getOrThrow()
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
        return ResponseEntity.ok("Transaction id ${result.tx.id} committed to ledger.")
        }



}