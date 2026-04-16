import { useState } from "react";
import { useNavigate } from "react-router";
import {
  WireAvatar,
  WireInput,
  WireTextarea,
  WireButton,
  WireSegmented,
  WireAnnotation,
  WireTopBar,
  WireDivider,
  WireCategoryBox,
  WireCheckbox,
} from "../components/WireComponents";

const CATEGORIES = [
  { emoji: "🍕", label: "Food" },
  { emoji: "🛒", label: "Supermarket" },
  { emoji: "💡", label: "Services" },
  { emoji: "🧹", label: "Cleaning" },
  { emoji: "🚗", label: "Transport" },
  { emoji: "🎬", label: "Entertain." },
  { emoji: "💊", label: "Health" },
  { emoji: "📦", label: "Other" },
];

const MEMBERS = [
  { label: "AG", name: "Ana García" },
  { label: "CG", name: "Carlos G." },
  { label: "LG", name: "Luis G." },
];

export function CreateExpenseScreen() {
  const navigate = useNavigate();
  const [category, setCategory] = useState(0);
  const [payer, setPayer] = useState(0);
  const [participants, setParticipants] = useState([true, true, false]);
  const [division, setDivision] = useState("Equal");

  const toggleParticipant = (i: number) =>
    setParticipants((prev) => prev.map((v, idx) => (idx === i ? !v : v)));

  const selectedCount = participants.filter(Boolean).length;

  return (
    <div className="flex flex-col flex-1 min-h-0 bg-[#F2F2F2]">
      {/* Top bar */}
      <WireTopBar
        title="New Expense"
        showBack
        onBack={() => navigate("/expenses")}
        rightAction={
          <button
            onClick={() => navigate("/expenses")}
            className="text-[11px] font-mono text-[#888]"
          >
            Cancel
          </button>
        }
      />

      <div className="flex-1 overflow-y-auto p-4 space-y-4 pb-6">
        {/* Description */}
        <WireInput
          label="Description"
          placeholder="e.g. Weekly supermarket"
          required
        />

        {/* Amount */}
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <span className="text-[11px] font-mono font-medium text-[#555] uppercase tracking-wider">
              Total amount
            </span>
            <span className="text-[10px] font-mono text-[#888]">*required</span>
            <WireAnnotation label="numeric-input" />
          </div>
          <div className="flex h-12 bg-white border border-[#C8C8C8] rounded overflow-hidden">
            <div className="w-10 bg-[#F0F0F0] border-r border-[#C8C8C8] flex items-center justify-center">
              <span className="text-[13px] font-mono text-[#888]">$</span>
            </div>
            <div className="flex-1 flex items-center px-3">
              <span className="text-[16px] font-mono text-[#ABABAB]">0.00</span>
            </div>
            <div className="px-3 flex items-center">
              <span className="text-[9px] font-mono text-[#CCC]">USD ▼</span>
            </div>
          </div>
        </div>

        <WireDivider />

        {/* Category */}
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <span className="text-[11px] font-mono font-medium text-[#555] uppercase tracking-wider">
              Category
            </span>
            <WireAnnotation label="icon-grid-selector" />
          </div>
          <div className="grid grid-cols-4 gap-2">
            {CATEGORIES.map((cat, i) => (
              <WireCategoryBox
                key={cat.label}
                emoji={cat.emoji}
                label={cat.label}
                active={category === i}
                onClick={() => setCategory(i)}
              />
            ))}
          </div>
        </div>

        <WireDivider />

        {/* Paid by */}
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <span className="text-[11px] font-mono font-medium text-[#555] uppercase tracking-wider">
              Paid by
            </span>
            <WireAnnotation label="member-selector" />
          </div>
          <div className="flex gap-3">
            {MEMBERS.map((m, i) => (
              <button
                key={i}
                onClick={() => setPayer(i)}
                className="flex flex-col items-center gap-1"
              >
                <div
                  className={`relative ${
                    payer === i ? "ring-2 ring-offset-1 ring-[#333] rounded-full" : ""
                  }`}
                >
                  <WireAvatar size="md" label={m.label} />
                  {payer === i && (
                    <div className="absolute -bottom-0.5 -right-0.5 w-4 h-4 bg-[#333] rounded-full flex items-center justify-center">
                      <svg width="8" height="6" viewBox="0 0 8 6" fill="none">
                        <path d="M1 3l2 2 4-4" stroke="white" strokeWidth="1.2" strokeLinecap="round" />
                      </svg>
                    </div>
                  )}
                </div>
                <span className={`text-[9px] font-mono ${payer === i ? "text-[#222] font-medium" : "text-[#AAA]"}`}>
                  {m.name.split(" ")[0]}
                </span>
              </button>
            ))}
          </div>
        </div>

        <WireDivider />

        {/* Participants */}
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <span className="text-[11px] font-mono font-medium text-[#555] uppercase tracking-wider">
              Participants
            </span>
            <span className="text-[9px] font-mono text-[#ABABAB]">{selectedCount} selected</span>
            <WireAnnotation label="multi-select members" />
          </div>
          <div className="space-y-2">
            {MEMBERS.map((m, i) => (
              <div
                key={i}
                onClick={() => toggleParticipant(i)}
                className={`flex items-center gap-3 px-3 py-2 bg-white border rounded-xl cursor-pointer ${
                  participants[i] ? "border-[#555]" : "border-[#E0E0E0]"
                }`}
              >
                <WireCheckbox checked={participants[i]} />
                <WireAvatar size="sm" label={m.label} />
                <span className="text-[12px] font-mono text-[#444]">{m.name}</span>
              </div>
            ))}
          </div>
        </div>

        <WireDivider />

        {/* Division mode */}
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <span className="text-[11px] font-mono font-medium text-[#555] uppercase tracking-wider">
              Division
            </span>
            <WireAnnotation label="toggle: equal / custom" />
          </div>
          <WireSegmented
            options={["Equal", "Custom"]}
            selected={division}
            onSelect={setDivision}
          />

          {division === "Equal" ? (
            <div className="flex items-center gap-2 px-3 py-2 bg-[#F5F5F5] border border-[#E8E8E8] rounded-xl">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#ABABAB" strokeWidth="2" strokeLinecap="round">
                <circle cx="12" cy="12" r="9" />
                <path d="M8 12h8M8 8h8" />
              </svg>
              <span className="text-[10px] font-mono text-[#ABABAB]">
                $0.00 ÷ {selectedCount} = $0.00 each
              </span>
            </div>
          ) : (
            // Custom division
            <div className="space-y-2">
              {MEMBERS.filter((_, i) => participants[i]).map((m, i) => (
                <div key={i} className="flex items-center gap-2">
                  <WireAvatar size="xs" label={m.label} />
                  <span className="flex-1 text-[11px] font-mono text-[#555]">{m.name.split(" ")[0]}</span>
                  <div className="flex h-8 w-28 bg-white border border-[#C8C8C8] rounded overflow-hidden">
                    <div className="w-6 bg-[#F0F0F0] border-r border-[#C8C8C8] flex items-center justify-center">
                      <span className="text-[10px] font-mono text-[#999]">$</span>
                    </div>
                    <div className="flex-1 flex items-center px-2">
                      <span className="text-[11px] font-mono text-[#ABABAB]">0.00</span>
                    </div>
                  </div>
                </div>
              ))}
              <div className="text-[9px] font-mono text-[#ABABAB] text-right">Total: $0.00 of $0.00</div>
            </div>
          )}
        </div>

        <WireDivider />

        {/* Date */}
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <span className="text-[11px] font-mono font-medium text-[#555] uppercase tracking-wider">Date</span>
            <WireAnnotation label="date-picker" />
          </div>
          <div className="h-10 bg-white border border-[#C8C8C8] rounded flex items-center px-3 gap-2">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#ABABAB" strokeWidth="2" strokeLinecap="round">
              <rect x="3" y="4" width="18" height="18" rx="2" />
              <line x1="16" y1="2" x2="16" y2="6" />
              <line x1="8" y1="2" x2="8" y2="6" />
              <line x1="3" y1="10" x2="21" y2="10" />
            </svg>
            <span className="text-[12px] font-mono text-[#555]">April 2, 2025</span>
            <span className="text-[9px] font-mono text-[#CCC] ml-auto">(default: today)</span>
          </div>
        </div>

        {/* Optional note */}
        <WireTextarea label="Note (optional)" placeholder="Add a note..." rows={2} />

        <WireDivider />

        {/* Actions */}
        <div className="space-y-2 pt-2">
          <WireButton variant="primary" fullWidth onClick={() => navigate("/expenses")}>
            SAVE EXPENSE
          </WireButton>
          <WireButton variant="secondary" fullWidth onClick={() => navigate("/expenses")}>
            Cancel
          </WireButton>
        </div>
      </div>
    </div>
  );
}