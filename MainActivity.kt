package com.example.eventplannerapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: EventViewModel by viewModels {
        EventViewModelFactory(
            EventRepository(
                AppDatabase.getDatabase(applicationContext).eventDao()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EventPlannerApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val category: String,
    val location: String,
    val dateTimeMillis: Long
)

@Dao
interface EventDao {
    @Insert
    suspend fun insertEvent(event: EventEntity)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("SELECT * FROM events ORDER BY dateTimeMillis ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: Int): EventEntity?
}

@Database(entities = [EventEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "event_planner_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class EventRepository(private val eventDao: EventDao) {
    fun getAllEvents(): Flow<List<EventEntity>> = eventDao.getAllEvents()

    suspend fun getEventById(id: Int): EventEntity? = eventDao.getEventById(id)

    suspend fun insertEvent(event: EventEntity) = eventDao.insertEvent(event)

    suspend fun updateEvent(event: EventEntity) = eventDao.updateEvent(event)

    suspend fun deleteEvent(event: EventEntity) = eventDao.deleteEvent(event)
}

class EventViewModel(private val repository: EventRepository) : ViewModel() {
    val allEvents = repository.getAllEvents()

    private val _selectedEvent = MutableStateFlow<EventEntity?>(null)
    val selectedEvent: StateFlow<EventEntity?> = _selectedEvent.asStateFlow()

    fun loadEventById(id: Int) {
        viewModelScope.launch {
            _selectedEvent.value = repository.getEventById(id)
        }
    }

    fun clearSelectedEvent() {
        _selectedEvent.value = null
    }

    fun insertEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.insertEvent(event)
        }
    }

    fun updateEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.updateEvent(event)
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }
}

class EventViewModelFactory(
    private val repository: EventRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            return EventViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class Screen(val route: String) {
    data object EventList : Screen("event_list")
    data object AddEvent : Screen("add_event")
    data object EditEvent : Screen("edit_event/{eventId}") {
        fun createRoute(eventId: Int): String = "edit_event/$eventId"
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun EventPlannerApp(viewModel: EventViewModel) {
    val navController = rememberNavController()
    val events by viewModel.allEvents.collectAsState(initial = emptyList())

    val bottomItems = listOf(
        BottomNavItem("Events", Screen.EventList.route, Icons.Default.List),
        BottomNavItem("Add Event", Screen.AddEvent.route, Icons.Default.Add)
    )

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        bottomBar = {
            NavigationBar {
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                bottomItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.EventList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.EventList.route) {
                EventListScreen(
                    events = events,
                    navController = navController,
                    onDelete = { event ->
                        viewModel.deleteEvent(event)
                    }
                )
            }

            composable(Screen.AddEvent.route) {
                AddEditEventScreen(
                    navController = navController,
                    viewModel = viewModel,
                    eventId = null
                )
            }

            composable(
                route = "edit_event/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.IntType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getInt("eventId")
                AddEditEventScreen(
                    navController = navController,
                    viewModel = viewModel,
                    eventId = eventId
                )
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    events: List<EventEntity>,
    navController: NavController,
    onDelete: (EventEntity) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Upcoming Events") })
        }
    ) { padding ->
        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No events added yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(events) { event ->
                    EventCard(
                        event = event,
                        onEdit = {
                            navController.navigate(Screen.EditEvent.createRoute(event.id))
                        },
                        onDelete = {
                            onDelete(event)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: EventEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text("Category: ${event.category}")
            Text("Location: ${event.location}")
            Text("Date & Time: ${formatter.format(Date(event.dateTimeMillis))}")

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventScreen(
    navController: NavController,
    viewModel: EventViewModel,
    eventId: Int?
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val existingEvent by viewModel.selectedEvent.collectAsState()

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Work") }
    var location by remember { mutableStateOf("") }
    var selectedDateTimeMillis by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val categories = listOf("Work", "Social", "Travel", "Study", "Personal")

    LaunchedEffect(eventId) {
        if (eventId != null) {
            viewModel.loadEventById(eventId)
        } else {
            viewModel.clearSelectedEvent()
        }
    }

    LaunchedEffect(existingEvent) {
        existingEvent?.let { event ->
            title = event.title
            category = event.category
            location = event.location
            selectedDateTimeMillis = event.dateTimeMillis
        }
    }

    fun openDateTimePicker() {
        val now = Calendar.getInstance()

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val pickedDateTime = Calendar.getInstance().apply {
                            set(year, month, dayOfMonth, hourOfDay, minute, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        selectedDateTimeMillis = pickedDateTime.timeInMillis
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    false
                ).show()
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun validateAndSave() {
        val now = System.currentTimeMillis()

        when {
            title.isBlank() -> {
                Toast.makeText(context, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            }

            selectedDateTimeMillis == null -> {
                Toast.makeText(context, "Please select date and time", Toast.LENGTH_SHORT).show()
            }

            selectedDateTimeMillis!! < now -> {
                Toast.makeText(context, "Past date/time is not allowed", Toast.LENGTH_SHORT).show()
            }

            else -> {
                val event = EventEntity(
                    id = eventId ?: 0,
                    title = title.trim(),
                    category = category,
                    location = location.trim(),
                    dateTimeMillis = selectedDateTimeMillis!!
                )

                if (eventId == null) {
                    viewModel.insertEvent(event)
                    Toast.makeText(context, "Event added successfully", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.updateEvent(event)
                    Toast.makeText(context, "Event updated successfully", Toast.LENGTH_SHORT).show()
                }

                viewModel.clearSelectedEvent()
                navController.navigate(Screen.EventList.route) {
                    popUpTo(Screen.EventList.route) { inclusive = true }
                }
            }
        }
    }

    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (eventId == null) "Add Event" else "Edit Event")
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth()
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { expanded = true },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                ) {
                    Text("Choose")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                category = item
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { openDateTimePicker() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pick Date & Time")
            }

            Text(
                text = if (selectedDateTimeMillis != null) {
                    "Selected: ${formatter.format(Date(selectedDateTimeMillis!!))}"
                } else {
                    "No date/time selected"
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { validateAndSave() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (eventId == null) "Save Event" else "Update Event")
            }
        }
    }
}