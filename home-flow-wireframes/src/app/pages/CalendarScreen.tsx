import { useState } from "react";
import { useNavigate } from "react-router";
import {
  WireAvatar,
  WireCard,
  WireCheckbox,
  WireChip,
  WirePriorityBadge,
  WireSegmented,
  WireAnnotation,
  WireTopBar,
  WireDivider,
} from "../components/WireComponents";

const DAYS_SHORT = ["Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"];

// April 2025 starts on Tuesday (index 1)
// 30 days
const APRIL_OFFSET = 1; // Mon=0, Tue=1
const APRIL_DAYS = 30;

// Which days have tasks and by whom (color codes)
const TASK_DOTS: Record<number, string[]> = {
  2: ["AG"],
  5: ["CG", "AG"],
  7: ["AG", "CG", "LG"],
  9: ["CG"],
  11: ["AG"],
  14: ["CG", "AG"],
  16: ["AG"],
  18: ["LG"],
  21: ["AG", "CG"],
  23: ["CG"],
  25: ["AG"],
  28: ["CG", "AG"],
};

const MEMBER_COLORS: Record<string, string> = {
  AG: "#555",
  CG: "#888",
  LG: "#BBB",
};

const SELECTED_DAY_TASKS = [
  { title: "Clean bathroom", assignee: "AG", priority: "high" as const, done: false },
  { title: "Pay electric bill", assignee: "CG", priority: "high" as const, done: false },
  { title: "Buy groceries", assignee: "LG", priority: "medium" as const, done: true },
];

export function CalendarScreen() {
  const navigate = useNavigate();
  const [view, setView] = useState("Monthly");
  const [selectedDay, setSelectedDay] = useState(7);
  const [memberFilter, setMemberFilter] = useState("All");

  // Build grid cells (empty + days)
  const cells: (number | null)[] = [];
  for (let i = 0; i < APRIL_OFFSET; i++) cells.push(null);
  for (let d = 1; d <= APRIL_DAYS; d++) cells.push(d);
  while (cells.length % 7 !== 0) cells.push(null);

  return (
    <div className="flex flex-col flex-1 min-h-0 bg-[#F2F2F2]">
      {/* Top bar */}
      <WireTopBar
        title="Calendar"
        rightAction={
          <button className="text-[#666]">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
              <line x1="4" y1="6" x2="20" y2="6" />
              <line x1="4" y1="12" x2="14" y2="12" />
              <line x1="4" y1="18" x2="10" y2="18" />
            </svg>
          </button>
        }
      />

      {/* View toggle */}
      <div className="bg-white px-4 py-2 border-b border-[#E8E8E8]">
        <WireSegmented
          options={["Monthly", "Weekly"]}
          selected={view}
          onSelect={setView}
        />
      </div>

      {/* Member filter chips */}
      <div className="bg-white px-4 py-2 border-b border-[#E8E8E8]">
        <div className="flex items-center gap-1.5">
          <span className="text-[9px] font-mono text-[#AAA] shrink-0">Filter:</span>
          {["All", "Ana (AG)", "Carlos (CG)", "Luis (LG)"].map((m, i) => (
            <WireChip
              key={m}
              label={m}
              active={memberFilter === m}
              dot={i > 0 ? Object.values(MEMBER_COLORS)[i - 1] : undefined}
              onClick={() => setMemberFilter(m)}
            />
          ))}
          <WireAnnotation label="member-filter" />
        </div>
      </div>

      <div className="flex-1 overflow-y-auto">
        {view === "Monthly" ? (
          <div className="bg-white">
            {/* Month header */}
            <div className="flex items-center justify-between px-4 py-3">
              <button className="text-[#AAA]">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                  <path d="M15 18l-6-6 6-6" />
                </svg>
              </button>
              <span className="text-[13px] font-mono font-medium text-[#333]">April 2025</span>
              <button className="text-[#AAA]">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                  <path d="M9 18l6-6-6-6" />
                </svg>
              </button>
            </div>

            {/* Day headers */}
            <div className="grid grid-cols-7 px-2">
              {DAYS_SHORT.map((d) => (
                <div key={d} className="text-center py-1">
                  <span className="text-[9px] font-mono text-[#ABABAB] font-medium">{d}</span>
                </div>
              ))}
            </div>

            {/* Calendar grid */}
            <div className="grid grid-cols-7 px-2 pb-2 gap-y-1">
              {cells.map((day, i) => {
                if (day === null) return <div key={`empty-${i}`} />;
                const isSelected = day === selectedDay;
                const isToday = day === 7; // simulated today
                const dots = TASK_DOTS[day] || [];
                return (
                  <button
                    key={day}
                    onClick={() => setSelectedDay(day)}
                    className={`flex flex-col items-center py-1 rounded-lg ${
                      isSelected ? "bg-[#222]" : isToday ? "bg-[#E8E8E8]" : ""
                    }`}
                  >
                    <span
                      className={`text-[12px] font-mono ${
                        isSelected ? "text-white" : isToday ? "text-[#222] font-medium" : "text-[#555]"
                      }`}
                    >
                      {day}
                    </span>
                    {/* Task dots */}
                    <div className="flex gap-0.5 mt-0.5 h-2 items-center">
                      {dots.slice(0, 3).map((m) => (
                        <div
                          key={m}
                          className="w-1.5 h-1.5 rounded-full"
                          style={{
                            background: isSelected ? "white" : MEMBER_COLORS[m],
                          }}
                        />
                      ))}
                    </div>
                  </button>
                );
              })}
            </div>

            {/* Legend */}
            <div className="flex items-center gap-3 px-4 pb-2 border-t border-[#F0F0F0] pt-2">
              <span className="text-[9px] font-mono text-[#ABABAB]">Legend:</span>
              {Object.entries(MEMBER_COLORS).map(([m, c]) => (
                <div key={m} className="flex items-center gap-1">
                  <div className="w-2 h-2 rounded-full" style={{ background: c }} />
                  <span className="text-[9px] font-mono text-[#888]">{m}</span>
                </div>
              ))}
              <WireAnnotation label="task-dots" />
            </div>

            <WireDivider label="Selected day" />

            {/* Tasks for selected day */}
            <div className="px-4 pb-4">
              <div className="flex items-center justify-between mb-2">
                <span className="text-[11px] font-mono font-medium text-[#444]">
                  Thursday, April {selectedDay}
                </span>
                <WireAnnotation label="day-task-list" />
              </div>

              <div className="space-y-2">
                {SELECTED_DAY_TASKS.map((task, i) => (
                  <div
                    key={i}
                    onClick={() => navigate("/tasks/detail")}
                    className="flex items-center gap-2 bg-white border border-[#E8E8E8] rounded-xl px-3 py-2 cursor-pointer"
                  >
                    <WireCheckbox checked={task.done} />
                    <WireAvatar size="xs" label={task.assignee} />
                    <span className={`flex-1 text-[11px] font-mono ${task.done ? "line-through text-[#ABABAB]" : "text-[#333]"}`}>
                      {task.title}
                    </span>
                    <WirePriorityBadge level={task.priority} />
                  </div>
                ))}
              </div>
            </div>
          </div>
        ) : (
          // Weekly view (simplified)
          <WeeklyView navigate={navigate} />
        )}
      </div>
    </div>
  );
}

function WeeklyView({ navigate }: { navigate: (path: string) => void }) {
  const days = ["Mo 7", "Tu 8", "We 9", "Th 10", "Fr 11", "Sa 12", "Su 13"];
  const hours = ["8am", "9am", "10am", "11am", "12pm", "1pm", "2pm", "3pm", "4pm", "5pm", "6pm"];

  // Some fake task blocks
  const blocks = [
    { day: 0, startHour: 1, height: 2, label: "Clean bathroom", color: "#666" },
    { day: 2, startHour: 3, height: 1, label: "Groceries", color: "#999" },
    { day: 3, startHour: 5, height: 2, label: "Pay bills", color: "#555" },
    { day: 4, startHour: 0, height: 1, label: "Car oil", color: "#888" },
  ];

  return (
    <div className="bg-white">
      <div className="flex items-center justify-between px-4 py-2 border-b border-[#F0F0F0]">
        <button className="text-[#AAA]">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
            <path d="M15 18l-6-6 6-6" />
          </svg>
        </button>
        <span className="text-[11px] font-mono text-[#555]">Apr 7 – Apr 13, 2025</span>
        <button className="text-[#AAA]">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
            <path d="M9 18l6-6-6-6" />
          </svg>
        </button>
      </div>

      {/* Day headers */}
      <div className="flex border-b border-[#F0F0F0]">
        <div className="w-10 shrink-0" />
        {days.map((d, i) => (
          <div
            key={d}
            className={`flex-1 py-1.5 text-center border-l border-[#F0F0F0] ${i === 0 ? "bg-[#F0F0F0]" : ""}`}
          >
            <div className="text-[8px] font-mono text-[#ABABAB]">{d.split(" ")[0]}</div>
            <div className={`text-[12px] font-mono font-medium ${i === 0 ? "text-[#222]" : "text-[#888]"}`}>
              {d.split(" ")[1]}
            </div>
          </div>
        ))}
      </div>

      {/* Grid */}
      <div className="flex overflow-x-auto relative" style={{ height: 330 }}>
        {/* Hours column */}
        <div className="w-10 shrink-0 flex flex-col">
          {hours.map((h) => (
            <div key={h} className="flex-1 flex items-start justify-end pr-1 pt-0.5">
              <span className="text-[8px] font-mono text-[#ABABAB]">{h}</span>
            </div>
          ))}
        </div>

        {/* Day columns */}
        {days.map((d, dayIdx) => (
          <div key={d} className="flex-1 border-l border-[#F0F0F0] relative">
            {/* Hour lines */}
            {hours.map((_, hIdx) => (
              <div
                key={hIdx}
                className="absolute left-0 right-0 border-t border-[#F5F5F5]"
                style={{ top: `${(hIdx / hours.length) * 100}%` }}
              />
            ))}
            {/* Task blocks */}
            {blocks
              .filter((b) => b.day === dayIdx)
              .map((b, i) => (
                <div
                  key={i}
                  onClick={() => navigate("/tasks/detail")}
                  className="absolute left-0.5 right-0.5 rounded flex items-start px-1 pt-0.5 cursor-pointer border border-[#D0D0D0]"
                  style={{
                    top: `${(b.startHour / hours.length) * 100}%`,
                    height: `${(b.height / hours.length) * 100}%`,
                    background: "#E8E8E8",
                  }}
                >
                  <span className="text-[8px] font-mono text-[#555] leading-tight truncate">
                    {b.label}
                  </span>
                </div>
              ))}
          </div>
        ))}
      </div>

      <div className="px-4 py-2">
        <WireAnnotation label="weekly-view · drag to reschedule (post-MVP)" />
      </div>
    </div>
  );
}