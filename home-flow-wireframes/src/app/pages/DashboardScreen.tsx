import { useNavigate } from "react-router";
import {
  WireAvatar,
  WireCard,
  WireCheckbox,
  WireSectionHeader,
  WirePriorityBadge,
  WireProgressBar,
  WireFAB,
  WireAnnotation,
  WireDivider,
  WireChip,
} from "../components/WireComponents";

export function DashboardScreen() {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col flex-1 min-h-0 bg-[#F2F2F2] relative">
      {/* Header */}
      <div className="bg-white px-4 pt-2 pb-3 border-b border-[#E8E8E8]">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <WireAvatar size="md" label="AG" />
            <div>
              <div className="text-[10px] font-mono text-[#999]">Good morning,</div>
              <div className="text-[14px] font-mono font-medium text-[#222]">Ana García 👋</div>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => navigate("/notifications")}
              className="relative w-9 h-9 rounded-full bg-[#F0F0F0] border border-[#E0E0E0] flex items-center justify-center"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#555" strokeWidth="2" strokeLinecap="round">
                <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9" />
                <path d="M13.73 21a2 2 0 01-3.46 0" />
              </svg>
              {/* Badge */}
              <div className="absolute -top-0.5 -right-0.5 w-4 h-4 bg-[#444] rounded-full flex items-center justify-center">
                <span className="text-[8px] text-white font-mono">3</span>
              </div>
            </button>
            <WireAnnotation label="header" />
          </div>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto p-4 space-y-4 pb-6">
        {/* Task Distribution Banner */}
        <WireCard>
          <div className="flex items-center justify-between mb-2">
            <span className="text-[11px] font-mono font-medium text-[#444] uppercase tracking-wider">
              Task Equilibrium
            </span>
            <WireAnnotation label="balance-banner" />
          </div>
          <WireProgressBar
            value={62}
            label="Ana García — 62%"
            sublabel="Carlos — 38%"
          />
          <div className="flex mt-2 gap-1">
            <WireAvatar size="xs" label="AG" />
            <div className="flex-1 h-4 bg-[#444] rounded-l-sm" style={{ flex: 62 }} />
            <div className="flex-1 h-4 bg-[#C0C0C0] rounded-r-sm" style={{ flex: 38 }} />
            <WireAvatar size="xs" label="CG" />
          </div>
          <div className="text-[9px] font-mono text-[#999] mt-1 text-center">
            Tap to see detail per member
          </div>
        </WireCard>

        {/* My tasks today */}
        <div>
          <WireSectionHeader title="My tasks today" action="See all →" onAction={() => navigate("/tasks")} />
          <div className="space-y-2">
            {[
              { title: "Clean bathroom", priority: "high" as const, time: "Before 10am", done: false },
              { title: "Buy groceries", priority: "medium" as const, time: "Before 2pm", done: false },
              { title: "Take out trash", priority: "low" as const, time: "Done ✓", done: true },
            ].map((task, i) => (
              <WireCard key={i} onClick={() => navigate("/tasks/detail")}>
                <div className="flex items-center gap-3">
                  <WireCheckbox checked={task.done} />
                  <div className="flex-1 min-w-0">
                    <span
                      className={`text-[12px] font-mono text-[#333] ${
                        task.done ? "line-through text-[#AAA]" : ""
                      }`}
                    >
                      {task.title}
                    </span>
                    <div className="text-[9px] font-mono text-[#999] mt-0.5">{task.time}</div>
                  </div>
                  <WirePriorityBadge level={task.priority} />
                </div>
              </WireCard>
            ))}
          </div>
          <WireAnnotation label="task-card × 3" />
        </div>

        <WireDivider />

        {/* Upcoming tasks */}
        <div>
          <WireSectionHeader title="Upcoming" action="See all →" onAction={() => navigate("/tasks")} />
          <div className="space-y-1.5">
            {[
              { title: "Pay electric bill", date: "Mon · Apr 7", who: "CG" },
              { title: "Buy cleaning supplies", date: "Wed · Apr 9", who: "AG" },
              { title: "Car oil change", date: "Fri · Apr 11", who: "CG" },
            ].map((t, i) => (
              <div key={i} className="flex items-center gap-2 py-1.5 px-3 bg-white rounded-lg border border-[#E8E8E8]">
                <WireAvatar size="xs" label={t.who} />
                <span className="flex-1 text-[11px] font-mono text-[#444]">{t.title}</span>
                <span className="text-[9px] font-mono text-[#888]">{t.date}</span>
              </div>
            ))}
          </div>
        </div>

        <WireDivider />

        {/* Expense summary */}
        <div>
          <WireSectionHeader title="Expenses this month" action="See →" onAction={() => navigate("/expenses")} />
          <div className="grid grid-cols-2 gap-2">
            <WireCard>
              <div className="text-[9px] font-mono text-[#888] uppercase tracking-wider">Balance</div>
              <div className="text-[18px] font-mono font-medium text-[#222] mt-0.5">$1,230</div>
              <div className="text-[9px] font-mono text-[#999]">April total</div>
            </WireCard>
            <WireCard>
              <div className="text-[9px] font-mono text-[#888] uppercase tracking-wider">You Owe</div>
              <div className="text-[18px] font-mono font-medium text-[#555] mt-0.5">$45</div>
              <div className="text-[9px] font-mono text-[#999]">→ Carlos García</div>
            </WireCard>
          </div>
          <WireAnnotation label="expense-summary" />
        </div>

        <WireDivider />

        {/* Recent Activity */}
        <div>
          <WireSectionHeader title="Recent activity" />
          <div className="space-y-2">
            {[
              { who: "CG", action: "completed", task: "Buy milk", time: "2h ago" },
              { who: "AG", action: "added expense", task: "Gas bill · $120", time: "5h ago" },
              { who: "LG", action: "joined the home", task: "", time: "Yesterday" },
            ].map((a, i) => (
              <div key={i} className="flex items-start gap-2.5 px-3 py-2 bg-white rounded-lg border border-[#E8E8E8]">
                <WireAvatar size="xs" label={a.who} />
                <div className="flex-1 min-w-0">
                  <span className="text-[11px] font-mono text-[#555]">
                    <span className="font-medium text-[#333]">{a.who}</span>{" "}
                    {a.action}
                    {a.task && (
                      <span className="text-[#777]"> "{a.task}"</span>
                    )}
                  </span>
                </div>
                <span className="text-[9px] font-mono text-[#AAA] shrink-0">{a.time}</span>
              </div>
            ))}
          </div>
          <WireAnnotation label="activity-feed" />
        </div>
      </div>

      {/* FAB */}
      <WireFAB onClick={() => navigate("/tasks/create")} />
    </div>
  );
}