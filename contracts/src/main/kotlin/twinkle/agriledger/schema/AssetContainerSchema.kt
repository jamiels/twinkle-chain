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
            val producerID: Int,

            @Column(name = "type")
            val type: String,

            @Column(name = "physicalContainerID")
            val physicalContainerID: UUID,

            @Column(name = "date")
            val dts: Instant
    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor(): this("", 0,"", UUID.randomUUID(), Instant.now())

    }
}