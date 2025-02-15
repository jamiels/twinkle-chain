package twinkle.agriledger.schema

import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.time.Instant
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * The family of schemas for DbConnectorState.
 */
object AssetContainerSchema

/**
 * An DbConnectorState schema.
 */
object AssetContainerSchemaV1 : MappedSchema(
        schemaFamily = AssetContainerSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentAssetContainer::class.java)) {
    @Entity
    @Table(name = "asset_container_states")
    class PersistentAssetContainer(

            @Column(name = "owner")
            val owner: String,

            @Column(name = "producerID")
            val producerId: Int,

            @Column(name = "pick_up_requestId")
            val prId: String,

            @Column(name = "stage")
            val stage: String,

            @Column(name = "type")
            val type: String,

            @Column(name = "physicalContainerID")
            val physicalContainerID: UUID,

            @Column(name = "date")
            val dts: Instant,

            @Column(name = "linearId")
            val linearId: String
    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor(): this("", 0,"", "", "", UUID.randomUUID(), Instant.now(), "")

    }
}