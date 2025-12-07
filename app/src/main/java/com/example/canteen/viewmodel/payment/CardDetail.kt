package com.example.canteen.viewmodel.payment

//@Entity
data class CardDetail(
    //@PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val maskedCard: String,   // **** **** **** 4242
    val expiry: String,
    val CVV: String
)

// Store in room rather than database