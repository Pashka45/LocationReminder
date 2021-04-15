package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
class SaveReminderViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Subject under test
    private lateinit var viewModel: SaveReminderViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var fakeSource: FakeDataSource

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupStatisticsViewModel() {
        // We initialise the repository with no tasks
        fakeSource = FakeDataSource()

        viewModel = SaveReminderViewModel(Application(), fakeSource)
    }

    @Test
    fun saveReminder_compareReminder() = runBlocking {
        val rem1 = ReminderDataItem("Title3", "Description3", "location1", 5.1, 5.2)
        viewModel.validateAndSaveReminder(rem1)

        val savedReminder = fakeSource.getReminder(rem1.id) as Result.Success

        assertThat(rem1.description, `is`(savedReminder.data.description))
        assertThat(rem1.id, `is`(savedReminder.data.id))
        assertThat(rem1.title, `is`(savedReminder.data.title))
        assertThat(rem1.location, `is`(savedReminder.data.location))
        assertThat(rem1.longitude, `is`(closeTo(savedReminder.data.longitude!! - .1, savedReminder.data.longitude!! + .1)))
        assertThat(rem1.latitude, `is`(closeTo(savedReminder.data.latitude!! - .1, savedReminder.data.latitude!! + .1)))

        fakeSource.deleteAllReminders()
    }

    @Test
    fun onClear_clearViewModelsFields () {
        viewModel.longitude.value = 5.1
        viewModel.latitude.value = 5.1

        viewModel.selectedPOI.value = PointOfInterest(
            LatLng(viewModel.latitude.value!!, viewModel.latitude.value!!),
            "SomePoiId",
            "SomePoiName"
        )

        viewModel.reminderDescription.value = "Description"
        viewModel.reminderTitle.value = "Title"

        viewModel.reminderSelectedLocationStr.value = "aaa"

        viewModel.onClear()

        assertThat(viewModel.latitude.getOrAwaitValue(), nullValue())
        assertThat(viewModel.longitude.getOrAwaitValue(), nullValue())
        assertThat(viewModel.selectedPOI.getOrAwaitValue(), nullValue())
        assertThat(viewModel.reminderDescription.getOrAwaitValue(), nullValue())
        assertThat(viewModel.reminderTitle.getOrAwaitValue(), nullValue())
        assertThat(viewModel.reminderSelectedLocationStr.getOrAwaitValue(), nullValue())
    }

    @Test
    fun validateEnteredData_checkError() {
        val rem1 = ReminderDataItem("Title", "Description", "location", 5.1, 5.2)
        rem1.title = ""

        var isValidated = viewModel.validateEnteredData(rem1)

        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
        assertThat(isValidated, `is`(false))

        rem1.title = "Title"
        rem1.location = ""

        isValidated = viewModel.validateEnteredData(rem1)

        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
        assertThat(isValidated, `is`(false))

        rem1.location = "location"

        isValidated = viewModel.validateEnteredData(rem1)

        assertThat(isValidated, `is`(true))
    }

    @Test
    fun setLocation_checkSaveLocationInViewModelField() {
        var latLng = LatLng(5.1, 5.2)
        val location = "location"

        viewModel.setLocation(latLng)
        assertThat(viewModel.latitude.getOrAwaitValue(), closeTo(latLng.latitude + .1, latLng.latitude - .1))
        assertThat(viewModel.longitude.getOrAwaitValue(), closeTo(latLng.longitude + .1, latLng.longitude - .1))
        assertThat(viewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(nullValue()))

        latLng = LatLng(6.1, 6.2)

        viewModel.setLocation(latLng, location)
        assertThat(viewModel.latitude.getOrAwaitValue(), closeTo(latLng.latitude + .1, latLng.latitude - .1))
        assertThat(viewModel.longitude.getOrAwaitValue(), closeTo(latLng.longitude + .1, latLng.longitude - .1))
        assertThat(viewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(location))

    }
}