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
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import org.springframework.web.bind.annotation.*
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
        val flowFuture = service.proxy.startFlow(::OriginateAssetFlowInitiator,
                assetData.toAssetContainerProperties(service.proxy),
                assetData.toGpsProperties(),
                assetData.toObligationProperties(service.proxy)).returnValue
        return executeTx(flowFuture)
    }

    @PostMapping("move")
    fun moveAsset(@RequestBody moveData: MoveData): ResponseEntity<String> {
        val flowFuture = service.proxy.startFlow(::MoveFlowInitiator,
                UniqueIdentifier.fromString(moveData.linearId),
                moveData.toGpsProperties()).returnValue
        return executeTx(flowFuture)
    }

    @GetMapping
    fun getAssets() = service.proxy.vaultQueryBy<AssetContainerState>(QueryCriteria.VaultQueryCriteria()).
            states.map { it.state.data }

    @GetMapping("trace")
    fun geAssetTrace(linearId: String) =
            service.proxy.vaultQueryBy<LocationState>(QueryCriteria.LinearStateQueryCriteria(
                    linearId = listOf(UniqueIdentifier.fromString(linearId)),
                    status = Vault.StateStatus.ALL))
                    .states.map { it.state.data }


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

            fun subscribeObservable(){
                updates.toBlocking().subscribe { newAsset ->
                    newAsset.produced.forEach {
                        when (contractStateType){
                            AssetContainerState::class.java -> println("new asset created: ${it.state.data}")
                            LocationState::class.java -> println("asset moved: ${it.state.data}")
                            else -> println("some updates: ${it.state.data}")
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