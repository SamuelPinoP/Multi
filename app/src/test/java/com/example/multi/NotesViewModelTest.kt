package com.example.multi

import com.example.multi.testutil.FakeNotesRepository
import com.example.multi.ui.notes.NotesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private fun note(id: Long, header: String = "n$id") =
        Note(id = id, header = header, content = "body $id", created = id, lastOpened = id)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_startsLoading_thenEmitsNotes() = runTest(dispatcher) {
        val repo = FakeNotesRepository(listOf(note(1), note(2)))
        val vm = NotesViewModel(repo)

        assertTrue(vm.uiState.value.isLoading)

        vm.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
        assertEquals(listOf(1L, 2L), vm.uiState.value.notes.map { it.id })
    }

    @Test
    fun moveToTrash_removesSelectedNotes_andTrashesThem() = runTest(dispatcher) {
        val repo = FakeNotesRepository(listOf(note(1), note(2), note(3)))
        val vm = NotesViewModel(repo)
        vm.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        vm.moveToTrash(setOf(1L, 3L))
        advanceUntilIdle()

        assertEquals(listOf(2L), vm.uiState.value.notes.map { it.id })
        assertEquals(listOf(1L, 3L), repo.trashed.map { it.id }.sorted())
    }

    @Test
    fun moveToTrash_withEmptySelection_isNoOp() = runTest(dispatcher) {
        val repo = FakeNotesRepository(listOf(note(1)))
        val vm = NotesViewModel(repo)
        vm.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        vm.moveToTrash(emptySet())
        advanceUntilIdle()

        assertEquals(listOf(1L), vm.uiState.value.notes.map { it.id })
        assertTrue(repo.trashed.isEmpty())
    }

    @Test
    fun importNote_addsNoteWithAttachment() = runTest(dispatcher) {
        val repo = FakeNotesRepository()
        val vm = NotesViewModel(repo)
        vm.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        vm.importNote("report.pdf", "content://docs/1")
        advanceUntilIdle()

        val notes = vm.uiState.value.notes
        assertEquals(1, notes.size)
        assertEquals("report.pdf", notes.first().header)
        assertEquals("content://docs/1", notes.first().attachmentUri)
    }

    @Test
    fun init_purgesExpiredTrashOnce() = runTest(dispatcher) {
        val repo = FakeNotesRepository()
        NotesViewModel(repo)
        advanceUntilIdle()

        assertEquals(1, repo.purgeCallCount)
    }

    @Test
    fun observeFailure_surfacesErrorMessage_andKeepsListEmpty() = runTest(dispatcher) {
        val repo = FakeNotesRepository().apply { failObserve = true }
        val vm = NotesViewModel(repo)
        vm.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.errorMessage)
        assertTrue(vm.uiState.value.notes.isEmpty())
        assertFalse(vm.uiState.value.isLoading)
    }
}
