import org.junit.Assert.assertEquals
import org.junit.Test

class OverageCalculationTest {
    private fun calc(completed: Int, target: Int): Int {
        return (completed - target).coerceAtLeast(0).coerceAtMost(20)
    }

    @Test
    fun overageExamples() {
        assertEquals(1, calc(4, 3))
        assertEquals(20, calc(24, 3))
        assertEquals(0, calc(2, 3))
    }
}
