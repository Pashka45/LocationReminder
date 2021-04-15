package com.udacity.project4.locationreminders.data.local

import android.provider.CalendarContract
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun getReminders_returnReminders() = mainCoroutineRule.runBlockingTest {
        val rem1 = ReminderDTO("Title1", "Description1", "location1", 5.1, 5.2)

        repository.saveReminder(rem1)

        val savedReminder = repository.getReminder(rem1.id) as Result.Success

        assertThat(rem1.description, Matchers.`is`(savedReminder.data.description))
        assertThat(rem1.id, Matchers.`is`(savedReminder.data.id))
        assertThat(rem1.title, Matchers.`is`(savedReminder.data.title))
        assertThat(rem1.location, Matchers.`is`(savedReminder.data.location))
        assertThat(rem1.longitude,
            Matchers.`is`(
                Matchers.closeTo(
                    savedReminder.data.longitude!! - .1,
                    savedReminder.data.longitude!! + .1
                )
            )
        )
        assertThat(rem1.latitude,
            Matchers.`is`(
                Matchers.closeTo(
                    savedReminder.data.latitude!! - .1,
                    savedReminder.data.latitude!! + .1
                )
            )
        )

        repository.deleteAllReminders()
    }

    @Test
    fun deleteAllReminders() = mainCoroutineRule.runBlockingTest {
        val reminder = ReminderDTO("Title3", "Description3", "location1", 5.1, 5.2)

        repository.saveReminder(reminder)

        val loaded = (repository.getReminders() as Result.Success).data.first()

        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.longitude, `is`(reminder.longitude))

        repository.deleteAllReminders()
        val loadedList = (repository.getReminders() as Result.Success).data

        Assert.assertTrue(loadedList.isEmpty())
    }

    @Test
    fun saveReminders_getById() = mainCoroutineRule.runBlockingTest {
        val reminder = ReminderDTO("Title3", "Description3", "location1", 5.1, 5.2)

        repository.saveReminder(reminder)

        val loaded = (repository.getReminder(reminder.id) as Result.Success).data

        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

}