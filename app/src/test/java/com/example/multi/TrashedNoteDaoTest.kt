import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.example.multi.data.EventDatabase
import com.example.multi.data.TrashedNoteEntity

class TrashedNoteDaoTest {
    private lateinit var context: Context
    private lateinit var db: EventDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, EventDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun deleteAllClearsNotes() = runBlocking {
        val dao = db.trashedNoteDao()
        dao.insert(TrashedNoteEntity(header = "h", content = "c", created = 0L, deleted = 0L))
        dao.deleteAll()
        assertTrue(dao.getNotes().isEmpty())
    }
}
