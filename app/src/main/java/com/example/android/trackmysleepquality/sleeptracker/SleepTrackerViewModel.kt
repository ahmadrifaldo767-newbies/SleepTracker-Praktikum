package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.launch

class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application
) : AndroidViewModel(application) {

        private var tonight = MutableLiveData<SleepNight?>()
        private val nights = database.getAllNights()

        val nightsString = nights.map { nights ->
                formatNights(nights, application.resources)
        }

        // ------
        val startButtonVisible = tonight.map { it == null }
        val stopButtonVisible = tonight.map { it != null }
        val clearButtonVisible = nights.map { it.isNotEmpty() }

        private val _showSnackbarEvent = MutableLiveData<Boolean>()
        val showSnackBarEvent: LiveData<Boolean>
                get() = _showSnackbarEvent

        private val _navigateToSleepQuality = MutableLiveData<SleepNight?>()
        val navigateToSleepQuality: LiveData<SleepNight?>
                get() = _navigateToSleepQuality

        fun doneShowingSnackbar() {
                _showSnackbarEvent.value = false
        }

        fun doneNavigating() {
                _navigateToSleepQuality.value = null
        }
        // ----------------------

        init {
                initializeTonight()
        }

        private fun initializeTonight() {
                viewModelScope.launch {
                        tonight.value = getTonightFromDatabase()
                }
        }

        private suspend fun getTonightFromDatabase(): SleepNight? {
                var night = database.getTonight()
                if (night?.endTimeMilli != night?.startTimeMilli) {
                        night = null
                }
                return night
        }

        fun onStartTracking() {
                viewModelScope.launch {
                        database.insert(SleepNight())
                        tonight.value = getTonightFromDatabase()
                }
        }

        fun onStopTracking() {
                viewModelScope.launch {
                        val oldNight = tonight.value ?: return@launch
                        oldNight.endTimeMilli = System.currentTimeMillis()
                        database.update(oldNight)

                        _navigateToSleepQuality.value = oldNight

                        // Reset tonight ke null agar data langsung tersimpan & UI ter-refresh seketika
                        tonight.value = null
                }
        }

        fun onClear() {
                viewModelScope.launch {
                        database.clear()
                        tonight.value = null

                        // Tambahan C1
                        _showSnackbarEvent.value = true
                }
        }
}