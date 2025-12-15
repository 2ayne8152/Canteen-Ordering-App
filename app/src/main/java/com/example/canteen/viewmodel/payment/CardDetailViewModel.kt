package com.example.canteen.viewmodel.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canteen.data.CardDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CardDetailViewModel : ViewModel() {
    // Holds the saved card (only ONE allowed)
    private val _savedCard = MutableStateFlow<CardDetail?>(null)
    val savedCard: StateFlow<CardDetail?> = _savedCard.asStateFlow()

    // Check if a card is already stored
    val hasSavedCard: Boolean
        get() = _savedCard.value != null

    /**
     * Save a new card (only one allowed)
     * If a card already exists, this will overwrite it unless you block it in UI.
     */
    fun saveCard(card: CardDetail) {
        viewModelScope.launch {
            _savedCard.value = card
        }
    }

    /**
     * Delete the saved card.
     */
    fun deleteCard() {
        viewModelScope.launch {
            _savedCard.value = null
        }
    }

    /**
     * Optional: Validate card format before saving.
     */
    fun isValidCard(cardNumber: String, expiry: String, cvv: String): Boolean {
        val cleanCard = cardNumber.replace(" ", "")

        return cleanCard.length in 13..19 &&
                expiry.matches(Regex("""^(0[1-9]|1[0-2])/\d{2}$""")) &&
                cvv.length in 3..4
    }

}
