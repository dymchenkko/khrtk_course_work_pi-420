import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.database.FirebaseDatabase
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
class FirebaseReadWriteTest {

    @Test
    fun writeAndReadDataFromFirebase() {
        val writeSignal = CountDownLatch(1)

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("test_data")

        myRef.setValue("&*&E#(@EH#*#(#33")
            .addOnCompleteListener {
                writeSignal.countDown()
                }

        myRef.get().addOnSuccessListener {
            Assert.assertEquals(it.value.toString(), "&*&E#(@EH#*#(#33")
            }

        writeSignal.await(10, TimeUnit.SECONDS)
    }
}
