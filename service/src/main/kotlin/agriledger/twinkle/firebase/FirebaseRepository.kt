package agriledger.twinkle.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import java.io.FileInputStream
import java.time.Instant
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

fun main(args : Array<String>) {
    val repo = FirebaseRepository()
    repo.cacheAsset("f14e0bd8-7edb-421f-8a17-554d81111f0d", "data", "owner", "type", Instant.now(), 12.3F, 13.4F)
    repo.cacheMove("f14e0bd8-7edb-421f-8a17-554d81111f0d", 13.3F, 77F)
    repo.cacheMove("f14e0bd8-7edb-421f-8a17-554d81111f0d", 777F, 7778F)
}

class FirebaseRepository{

    // lazy init
    companion object {
        private var firebasDatabase: FirebaseDatabase? = null
    }

    fun cacheAsset(linearId: String,
                  data: String,
                  owner: String,
                  type: String,
                  dts: Instant,
                  latitude: Float,
                   longitude: Float){
        val asset = Asset(data, owner, type, dts)
        save(keysPath = "/asset/$linearId", value = asset)
        cacheMove(linearId, latitude, longitude)
    }

    fun cacheMove(linearId: String,
                  latitude: Float,
                  longitude: Float){
        val gps = Gps(longitude, latitude)
        save(keysPath = "/move/$linearId/${Instant.now().toEpochMilli()}", value = gps)
    }


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
            val serviceAccount = FileInputStream("/Users/antongermasev/IdeaProjects/" +
                    "twinkle/agriledger-893ec-firebase-adminsdk-p44mc-a2be295afe.json")

            val options = FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://agriledger-893ec.firebaseio.com/")
                    .build()

            FirebaseApp.initializeApp(options)
            firebasDatabase = FirebaseDatabase.getInstance()!!
            return firebasDatabase
        }
        return firebasDatabase
    }

    data class Asset(val data: String,
                     val owner: String,
                     val type: String,
                     val dts: Instant)

    data class Gps(val longitude: Float,
                   val latitude: Float)






}