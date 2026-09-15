package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.supabase.SupabaseSyncState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SupabaseSyncCard(
    syncState: SupabaseSyncState,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSetupDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .testTag("supabase_sync_card"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                when (syncState) {
                                    is SupabaseSyncState.Synced -> Color(0xFF10B981).copy(alpha = 0.15f)
                                    is SupabaseSyncState.Syncing -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    is SupabaseSyncState.Error -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (syncState) {
                                is SupabaseSyncState.Synced -> Icons.Default.CloudDone
                                is SupabaseSyncState.Syncing -> Icons.Default.CloudSync
                                is SupabaseSyncState.Error -> Icons.Default.Warning
                                is SupabaseSyncState.NotConfigured -> Icons.Default.CloudOff
                                else -> Icons.Default.Cloud
                            },
                            contentDescription = "Supabase Status",
                            tint = when (syncState) {
                                is SupabaseSyncState.Synced -> Color(0xFF059669)
                                is SupabaseSyncState.Syncing -> MaterialTheme.colorScheme.primary
                                is SupabaseSyncState.Error -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Supabase Cloud",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (syncState) {
                                is SupabaseSyncState.Synced -> "Synced with cloud"
                                is SupabaseSyncState.Syncing -> "Syncing database…"
                                is SupabaseSyncState.Error -> "Sync issue"
                                is SupabaseSyncState.NotConfigured -> "Not connected"
                                is SupabaseSyncState.Idle -> "Ready to sync"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = when (syncState) {
                                is SupabaseSyncState.Synced -> Color(0xFF059669)
                                is SupabaseSyncState.Syncing -> MaterialTheme.colorScheme.primary
                                is SupabaseSyncState.Error -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                IconButton(
                    onClick = { showSetupDialog = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Supabase Setup Info",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (syncState) {
                is SupabaseSyncState.Synced -> {
                    val dateStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(syncState.lastSyncTimestamp))
                    Text(
                        text = "Last synced: $dateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = onSyncNow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .testTag("supabase_sync_now_button"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync Now", fontSize = 11.sp)
                    }
                }
                is SupabaseSyncState.Syncing -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Syncing chats with Supabase…",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                is SupabaseSyncState.Error -> {
                    Text(
                        text = syncState.message,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 10.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = onSyncNow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .testTag("supabase_retry_sync_button"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retry Sync", fontSize = 11.sp)
                    }
                }
                is SupabaseSyncState.NotConfigured -> {
                    Text(
                        text = "Add SUPABASE_URL & ANON_KEY in Secrets panel to store chat data in Supabase.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { showSetupDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .testTag("supabase_setup_button"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Supabase Setup & SQL", fontSize = 11.sp)
                    }
                }
                is SupabaseSyncState.Idle -> {
                    OutlinedButton(
                        onClick = onSyncNow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .testTag("supabase_sync_now_button"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync with Supabase", fontSize = 11.sp)
                    }
                }
            }
        }
    }

    if (showSetupDialog) {
        SupabaseSetupDialog(onDismiss = { showSetupDialog = false })
    }
}

private const val SUPABASE_SQL_SCHEMA = """-- Social AI Supabase Schema
create table if not exists chat_sessions (
  id bigint primary key,
  title text not null,
  created_at bigint not null,
  updated_at bigint not null,
  model text not null default 'openrouter/free'
);

create table if not exists chat_messages (
  id bigint primary key,
  session_id bigint not null references chat_sessions(id) on delete cascade,
  role text not null,
  content text not null,
  timestamp bigint not null,
  is_error boolean not null default false
);

alter table chat_sessions enable row level security;
alter table chat_messages enable row level security;

create policy "anon_all_sessions" on chat_sessions for all to anon using (true) with check (true);
create policy "anon_all_messages" on chat_messages for all to anon using (true) with check (true);"""

@Composable
fun SupabaseSetupDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Supabase Setup Guide", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                Text(
                    text = "1. AI Studio Secrets Setup",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Open the Secrets panel in Google AI Studio and configure:\n" +
                            "• SUPABASE_URL (e.g. https://xyz.supabase.co)\n" +
                            "• SUPABASE_ANON_KEY (from Supabase Settings > API)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "2. Supabase SQL Schema",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Run this in your Supabase project's SQL Editor:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = SUPABASE_SQL_SCHEMA,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Supabase SQL", SUPABASE_SQL_SCHEMA)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "SQL schema copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy SQL", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}
