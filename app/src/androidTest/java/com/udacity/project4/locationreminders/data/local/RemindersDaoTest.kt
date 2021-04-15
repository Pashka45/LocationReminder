package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminders_getById() = runBlockingTest {
        val reminder = ReminderDTO("Title3", "Description3", "location1", 5.1, 5.2)

        database.reminderDao().saveReminder(reminder)

        val loaded = database.reminderDao().getReminderById(reminder.id)

        assertThat(loaded?.description, `is`(reminder.description))
        assertThat(loaded?.title, `is`(reminder.title))
        assertThat(loaded?.latitude, `is`(reminder.latitude))
        assertThat(loaded?.location, `is`(reminder.location))
        assertThat(loaded?.longitude, `is`(reminder.longitude))
    }

    @Test
    fun getReminders_getById() = runBlockingTest {
        val reminder = ReminderDTO("Title3", "Description3", "location1", 5.1, 5.2)

        database.reminderDao().saveReminder(reminder)

        val loaded = database.reminderDao().getReminders().first()

        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.longitude, `is`(reminder.longitude))

        database.reminderDao().deleteAllReminders()
    }

    @Test
    fun deleteAllReminders() = runBlockingTest {
        val reminder = ReminderDTO("Title3", "Description3", "location1", 5.1, 5.2)

        database.reminderDao().saveReminder(reminder)

        val loaded = database.reminderDao().getReminders().first()

        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.longitude, `is`(reminder.longitude))

        database.reminderDao().deleteAllReminders()
        val loadedList = database.reminderDao().getReminders()

        assertTrue(loadedList.isEmpty())
    }
}