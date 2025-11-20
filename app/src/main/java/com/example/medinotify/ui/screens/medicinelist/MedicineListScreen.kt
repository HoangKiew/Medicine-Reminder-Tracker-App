package com.example.medinotify.ui.screens.medicinelist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.medinotify.viewmodel.MedicineViewModel
import androidx.compose.ui.unit.dp

@Composable
fun MedicineListScreen(
    navController: NavController,
    vm: MedicineViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    modifier: Modifier = Modifier
) {

    // ⭐ LẦN ĐẦU MỞ MÀN → LOAD DANH SÁCH THUỐC
    LaunchedEffect(true) {
        vm.loadMedicines("U001")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text("Danh sách thuốc", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        // Vì ViewModel KHÔNG CÓ loading → bỏ đoạn loading kia

        LazyColumn {
            items(vm.medicines) { m ->

                val id = m.medicineId ?: ""

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {

                        Text(
                            m.name ?: "Không tên",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text("Loại: ${m.medicineType ?: "-"}")
                        Text("Liều lượng: ${m.dosage ?: "-"}")
                        Text("Lần uống/ngày: ${m.timesPerDay ?: "-"}")

                        Spacer(Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                if (id.isNotEmpty()) {
                                    vm.deleteMedicine(id) {
                                        vm.loadMedicines()    // ⭐ Reload sau khi xóa
                                    }
                                }
                            }
                        ) {
                            Text("Xoá")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("add") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Thêm thuốc mới")
        }
    }
}
