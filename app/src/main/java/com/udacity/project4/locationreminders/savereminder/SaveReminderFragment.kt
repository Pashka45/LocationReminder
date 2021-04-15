package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.RemindersActivity.Companion.ACTION_GEOFENCE_EVENT
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.GeofenceConstants
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject


private const val TAG = "SaveReminderFragment"


class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private lateinit var flc: FusedLocationProviderClient

    private lateinit var geofencingClient: GeofencingClient

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        Log.i(TAG, "onCreateView " + _viewModel.reminderTitle.value.toString())
        if (_viewModel.reminderTitle.value != null) {
            binding.reminderTitle.setText(_viewModel.reminderTitle.value)
        }

        if (_viewModel.reminderDescription.value != null) {
            binding.reminderTitle.setText(_viewModel.reminderTitle.value)
        }
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel
        flc = activity?.let { LocationServices.getFusedLocationProviderClient(it) }!!

        //binding.

        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location

                flc.lastLocation.addOnSuccessListener { location ->
                    if (location == null) {
                        _viewModel.clientLat.value = 50.0
                        _viewModel.clientLong.value = 49.0
                    } else {
                        _viewModel.clientLat.value = location.latitude
                        _viewModel.clientLong.value = location.longitude
                    }
                }

                _viewModel.navigationCommand.value =
                    NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val reminderDataItem = ReminderDataItem(
                _viewModel.reminderTitle.value,
                _viewModel.reminderDescription.value,
                _viewModel.reminderSelectedLocationStr.value,
                _viewModel.latitude.value, _viewModel.longitude.value
            )

            Log.i(
                "saveLocation",
                reminderDataItem.toString()
            )
            _viewModel.validateAndSaveReminder(reminderDataItem)
            if (_viewModel.validateEnteredData(reminderDataItem)) {
                addGeofence(reminderDataItem)
                _viewModel.onClear()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(reminderDataItem: ReminderDataItem) {
        val geofence = Geofence.Builder()
            .setRequestId(reminderDataItem.id)
            .setCircularRegion(reminderDataItem.latitude!!,
                reminderDataItem.longitude!!,
                GeofenceConstants.GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT

        val geofencePendingIntent = PendingIntent.getBroadcast(requireActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnCompleteListener {
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                    addOnSuccessListener {
                        Log.i("Add Geofence", geofence.requestId)
                    }
                    addOnFailureListener {
                        if (it.message != null) {
                            Log.w(TAG, it.message!!)
                        }
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i("onOptionsItemSelected", item.itemId.toString() + " " + R.id.homeAsUp)
        return when (item.itemId) {
            R.id.homeAsUp -> {
                activity?.onBackPressed()
                Log.i(TAG, "clicked")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
