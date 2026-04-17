import { useNavigate } from "react-router";
import {
  WireAvatar,
  WireCard,
  WireCheckbox,
  WireChip,
  WirePriorityBadge,
  WireStatusChip,
  WireFAB,
  WireAnnotation,
  WireTopBar,
} from "../components/WireComponents";

const TASKS = [
  {
    id: 1,
    title: "Clean bathroom",
    assignee: "AG",
    dueLabel: "Today",
    dueStatus: "overdue",
    priority: "high" as const,
    tags: ["Limpieza"],
    done: false,
  },
  {
    id: 2,
    title: "Buy groceries",
    assignee: "CG",
    dueLabel: "Yesterday ⚠",
    dueStatus: "overdue",
    priority: "medium" as const,
    tags: ["Shopping", "Comida"],
    done: false,
  },
  {
    id: 3,
    title: "Pay electric bill",
    assignee: "AG",
    dueLabel: "Apr 7",
    dueStatus: "in-progress",
    priority: "high" as const,
    tags: ["Bills"],
    done: false,
  },
  {
    id: 4,
    title: "Vacuum living room",
    assignee: "LG",
    dueLabel: "Apr 9",
    dueStatus: "pending",
    priority: "low" as const,
    tags: ["Limpieza"],
    done: false,
  },
  {
    id: 5,
    title: "Take out trash",
    assignee: "CG",
    dueLabel: "Done ✓",
    dueStatus: "done",
    priority: "low" as const,
    tags: [],
    done: true,
  },
];

const FILTER_CHIPS = ["All", "My tasks", "Ana", "Carlos", "Luis"];
const PRIORITY_CHIPS = ["All", "High", "Medium", "Low"];

export function TasksListScreen() {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col flex-1 min-h-0 bg-[#F2F2F2] relative">
      {/* Top bar */}
      <WireTopBar
        title="Tareas"
        rightAction={
          <div className="flex items-center gap-2">
            <WireAnnotation label="sort ↕" />
            <button className="text-[#666]">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                <line x1="4" y1="6" x2="20" y2="6" />
                <line x1="4" y1="12" x2="14" y2="12" />
                <line x1="4" y1="18" x2="10" y2="18" />
              </svg>
            </button>
          </div>
        }
      />

      {/* Search bar */}
      <div className="px-4 pt-3 pb-2 bg-white border-b border-[#E8E8E8]">
        <div className="flex items-center gap-2 h-9 bg-[#F0F0F0] border border-[#E0E0E0] rounded-lg px-3">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#ABABAB" strokeWidth="2" strokeLinecap="round">
            <circle cx="11" cy="11" r="8" />
            <path d="M21 21l-4.35-4.35" />
          </svg>
          <span className="text-[11px] font-mono text-[#ABABAB]">Search by title or label...</span>
          <WireAnnotation label="search-bar" />
        </div>
      </div>

      {/* Filter chips */}
      <div className="bg-white px-4 py-2 border-b border-[#E8E8E8]">
        <div className="flex gap-1.5 overflow-x-auto pb-1 no-scrollbar">
          {FILTER_CHIPS.map((chip, i) => (
            <WireChip key={chip} label={chip} active={i === 0} />
          ))}
          <span className="text-[#DDD] mx-1 self-center">|</span>
          <WireAnnotation label="filter-chips" />
        </div>
        {/* Priority filter row */}
        <div className="flex gap-1.5 mt-1.5 overflow-x-auto no-scrollbar">
          <span className="text-[9px] font-mono text-[#AAA] self-center shrink-0">Priority:</span>
          {PRIORITY_CHIPS.map((chip, i) => (
            <WireChip key={chip} label={chip} active={i === 0} />
          ))}
        </div>
      </div>

      {/* Results count */}
      <div className="px-4 py-1.5 flex items-center justify-between">
        <span className="text-[9px] font-mono text-[#999]"> tareas encontradas</span>
        <WireAnnotation label="task-list" />
      </div>

      {/* Task list */}
      <div className="flex-1 overflow-y-auto px-4 pb-4 space-y-2">
        {TASKS.map((task) => (
          <WireCard key={task.id} onClick={() => navigate("/tasks/detail")}>
            <div className="flex items-start gap-2.5">
              {/* Checkbox */}
              <div className="pt-0.5">
                <WireCheckbox checked={task.done} />
              </div>

              {/* Content */}
              <div className="flex-1 min-w-0 space-y-1">
                <div className="flex items-center gap-2 flex-wrap">
                  <span
                    className={`text-[12px] font-mono font-medium ${
                      task.done ? "line-through text-[#ABABAB]" : "text-[#222]"
                    }`}
                  >
                    {task.title}
                  </span>
                  <WirePriorityBadge level={task.priority} />
                </div>

                {/* Meta row */}
                <div className="flex items-center gap-2 flex-wrap">
                  <WireAvatar size="xs" label={task.assignee} />
                  <span
                    className={`text-[9px] font-mono ${
                      task.dueStatus === "overdue" ? "text-[#666] font-medium" : "text-[#999]"
                    }`}
                  >
                    {task.dueLabel}
                  </span>
                  <WireStatusChip status={task.dueStatus as any} />
                </div>

                {/* Tags */}
                {task.tags.length > 0 && (
                  <div className="flex gap-1 flex-wrap">
                    {task.tags.map((tag) => (
                      <span
                        key={tag}
                        className="text-[8px] font-mono px-1.5 py-0.5 bg-[#F0F0F0] border border-[#E0E0E0] rounded text-[#777]"
                      >
                        {tag}
                      </span>
                    ))}
                  </div>
                )}
              </div>

              {/* Swipe hint */}
              <div className="flex flex-col items-end gap-1 shrink-0">
                <button className="text-[#CCC]">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <circle cx="12" cy="5" r="1" fill="currentColor" />
                    <circle cx="12" cy="12" r="1" fill="currentColor" />
                    <circle cx="12" cy="19" r="1" fill="currentColor" />
                  </svg>
                </button>
              </div>
            </div>
          </WireCard>
        ))}

        {/* Swipe hint annotation */}
        <div className="flex justify-center">
          <span className="text-[9px] font-mono text-[#C0C0C0] border border-dashed border-[#DDD] px-2 py-1 rounded">
            ← Swipe left to delete · Swipe right to complete
          </span>
        </div>
      </div>

      {/* FAB */}
      <WireFAB onClick={() => navigate("/tasks/create")} />
    </div>
  );
}