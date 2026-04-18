package com.hom8.app.presentation.expenses.balances

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.hom8.app.R
import com.hom8.app.presentation.expenses.payment.RecordPaymentDialog
import com.hom8.app.util.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class BalancesFragment : Fragment() {

    private val viewModel: BalancesViewModel by viewModels()

    @Inject
    lateinit var session: SessionManager
    private val currencyFmt = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    private val dateFmt = SimpleDateFormat("MMM d, yyyy", Locale("es", "ES"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_balances, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    bindState(view, state)
                }
            }
        }
    }

    private fun bindState(view: View, state: BalancesUiState) {
        val tvNet = view.findViewById<TextView>(R.id.tvNetAmount)
        val tvNetLabel = view.findViewById<TextView>(R.id.tvNetLabel)
        val tvTheyOwe = view.findViewById<TextView>(R.id.tvTheyOweTotal)
        val tvYouOwe = view.findViewById<TextView>(R.id.tvYouOweTotal)
        val layoutPending = view.findViewById<LinearLayout>(R.id.layoutPendingSettlements)
        val layoutSettled = view.findViewById<LinearLayout>(R.id.layoutSettledPayments)
        val cardAllSettled = view.findViewById<MaterialCardView>(R.id.cardAllSettled)

        // Net position
        val netColor = if (state.netAmount >= 0) {
            ContextCompat.getColor(requireContext(), R.color.balancePositive)
        } else {
            ContextCompat.getColor(requireContext(), R.color.balanceNegative)
        }
        tvNet.text = if (state.netAmount >= 0) {
            "+${currencyFmt.format(state.netAmount)}"
        } else {
            currencyFmt.format(state.netAmount)
        }
        tvNet.setTextColor(netColor)
        tvNetLabel.text = when {
            state.netAmount > 0 -> "En general, te deben dinero"
            state.netAmount < 0 -> "En general, debes dinero"
            else -> "Estás al día"
        }

        tvTheyOwe.text = currencyFmt.format(state.theyOweAmount)
        tvYouOwe.text = currencyFmt.format(state.youOweAmount)

        // Pending debts
        layoutPending.removeAllViews()
        if (state.pendingDebts.isEmpty()) {
            cardAllSettled.visibility = View.VISIBLE
        } else {
            cardAllSettled.visibility = View.GONE
            state.pendingDebts.forEach { debt ->
                layoutPending.addView(buildDebtView(debt))
            }
        }

        // Settled payments
        layoutSettled.removeAllViews()
        if (state.settledPayments.isEmpty()) {
            val emptyTv = TextView(requireContext()).apply {
                typeface = ResourcesCompat.getFont(requireContext(), R.font.jetbrains_mono)
                text = "Aún no hay pagos registrados"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextTertiary))
                textSize = 12f
                setPadding(0, 16, 0, 16)
            }
            layoutSettled.addView(emptyTv)
        } else {
            state.settledPayments.take(10).forEach { payment ->
                layoutSettled.addView(buildSettledView(payment))
            }
        }
    }

    private fun buildDebtView(debt: DebtItem): View {
        val context = requireContext()
        val card = MaterialCardView(context).apply {
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.bottomMargin = resources.getDimensionPixelSize(R.dimen.spacing2)
            layoutParams = lp
            radius = resources.getDimension(R.dimen.cardCorner)
            strokeColor = ContextCompat.getColor(context, R.color.colorOutlineVariant)
            strokeWidth = 1
            cardElevation = 2f
        }

        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(
                resources.getDimensionPixelSize(R.dimen.spacing3),
                resources.getDimensionPixelSize(R.dimen.spacing3),
                resources.getDimensionPixelSize(R.dimen.spacing3),
                resources.getDimensionPixelSize(R.dimen.spacing3)
            )
        }

        val tvDebtDesc = TextView(context).apply {
            typeface = ResourcesCompat.getFont(context, R.font.jetbrains_mono)
            val arrow = if (debt.isYouDebtor) "→" else "←"
            text = "${debt.fromName} $arrow ${debt.toName}: ${currencyFmt.format(debt.amount)}"
            setTextColor(ContextCompat.getColor(context, R.color.colorTextPrimary))
            textSize = 13f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        row.addView(tvDebtDesc)

        if (debt.isYouDebtor) {
            val btnPay = MaterialButton(context, null, com.google.android.material.R.attr.materialButtonStyle).apply {
                text = getString(R.string.expenses_pay)
                setTextColor(ContextCompat.getColor(context, R.color.colorOnPrimary))
                backgroundTintList = android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.colorPrimary)
                )
                textSize = 11f
                cornerRadius = 8
                setPadding(24, 8, 24, 8)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener { 
                    showPaymentDialog(debt)
                }
            }
            row.addView(btnPay)
        }

        card.addView(row)
        return card
    }

    private fun buildSettledView(payment: com.hom8.app.data.local.entity.PaymentEntity): View {
        val context = requireContext()

        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.bottomMargin = resources.getDimensionPixelSize(R.dimen.spacing2)
            layoutParams = lp
            setPadding(
                resources.getDimensionPixelSize(R.dimen.spacing2),
                resources.getDimensionPixelSize(R.dimen.spacing2),
                resources.getDimensionPixelSize(R.dimen.spacing2),
                resources.getDimensionPixelSize(R.dimen.spacing2)
            )
        }

        val tvSettledDesc = TextView(context).apply {
            typeface = ResourcesCompat.getFont(context, R.font.jetbrains_mono)
            val dir = if (payment.fromUserId == session.userId) "Pagaste" else "Recibiste"
            text = "$dir ${currencyFmt.format(payment.monto)} · ${dateFmt.format(Date(payment.fecha))}"
            setTextColor(ContextCompat.getColor(context, R.color.colorTextSecondary))
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        row.addView(tvSettledDesc)

        val tvSettledBadge = TextView(context).apply {
            typeface = ResourcesCompat.getFont(context, R.font.jetbrains_mono)
            text = getString(R.string.expenses_settled)
            setTextColor(ContextCompat.getColor(context, R.color.colorTertiary))
            textSize = 10f
            setTypeface(typeface, Typeface.BOLD)
        }
        row.addView(tvSettledBadge)

        return row
    }

    private fun showPaymentDialog(debt: DebtItem) {
        RecordPaymentDialog.newInstance(
            fromName = debt.fromName,
            toName = debt.toName,
            amount = debt.amount
        ) { amount, note ->
            viewModel.recordPayment(debt, amount, note)
        }.show(childFragmentManager, "RecordPaymentDialog")
    }
}
