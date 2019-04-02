package agriledger.twinkle.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.FirebaseDatabase
import java.io.FileInputStream
import java.time.Instant
import java.util.concurrent.CountDownLatch

fun main(args : Array<String>) {
    //FirebaseRepository().save()
}

class FirebaseRepository(val linearId: String,
                         val data: String,
                         val owner: String,
                         val type: String,
                         val dts: Instant){

    // lazy init
    companion object {
        private var firebasDatabase: FirebaseDatabase? = null
    }


    fun save(){
        val firebaseDatabase = getFirebaseDb()!!

        /* Get database root reference */
        val databaseReference = firebaseDatabase.getReference("/")

        /* Get existing child or will be created new child. */
        val assetReference = databaseReference.child("asset")

        assetReference.child(linearId).setValueAsync(Asset(data, owner, type, dts));

        /**
         * The Firebase Java client uses daemon threads, meaning it will not prevent a process from exiting.
         * So we'll wait(countDownLatch.await()) until firebase saves record. Then decrement `countDownLatch` value
         * using `countDownLatch.countDown()` and application will continues its execution.
         */
        /*val countDownLatch = CountDownLatch(1)
        childReference.setValue("4") { _, _ ->
            System.out.println("Record saved!");
            // decrement countDownLatch value and application will be continues its execution.
            countDownLatch.countDown();
        }

        countDownLatch.await()*/
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





}