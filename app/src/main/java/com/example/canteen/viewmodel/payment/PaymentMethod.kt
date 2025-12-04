package com.example.canteen.viewmodel.payment

//@Entity
data class PaymentMethod(
    //@PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val maskedCard: String,   // **** **** **** 4242
    val expiry: String,
    val tngPhone: String?     // Optional
)

// Store in room rather than database