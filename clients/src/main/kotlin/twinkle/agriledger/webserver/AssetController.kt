package twinkle.agriledger.webserver

import twinkle.agriledger.flows.MoveFlowInitiator
import net.corda.core.messaging.startFlow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import twinkle.agriledger.flows.OriginateAssetFlowInitiator
import twinkle.agriledger.states.AssetContainerState
import twinkle.agriledger.states.LocationState
import twinkle.agriledger.webserver.servises.NodeService
import net.corda.core.concurrent.CordaFuture
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.Sort
import net.corda.core.node.services.vault.SortAttribute
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import org.springframework.web.bind.annotation.*
import twinkle.agriledger.states.ObligationState
import twinkle.agriledger.webserver.servises.FirebaseService
import java.time.Instant
import java.util.concurrent.CountDownLatch
import javax.annotation.PostConstruct
import kotlin.concurrent.thread

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("asset") // The paths for HTTP requests are relative to this base path.
class AssetController(val service: NodeService) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }


    @PostMapping("create")
    fun createAsset(@RequestBody assetData: AssetContainerData): ResponseEntity<String> {
        val assetData = if (assetData.owner == null){
            assetData.copy(owner = service.whoami().get("me").toString(), beneficiary = service.beneficiary().toString())
        } else assetData
        val flowFuture = service.proxy.startFlow(::OriginateAssetFlowInitiator,
                assetData.toAssetContainerProperties(service.proxy),
                assetData.toGpsProperties(),
                assetData.toObligationProperties(service.proxy)).returnValue
        return executeTx(flowFuture)
    }


    @PostMapping("move")
    fun moveAsset(@RequestBody moveData: MoveData): ResponseEntity<String> {
        val flowFuture = service.proxy.startFlow(::MoveFlowInitiator,
                moveData.physicalContainerID,
                moveData.toGpsProperties()).returnValue
        return executeTx(flowFuture)
    }


    @GetMapping
    fun getAssets(): List<AssetContainerState> {
        val sortAttribute = SortAttribute.Standard(Sort.VaultStateAttribute.RECORDED_TIME)
        val sort = Sort(setOf(Sort.SortColumn(sortAttribute, Sort.Direction.DESC)))
        return service.proxy.vaultQueryBy<AssetContainerState>(sorting = sort).states.map { it.state.data }
    }


    @GetMapping("trace")
    fun geAssetTrace(linearId: String) =
            service.proxy.vaultQueryBy<LocationState>(QueryCriteria.LinearStateQueryCriteria(
                    linearId = listOf(UniqueIdentifier.fromString(linearId))))
                    .states.map { it.state.data }

    @GetMapping("trace-status")
    fun geAssetTraceStatus(@RequestParam linearId: String) = geStateAndStatus(linearId, LocationState::class.java)


    @GetMapping("obligation-status")
    fun geAssetObligationStatus(@RequestParam linearId: String) = geStateAndStatus(linearId, ObligationState::class.java)


    private fun <T : ContractState>geStateAndStatus(linearId: String, contractState: Class<out T>): ResponseEntity<List<StateAndStatus>> {
        val vaultTrace = service.proxy.vaultQueryByCriteria(QueryCriteria.LinearStateQueryCriteria(
                linearId = listOf(UniqueIdentifier.fromString(linearId)),
                status = Vault.StateStatus.ALL), contractState)
        val statesAndStatuses = mutableListOf<StateAndStatus>()
        val states = vaultTrace.states.iterator()
        val statesMetadata = vaultTrace.statesMetadata.iterator()
        while (states.hasNext() && statesMetadata.hasNext()) {
            val state = states.next()
            val stateMetadata = statesMetadata.next()
            statesAndStatuses.add(StateAndStatus(state.ref.txhash.toString(), state.state.data, stateMetadata.status, stateMetadata.recordedTime))
        }
        return ResponseEntity.ok().body(statesAndStatuses)
    }

    data class StateAndStatus(val tx: String, val state: ContractState, val status: Vault.StateStatus, val recordedTime: Instant)


    private fun executeTx(flowFuture: CordaFuture<SignedTransaction>): ResponseEntity<String> {
        val result = try {
            flowFuture.getOrThrow()
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
        return ResponseEntity.ok("Transaction id ${result.tx.id} committed to ledger.")
    }


    @PostConstruct
    private fun subscribeAssetUpdate() = subscribObservable(AssetContainerState::class.java)


    @PostConstruct
    private fun subscribeAssetMoveUpdate() = subscribObservable(LocationState::class.java)


    private fun <T : ContractState> subscribObservable(contractStateType: Class<out T>) {
        val countDownLatch = CountDownLatch(1)
        // Track Asset updates in the vault
        val (snapshot, updates) = service.proxy.vaultTrack(contractStateType)
        thread {
            val updates = updates
            countDownLatch.countDown()

            fun subscribeObservable() {
                updates.toBlocking().subscribe { newAsset ->
                    newAsset.produced.forEach {
                        val contractState = it.state.data
                        when (contractState) {
                            is AssetContainerState -> {
                                // cache new asset data into firebase
                                FirebaseService().cacheAsset(contractState.linearId.toString(),
                                        contractState.assetContainer)
                                println("new asset created: $contractState")
                            }
                            is LocationState -> {
                                FirebaseService().cacheMove(contractState.linearId.toString(), contractState.gps)
                                println("asset moved: ${contractState}")
                            }
                            else -> println("some updates: ${contractState}")
                        }

                    }
                }
                subscribeObservable()
            }
            subscribeObservable()
        }
        countDownLatch.await()
    }
}

