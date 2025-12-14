package com.example.medinotify.ui.screens.reminder

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.medinotify.data.repository.MedicineRepository
import com.example.medinotify.worker.MedicineReminderWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.concurrent.TimeUnit

class MedicineReminderViewModel(
    private val repository: MedicineRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val timeFormatter = DateTimeFormatter.ofPattern("H:mm").withResolverStyle(ResolverStyle.STRICT)

    fun markAsTaken(medicineId: String, timeString: String, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                val time = try { LocalTime.parse(timeString, timeFormatter) }
                catch (e: Exception) {
                    try { LocalTime.parse(timeString) }
                    catch (e2: Exception) { null } }
                if (time != null) {
                    repository.updateScheduleStatus(medicineId, time, true)
                    delay(300)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                onDone()
            }
        }
    }

    //10
    fun snoozeReminder(id: String, name: String, dosage: String, time: String) {
        val data = Data.Builder()
            .putString("MEDICINE_ID", id)
            .putString("MEDICINE_NAME", name)
            .putString("MEDICINE_DOSAGE", dosage)
            .putString("SCHEDULE_TIME", time)
            .build()

        // Tạo yêu cầu WorkManager chạy sau 10 phút
        val snoozeWork = OneTimeWorkRequestBuilder<MedicineReminderWorker>()
            .setInitialDelay(10, TimeUnit.MINUTES)
            .setInputData(data)
            .addTag("snooze_reminder")
            .build()

        workManager.enqueue(snoozeWork)
        Log.d("ReminderVM", "Đã đặt báo lại sau 10 phút cho thuốc: $name")
    }
}