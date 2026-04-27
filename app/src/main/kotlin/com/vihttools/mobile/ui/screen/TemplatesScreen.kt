package com.vihttools.mobile.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vihttools.mobile.data.Template

@Composable
fun TemplatesScreen(
    templates: List<Template>,
    onAddTemplate: (Template) -> Unit,
    onDeleteTemplate: (Template) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newLabel by remember { mutableStateOf("") }
    var newCommand by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2D2D2D))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "← Templates",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                modifier = Modifier.clickable { onBack() }
            )
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE63946)
                )
            ) {
                Text("Add", color = Color.White)
            }
        }

        // Templates List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            items(templates) { template ->
                TemplateCard(
                    template = template,
                    onDelete = { onDeleteTemplate(template) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Add Template Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Template") },
                text = {
                    Column {
                        TextField(
                            value = newLabel,
                            onValueChange = { newLabel = it },
                            label = { Text("Label (e.g., Warn)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        TextField(
                            value = newCommand,
                            onValueChange = { newCommand = it },
                            label = { Text("Command (use {ID} for player ID)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newLabel.isNotEmpty() && newCommand.isNotEmpty()) {
                                onAddTemplate(
                                    Template(
                                        label = newLabel,
                                        command = newCommand
                                    )
                                )
                                newLabel = ""
                                newCommand = ""
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    Button(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun TemplateCard(
    template: Template,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
                Text(
                    text = template.command,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF457B9D),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Button(
                onClick = onDelete,
                modifier = Modifier
                    .size(40.dp)
                    .padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF606060)
                )
            ) {
                Text("✕", color = Color.White, fontSize = MaterialTheme.typography.bodySmall.fontSize)
            }
        }
    }
}

@Composable
fun Clickable(modifier: Modifier = Modifier, onClick: () -> Unit) {
    // Placeholder for clickable modifier
}
