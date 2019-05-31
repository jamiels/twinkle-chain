package twinkle.agriledger.webserver.servises

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.util.ResourceUtils
import twinkle.agriledger.states.AssetContainerProperties
import twinkle.agriledger.states.GpsProperties
import java.io.File
import java.io.FileInputStream
import java.time.Instant
import java.util.concurrent.CountDownLatch

fun main(args : Array<String>) {
    val repo = FirebaseService()
    //repo.cacheAsset("f14e0bd8-7edb-421f-8a17-554d81111f0d", AssetContainerProperties("data", "owner", "type", Instant.now(), 12.3F, 13.4F))
    //repo.cacheMove("f14e0bd8-7edb-421f-8a17-554d81111f0d", GpsProperties(7F, 77F))
    //repo.cacheMove("f14e0bd8-7edb-421f-8a17-554d81111f0d", GpsProperties(777F, 7777F))
}

@Service
class FirebaseService{

    // lazy init
    companion object {
        private var firebasDatabase: FirebaseDatabase? = null
    }

    fun cacheAsset(linearId: String, assetContainer: AssetContainerProperties, physicalContainerID: String){
        val assetToCache = AssetToCache(
                assetContainer.producerID,
                physicalContainerID,
                assetContainer.owner.toString(),
                assetContainer.type,
                assetContainer.dts)
        save(keysPath = "/asset/$linearId", value = assetToCache)
    }

    data class AssetToCache(val producerID: Int,
                            val physicalContainerID: String,
                            val owner: String,
                            val type: String,
                            val dts: Instant)

    fun cacheMove(linearId: String,
                  gps: GpsProperties,
                  physicalContainerID: String){
        save(keysPath = "/move/$linearId/${Instant.now().toEpochMilli()}", value = MoveToCache(gps, physicalContainerID))
    }

    data class MoveToCache(val gps: GpsProperties,
                           val physicalContainerID: String)


    private fun save(keysPath: String, value: Any){
        val firebaseDatabase = getFirebaseDb()!!

        /* Get database root reference */
        val ref = firebaseDatabase.getReference(keysPath)

        /* Get existing child or will be created new child. */

        //val assetReference = databaseReference.child(keysPath)
        //assetReference.setValueAsync(value);

        /**
         * The Firebase Java client uses daemon threads, meaning it will not prevent a process from exiting.
         * So we'll wait(countDownLatch.await()) until firebase saves record. Then decrement `countDownLatch` value
         * using `countDownLatch.countDown()` and application will continues its execution.
         */
        val countDownLatch = CountDownLatch(1)
        ref.setValue(value) { _, _ ->
            System.out.println("Record saved!");
            // decrement countDownLatch value and application will be continues its execution.
            countDownLatch.countDown();
        }

        countDownLatch.await()
    }

    /**
     * initialize firebase.
     */
    private fun getFirebaseDb(): FirebaseDatabase? {
        if (firebasDatabase == null) { // init firebase

            val serviceAccount = ClassPathResource("transactionscache-firebase-adminsdk-51vb7-987ea9e7b2.json").getInputStream()

            val options = FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://transactionscache.firebaseio.com")
                    .build()

            try {
                if(FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                }
            } catch (e: Exception){
                println("Error occurs when initializing firebase: ${e.message}")
            }


            firebasDatabase = FirebaseDatabase.getInstance()!!
            return firebasDatabase
        }
        return firebasDatabase
    }







}