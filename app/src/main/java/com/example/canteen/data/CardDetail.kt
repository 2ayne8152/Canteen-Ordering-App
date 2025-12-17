package com.example.canteen.data

//@Entity
data class CardDetail(
    //@PrimaryKey(autoGenerate = true) val id: Int = 0,
    val maskedCard: String,   // **** **** **** 4242
    val expiry: String,
    val CVV: String
)