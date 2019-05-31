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
object LocationSchema

/**
 * An DbConnectorState schema.
 */
object LocationSchemaV1 : MappedSchema(
        schemaFamily = LocationSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentLocation::class.java)) {
    @Entity
    @Table(name = "location_states")
    class PersistentLocation(

            @Column(name = "longitude")
            val longitude: Float,

            @Column(name = "latitude")
            val latitude: Float,

            @Column(name = "physicalContainerID")
            val physicalContainerID: UUID,

            @Column(name = "linearId")
            val linearId: String
    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor(): this(-1F, -1F,UUID.randomUUID(), "")

    }
}