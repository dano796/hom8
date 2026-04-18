package com.hom8.app.presentation.expenses.payment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.hom8.app.R
import java.text.NumberFormat
import java.util.Locale

class RecordPaymentDialog : DialogFragment() {

    private var onPaymentConfirmed: ((Double, String) -> Unit)? = null
    private val currencyFmt = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    companion object {
        private const val ARG_FROM_NAME = "from_name"
        private const val ARG_TO_NAME = "to_name"
        private const val ARG_AMOUNT = "amount"

        fun newInstance(
            fromName: String,
            toName: String,
            amount: Double,
            onConfirmed: (Double, String) -> Unit
        ): RecordPaymentDialog {
            return RecordPaymentDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_FROM_NAME, fromName)
                    putString(ARG_TO_NAME, toName)
                    putDouble(ARG_AMOUNT, amount)
                }
                onPaymentConfirmed = onConfirmed
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_record_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fromName = arguments?.getString(ARG_FROM_NAME) ?: ""
        val toName = arguments?.getString(ARG_TO_NAME) ?: ""
        val amount = arguments?.getDouble(ARG_AMOUNT) ?: 0.0

        val tvTitle = view.findViewById<TextView>(R.id.tvPaymentTitle)
        val tvDescription = view.findViewById<TextView>(R.id.tvPaymentDescription)
        val etAmount = view.findViewById<TextInputEditText>(R.id.etPaymentAmount)
        val etNote = view.findViewById<TextInputEditText>(R.id.etPaymentNote)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelPayment)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btnConfirmPayment)

        tvTitle.text = "Registrar pago"
        tvDescription.text = "$fromName pagará a $toName"
        etAmount.setText(amount.toString())

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnConfirm.setOnClickListener {
            val amountText = etAmount.text.toString().trim()
            val note = etNote.text.toString().trim()
            
            val parsedAmount = amountText.toDoubleOrNull()
            if (parsedAmount == null || parsedAmount <= 0) {
                etAmount.error = "Ingresa un monto válido"
                return@setOnClickListener
            }

            onPaymentConfirmed?.invoke(parsedAmount, note)
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }
}
