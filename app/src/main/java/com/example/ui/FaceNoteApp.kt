package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CurrentUser
import com.example.data.FriendProfile
import com.example.data.HometownMemory
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceNoteApp(viewModel: FriendViewModel) {
    val context = LocalContext.current
    val profiles by viewModel.filteredProfiles.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val memories by viewModel.memories.collectAsStateWithLifecycle()
    val selectedProfile by viewModel.selectedProfile.collectAsStateWithLifecycle()
    val showRegisterModal by viewModel.showRegisterModal.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val totalEarnings by viewModel.totalEarnings.collectAsStateWithLifecycle()
    val earningLogs by viewModel.earningLogs.collectAsStateWithLifecycle()

    var showPostMemoryDialog by remember { mutableStateOf(false) }
    var showCashOutDialog by remember { mutableStateOf(false) }
    var showLaunchAdDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // FaceNote Logo
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Face",
                                color = FaceNoteBlue,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            )
                            Text(
                                text = "Note",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }

                        // Top auth status/action buttons
                        if (currentUser != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = currentUser?.name ?: "",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 120.dp)
                                )
                                TextButton(
                                    onClick = {
                                        viewModel.logout()
                                        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("logout_button")
                                ) {
                                    Text("Logout", fontSize = 13.sp)
                                }
                            }
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { viewModel.setShowRegisterModal(true) },
                                    modifier = Modifier.testTag("login_action_button")
                                ) {
                                    Text("Log In", color = FaceNoteGray)
                                }
                                Button(
                                    onClick = { viewModel.setShowRegisterModal(true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = FaceNoteBlue),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("join_free_button")
                                ) {
                                    Text("Join Free", fontSize = 13.sp, color = Color.White)
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.border(0.5.dp, FaceNoteBorder).windowInsetsPadding(WindowInsets.statusBars)
            )
        }
    ) { innerPadding ->
        var activeTabMode by remember { mutableStateOf(0) } // 0 = Mobile App, 1 = Web Site Simulator
        var mobilePlatformState by remember { mutableStateOf("android") } // "android" or "ios"
        
        // Web Site Simulator States
        var browserUrlInput by remember { mutableStateOf("https://facenote.org/") }
        var currentWebSubTab by remember { mutableStateOf("home") } // "home", "code", "server", "seo"
        var serverPowerOn by remember { mutableStateOf(true) }
        var seoTitle by remember { mutableStateOf("FaceNote - Find Your Lost Hometown Classmates") }
        var seoDescription by remember { mutableStateOf("Reconnect with school friends, neighborhood pals, and childhood buddies on search-by-hometown directories. Claim your free profile!") }
        var seoKeywords by remember { mutableStateOf("find classmates, hometown directory, lookup school friends, local gossip reunion") }
        var copySuccessVisible by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(FaceNoteBg)
        ) {
            // Environment switcher
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FaceNoteBlueSoft),
                border = BorderStroke(1.dp, FaceNoteBlue.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val mobileSelected = activeTabMode == 0
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (mobileSelected) FaceNoteBlue else Color.Transparent)
                            .clickable { activeTabMode = 0 }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Mobile View",
                            tint = if (mobileSelected) Color.White else FaceNoteBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mobile App View",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (mobileSelected) Color.White else FaceNoteSlate
                        )
                    }

                    val webSelected = activeTabMode == 1
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (webSelected) FaceNoteBlue else Color.Transparent)
                            .clickable { activeTabMode = 1 }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Web View",
                            tint = if (webSelected) Color.White else FaceNoteBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FaceNote Desktop Web",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (webSelected) Color.White else FaceNoteSlate
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEF4444), shape = CircleShape)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("DEV", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (activeTabMode == 0) {
                // Platform Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .background(Color(0xFFCBD5E1).copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isAndroid = mobilePlatformState == "android"
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isAndroid) Color.White else Color.Transparent)
                            .clickable { mobilePlatformState = "android" }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🤖 Android (Material 3)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAndroid) FaceNoteBlue else FaceNoteGray
                        )
                    }

                    val isIos = mobilePlatformState == "ios"
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isIos) Color.White else Color.Transparent)
                            .clickable { mobilePlatformState = "ios" }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🍎 iOS (Cupertino Style)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isIos) Color(0xFF007AFF) else FaceNoteGray
                        )
                    }
                }

                if (mobilePlatformState == "android") {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
            // HERO SECTION
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "WELCOME TO FACENOTE",
                        color = FaceNoteBlue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Find your old lost friends on FaceNote, built to reconnect you.",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        lineHeight = 36.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Rediscover childhood friends, school classmates, and past neighbors. Type a name below to see if they are looking for you.",
                        color = FaceNoteGray,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            // SHARED EARNINGS AND DYNAMIC CASH TICKER BLOCK
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = BorderStroke(1.dp, FaceNoteBlue.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Title row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Earnings Icon",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Active Ad-Revenue Share",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FaceNoteSlate
                                )
                            }
                            
                            // Live status blinking indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Text(
                                    text = "LIVE GENERATING",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF10B981),
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Large Ticker Number and Rate
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Your Reconnection Fund Balance",
                                    fontSize = 11.sp,
                                    color = FaceNoteGray,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = String.format(java.util.Locale.US, "$%.2f", totalEarnings),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.testTag("total_earnings_ticker")
                                )
                            }

                            // Growth rate badge
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5))
                            ) {
                                Text(
                                    text = "+$0.01/sec active",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Cash Out & Sponsor Ad
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showCashOutDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("cash_out_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FaceNoteBlueSoft),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Wallet Icon",
                                    tint = FaceNoteBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cash Out",
                                    color = FaceNoteBlue,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    if (currentUser == null) {
                                        viewModel.setShowRegisterModal(true)
                                        Toast.makeText(context, "Register to unlock regional buddy sponsor ads!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        showLaunchAdDialog = true
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("sponsor_ad_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FaceNoteBlue),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Campaign Icon",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sponsor Buddy Ad",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Mini Logs Scrolling Display
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(FaceNoteBg, shape = RoundedCornerShape(12.dp))
                                .border(1.dp, FaceNoteBorder, shape = RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "REVENUE ACTIVITIES",
                                    fontSize = 10.sp,
                                    color = FaceNoteGray,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                earningLogs.take(3).forEach { log ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = log,
                                            fontSize = 11.sp,
                                            color = FaceNoteSlate,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SEARCH BAR block
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(FaceNoteBg, shape = RoundedCornerShape(12.dp))
                                .border(1.dp, FaceNoteBorder, shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Icon",
                                tint = FaceNoteGray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextField(
                                value = searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                placeholder = { Text("Enter an old friend's name...", color = FaceNoteGray) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = FaceNoteSlate,
                                    unfocusedTextColor = FaceNoteSlate
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("search_name_input"),
                                maxLines = 1,
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.triggerSearchReward()
                                if (currentUser == null) {
                                    viewModel.setShowRegisterModal(true)
                                } else {
                                    Toast.makeText(context, "Searching filters...", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("search_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FaceNoteBlue)
                        ) {
                            Text("Search FaceNote", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Stats icon",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Over 12,000 successful reconnections recently",
                        color = FaceNoteGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // LOCAL AREA DISCOVERY LIST
            item {
                Spacer(modifier = Modifier.height(36.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "People who recently joined from your area",
                        fontWeight = FontWeight.ExtraBold,
                        color = FaceNoteSlate,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Based on your approximate location",
                        color = FaceNoteGray,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // GRID / LIST CARDS of Friends
            if (profiles.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "No results",
                                tint = FaceNoteGray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No friends found matching \"$searchQuery\"",
                                color = FaceNoteSlate,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Try searching for class years (e.g. 2012) or other neighborhoods.",
                                color = FaceNoteGray,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(profiles) { profile ->
                    FriendCard(
                        profile = profile,
                        isLoggedIn = currentUser != null,
                        onClick = {
                            if (currentUser == null) {
                                viewModel.setShowRegisterModal(true)
                            } else {
                                viewModel.selectProfile(profile)
                            }
                        }
                    )
                }
            }

            // MEMORY BOARD / COMMUNITY WALL SECTION
            item {
                Spacer(modifier = Modifier.height(40.dp))
                Divider(modifier = Modifier.padding(horizontal = 20.dp), color = FaceNoteBorder)
                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Hometown Memory Wall",
                                fontWeight = FontWeight.ExtraBold,
                                color = FaceNoteSlate,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Class updates & nostalgic local gossip",
                                color = FaceNoteGray,
                                fontSize = 13.sp
                            )
                        }

                        if (currentUser != null) {
                            Button(
                                onClick = { showPostMemoryDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = FaceNoteBlueSoft),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("post_memory_wall_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Post memory",
                                    tint = FaceNoteBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Recall", color = FaceNoteBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (memories.isEmpty()) {
                        Text(
                            text = "No memories posted yet. Be the first to share!",
                            color = FaceNoteGray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        memories.forEach { memory ->
                            MemoryCard(memory)
                        }
                    }
                }
            }
        }
    } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        AppleDeviceSimulator(
                            currentUser = currentUser,
                            profiles = profiles,
                            memories = memories,
                            totalEarnings = totalEarnings,
                            earningLogs = earningLogs,
                            searchQuery = searchQuery,
                            onSearchChanged = { viewModel.updateSearchQuery(it) },
                            onSearchClicked = { viewModel.triggerSearchReward() },
                            onJoinClicked = { viewModel.setShowRegisterModal(true) },
                            onLogoutClicked = { viewModel.logout() },
                            onProfileSelected = { viewModel.selectProfile(it) },
                            onCashOutClicked = { showCashOutDialog = true },
                            onSponsorAdClicked = {
                                if (currentUser == null) {
                                    viewModel.setShowRegisterModal(true)
                                    Toast.makeText(context, "Register to sponsor buddies!", Toast.LENGTH_SHORT).show()
                                } else {
                                    showLaunchAdDialog = true
                                }
                            },
                            onPostMemoryClicked = { showPostMemoryDialog = true }
                        )
                    }
                }
    } else {
            // Interactive Desktop Web platform layout representation
                FaceNoteWebPortal(
                    profiles = profiles,
                    currentUser = currentUser,
                    memories = memories,
                    totalEarnings = totalEarnings,
                    earningLogs = earningLogs,
                    searchQuery = searchQuery,
                    onUpdateSearchQuery = { viewModel.updateSearchQuery(it) },
                    onPostMemory = { viewModel.postMemory(it) },
                    onRegister = { viewModel.setShowRegisterModal(true) },
                    onSelectProfile = { viewModel.selectProfile(it) },
                    onCashOut = { showCashOutDialog = true },
                    onSponsorAd = {
                        if (currentUser == null) {
                            viewModel.setShowRegisterModal(true)
                            Toast.makeText(context, "Register to sponsor buddies!", Toast.LENGTH_SHORT).show()
                        } else {
                            showLaunchAdDialog = true
                        }
                    },
                    browserUrlInput = browserUrlInput,
                    onBrowserUrlInputChanged = { browserUrlInput = it },
                    currentWebSubTab = currentWebSubTab,
                    onSubTabChanged = { currentWebSubTab = it },
                    serverPowerOn = serverPowerOn,
                    onServerPowerChanged = { serverPowerOn = it },
                    seoTitle = seoTitle,
                    onSeoTitleChanged = { seoTitle = it },
                    seoDescription = seoDescription,
                    onSeoDescriptionChanged = { seoDescription = it },
                    seoKeywords = seoKeywords,
                    onSeoKeywordsChanged = { seoKeywords = it },
                    copySuccessVisible = copySuccessVisible,
                    onCopySuccess = { copySuccessVisible = it },
                    onChangeEarningBonus = { amount, reason -> viewModel.addEarningBonus(amount, reason, logAction = true) }
                )
            }
        }
    }

    // Modal: REVEAL CONNECTION (REGISTER)
    if (showRegisterModal) {
        RegisterModelDialog(
            onDismiss = { viewModel.setShowRegisterModal(false) },
            onSubmit = { name, email, hometown, searchIntent ->
                viewModel.registerUser(name, email, hometown, searchIntent)
                Toast.makeText(context, "Welcome, $name! Profiles Unlocked!", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Modal: PROFILE DETAIL & CONVERSATION START
    selectedProfile?.let { profile ->
        ProfileDetailDialog(
            profile = profile,
            onDismiss = { viewModel.selectProfile(null) },
            onSendRequest = {
                Toast.makeText(context, "Connection request sent to ${profile.name}! They will receive your email/note.", Toast.LENGTH_LONG).show()
                viewModel.selectProfile(null)
            }
        )
    }

    // Modal: POST HOMETOWN MEMORY
    if (showPostMemoryDialog) {
        PostMemoryDialog(
            onDismiss = { showPostMemoryDialog = false },
            onSubmit = { text ->
                viewModel.postMemory(text)
                showPostMemoryDialog = false
                Toast.makeText(context, "Memory posted successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: CASH OUT TO BANK / PAYPAL
    if (showCashOutDialog) {
        CashOutDialog(
            currentBalance = totalEarnings,
            onDismiss = { showCashOutDialog = false },
            onSubmitPayout = { paymentMethod, address ->
                viewModel.addEarningBonus(-totalEarnings, "Cash Out executed", logAction = true)
                showCashOutDialog = false
                Toast.makeText(context, "Payout of $${String.format(java.util.Locale.US, "%.2f", totalEarnings)} successfully sent to $address via $paymentMethod!", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Modal: SPONSOR RECONNECTION BUDDY AD
    if (showLaunchAdDialog) {
        SponsorAdDialog(
            currentBalance = totalEarnings,
            profiles = profiles,
            onDismiss = { showLaunchAdDialog = false },
            onSubmitSponsor = { amount, profileName ->
                viewModel.addEarningBonus(-amount, "Sponsored ad: $profileName", logAction = true)
                showLaunchAdDialog = false
                Toast.makeText(context, "$$amount campaign active for $profileName! Placed region-wide in $profileName's hometown class feed.", Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
fun FriendCard(
    profile: FriendProfile,
    isLoggedIn: Boolean,
    onClick: () -> Unit
) {
    val niceAvatarColors = listOf(
        Color(0xFF3B82F6), // Blue
        Color(0xFF10B981), // Emerald
        Color(0xFF8B5CF6), // Purple
        Color(0xFFF59E0B), // Amber
        Color(0xFFEC4899), // Pink
        Color(0xFF6366F1)  // Indigo
    )
    val avatarColor = niceAvatarColors[profile.avatarColorSeed % niceAvatarColors.size]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
            .testTag("friend_profile_card_${profile.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, FaceNoteBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar (blurred if not logged in)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(avatarColor.copy(alpha = if (isLoggedIn) 1f else 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoggedIn) {
                        Text(
                            text = profile.name.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked Profile",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Credentials / Name
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoggedIn) {
                        Text(
                            text = profile.name,
                            fontWeight = FontWeight.Bold,
                            color = FaceNoteSlate,
                            fontSize = 16.sp
                        )
                    } else {
                        // Blurred name visual representation
                        Text(
                            text = obfuscateSequence(profile.name),
                            fontWeight = FontWeight.Bold,
                            color = FaceNoteSlate.copy(alpha = 0.5f),
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = profile.hometown,
                        color = FaceNoteGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (!isLoggedIn) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Unlock icon",
                        tint = FaceNoteBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = FaceNoteBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = FaceNoteBlueSoft,
                    contentColor = FaceNoteBlue
                ) {
                    Text(
                        text = profile.statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // CTA
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isLoggedIn) "View Profile" else "Unlock Profile",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FaceNoteBlue
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Arrow right icon",
                        tint = FaceNoteBlue,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MemoryCard(memory: HometownMemory) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, FaceNoteBorder.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Comment Bubble",
                    tint = FaceNoteBlue,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = memory.authorName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = FaceNoteSlate
                )
                Text(
                    text = "•",
                    color = FaceNoteGray,
                    fontSize = 12.sp
                )
                Text(
                    text = memory.hometownLocation,
                    color = FaceNoteGray,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"${memory.memoryText}\"",
                fontSize = 13.sp,
                color = FaceNoteSlate,
                lineHeight = 18.sp,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Dialog window for Login/Join Free
@Composable
fun RegisterModelDialog(
    onDismiss: () -> Unit,
    onSubmit: (name: String, email: String, hometown: String, searchIntent: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var hometown by remember { mutableStateOf("") }
    var searchIntent by remember { mutableStateOf("") }

    var errors by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("auth_modal_content")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reveal Your Connections",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = FaceNoteSlate
                    )
                    IconButton(
                        onClick = { onDismiss() },
                        modifier = Modifier.testTag("close_modal_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Dialogue")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Create a quick free account to view full profile details and send messages.",
                    fontSize = 13.sp,
                    color = FaceNoteGray,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Fields
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Full Name") },
                    placeholder = { Text("e.g. John Doe") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_name_field"),
                    singleLine = true,
                    isError = errors && name.isBlank()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Your Email Address") },
                    placeholder = { Text("e.g. john@example.com") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_field"),
                    singleLine = true,
                    isError = errors && email.isBlank()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = hometown,
                    onValueChange = { hometown = it },
                    label = { Text("Class Year / Neighborhood / Hometown") },
                    placeholder = { Text("e.g. Oakridge High 2012") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_hometown_field"),
                    singleLine = true,
                    isError = errors && hometown.isBlank()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchIntent,
                    onValueChange = { searchIntent = it },
                    label = { Text("Who are you looking for?") },
                    placeholder = { Text("e.g. Searching for the 2012 track squad") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                if (errors) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please fill in all required fields (Name, Email, School/Hometown).",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (name.isBlank() || email.isBlank() || hometown.isBlank()) {
                            errors = true
                        } else {
                            onSubmit(name, email, hometown, searchIntent)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = FaceNoteBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Unlock Connections", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                }
            }
        }
    }
}

// Dialog window for Detail profile
@Composable
fun ProfileDetailDialog(
    profile: FriendProfile,
    onDismiss: () -> Unit,
    onSendRequest: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("friend_profile_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Friend Profile",
                        fontSize = 14.sp,
                        color = FaceNoteBlue,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { onDismiss() },
                        modifier = Modifier.testTag("close_profile_dialog_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Profil")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(FaceNoteBlueSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.name.take(2).uppercase(),
                            color = FaceNoteBlue,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column {
                        Text(
                            text = profile.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = FaceNoteSlate
                        )
                        Text(
                            text = profile.hometown,
                            fontSize = 13.sp,
                            color = FaceNoteGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = FaceNoteBorder)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "A Memory They Shared:",
                    fontSize = 12.sp,
                    color = FaceNoteGray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FaceNoteBg, shape = RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "\"${profile.memoryDetail}\"",
                        fontSize = 14.sp,
                        color = FaceNoteSlate,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onSendRequest() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("send_reconnect_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = FaceNoteBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Re-connect with ${profile.name.substringBefore(" ")}", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// Dialog for posting hometown memory
@Composable
fun PostMemoryDialog(
    onDismiss: () -> Unit,
    onSubmit: (text: String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("memory_dialog_content")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Share Nostalgic Memory",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FaceNoteSlate
                    )
                    IconButton(onClick = { onDismiss() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Dialogue")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Post a memory so neighborhood friends can recall those times with you!",
                    fontSize = 12.sp,
                    color = FaceNoteGray
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Remember when we used to play soccer in the vacant back lot behind Lincoln Elementary?...", fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("memory_textbox"),
                    maxLines = 5,
                    isError = error && text.isBlank()
                )

                if (error) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Memory cannot be empty.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (text.isBlank()) {
                            error = true
                        } else {
                            onSubmit(text)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("memory_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = FaceNoteBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Post to Board", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// Obfuscate standard string to represent dynamic blur effect comfortably on older APIs
fun obfuscateSequence(name: String): String {
    val items = name.split(" ")
    return items.joinToString(" ") { field ->
        if (field.isEmpty()) "" else {
            val first = field.first()
            val remaining = "•".repeat(field.length - 1)
            "$first$remaining"
        }
    }
}

@Composable
fun CashOutDialog(
    currentBalance: Double,
    onDismiss: () -> Unit,
    onSubmitPayout: (method: String, address: String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("PayPal") }
    var addressInput by remember { mutableStateOf("") }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("cashout_dialog_content")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cash Out Earnings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = FaceNoteSlate
                    )
                    IconButton(onClick = { onDismiss() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Dialogue")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Transfer your automatically generated FaceNote ad-share balance securely to your account.",
                    fontSize = 13.sp,
                    color = FaceNoteGray,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Balance display card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FaceNoteBlueSoft),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Available to Transfer",
                            fontSize = 11.sp,
                            color = FaceNoteBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format(java.util.Locale.US, "$%.2f", currentBalance),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select Payment Method",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FaceNoteSlate
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("PayPal", "Venmo", "Bank Direct").forEach { method ->
                        val isSelected = selectedMethod == method
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedMethod = method },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) FaceNoteBlue else FaceNoteBorder
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) FaceNoteBlueSoft else Color.White
                            )
                        ) {
                            Text(
                                text = method,
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) FaceNoteBlue else FaceNoteSlate
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    label = { Text(if (selectedMethod == "Bank Direct") "Account/Routing detail" else "Email or Username") },
                    placeholder = { Text(if (selectedMethod == "PayPal") "example@paypal.com" else "Enter payment token...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cashout_input"),
                    singleLine = true,
                    isError = hasAttemptedSubmit && addressInput.isBlank()
                )

                if (hasAttemptedSubmit && addressInput.isBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("This field cannot be empty.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                val isBalanceSufficient = currentBalance >= 10.00

                if (!isBalanceSufficient) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Threshold Info",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Minimum cash out is $10.00. Keep the app open, search, and contribute to qualify!",
                                fontSize = 11.sp,
                                color = Color(0xFF92400E),
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = {
                        hasAttemptedSubmit = true
                        if (addressInput.isNotBlank() && isBalanceSufficient) {
                            onSubmitPayout(selectedMethod, addressInput)
                        }
                    },
                    enabled = isBalanceSufficient,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("cashout_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = FaceNoteBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Initiate Payout", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SponsorAdDialog(
    currentBalance: Double,
    profiles: List<FriendProfile>,
    onDismiss: () -> Unit,
    onSubmitSponsor: (amount: Double, profileName: String) -> Unit
) {
    val options = listOf(
        3.00,
        5.00,
        10.00,
        25.00
    )
    var selectedAmount by remember { mutableStateOf(3.00) }
    var selectedProfileName by remember { mutableStateOf(profiles.firstOrNull()?.name ?: "Sarah Jenkins") }
    var isExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("sponsor_dialog_content")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sponsor Buddy Ad",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = FaceNoteSlate
                    )
                    IconButton(onClick = { onDismiss() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Dialogue")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Boost finding classmate directories region-wide. Use your generated earnings to place Local Sponsor Ads about this search!",
                    fontSize = 13.sp,
                    color = FaceNoteGray,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Select profile to boost
                Text(
                    text = "Which lost classmate to boost?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FaceNoteSlate
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FaceNoteBg, shape = RoundedCornerShape(10.dp))
                        .border(1.dp, FaceNoteBorder, shape = RoundedCornerShape(10.dp))
                        .clickable { isExpanded = !isExpanded }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedProfileName,
                            fontSize = 13.sp,
                            color = FaceNoteSlate,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Drop icon",
                            tint = FaceNoteGray
                        )
                    }
                }

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, FaceNoteBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(profiles) { profile ->
                                Text(
                                    text = profile.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedProfileName = profile.name
                                            isExpanded = false
                                        }
                                        .padding(12.dp),
                                    fontSize = 13.sp,
                                    color = FaceNoteSlate,
                                    fontWeight = if (selectedProfileName == profile.name) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Boost amount
                Text(
                    text = "Choose Boost Budget",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FaceNoteSlate
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.forEach { amount ->
                        val isSelected = selectedAmount == amount
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedAmount = amount },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) FaceNoteBlue else FaceNoteBorder
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) FaceNoteBlueSoft else Color.White
                            )
                        ) {
                            Text(
                                text = String.format(java.util.Locale.US, "$%.0f", amount),
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) FaceNoteBlue else FaceNoteSlate
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val isBalanceSufficient = currentBalance >= selectedAmount

                if (!isBalanceSufficient) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = BorderStroke(1.dp, Color(0xFFFEE2E2)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Error Info",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Insufficient balance. Your current Connection Fund balance is $${String.format(java.util.Locale.US, "%.2f", currentBalance)}",
                                fontSize = 11.sp,
                                color = Color(0xFF991B1B),
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = {
                        if (isBalanceSufficient) {
                            onSubmitSponsor(selectedAmount, selectedProfileName)
                        }
                    },
                    enabled = isBalanceSufficient,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("sponsor_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = FaceNoteBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Launch Sponsor Campaign",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// FACENOTE ENTERPRISE DESKTOP WEB ENGINE & REACTIVE CODE EXPORTER
// ---------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FaceNoteWebPortal(
    profiles: List<FriendProfile>,
    currentUser: CurrentUser?,
    memories: List<HometownMemory>,
    totalEarnings: Double,
    earningLogs: List<String>,
    searchQuery: String,
    onUpdateSearchQuery: (String) -> Unit,
    onPostMemory: (String) -> Unit,
    onRegister: () -> Unit,
    onSelectProfile: (FriendProfile) -> Unit,
    onCashOut: () -> Unit,
    onSponsorAd: () -> Unit,
    browserUrlInput: String,
    onBrowserUrlInputChanged: (String) -> Unit,
    currentWebSubTab: String,
    onSubTabChanged: (String) -> Unit,
    serverPowerOn: Boolean,
    onServerPowerChanged: (Boolean) -> Unit,
    seoTitle: String,
    onSeoTitleChanged: (String) -> Unit,
    seoDescription: String,
    onSeoDescriptionChanged: (String) -> Unit,
    seoKeywords: String,
    onSeoKeywordsChanged: (String) -> Unit,
    copySuccessVisible: Boolean,
    onCopySuccess: (Boolean) -> Unit,
    onChangeEarningBonus: (Double, String) -> Unit
) {
    val context = LocalContext.current
    
    // Live Server Terminal Simulation
    var simulatedServerLogs by remember {
        mutableStateOf(
            listOf(
                "INFO  [SYS] Host instance initial handshake (SSL Port 443)...",
                "SUCCESS  [DB] SQLite/Room database layer connection pool ACTIVE",
                "SUCCESS  [WEB] Simulated server bound to https://facenote.org/",
                "WS_PIPE  [SEC] Shared-Revenue telemetry channel initiated"
            )
        )
    }

    // Trigger log entries when states edit
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank() && serverPowerOn) {
            val randMs = (10..80).random()
            simulatedServerLogs = listOf(
                "GET /api/directory?q=${searchQuery} 200 OK - ${randMs}ms (Host: local_node_1)",
                "TELEMETRY: Synced user search criteria of \"${searchQuery}\"",
                "+$0.50 ad-share bonus credited to connection fund active"
            ) + simulatedServerLogs.take(12)
        }
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null && serverPowerOn) {
            simulatedServerLogs = listOf(
                "POST /api/auth/register-hometown 201 Created - 14ms",
                "SUCCESS: Account synchronized with token header: ${currentUser.email.hashCode()}"
            ) + simulatedServerLogs.take(12)
        }
    }

    LaunchedEffect(memories.size) {
        if (memories.isNotEmpty() && serverPowerOn) {
            simulatedServerLogs = listOf(
                "POST /api/memory-wall 20Created - 11ms",
                "SUCCESS: Published new nostalgia board comment regional broadcast"
            ) + simulatedServerLogs.take(12)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Desktop browser envelope frame
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = FaceNoteBg),
            border = BorderStroke(2.dp, FaceNoteSlate.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Browser Top Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE2E8F0))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Laptop dots
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Navigation Actions
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = FaceNoteSlate.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Forward",
                        tint = FaceNoteSlate.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload",
                        tint = FaceNoteSlate.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp).clickable {
                            if (serverPowerOn) {
                                simulatedServerLogs = listOf("INFO  [SYS] Client initiated hard reload of index.html") + simulatedServerLogs.take(10)
                                Toast.makeText(context, "Webpage cache cleared and reloaded!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    // URL Input Address Bar
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White, shape = RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFCBD5E1), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "SSL Encrypted",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (serverPowerOn) "https://facenote.org/${if (currentWebSubTab != "home") currentWebSubTab else ""}" else "https://facenote.org/503-error.html",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = FaceNoteSlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Power Status
                    Box(
                        modifier = Modifier
                            .background(
                                if (serverPowerOn) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (serverPowerOn) "SERVER: ONLINE" else "SERVER: OFFLINE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = if (serverPowerOn) Color(0xFF065F46) else Color(0xFF991B1B)
                        )
                    }
                }

                // Desktop Browser Bookmark / Tab Navigation Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9))
                        .border(BorderStroke(0.5.dp, Color(0xFFE2E8F0)))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        Triple("home", "🌐 Live Index Web", Icons.Default.Home),
                        Triple("code", "💻 Code Exporter (HTML/CSS)", Icons.Default.Edit),
                        Triple("server", "⚙️ Server Hosting Console", Icons.Default.Settings),
                        Triple("seo", "📂 SEO & Sharing Preview", Icons.Default.Search)
                    )

                    tabs.forEach { (key, label, icon) ->
                        val isSelected = currentWebSubTab == key
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color.White else Color.Transparent)
                                .clickable { onSubTabChanged(key) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) FaceNoteBlue else FaceNoteGray,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) FaceNoteSlate else FaceNoteGray
                            )
                        }
                    }
                }

                // Browser Screen Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White)
                ) {
                    if (!serverPowerOn) {
                        // 503 Web Server Error view representation
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF8FAFC))
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Off server",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "503 Service Unavailable",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = FaceNoteSlate
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "The FaceNote web hosting daemon is currently offline. Power on the Server in the Server Hosting Console tab to restore live web requests.",
                                fontSize = 12.sp,
                                color = FaceNoteGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.widthIn(max = 400.dp),
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { onServerPowerChanged(true) },
                                colors = ButtonDefaults.buttonColors(containerColor = FaceNoteBlue)
                            ) {
                                Text("Power On Web Engine", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Switch active web tabs
                        when (currentWebSubTab) {
                            "home" -> {
                                // RENDER Desktop 3-column Layout replica of FaceNote Website
                                Row(modifier = Modifier.fillMaxSize()) {
                                    // COLUMN 1: LEFT DESKTOP NAV SIDEPANE
                                    Column(
                                        modifier = Modifier
                                            .width(220.dp)
                                            .fillMaxHeight()
                                            .background(Color(0xFFF8FAFC))
                                            .border(BorderStroke(0.5.dp, Color(0xFFE2E8F0)))
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Face",
                                                    color = FaceNoteBlue,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 20.sp
                                                )
                                                Text(
                                                    text = "Note Portal",
                                                    color = FaceNoteSlate,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp
                                                )
                                            }

                                            HorizontalDivider(color = Color(0xFFE2E8F0))

                                            // Shortcuts
                                            Text("COORDINATED MAPS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FaceNoteGray)
                                            
                                            listOf(
                                                "🌐 Discover Hub" to true,
                                                "📂 Nost nostalgia discussion" to false,
                                                "⚡ Live Traffic Earnings" to false
                                            ).forEach { (label, act) ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (act) FaceNoteBlueSoft else Color.Transparent)
                                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = label,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (act) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (act) FaceNoteBlue else FaceNoteSlate
                                                    )
                                                }
                                            }
                                        }

                                        // Web Cash widget
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("Shared Ad fund", fontSize = 10.sp, color = FaceNoteGray, fontWeight = FontWeight.Bold)
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = String.format(java.util.Locale.US, "$%.2f", totalEarnings),
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color(0xFF10B981)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                                }
                                                Spacer(modifier = Modifier.height(10.dp))
                                                TextButton(
                                                    onClick = { onCashOut() },
                                                    contentPadding = PaddingValues(0.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("CASH OUT NOW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FaceNoteBlue)
                                                }
                                            }
                                        }
                                    }

                                    // COLUMN 2: MIDDLE MAIN AREA
                                    LazyColumn(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        item {
                                            // Website header block
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        Brush.horizontalGradient(listOf(FaceNoteBlueSoft, Color(0xFFEEF2F6))),
                                                        shape = RoundedCornerShape(16.dp)
                                                    )
                                                    .padding(20.dp)
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "Reconnect Regional Database (Web Server Live)",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = FaceNoteBlue
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "A client website running on SQLite and synchronized dynamically. Search or add memories to check it out.",
                                                        fontSize = 11.sp,
                                                        color = FaceNoteSlate,
                                                        lineHeight = 16.sp
                                                    )
                                                }
                                            }
                                        }

                                        item {
                                            // Web Desktop Directory Search bar representation
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Text("Lookup lost high school classmates & old childhood friends", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FaceNoteSlate)
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        OutlinedTextField(
                                                            value = searchQuery,
                                                            onValueChange = { onUpdateSearchQuery(it) },
                                                            placeholder = { Text("Search by name, hometown class, year...") },
                                                            modifier = Modifier.weight(1f),
                                                            singleLine = true,
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        Button(
                                                            onClick = { onChangeEarningBonus(0.50, "Website lookup trigger") },
                                                            colors = ButtonDefaults.buttonColors(containerColor = FaceNoteBlue),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Text("Query Directory", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Web profile grids
                                        item {
                                            Text(
                                                text = "Matched profiles (${profiles.size})",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FaceNoteSlate
                                            )
                                        }

                                        if (profiles.isEmpty()) {
                                            item {
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Icon(imageVector = Icons.Default.Search, contentDescription = "none", tint = FaceNoteGray)
                                                        Spacer(modifier = Modifier.height(10.dp))
                                                        Text("No classmate found. Change the filter above to sync.", fontSize = 12.sp, color = FaceNoteSlate)
                                                    }
                                                }
                                            }
                                        } else {
                                            items(profiles) { profile ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { onSelectProfile(profile) },
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(14.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(36.dp)
                                                                    .clip(CircleShape)
                                                                    .background(Color(0xFFE2E8F0)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = profile.name.take(1),
                                                                    color = FaceNoteBlue,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.width(12.dp))
                                                            Column {
                                                                Text(profile.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FaceNoteSlate)
                                                                Text("Hometown: ${profile.hometown}", fontSize = 11.sp, color = FaceNoteGray)
                                                            }
                                                        }
                                                        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "details", tint = FaceNoteGray)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // COLUMN 3: RIGHT BULLETIN & LOCAL BOARD
                                    Column(
                                        modifier = Modifier
                                            .width(240.dp)
                                            .fillMaxHeight()
                                            .background(Color(0xFFF8FAFC))
                                            .border(BorderStroke(0.5.dp, Color(0xFFE2E8F0)))
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text("BULLETIN & DISCOVERY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FaceNoteGray)
                                        
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("Retro Booster Ad", fontSize = 9.sp, fontWeight = FontWeight.Black, color = FaceNoteBlue)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Boost discovery rates region-wide. Sponsors placement using live fund balances.", fontSize = 11.sp, lineHeight = 15.sp, color = FaceNoteSlate)
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Button(
                                                    onClick = { onSponsorAd() },
                                                    modifier = Modifier.fillMaxWidth().height(32.dp),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Text("Launch Boost", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                        }

                                        // Mini Memory List Web view
                                        Text("LOCAL NOSTALGIA BOARD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FaceNoteGray)
                                        LazyColumn(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (memories.isEmpty()) {
                                                item {
                                                    Text("No discussion logs published yet.", fontSize = 11.sp, color = FaceNoteGray)
                                                }
                                            } else {
                                                items(memories.take(4)) { m ->
                                                    Card(
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                                                    ) {
                                                        Column(modifier = Modifier.padding(10.dp)) {
                                                            Text(m.hometownLocation, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = FaceNoteBlue)
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text(m.memoryText, fontSize = 11.sp, color = FaceNoteSlate, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            "code" -> {
                                // CODE HTML SOURCE CODE EXPORTER
                                val webpageTitle = if (seoTitle.isBlank()) "FaceNote Class Reconnect" else seoTitle
                                val webpageDesc = if (seoDescription.isBlank()) "Interactive hometown friend finder directory." else seoDescription
                                val exportedHtml = generateLiveHtml(totalEarnings, searchQuery, profiles, memories, webpageTitle, webpageDesc)

                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Header tool card in exporter
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF8FAFC))
                                            .border(BorderStroke(0.5.dp, Color(0xFFE2E8F0)))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Active Headless HTML & Tailwind Code Exporter", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FaceNoteSlate)
                                            Text("Exports fully dynamic browser-ready template loaded with current profiles database and tickers.", fontSize = 10.sp, color = FaceNoteGray)
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Button(
                                                onClick = {
                                                    val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                    val clip = android.content.ClipData.newPlainText("FaceNote Live Website", exportedHtml)
                                                    clipboardManager.setPrimaryClip(clip)
                                                    onCopySuccess(true)
                                                    Toast.makeText(context, "Full HTML/CSS codebase copied to clipboard!", Toast.LENGTH_LONG).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = FaceNoteBlue),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Share, contentDescription = "Copy text", tint = Color.White, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Copy Website Code", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }

                                    // Terminal editor with line numbering
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .background(Color(0xFF0F172A))
                                            .padding(12.dp)
                                    ) {
                                        val lineList = exportedHtml.split("\n")
                                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                                            items(lineList.size) { index ->
                                                Row(modifier = Modifier.fillMaxWidth()) {
                                                    Text(
                                                        text = String.format(java.util.Locale.US, "%03d  ", index + 1),
                                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF64748B),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = lineList[index],
                                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                        fontSize = 11.sp,
                                                        color = if (lineList[index].trim().startsWith("<")) Color(0xFF38BDF8) else Color(0xFFE2E8F0)
                                                    )
                                                }
                                            }
                                        }

                                        // Toast popup inside code screen
                                        if (copySuccessVisible) {
                                            Card(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .padding(20.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(imageVector = Icons.Default.Check, contentDescription = "OK", tint = Color.White, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Copied successfully! Paste into local index.html", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    TextButton(onClick = { onCopySuccess(false) }) {
                                                        Text("Dismiss", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            "server" -> {
                                // NODE SERVER HOSTING DASHBOARD
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFF1F5F9))
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Control switches
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("Apache / Node.js Engine Status", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FaceNoteSlate)
                                                    Text("Powers remote directory caching & telemetry systems.", fontSize = 11.sp, color = FaceNoteGray)
                                                }
                                                Switch(
                                                    checked = serverPowerOn,
                                                    onCheckedChange = { onServerPowerChanged(it) }
                                                )
                                            }
                                        }
                                    }

                                    // Server load monitors
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Text("HTTP PORT", fontSize = 10.sp, color = FaceNoteGray, fontWeight = FontWeight.Bold)
                                                Text("443 (SSL Secure)", fontSize = 14.sp, fontWeight = FontWeight.Black, color = FaceNoteSlate)
                                            }
                                        }

                                        Card(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Text("LATENCY RATE", fontSize = 10.sp, color = FaceNoteGray, fontWeight = FontWeight.Bold)
                                                Text("2.4 ms (Uptime: 99.9%)", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                                            }
                                        }
                                    }

                                    // Logs terminal
                                    Text("LIVE CLUSTERS STACK OUTPUT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FaceNoteGray)
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                                    ) {
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(12.dp)
                                        ) {
                                            items(simulatedServerLogs) { log ->
                                                Text(
                                                    text = log,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                    fontSize = 11.sp,
                                                    color = when {
                                                        log.startsWith("SUCCESS") -> Color(0xFF10B981)
                                                        log.startsWith("INFO") -> Color(0xFF38BDF8)
                                                        log.startsWith("POST") -> Color(0xFFF59E0B)
                                                        else -> Color(0xFFCBD5E1)
                                                    },
                                                    modifier = Modifier.padding(vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            "seo" -> {
                                // SEO SOCIAL CARDS AND METRICS EDITOR
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFF8FAFC))
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    item {
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                Text("HTML Header / Search Engine Metadata Editor", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FaceNoteSlate)
                                                
                                                OutlinedTextField(
                                                    value = seoTitle,
                                                    onValueChange = { onSeoTitleChanged(it) },
                                                    label = { Text("Webpage Title (<title>)") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                                                OutlinedTextField(
                                                    value = seoDescription,
                                                    onValueChange = { onSeoDescriptionChanged(it) },
                                                    label = { Text("Meta Description (meta name=\"description\")") },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    maxLines = 3
                                                )

                                                OutlinedTextField(
                                                    value = seoKeywords,
                                                    onValueChange = { onSeoKeywordsChanged(it) },
                                                    label = { Text("Page Index Keyphrases (Keywords)") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }

                                    item {
                                        Text("LIVE GOOGLE SEARCH ENGINE REPLICATION PREVIEW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FaceNoteGray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(FaceNoteBlue), contentAlignment = Alignment.Center) {
                                                        Text("F", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Column {
                                                        Text("FaceNote Website", fontSize = 10.sp, color = FaceNoteSlate, fontWeight = FontWeight.Bold)
                                                        Text("https://facenote.org ", fontSize = 9.sp, color = FaceNoteGray)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = if (seoTitle.isBlank()) "Class Directory Lookup & Regional Bulletins" else seoTitle,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF1A0DAB),
                                                    modifier = Modifier.clickable {
                                                        Toast.makeText(context, "Google crawl test passed!", Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = if (seoDescription.isBlank()) "No page description specified yet. Build relevance tags above." else seoDescription,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF4D5156),
                                                    lineHeight = 15.sp
                                                )
                                            }
                                        }
                                    }

                                    item {
                                        Text("SOCIAL NETWORK DISPATCH CARD (OPENGRAPH / OG:CARD)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FaceNoteGray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Column {
                                                // Shared Banner Replica
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(110.dp)
                                                        .background(Brush.horizontalGradient(listOf(FaceNoteBlue, Color(0xFF1E3A8A)))),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text("FaceNote Connect", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                                                        Text("Reuniting School Alumnis Worldwide", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
                                                    }
                                                }
                                                
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Text("FACENOTE.ORG", fontSize = 9.sp, color = FaceNoteGray, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(seoTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FaceNoteSlate)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(seoDescription, fontSize = 10.sp, color = FaceNoteGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(24.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun generateLiveHtml(
    earnings: Double,
    query: String,
    profiles: List<FriendProfile>,
    memories: List<HometownMemory>,
    seoTitle: String,
    seoDesc: String
): String {
    val profilesSnippet = if (profiles.isEmpty()) {
        """            <div class="bg-white p-6 rounded-xl border border-slate-100 shadow-sm text-center text-slate-400">
                No matching profiles found for "${query}" in high school records.
            </div>"""
    } else {
        profiles.joinToString("\n") { p ->
            """            <div class="bg-white p-4 rounded-xl border border-slate-100 shadow-sm hover:shadow-md transition">
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full flex items-center justify-center text-white font-bold bg-blue-600">
                        ${p.name.take(1)}
                    </div>
                    <div>
                        <h4 class="font-bold text-slate-800">${p.name}</h4>
                        <span class="text-xs text-blue-600 bg-blue-50 px-2 py-0.5 rounded-full">${p.hometown}</span>
                    </div>
                </div>
                <p class="text-sm text-slate-600 mt-3 italic">"${p.statusText}"</p>
                <div class="text-xs text-slate-500 mt-2 bg-slate-50 p-2 rounded-lg border border-slate-100">
                    <span class="font-semibold block mb-1">Hometown Nostalgia Detail:</span>
                    ${p.memoryDetail}
                </div>
            </div>"""
        }
    }

    val memoriesSnippet = if (memories.isEmpty()) {
        """            <p class="text-slate-400 text-xs italic text-center py-4">No local gossip comments posted yet.</p>"""
    } else {
        memories.joinToString("\n") { m ->
            """            <div class="bg-slate-50 p-3 rounded-lg border border-slate-100 text-xs text-slate-700">
                <div class="flex justify-between items-center mb-1">
                    <span class="font-bold text-blue-600">${m.hometownLocation} Feed</span>
                    <span class="text-[10px] text-slate-400">Class Memory</span>
                </div>
                <p class="italic">"${m.memoryText}"</p>
            </div>"""
        }
    }

    return """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${seoTitle}</title>
    <meta name="description" content="${seoDesc}">
    <!-- Tailwind CSS link for fully responsive high-fidelity looks -->
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-50 min-h-screen font-sans">

    <!-- Header bar -->
    <header class="bg-white border-b border-slate-200 sticky top-0 z-50">
        <div class="max-w-6xl mx-auto px-4 h-16 flex items-center justify-between">
            <div class="flex items-center gap-2">
                <span class="text-blue-600 text-2xl font-black tracking-tight">Face<span class="text-slate-800 font-bold">Note</span></span>
                <span class="bg-green-100 text-green-800 text-[10px] font-bold px-2 py-0.5 rounded-full">WEB SYNCED</span>
            </div>
            
            <div class="flex items-center gap-4">
                <!-- Live Ad revenue shared ticker -->
                <div class="flex items-center gap-2 bg-green-50 border border-green-200 px-3 py-1.5 rounded-lg">
                    <span class="w-2 h-2 rounded-full bg-green-500 animate-pulse"></span>
                    <span class="text-xs text-slate-500">Live Ad-Share:</span>
                    <span class="text-sm font-black text-green-600">$$${String.format(java.util.Locale.US, "%.2f", earnings)}</span>
                </div>
                <button class="bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold px-4 py-2 rounded-lg transition shadow-sm">
                    Claim Free Profile
                </button>
            </div>
        </div>
    </header>

    <!-- Main Container Layout -->
    <main class="max-w-6xl mx-auto px-4 py-8 grid grid-cols-1 md:grid-cols-12 gap-6">
        
        <!-- Left Sidebar Navigation -->
        <aside class="md:col-span-3 space-y-4">
            <div class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
                <nav class="space-y-1">
                    <a href="#" class="flex items-center gap-3 px-3 py-2 text-slate-700 hover:bg-slate-50 rounded-lg text-sm font-semibold text-blue-600 bg-blue-50">
                        🌐 Discover Hub
                    </a>
                    <a href="#" class="flex items-center gap-3 px-3 py-2 text-slate-600 hover:bg-slate-50 rounded-lg text-sm font-semibold">
                        📂 Memories Forum
                    </a>
                    <a href="#" class="flex items-center gap-3 px-3 py-2 text-slate-600 hover:bg-slate-50 rounded-lg text-sm font-semibold">
                        ⚡ Revenue Activities
                    </a>
                </nav>
            </div>
            
            <div class="bg-blue-900 text-white p-5 rounded-xl shadow-md">
                <h4 class="font-bold text-sm">Hometown Connection</h4>
                <p class="text-xs text-blue-200 mt-2">Reconnecting families, old classmates, and lost friends since school days.</p>
            </div>
        </aside>

        <!-- Middle Main Feed (Directory Grid) -->
        <section class="md:col-span-6 space-y-6">
            <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
                <h1 class="text-xl font-bold text-slate-800">Lookup Regional Directories</h1>
                <p class="text-slate-500 text-xs mt-1">Enter a classmate's name or your high school graduation year to find matches.</p>
                
                <div class="mt-4 flex gap-2">
                    <input type="text" value="${query}" placeholder="Search name or year..." class="w-full px-4 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:border-blue-500">
                    <button class="bg-blue-600 text-white px-5 py-2 rounded-lg text-sm font-bold shadow-sm">Search</button>
                </div>
            </div>

            <!-- Profile Cards Grid -->
            <div class="space-y-3">
                <h3 class="font-bold text-slate-800 text-sm flex justify-between items-center px-1">
                    <span>Matched directory entries</span>
                    <span class="text-xs text-slate-400 font-normal">${profiles.size} results</span>
                </h3>
                <div class="grid grid-cols-1 gap-4">
${profilesSnippet}
                </div>
            </div>
        </section>

        <!-- Right Side Panel (Live Local Boards) -->
        <aside class="md:col-span-3 space-y-4">
            <div class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
                <h3 class="font-bold text-slate-800 text-xs tracking-wider uppercase mb-3 text-slate-400">Class Memory Board</h3>
                <div class="space-y-2">
${memoriesSnippet}
                </div>
            </div>
        </aside>

    </main>

</body>
</html>"""
}

@Composable
fun AppleDeviceSimulator(
    currentUser: CurrentUser?,
    profiles: List<FriendProfile>,
    memories: List<HometownMemory>,
    totalEarnings: Double,
    earningLogs: List<String>,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    onSearchClicked: () -> Unit,
    onJoinClicked: () -> Unit,
    onLogoutClicked: () -> Unit,
    onProfileSelected: (FriendProfile) -> Unit,
    onCashOutClicked: () -> Unit,
    onSponsorAdClicked: () -> Unit,
    onPostMemoryClicked: () -> Unit
) {
    val context = LocalContext.current
    val niceAvatarColors = listOf(
        Color(0xFF007AFF), // iOS System Blue
        Color(0xFF34C759), // iOS System Green
        Color(0xFFAF52DE), // iOS System Purple
        Color(0xFFFF9500), // iOS System Orange
        Color(0xFFFF2D55), // iOS System Pink
        Color(0xFF5856D6)  // iOS System Indigo
    )

    // iOS Device Outer Shell Frame
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)), // Deep luxury border
        border = BorderStroke(4.dp, Color(0xFF2C2C2E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF2F2F7)) // Standard iOS light grey grouped background
                .padding(vertical = 4.dp, horizontal = 4.dp)
        ) {
            // iOS Top Bezel: Clock, Dynamic Island, Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time
                Text(
                    text = "9:41",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                )

                // Dynamic Island cutout
                Box(
                    modifier = Modifier
                        .size(width = 96.dp, height = 22.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Small lens reflex reflection
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1B2E3C))
                        )
                    }
                }

                // Status Indicators: Wifi, Signal, Battery
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cell signal bars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Box(modifier = Modifier.size(width = 2.dp, height = 4.dp).background(Color.Black))
                        Box(modifier = Modifier.size(width = 2.dp, height = 6.dp).background(Color.Black))
                        Box(modifier = Modifier.size(width = 2.dp, height = 8.dp).background(Color.Black))
                        Box(modifier = Modifier.size(width = 2.dp, height = 10.dp).background(Color.Black))
                    }

                    // WiFi symbol indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📶", fontSize = 9.sp, color = Color.Black)
                    }

                    // iOS Style Battery
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 20.dp, height = 10.dp)
                                .border(1.dp, Color.Black, shape = RoundedCornerShape(2.dp))
                                .padding(1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.9f)
                                    .background(Color(0xFF34C759), shape = RoundedCornerShape(1.dp))
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(width = 1.5.dp, height = 4.dp)
                                .background(Color.Black, shape = RoundedCornerShape(1.dp))
                        )
                    }
                }
            }

            // iOS Header Web/App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.94f))
                    .border(BorderStroke(0.5.dp, Color(0xFFD1D1D6)))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Title or Logo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF007AFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("f", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FaceNote",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Right Profile Status
                if (currentUser != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = currentUser.name.substringBefore(" "),
                            color = Color(0xFF007AFF),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Log Out",
                            color = Color(0xFFFF3B30),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { onLogoutClicked() }
                        )
                    }
                } else {
                    Text(
                        text = "Sign In",
                        color = Color(0xFF007AFF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onJoinClicked() }
                    )
                }
            }

            // Inner Simulator Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(580.dp)
                    .background(Color(0xFFF2F2F7)),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // iOS Welcome Banner
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = " DESIGNED FOR IOS",
                            color = Color(0xFF8E8E93),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Classmates & Friends Portal",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Find high school pals, neighborhood buddies, and childhood connections on the premium iOS experience.",
                            color = Color(0xFF8E8E93),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Cupertino Ticker widget
                item {
                    val formattedEarnings = String.format(java.util.Locale.US, "$%.2f", totalEarnings)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "iOS Revenue",
                                        tint = Color(0xFFFFCC00),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Active Ad-Revenue Share",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF34C759))
                                    )
                                    Text(
                                        text = "iOS HIGH-RATE ACTIVE",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34C759)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Your Reconnection Fund Balance",
                                fontSize = 11.sp,
                                color = Color(0xFF8E8E93)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formattedEarnings,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34C759)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Apple outline button row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onCashOutClicked() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF).copy(alpha = 0.1f)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("Cash Out", color = Color(0xFF007AFF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { onSponsorAdClicked() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("Sponsor Buddy", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Apple Search Bar Input
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .background(Color(0xFF767680).copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFF3C3C43).copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search by classmate name...",
                                            color = Color(0xFF3C3C43).copy(alpha = 0.6f),
                                            fontSize = 14.sp
                                        )
                                    }
                                    androidx.compose.foundation.text.BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { onSearchChanged(it) },
                                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 14.sp),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }
                            }

                            Text(
                                text = "Search",
                                color = Color(0xFF007AFF),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable { onSearchClicked() }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                // IOS CLASSMATES LIST VIEW
                item {
                    Text(
                        text = "REGIONAL CLASS DIRECTORY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8E8E93),
                        modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 6.dp)
                    )
                }

                if (profiles.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No iOS classmate entries found",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                } else {
                    // Standard Apple Grouped Table Cell list representation
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            Column {
                                profiles.forEachIndexed { index, profile ->
                                    val loggedIn = currentUser != null
                                    val avatarClr = niceAvatarColors[profile.avatarColorSeed % niceAvatarColors.size]
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onProfileSelected(profile) }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // iOS style Squircle Avatar
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(avatarClr.copy(alpha = if (loggedIn) 1f else 0.4f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (loggedIn) {
                                                Text(
                                                    text = profile.name.take(1).uppercase(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Secured",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (loggedIn) profile.name else obfuscateSequence(profile.name),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${profile.hometown} • Class Status: ${profile.statusText}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF8E8E93)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (!loggedIn) {
                                                Text(
                                                    text = "Unlock",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF007AFF),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                            }
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowRight,
                                                contentDescription = "Details",
                                                tint = Color(0xFFC7C7CC),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    if (index < profiles.lastIndex) {
                                        Divider(
                                            modifier = Modifier.padding(start = 70.dp),
                                            color = Color(0xFFC6C6C8).copy(alpha = 0.4f),
                                            thickness = 0.5.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Apple Memory Board Section
                item {
                    Text(
                        text = "IOS NOSTALGIA BULLETIN BOARD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8E8E93),
                        modifier = Modifier.padding(start = 20.dp, top = 22.dp, bottom = 6.dp)
                    )
                }

                if (memories.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Text(
                                text = "Be the first to share old class gossip!",
                                fontSize = 12.sp,
                                color = Color(0xFF8E8E93),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            memories.forEach { memory ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF007AFF).copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Face,
                                                    contentDescription = null,
                                                    tint = Color(0xFF007AFF),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                            Text(
                                                text = memory.authorName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color.Black
                                            )
                                            Text(
                                                text = "•",
                                                color = Color(0xFF8E8E93),
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = memory.hometownLocation,
                                                color = Color(0xFF8E8E93),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "\"${memory.memoryText}\"",
                                            fontSize = 12.sp,
                                            color = Color(0xFF1C1C1E),
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Call to action button for memories
                if (currentUser != null) {
                    item {
                        Button(
                            onClick = { onPostMemoryClicked() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Old Memory Note", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // iOS Bottom Home Bar Indicator bezel space
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 124.dp, height = 5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                )
            }
        }
    }
}

