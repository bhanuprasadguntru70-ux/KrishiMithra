package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.TransactionEntity
import com.example.ui.AgriViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    viewModel: AgriViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val transactions by viewModel.userTransactions.collectAsState()
    val scope = rememberCoroutineScope()

    var showRazorpaySheet by remember { mutableStateOf(false) }
    var selectedPlan by remember { mutableStateOf<PremiumPlan?>(null) }
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Plans, 1 = Invoice History

    // Payment result state
    var showResultScreen by remember { mutableStateOf(false) }
    var paymentSuccess by remember { mutableStateOf(true) }
    var lastTransactionDetails by remember { mutableStateOf<TransactionDetails?>(null) }

    val plans = listOf(
        PremiumPlan(
            name = "Monthly Premium",
            price = 99.0,
            period = "Month",
            description = "Perfect for testing advanced recommendations and yield analysis.",
            features = listOf(
                "Unlimited Gemini Crop Yield Forecasts",
                "Advanced AI Disease Treatment recommendations",
                "Ad-free Chatbot interface",
                "Premium Weather & price fluctuation alerts"
            ),
            icon = Icons.Default.Spa,
            tag = null,
            gradient = Brush.linearGradient(listOf(Color(0xFF2E7D32), Color(0xFF4CAF50)))
        ),
        PremiumPlan(
            name = "Yearly Premium",
            price = 999.0,
            period = "Year",
            description = "Most popular choice for active farmers seeking maximum season yield.",
            features = listOf(
                "All Monthly features",
                "Advanced Soil Moisture trend tracking",
                "Priority response from Agri AI Experts",
                "Direct connection to regional support guides",
                "Save up to 15% with annual pricing"
            ),
            icon = Icons.Default.Star,
            tag = "MOST POPULAR",
            gradient = Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF1E88E5)))
        ),
        PremiumPlan(
            name = "Lifetime Pro",
            price = 4999.0,
            period = "Lifetime",
            description = "Ultimate investment for generations of crop planning and farm wealth.",
            features = listOf(
                "Unlimited everything forever",
                "Exclusive access to regional pre-harvest price predictions",
                "Beta access to experimental AI agronomy models",
                "VIP Customer Support line 24/7",
                "One-time payment"
            ),
            icon = Icons.Default.WorkspacePremium,
            tag = "BEST VALUE",
            gradient = Brush.linearGradient(listOf(Color(0xFFFF8F00), Color(0xFFFFB300)))
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium Subscription", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Segmented tabs for Plan / Invoice History
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Subscription Plans", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.CardMembership, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Payment History", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.History, contentDescription = null) }
                )
            }

            if (activeTab == 0) {
                // Subscription Plans Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Premium Promo Header Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = if (currentUser?.isPremium == true) "You are a Premium Member! 🎉" else "Unlock Advanced AI Features",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = if (currentUser?.isPremium == true) "Active Plan: ${currentUser?.subscriptionPlan}" else "Get access to precision AI tools for higher yield.",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    items(plans) { plan ->
                        PlanCard(
                            plan = plan,
                            isCurrentPlan = currentUser?.isPremium == true && currentUser?.subscriptionPlan == plan.name,
                            onSubscribeClick = {
                                selectedPlan = plan
                                showRazorpaySheet = true
                            }
                        )
                    }

                    if (currentUser?.isPremium == true) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { viewModel.cancelSubscription() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel Active Subscription")
                            }
                        }
                    }
                }
            } else {
                // Payment History Tab
                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Payments Found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Your subscription and payment invoice receipts will appear here.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(transactions) { tx ->
                            TransactionItemCard(transaction = tx)
                        }
                    }
                }
            }
        }

        // Razorpay Payment Simulation Sheet
        if (showRazorpaySheet && selectedPlan != null) {
            RazorpaySimulationSheet(
                planName = selectedPlan!!.name,
                amount = selectedPlan!!.price,
                billingName = currentUser?.name ?: "Valued Farmer",
                billingPhone = currentUser?.phone ?: "9999999999",
                onDismiss = { showRazorpaySheet = false },
                onPaymentComplete = { paymentSuccessResult, details ->
                    showRazorpaySheet = false
                    paymentSuccess = paymentSuccessResult
                    lastTransactionDetails = details
                    
                    // Dispatch updates to ViewModel
                    viewModel.subscribeUser(
                        planName = details.planName,
                        amount = details.amount,
                        paymentMethod = details.paymentMethod,
                        orderId = details.orderId,
                        status = if (paymentSuccessResult) "SUCCESS" else "FAILED"
                    )
                    
                    showResultScreen = true
                }
            )
        }

        // Success / Failure Overlay Dialog Screen
        if (showResultScreen && lastTransactionDetails != null) {
            PaymentResultDialog(
                success = paymentSuccess,
                details = lastTransactionDetails!!,
                onDismiss = {
                    showResultScreen = false
                    lastTransactionDetails = null
                }
            )
        }
    }
}

@Composable
fun PlanCard(
    plan: PremiumPlan,
    isCurrentPlan: Boolean,
    onSubscribeClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = if (isCurrentPlan) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Color band
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(plan.gradient)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = plan.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = plan.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    if (plan.tag != null) {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(50.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = plan.tag,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                // Price text
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "₹${plan.price.toInt()}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " / ${plan.period}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = plan.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))

                // Feature items list
                plan.features.forEach { feature ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = feature,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Subscribe Button or Active Indicator
                if (isCurrentPlan) {
                    Button(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            disabledContentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Active Plan", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = onSubscribeClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("subscribe_button_${plan.name.replace(" ", "_").lowercase()}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Get Started", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemCard(transaction: TransactionEntity) {
    var showInvoiceDialog by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(transaction.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (transaction.status == "SUCCESS") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (transaction.status == "SUCCESS") Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (transaction.status == "SUCCESS") Color(0xFF388E3C) else Color(0xFFD32F2F)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.planName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ref: ${transaction.orderId.take(12)}...",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${transaction.amount.toInt()}",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = transaction.paymentMethod,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Invoice",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { showInvoiceDialog = true }
                        .padding(vertical = 2.dp)
                )
            }
        }
    }

    if (showInvoiceDialog) {
        InvoiceDetailsDialog(transaction = transaction, onDismiss = { showInvoiceDialog = false })
    }
}

@Composable
fun InvoiceDetailsDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = "Agri AI Tax Invoice",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                // Billing details
                InvoiceRow(label = "Invoice No", value = "INV-${transaction.id}-${transaction.timestamp.toString().takeLast(6)}")
                InvoiceRow(label = "Date & Time", value = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(transaction.timestamp)))
                InvoiceRow(label = "Account", value = transaction.userEmail)
                InvoiceRow(label = "Plan Purchased", value = transaction.planName)
                InvoiceRow(label = "Reference ID", value = transaction.orderId)
                InvoiceRow(label = "Payment Gateway", value = "Razorpay Standard")
                InvoiceRow(label = "Payment Mode", value = transaction.paymentMethod)

                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                // Price details
                val sgst = transaction.amount * 0.09
                val cgst = transaction.amount * 0.09
                val base = transaction.amount - sgst - cgst

                InvoiceRow(label = "Subtotal (Excl. Tax)", value = "₹${"%.2f".format(base)}")
                InvoiceRow(label = "CGST @ 9%", value = "₹${"%.2f".format(cgst)}")
                InvoiceRow(label = "SGST @ 9%", value = "₹${"%.2f".format(sgst)}")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Amount Paid", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
                        Text("₹${transaction.amount.toInt()}.00", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                    }
                }

                Text(
                    text = "This is an electronically generated receipt. No physical signature is required.",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
fun InvoiceRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

// RAZORPAY NATIVE PAYMENT SHEET SIMULATOR
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RazorpaySimulationSheet(
    planName: String,
    amount: Double,
    billingName: String,
    billingPhone: String,
    onDismiss: () -> Unit,
    onPaymentComplete: (Boolean, TransactionDetails) -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1 = Main, 2 = Card Details, 3 = UPI, 4 = Net Banking, 5 = Wallets, 6 = OTP verification
    var selectedMethod by remember { mutableStateOf("") }
    var processing by remember { mutableStateOf(false) }

    // Card state
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }

    // UPI State
    var upiId by remember { mutableStateOf("") }

    // OTP State
    var otpCode by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color.White)
                    .padding(vertical = 20.dp, horizontal = 24.dp)
                    .navigationBarsPadding()
            ) {
                // Razorpay Branded Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ImageVectorIcon(
                            icon = Icons.Default.Payments,
                            tint = Color(0xFF0F2C59),
                            size = 28
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "razorpay",
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F2C59),
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Secure Checkout",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${amount.toInt()}.00",
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F2C59),
                            fontSize = 20.sp
                        )
                        Text(
                            text = planName,
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                if (processing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF0F2C59))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Processing payment with banks...",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F2C59),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Please do not press back or refresh.",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    when (step) {
                        1 -> { // Selection Menu
                            Text("SELECT PAYMENT METHOD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(10.dp))

                            MethodRow(
                                title = "Cards (Visa, MasterCard, RuPay)",
                                subtitle = "Pay using Credit or Debit cards",
                                icon = Icons.Default.CreditCard,
                                onClick = { step = 2; selectedMethod = "Card" }
                            )

                            MethodRow(
                                title = "UPI (Google Pay, PhonePe, Paytm)",
                                subtitle = "Instant checkout using any UPI App",
                                icon = Icons.Default.QrCode,
                                onClick = { step = 3; selectedMethod = "UPI" }
                            )

                            MethodRow(
                                title = "Net Banking",
                                subtitle = "Pay via popular Indian banks",
                                icon = Icons.Default.AccountBalance,
                                onClick = { step = 4; selectedMethod = "Net Banking" }
                            )

                            MethodRow(
                                title = "Wallets",
                                subtitle = "Paytm, PhonePe, and Mobikwik",
                                icon = Icons.Default.AccountBalanceWallet,
                                onClick = { step = 5; selectedMethod = "Wallet" }
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel Payment")
                            }
                        }

                        2 -> { // Card Details
                            Text("ENTER CARD DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = { if (it.length <= 16 && it.all { c -> c.isDigit() }) cardNumber = it },
                                label = { Text("Card Number") },
                                leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.Gray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = cardExpiry,
                                    onValueChange = { if (it.length <= 5) cardExpiry = it },
                                    label = { Text("Expiry (MM/YY)") },
                                    placeholder = { Text("12/29") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = cardCvv,
                                    onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) cardCvv = it },
                                    label = { Text("CVV") },
                                    placeholder = { Text("123") },
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = cardName,
                                onValueChange = { cardName = it },
                                label = { Text("Cardholder Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = { step = 1 },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Back")
                                }

                                Button(
                                    onClick = {
                                        if (cardNumber.length == 16 && cardCvv.length == 3 && cardName.isNotBlank()) {
                                            step = 6 // OTP Screen
                                        }
                                    },
                                    enabled = cardNumber.length == 16 && cardCvv.length == 3 && cardName.isNotBlank(),
                                    modifier = Modifier.weight(2f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F2C59))
                                ) {
                                    Text("Pay ₹${amount.toInt()}", color = Color.White)
                                }
                            }
                        }

                        3 -> { // UPI Selection & Input
                            Text("PAY VIA UPI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                UpiAppBadge(name = "Google Pay", icon = Icons.Default.Payments, selected = upiId == "gpay@ybl") { upiId = "gpay@ybl" }
                                UpiAppBadge(name = "PhonePe", icon = Icons.Default.Smartphone, selected = upiId == "phonepe@ybl") { upiId = "phonepe@ybl" }
                                UpiAppBadge(name = "Paytm", icon = Icons.Default.AccountBalanceWallet, selected = upiId == "paytm@upi") { upiId = "paytm@upi" }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("OR ENTER UPI ID", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = upiId,
                                onValueChange = { upiId = it },
                                label = { Text("Enter UPI ID (e.g. username@upi)") },
                                placeholder = { Text("username@ybl") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = { step = 1 },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Back")
                                }

                                Button(
                                    onClick = {
                                        if (upiId.contains("@")) {
                                            processing = true
                                            scope.launch {
                                                delay(2000)
                                                processing = false
                                                onPaymentComplete(
                                                    true,
                                                    TransactionDetails(
                                                        planName = planName,
                                                        amount = amount,
                                                        paymentMethod = "UPI ($upiId)",
                                                        orderId = "pay_" + UUID.randomUUID().toString().replace("-", "").take(14)
                                                    )
                                                )
                                            }
                                        }
                                    },
                                    enabled = upiId.contains("@"),
                                    modifier = Modifier.weight(2f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F2C59))
                                ) {
                                    Text("Pay securely via UPI", color = Color.White)
                                }
                            }
                        }

                        4 -> { // Net Banking
                            Text("POPULAR INDIAN BANKS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(12.dp))

                            val banks = listOf(
                                "State Bank of India",
                                "HDFC Bank",
                                "ICICI Bank",
                                "Axis Bank",
                                "Punjab National Bank",
                                "Kotak Mahindra Bank"
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                banks.forEach { bank ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                processing = true
                                                scope.launch {
                                                    delay(2500)
                                                    processing = false
                                                    onPaymentComplete(
                                                        true,
                                                        TransactionDetails(
                                                            planName = planName,
                                                            amount = amount,
                                                            paymentMethod = "Net Banking ($bank)",
                                                            orderId = "pay_" + UUID.randomUUID().toString().replace("-", "").take(14)
                                                        )
                                                    )
                                                }
                                            }
                                            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(bank, fontWeight = FontWeight.Medium, color = Color(0xFF0F2C59))
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { step = 1 },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Back")
                            }
                        }

                        5 -> { // Wallets
                            Text("POPULAR WALLETS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(12.dp))

                            val wallets = listOf(
                                "Paytm Wallet",
                                "PhonePe Wallet",
                                "Mobikwik",
                                "Amazon Pay Balance"
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                wallets.forEach { wallet ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                processing = true
                                                scope.launch {
                                                    delay(2000)
                                                    processing = false
                                                    onPaymentComplete(
                                                        true,
                                                        TransactionDetails(
                                                            planName = planName,
                                                            amount = amount,
                                                            paymentMethod = wallet,
                                                            orderId = "pay_" + UUID.randomUUID().toString().replace("-", "").take(14)
                                                        )
                                                    )
                                                }
                                            }
                                            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(wallet, fontWeight = FontWeight.Medium, color = Color(0xFF0F2C59))
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { step = 1 },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Back")
                            }
                        }

                        6 -> { // OTP screen for Card Verification
                            Text("SECURE 3D PAYMENT VERIFICATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(12.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                border = BorderStroke(1.dp, Color(0xFFFFB74D))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFE65100))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "A simulated OTP has been sent to your mobile registered with the card ending in ${cardNumber.takeLast(4)}.",
                                        fontSize = 12.sp,
                                        color = Color(0xFFE65100)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otpCode = it },
                                label = { Text("Enter 6-Digit OTP") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        processing = true
                                        scope.launch {
                                            delay(1500)
                                            processing = false
                                            onPaymentComplete(
                                                false, // Simulated Failure
                                                TransactionDetails(
                                                    planName = planName,
                                                    amount = amount,
                                                    paymentMethod = "Credit Card",
                                                    orderId = "pay_" + UUID.randomUUID().toString().replace("-", "").take(14)
                                                )
                                            )
                                        }
                                    },
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                                ) {
                                    Text("Simulate Fail", color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        if (otpCode.length >= 4) {
                                            processing = true
                                            scope.launch {
                                                delay(2500)
                                                processing = false
                                                onPaymentComplete(
                                                    true, // Success
                                                    TransactionDetails(
                                                        planName = planName,
                                                        amount = amount,
                                                        paymentMethod = "Credit Card",
                                                        orderId = "pay_" + UUID.randomUUID().toString().replace("-", "").take(14)
                                                    )
                                                )
                                            }
                                        }
                                    },
                                    enabled = otpCode.length >= 4,
                                    modifier = Modifier.weight(2f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                ) {
                                    Text("Verify & Pay", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MethodRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFF0F4F8), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF0F2C59))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF0F2C59), fontSize = 14.sp)
            Text(subtitle, color = Color.Gray, fontSize = 11.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
fun RowScope.UpiAppBadge(
    name: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .border(
                1.dp,
                if (selected) Color(0xFF0F2C59) else Color.LightGray.copy(alpha = 0.6f),
                RoundedCornerShape(12.dp)
            )
            .background(if (selected) Color(0xFF0F2C59).copy(alpha = 0.05f) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color(0xFF0F2C59) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (selected) Color(0xFF0F2C59) else Color.Gray)
        }
    }
}

// SUCCESS / FAILURE MODAL
@Composable
fun PaymentResultDialog(
    success: Boolean,
    details: TransactionDetails,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            if (success) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (success) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.size(48.dp)
                    )
                }

                Text(
                    text = if (success) "Payment Successful! 🎉" else "Payment Failed! ❌",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = if (success) Color(0xFF2E7D32) else Color(0xFFC62828),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (success) {
                        "Your account has been upgraded to Premium. All smart AI crop features are now fully unlocked."
                    } else {
                        "There was a problem processing your payment transaction. Please try again or use a different payment method."
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                // Detail columns
                InvoiceRow(label = "Amount Paid", value = "₹${details.amount.toInt()}")
                InvoiceRow(label = "Reference Order ID", value = details.orderId)
                InvoiceRow(label = "Payment Gateway", value = "Razorpay Secure")
                InvoiceRow(label = "Selected Plan", value = details.planName)
                InvoiceRow(label = "Method", value = details.paymentMethod)

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (success) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                ) {
                    Text(if (success) "Start Exploring Pro features" else "Try Again", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// Dynamic Helpers
@Composable
fun ImageVectorIcon(icon: ImageVector, tint: Color, size: Int) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(size.dp)
    )
}

data class PremiumPlan(
    val name: String,
    val price: Double,
    val period: String,
    val description: String,
    val features: List<String>,
    val icon: ImageVector,
    val tag: String?,
    val gradient: Brush
)

data class TransactionDetails(
    val planName: String,
    val amount: Double,
    val paymentMethod: String,
    val orderId: String
)
