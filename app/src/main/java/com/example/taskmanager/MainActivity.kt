package com.example.taskmanager

import android.media.tv.interactive.TvInteractiveAppServiceInfo
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

data class Task(
    val id: Int,
    val name: String,
    val category: String,
    val priority: Int,
    val done: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                TaskManagerScreen()
            }
        }
    }
}

@Composable
fun TaskManagerScreen() {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }
    var priorityError by remember { mutableStateOf(false) }

    var nextID by remember { mutableIntStateOf(1) }

    val tasks = remember { mutableStateListOf<Task>() }
    var search by remember { mutableStateOf("") }
    val filteredTasks = tasks.filter { game ->
        game.name.contains(search.trim(), ignoreCase = true) ||
        game.category.contains(search.trim(), ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "TaskManager",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Szervezd meg a napi teendőidet!",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                nameError = false
            },
            label = { Text("Feladat neve") },
            isError = nameError,
            supportingText = {
                if (nameError) Text("A név megadása kötelező!")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = category,
            onValueChange = {
                category = it
                categoryError = false
            },
            label = { Text("Kategória") },
            isError = categoryError,
            supportingText = {
                if (categoryError) Text("A kategória megadása kötelező!")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = priority,
            onValueChange = {
                priority = it
                priorityError = false
            },
            label = { Text("editPriority") },
            isError = priorityError,
            supportingText = {
                if (priorityError) Text("A prioritás megadása kötelező!")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                nameError = name.isBlank()
                categoryError = category.isBlank()
                val parsedPriority = priority.toIntOrNull()
                priorityError = parsedPriority == null || parsedPriority !in 1..3

                if (nameError || categoryError || priorityError) {
                    Toast.makeText(
                        context,
                        "Ellenőrizd a megadott adatokat!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    tasks.add(
                        Task(
                            id = nextID++,
                            name = name.trim(),
                            category = category.trim(),
                            priority = parsedPriority!!,
                            done = false
                        )
                    )
                    name = ""
                    category = ""
                    priority = ""

                    Toast.makeText(
                        context,
                        "Feladat sikeresen hozzáadva!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ){
            Text("Feladat hozzáadása")
        }

        HorizontalDivider()

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("Keresés név vagy kategória alapján") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        HorizontalDivider()

        when{
            tasks.isEmpty() -> {
                Text(
                    text = "Még nincs feladat hozzáadva.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            filteredTasks.isEmpty() -> {
                Text(
                    text = "Nincs találat a keresésnek megfelelően.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = filteredTasks,
                        key = { it.id }
                    ){ task ->
                        TaskCard(
                            task,
                            onDelete = { tasks.removeAll { it.id == task.id } },
                            onEdit = { editedTask ->
                                val index = tasks.indexOfFirst { it.id == editedTask.id }
                                if (index != -1) tasks[index] = editedTask

                                Toast.makeText(
                                    context,
                                    "Feladat sikeresen módosítva!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onDelete: () -> Unit,
    onEdit: (Task) -> Unit
){
    val context = LocalContext.current

    var editName by remember(task.id, task.name) { mutableStateOf(task.name) }
    var editCategory by remember(task.id, task.category) { mutableStateOf(task.category) }
    var editPriority by remember(task.id, task.priority) { mutableStateOf(task.priority.toString()) }

    var editNameError by remember { mutableStateOf(false) }
    var editCategoryError by remember { mutableStateOf(false) }
    var editPriorityError by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TaskAvatar( task.priority )
            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(text = task.category, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = when (task.priority) {
                        1 -> "Alacsony prioritás"
                        2 -> "Közepes prioritás"
                        else -> "Magas prioritás"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = when (task.done) {
                        true -> "Kész"
                        false -> "Nincs kész"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = {onEdit(Task(
                        id = task.id,
                        name = task.name,
                        category = task.category,
                        priority = task.priority,
                        done = !task.done
                    ))},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (task.done) {
                            true -> "Újra aktív"
                            false -> "Elkészült"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

        }
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    editName = task.name
                    editCategory = task.category
                    editPriority = task.priority.toString()
                    editNameError = false;
                    editCategoryError = false;
                    editPriorityError = false;
                    showEditDialog = true
                }
            ) {
               Icon (
                   painter = painterResource(id = R.drawable.edit),
                   contentDescription = "Szerkesztés"
               )
            }

            IconButton(
                onClick = {showDeleteDialog = true},
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.delete),
                    contentDescription = "Törlés"
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Feladat törlése") },
            text = { Text("Biztosan törölni szeretnéd a ${task.name} Feldatot?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Törlés")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Mégse")
                }
            }
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Feladat szerkesztése") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = {
                            editName = it
                            editNameError = false
                        },
                        label = { Text("Feladat címe") },
                        isError = editNameError,
                        supportingText = {
                            if (editNameError) Text("A név megadása kötelező!")
                        },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editCategory,
                        onValueChange = {
                            editCategory = it
                            editCategoryError = false
                        },
                        label = { Text("Platform") },
                        isError = editCategoryError,
                        supportingText = {
                            if (editCategoryError) Text("A kategória megadása kötelező!")
                        },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editPriority,
                        onValueChange = {
                            editPriority = it
                            editPriorityError = false
                        },
                        label = { Text("Prioritás (1–3)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = editPriorityError,
                        supportingText = {
                            if (editPriorityError) {
                                Text("Az Prioritás 1 és 3 között lehet!")
                            }
                        },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        editNameError = editName.isBlank()
                        editCategoryError = editCategory.isBlank()
                        val parsedPriority = editPriority.toIntOrNull()
                        editPriorityError = parsedPriority == null || parsedPriority !in 1..3

                        if (editNameError || editCategoryError || editPriorityError) {
                            Toast.makeText(
                                context,
                                "Ellenőrizd a megadott adatokat!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            onEdit(
                                Task(
                                    id = task.id,
                                    name = editName.trim(),
                                    category = editCategory.trim(),
                                    priority = parsedPriority!!,
                                    done = task.done
                                )
                            )
                            showEditDialog = false
                        }
                    }
                ) {
                    Text("Mentés")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Mégse")
                }
            }
        )
    }
}

@Composable
fun TaskAvatar(priority: Int) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = priority.toString(),
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}