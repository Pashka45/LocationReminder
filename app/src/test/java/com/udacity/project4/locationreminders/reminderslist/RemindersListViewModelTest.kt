package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.hamcrest.MatcherAssert.assertThat

@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Subject under test
    private lateinit var viewModel: RemindersListViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var fakeSource: FakeDataSource

    private val rem1 = ReminderDTO("Title3", "Description3", "location1", 5.1, 5.2)

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupStatisticsViewModel() {
        // We initialise the repository with no tasks
        fakeSource = FakeDataSource()

        viewModel = RemindersListViewModel(Application(), fakeSource)
    }

    @Test
    fun loadReminders_returnOneReminder() = runBlocking {

        fakeSource.saveReminder(rem1)

        viewModel.loadReminders()
        val reminder: ReminderDataItem = viewModel.remindersList.value?.first()!!

        assertThat(reminder.description, `is`("Description3"))
        assertThat(reminder.title, `is`("Title3"))
        assertThat(reminder.location, `is`("location1"))
        assertThat(reminder.longitude, `is`(5.2))
        assertThat(reminder.latitude, `is`(5.1))

        fakeSource.deleteAllReminders()
    }
}