import { useNavigate } from "react-router";
import {
  WireAvatar,
  WireCard,
  WireButton,
  WireAnnotation,
  WireTopBar,
  WireDivider,
} from "../components/WireComponents";

const DEBTS = [
  { from: "CG", fromName: "Carlos García", to: "AG", toName: "Ana García", amount: "$120.00", date: "Apr 2" },
  { from: "AG", fromName: "Ana García", to: "LG", toName: "Luis García", amount: "$45.00", date: "Mar 31" },
];

const PAYMENTS_HISTORY = [
  { from: "LG", fromName: "Luis", to: "AG", toName: "Ana", amount: "$30.00", date: "Mar 25" },
  { from: "CG", fromName: "Carlos", to: "LG", toName: "Luis", amount: "$15.00", date: "Mar 18" },
];

export function BalancesScreen() {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col flex-1 min-h-0 bg-[#F2F2F2]">
      <WireTopBar
        title="Balances"
        showBack
        onBack={() => navigate("/expenses")}
      />

      <div className="flex-1 overflow-y-auto p-4 space-y-4 pb-6">
        {/* Summary header */}
        <WireCard>
          <div className="flex items-center justify-between mb-1">
            <span className="text-[10px] font-mono text-[#888] uppercase tracking-wider">Net position</span>
            <WireAnnotation label="balance-summary" />
          </div>
          <div className="text-[20px] font-mono font-medium text-[#333]">+ $75.00</div>
          <div className="text-[10px] font-mono text-[#ABABAB]">You are owed more than you owe</div>
          <div className="h-px bg-[#E8E8E8] my-2" />
          <div className="grid grid-cols-2 gap-2 text-center">
            <div>
              <div className="text-[9px] font-mono text-[#999]">Owed to you</div>
              <div className="text-[14px] font-mono text-[#444]">$120.00</div>
            </div>
            <div>
              <div className="text-[9px] font-mono text-[#999]">You owe</div>
              <div className="text-[14px] font-mono text-[#777]">$45.00</div>
            </div>
          </div>
        </WireCard>

        {/* Pending debts */}
        <div>
          <div className="flex items-center gap-2 mb-2">
            <span className="text-[11px] font-mono font-medium text-[#555] uppercase tracking-wider">
              Pending settlements
            </span>
            <WireAnnotation label="debt-list" />
          </div>
          <div className="space-y-2">
            {DEBTS.map((debt, i) => (
              <WireCard key={i}>
                <div className="flex items-center gap-3">
                  {/* From */}
                  <div className="flex flex-col items-center gap-0.5">
                    <WireAvatar size="sm" label={debt.from} />
                    <span className="text-[8px] font-mono text-[#AAA]">{debt.fromName.split(" ")[0]}</span>
                  </div>

                  {/* Arrow */}
                  <div className="flex-1 flex flex-col items-center gap-0.5">
                    <span className="text-[13px] font-mono font-medium text-[#333]">{debt.amount}</span>
                    <div className="flex items-center gap-1 w-full">
                      <div className="flex-1 h-px bg-[#C8C8C8]" />
                      <svg width="10" height="10" viewBox="0 0 10 10" fill="none">
                        <path d="M3 1l4 4-4 4" stroke="#888" strokeWidth="1.2" strokeLinecap="round" />
                      </svg>
                    </div>
                    <span className="text-[8px] font-mono text-[#ABABAB]">since {debt.date}</span>
                  </div>

                  {/* To */}
                  <div className="flex flex-col items-center gap-0.5">
                    <WireAvatar size="sm" label={debt.to} />
                    <span className="text-[8px] font-mono text-[#AAA]">{debt.toName.split(" ")[0]}</span>
                  </div>

                  {/* Action */}
                  <WireButton variant="ghost" size="sm">
                    Pay
                  </WireButton>
                </div>

                {/* Confirm payment modal hint */}
                <div className="mt-2 text-[8px] font-mono text-[#ABABAB] bg-[#F8F8F8] border border-dashed border-[#E0E0E0] px-2 py-1 rounded">
                  Tap "Pay" → bottom sheet: amount + date + note → confirm
                </div>
              </WireCard>
            ))}
          </div>
        </div>

        <WireDivider label="payment history" />

        {/* Payment history */}
        <div>
          <div className="flex items-center gap-2 mb-2">
            <span className="text-[11px] font-mono font-medium text-[#555] uppercase tracking-wider">
              Settled payments
            </span>
            <WireAnnotation label="payment-history" />
          </div>
          <div className="space-y-2">
            {PAYMENTS_HISTORY.map((p, i) => (
              <div key={i} className="flex items-center gap-2 bg-white border border-[#E8E8E8] rounded-xl px-3 py-2">
                <WireAvatar size="xs" label={p.from} />
                <span className="text-[10px] font-mono text-[#666] flex-1">
                  {p.fromName} paid {p.toName}
                </span>
                <span className="text-[11px] font-mono text-[#555]">{p.amount}</span>
                <span className="text-[9px] font-mono text-[#ABABAB]">{p.date}</span>
                <span className="text-[8px] font-mono px-1.5 py-0.5 bg-[#F0F0F0] border border-[#E0E0E0] rounded text-[#ABABAB]">
                  ✓ Settled
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* All settled state hint */}
        <WireCard className="border-dashed border-[#D0D0D0]">
          <div className="text-center py-2 space-y-1">
            <div className="text-lg">🎉</div>
            <div className="text-[10px] font-mono text-[#888]">
              When all debts are settled, this shows:
            </div>
            <div className="text-[11px] font-mono font-medium text-[#555]">
              "Everyone is up to date!"
            </div>
            <WireAnnotation label="empty-state: balance-zero" />
          </div>
        </WireCard>
      </div>
    </div>
  );
}