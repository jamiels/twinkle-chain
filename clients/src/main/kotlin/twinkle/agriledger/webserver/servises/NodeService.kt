package twinkle.agriledger.webserver.servises

import net.corda.core.identity.CordaX500Name
import org.springframework.stereotype.Service



@Service
class NodeService(rpc: NodeRPCConnection){

    val proxy = rpc.proxy
    private val me = proxy.nodeInfo().legalIdentities.first()
    private val myLegalName = me.name


    fun whoami() = mapOf("me" to myLegalName)

    /**
     * Returns all parties registered with the network map.
     */
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = proxy.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentities.first().name }
                .filter { it !in listOf(myLegalName) })
    }

    fun beneficiary() = proxy.networkMapSnapshot()
                .map { it.legalIdentities.first().name }
                .filter { it !in listOf(myLegalName) && it.organisation != "Notary" }.first()




}