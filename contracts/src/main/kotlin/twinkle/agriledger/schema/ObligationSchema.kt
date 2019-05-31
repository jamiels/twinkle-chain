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
object ObligationSchema

/**
 * An DbConnectorState schema.
 */
object ObligationSchemaV1 : MappedSchema(
        schemaFamily = ObligationSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentObligation::class.java)) {
    @Entity
    @Table(name = "Obligation_states")
    class PersistentObligation(
            @Column(name = "physicalContainerID")
            val physicalContainerID: UUID,

            @Column(name = "linearId")
            val linearId: String
    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor(): this(UUID.randomUUID(), "")

    }
}