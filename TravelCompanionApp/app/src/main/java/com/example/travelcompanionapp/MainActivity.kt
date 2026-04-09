package com.example.travelcompanionapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TravelCompanionApp()
            }
        }
    }
}

@Composable
fun TravelCompanionApp() {
    val context = LocalContext.current

    val categories = listOf("Currency", "Fuel / Distance / Liquid", "Temperature")
    val currencyUnits = listOf("USD", "AUD", "EUR", "JPY", "GBP")
    val fuelUnits = listOf("mpg", "km/L", "Gallon (US)", "Liters", "Nautical Mile", "Kilometers")
    val temperatureUnits = listOf("Celsius", "Fahrenheit", "Kelvin")

    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var selectedFromUnit by remember { mutableStateOf(currencyUnits[0]) }
    var selectedToUnit by remember { mutableStateOf(currencyUnits[1]) }
    var inputValue by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("Your converted result will appear here") }

    val currentUnits = when (selectedCategory) {
        "Currency" -> currencyUnits
        "Fuel / Distance / Liquid" -> fuelUnits
        "Temperature" -> temperatureUnits
        else -> currencyUnits
    }

    LaunchedEffect(selectedCategory) {
        selectedFromUnit = currentUnits.first()
        selectedToUnit = if (currentUnits.size > 1) currentUnits[1] else currentUnits.first()
        inputValue = ""
        resultText = "Your converted result will appear here"
    }

    val primaryColor = when (selectedCategory) {
        "Currency" -> Color(0xFF7B1FA2)
        "Fuel / Distance / Liquid" -> Color(0xFF2E7D32)
        "Temperature" -> Color(0xFFC62828)
        else -> Color(0xFF7B1FA2)
    }

    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF8F5FF),
            Color(0xFFF4FAFF),
            Color(0xFFFFFBF7)
        )
    )

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(
                        bottomStart = 30.dp,
                        topEnd = 30.dp,
                        bottomEnd = 12.dp,
                        topStart = 12.dp
                    ),
                    colors = CardDefaults.cardColors(containerColor = primaryColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Traveller Unit Converter",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Fast and simple conversions for travel essentials",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        SmallTitle("Category")
                        FancyDropdown(
                            options = categories,
                            selectedOption = selectedCategory,
                            onOptionSelected = { selectedCategory = it },
                            primaryColor = primaryColor
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SmallTitle("Source Unit")
                        FancyDropdown(
                            options = currentUnits,
                            selectedOption = selectedFromUnit,
                            onOptionSelected = { selectedFromUnit = it },
                            primaryColor = primaryColor
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SmallTitle("Destination Unit")
                        FancyDropdown(
                            options = currentUnits,
                            selectedOption = selectedToUnit,
                            onOptionSelected = { selectedToUnit = it },
                            primaryColor = primaryColor
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SmallTitle("Value")
                        OutlinedTextField(
                            value = inputValue,
                            onValueChange = { inputValue = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Enter number") },
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color(0xFFD0D0D0),
                                cursorColor = primaryColor,
                                focusedLabelColor = primaryColor,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val trimmedInput = inputValue.trim()

                                if (trimmedInput.isEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "Please enter a value",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    resultText = "Error: input is empty"
                                    return@Button
                                }

                                val numericValue = trimmedInput.toDoubleOrNull()
                                if (numericValue == null) {
                                    Toast.makeText(
                                        context,
                                        "Please enter a valid number",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    resultText = "Error: invalid numeric input"
                                    return@Button
                                }

                                if (selectedFromUnit == selectedToUnit) {
                                    Toast.makeText(
                                        context,
                                        "Both units are the same",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    resultText = "Converted Value: %.2f %s".format(
                                        numericValue,
                                        selectedToUnit
                                    )
                                    return@Button
                                }

                                if ((selectedCategory == "Currency" || selectedCategory == "Fuel / Distance / Liquid") && numericValue < 0) {
                                    Toast.makeText(
                                        context,
                                        "Negative values are not allowed here",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    resultText =
                                        "Error: negative values are not valid for $selectedCategory"
                                    return@Button
                                }

                                val result = when (selectedCategory) {
                                    "Currency" -> convertCurrency(
                                        selectedFromUnit,
                                        selectedToUnit,
                                        numericValue
                                    )

                                    "Fuel / Distance / Liquid" -> convertFuelDistanceLiquid(
                                        selectedFromUnit,
                                        selectedToUnit,
                                        numericValue
                                    )

                                    "Temperature" -> convertTemperature(
                                        selectedFromUnit,
                                        selectedToUnit,
                                        numericValue
                                    )

                                    else -> null
                                }

                                if (result == null) {
                                    Toast.makeText(
                                        context,
                                        "Conversion not supported",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    resultText = "Error: unsupported conversion"
                                } else {
                                    resultText =
                                        "Converted Value: %.2f %s".format(result, selectedToUnit)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Text(
                                text = "Start Conversion",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(25.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(primaryColor.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {

                    Text(
                        text = "Conversion Result",
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = resultText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val isError = resultText.contains("Error")

                    Text(
                        text = if (isError) "⚠️ Check input" else "✅ Success",
                        color = if (isError) Color.Red else Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

@Composable
fun SmallTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = Color(0xFF4A4A4A),
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FancyDropdown(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    primaryColor: Color
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color(0xFFD0D0D0),
                cursorColor = primaryColor,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onOptionSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun convertCurrency(from: String, to: String, value: Double): Double {
    val usdValue = when (from) {
        "USD" -> value
        "AUD" -> value / 1.55
        "EUR" -> value / 0.92
        "JPY" -> value / 148.50
        "GBP" -> value / 0.78
        else -> value
    }

    return when (to) {
        "USD" -> usdValue
        "AUD" -> usdValue * 1.55
        "EUR" -> usdValue * 0.92
        "JPY" -> usdValue * 148.50
        "GBP" -> usdValue * 0.78
        else -> usdValue
    }
}

fun convertFuelDistanceLiquid(from: String, to: String, value: Double): Double? {
    return when {
        from == "mpg" && to == "km/L" -> value * 0.425
        from == "km/L" && to == "mpg" -> value / 0.425
        from == "Gallon (US)" && to == "Liters" -> value * 3.785
        from == "Liters" && to == "Gallon (US)" -> value / 3.785
        from == "Nautical Mile" && to == "Kilometers" -> value * 1.852
        from == "Kilometers" && to == "Nautical Mile" -> value / 1.852
        else -> null
    }
}

fun convertTemperature(from: String, to: String, value: Double): Double? {
    return when {
        from == "Celsius" && to == "Fahrenheit" -> (value * 1.8) + 32
        from == "Fahrenheit" && to == "Celsius" -> (value - 32) / 1.8
        from == "Celsius" && to == "Kelvin" -> value + 273.15
        from == "Kelvin" && to == "Celsius" -> value - 273.15
        from == "Fahrenheit" && to == "Kelvin" -> ((value - 32) / 1.8) + 273.15
        from == "Kelvin" && to == "Fahrenheit" -> ((value - 273.15) * 1.8) + 32
        else -> null
    }
}