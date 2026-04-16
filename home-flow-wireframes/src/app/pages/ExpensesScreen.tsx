import { useNavigate } from "react-router";
import {
  WireAvatar,
  WireCard,
  WireChip,
  WireFAB,
  WireAnnotation,
  WireTopBar,
  WireDivider,
  WireButton,
} from "../components/WireComponents";

const EXPENSES = [
  {
    id: 1,
    emoji: "🍕",
    category: "Food",
    description: "Dinner at restaurant",
    payer: "AG",
    payerName: "Ana",
    amount: "$64.00",
    split: "÷ 2",
    date: "Apr 2",
    yours: "$32.00",
  },
  {
    id: 2,
    emoji: "🛒",
    category: "Supermarket",
    description: "Weekly groceries",
    payer: "CG",
    payerName: "Carlos",
    amount: "$89.50",
    split: "÷ 3",
    date: "Apr 1",
    yours: "$29.83",
  },
  {
    id: 3,
    emoji: "💡",
    category: "Services",
    description: "Electric bill — March",
    payer: "AG",
    payerName: "Ana",
    amount: "$120.00",
    split: "÷ 2",
    date: "Mar 31",
    yours: "$60.00",
  },
  {
    id: 4,
    emoji: "🧹",
    category: "Cleaning",
    description: "Cleaning supplies",
    payer: "LG",
    payerName: "Luis",
    amount: "$34.20",
    split: "÷ 3",
    date: "Mar 30",
    yours: "$11.40",
  },
  {
    id: 5,
    emoji: "🎬",
    category: "Entertainment",
    description: "Netflix subscription",
    payer: "AG",
    payerName: "Ana",
    amount: "$18.00",
    split: "÷ 3",
    date: "Mar 28",
    yours: "$6.00",
  },
];

export function ExpensesScreen() {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col flex-1 min-h-0 bg-[#F2F2F2] relative">
      {/* Top bar */}
      <WireTopBar
        title="Expenses"
        rightAction={
          <div className="flex items-center gap-2">
            <WireAnnotation label="menu" />
            <button className="text-[#666]">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="5" r="1" fill="currentColor" />
                <circle cx="12" cy="12" r="1" fill="currentColor" />
                <circle cx="12" cy="19" r="1" fill="currentColor" />
              </svg>
            </button>
          </div>
        }
      />

      {/* Balance summary cards */}
      <div className="bg-white px-4 py-3 border-b border-[#E8E8E8]">
        <div className="flex items-center justify-between mb-2">
          <span className="text-[10px] font-mono text-[#888] uppercase tracking-wider">April 2025 Balance</span>
          <button
            onClick={() => navigate("/expenses/balances")}
            className="text-[10px] font-mono text-[#555] underline"
          >
            See details →
          </button>
        </div>
        <div className="grid grid-cols-2 gap-2">
          <WireCard className="border-[#C8C8C8]">
            <div className="flex items-start gap-1.5">
              <div className="w-2 h-2 rounded-full bg-[#555] mt-1.5 shrink-0" />
              <div>
                <div className="text-[9px] font-mono text-[#888]">They owe you</div>
                <div className="text-[16px] font-mono font-medium text-[#333]">$120.00</div>
                <div className="text-[9px] font-mono text-[#ABABAB]">Carlos → you</div>
              </div>
            </div>
          </WireCard>
          <WireCard className="border-[#C8C8C8]">
            <div className="flex items-start gap-1.5">
              <div className="w-2 h-2 rounded-full bg-[#ABABAB] mt-1.5 shrink-0" />
              <div>
                <div className="text-[9px] font-mono text-[#888]">You owe</div>
                <div className="text-[16px] font-mono font-medium text-[#666]">$45.00</div>
                <div className="text-[9px] font-mono text-[#ABABAB]">You → Luis</div>
              </div>
            </div>
          </WireCard>
        </div>
        <WireAnnotation label="balance-cards" />
      </div>

      {/* Filter chips */}
      <div className="bg-white px-4 py-2 border-b border-[#E8E8E8]">
        <div className="flex gap-1.5 overflow-x-auto no-scrollbar pb-0.5">
          {["All", "Food", "Services", "Cleaning", "Entertainment", "April ▼"].map((chip, i) => (
            <WireChip key={chip} label={chip} active={i === 0} />
          ))}
        </div>
      </div>

      {/* Expense list */}
      <div className="flex-1 overflow-y-auto p-4 space-y-2 pb-24">
        <div className="flex items-center justify-between mb-1">
          <span className="text-[9px] font-mono text-[#999]">5 expenses · total $325.70</span>
          <WireAnnotation label="expense-list" />
        </div>

        {EXPENSES.map((exp) => (
          <WireCard key={exp.id}>
            <div className="flex items-start gap-3">
              {/* Category icon */}
              <div className="w-10 h-10 rounded-xl bg-[#F0F0F0] border border-[#E0E0E0] flex items-center justify-center text-lg shrink-0">
                {exp.emoji}
              </div>

              {/* Content */}
              <div className="flex-1 min-w-0">
                <div className="flex items-start justify-between">
                  <div className="min-w-0">
                    <div className="text-[12px] font-mono font-medium text-[#333] truncate">
                      {exp.description}
                    </div>
                    <div className="flex items-center gap-1.5 mt-0.5">
                      <WireAvatar size="xs" label={exp.payer} />
                      <span className="text-[9px] font-mono text-[#888]">
                        {exp.payerName} paid · {exp.date}
                      </span>
                    </div>
                  </div>
                  <div className="text-right shrink-0 ml-2">
                    <div className="text-[12px] font-mono font-medium text-[#333]">{exp.amount}</div>
                    <div className="text-[9px] font-mono text-[#ABABAB]">{exp.split}</div>
                  </div>
                </div>
                {/* Your share */}
                <div className="mt-1.5 flex items-center gap-1 pt-1.5 border-t border-[#F0F0F0]">
                  <span className="text-[9px] font-mono text-[#ABABAB]">Your share:</span>
                  <span className="text-[10px] font-mono font-medium text-[#666]">{exp.yours}</span>
                  <span className="text-[8px] font-mono px-1.5 py-0.5 bg-[#F5F5F5] border border-[#E8E8E8] rounded text-[#ABABAB] ml-auto">
                    {exp.category}
                  </span>
                </div>
              </div>
            </div>
          </WireCard>
        ))}

        {/* Balances section */}
        <WireDivider label="quick balances" />
        <WireCard>
          <div className="flex items-center justify-between mb-2">
            <span className="text-[10px] font-mono text-[#888] uppercase tracking-wider">Pending settlements</span>
            <WireAnnotation label="balances-section" />
          </div>
          <div className="space-y-2">
            {[
              { from: "CG", fromName: "Carlos", to: "AG", toName: "Ana", amount: "$120.00" },
              { from: "AG", fromName: "Ana", to: "LG", toName: "Luis", amount: "$45.00" },
            ].map((debt, i) => (
              <div key={i} className="flex items-center gap-2 py-1.5">
                <WireAvatar size="xs" label={debt.from} />
                <div className="flex-1">
                  <span className="text-[10px] font-mono text-[#444]">
                    {debt.fromName} → {debt.toName}
                  </span>
                </div>
                <span className="text-[11px] font-mono font-medium text-[#333]">{debt.amount}</span>
                <WireButton variant="ghost" size="sm" onClick={() => navigate("/expenses/balances")}>
                  Pay
                </WireButton>
              </div>
            ))}
          </div>
        </WireCard>
      </div>

      {/* FAB */}
      <WireFAB onClick={() => navigate("/expenses/create")} />
    </div>
  );
}