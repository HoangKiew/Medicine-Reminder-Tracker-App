package com.example.medinotify.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.api.*
import kotlinx.coroutines.launch

class MedicineViewModel(
    private val repo: MedicineRepository = MedicineRepository()
) : ViewModel() {

    /* -----------------------------------------------------------
       1) LỊCH UỐNG THUỐC THEO NGÀY (HomeScreen)
       ----------------------------------------------------------- */

    var daySchedule by mutableStateOf(listOf<LogEntryDTO>())
        private set

    var loadingSchedule by mutableStateOf(false)
        private set

    fun loadScheduleByDate(userId: String = "U001", date: String) {
        viewModelScope.launch {
            loadingSchedule = true

            try {
                val res = repo.getScheduleByDate(userId, date)
                daySchedule = res.body()?.schedule ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                daySchedule = emptyList()
            }

            loadingSchedule = false
        }
    }

    /* ====================== ACTIONS ====================== */

    // ✔ ĐÁNH DẤU ĐÃ UỐNG
    fun markAsTaken(logId: String, date: String, userId: String = "U001") {
        viewModelScope.launch {
            try {
                val result = repo.markTaken(logId)
                if (result.body()?.success == true) {
                    loadScheduleByDate(userId, date)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ✔ LÁT NỮA (delay X phút)
    fun markLater(logId: String, minutes: Int, date: String, userId: String = "U001") {
        viewModelScope.launch {
            try {
                val result = repo.markLater(logId, minutes)
                if (result.body()?.success == true) {
                    loadScheduleByDate(userId, date)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ✔ BỎ QUA (Missed)
    fun markMissed(logId: String, date: String, userId: String = "U001") {
        viewModelScope.launch {
            try {
                val result = repo.markMissed(logId)
                if (result.body()?.success == true) {
                    loadScheduleByDate(userId, date)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    /* -----------------------------------------------------------
       2) MEDICINE CRUD
       ----------------------------------------------------------- */

    var medicines by mutableStateOf(listOf<MedicineDTO>())
        private set

    var loadingMedicines by mutableStateOf(false)
        private set

    fun loadMedicines(uid: String = "U001") {
        viewModelScope.launch {
            loadingMedicines = true
            try {
                val res = repo.getMedicines(uid)
                medicines = res.body()?.medicines ?: emptyList()
            } catch (_: Exception) {}
            loadingMedicines = false
        }
    }

    fun addMedicine(dto: MedicineDTO, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repo.addMedicine(dto)
                loadMedicines()
                onDone()
            } catch (_: Exception) {}
        }
    }

    fun deleteMedicine(id: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repo.deleteMedicine(id)
                loadMedicines()
                onDone()
            } catch (_: Exception) {}
        }
    }
}
