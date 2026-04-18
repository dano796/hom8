import { useState } from "react";
import { useNavigate } from "react-router";
import {
  WireAvatar,
  WireInput,
  WireTextarea,
  WireButton,
  WireSegmented,
  WireToggle,
  WireChip,
  WireAnnotation,
  WireTopBar,
  WireDivider,
  WireCheckbox,
} from "../components/WireComponents";

export function CreateTaskScreen() {
  const navigate = useNavigate();
  const [priority, setPriority] = useState("Medium");
  const [recurrence, setRecurrence] = useState(false);
  const [freqSel, setFreqSel] = useState("Weekly");
  const [selectedAssignee, setSelectedAssignee] = useState(0);
  const [activeLabels, setActiveLabels] = useState<string[]>(["Limpieza"]);

  const members = [
    { label: "AG", name: "Ana" },
    { label: "CG", name: "Carlos" },
    { label: "LG", name: "Luis" },
  ];

  const labelOptions = ["Limpieza", "Kitchen", "Shopping", "Bills", "Garden"];

  const toggleLabel = (l: string) =>
    setActiveLabels((prev) =>
      prev.includes(l) ? prev.filter((x) => x !== l) : [...prev, l]
    );

  return (
    <div className="flex flex-col flex-1 min-h-0 bg-[#F2F2F2]">
      {/* Top bar */}
      <WireTopBar
        title="Nueva Tarea"
        showBack
        onBack={() => navigate("/tasks")}
        rightAction={
          <button
            onClick={() => navigate("/tasks")}
            className="text-[11px] font-mono text-[#888]"
          >
            Cancel
          </button>
        }
      />

      <div className="flex-1 overflow-y-auto p-4 space-y-4 pb-6">
        {/* Title */}
        <WireInput
          label="Título"
          placeholder="ej. Limpiar la cocina"
          required
        />

        {/* Description */}
        <WireTextarea label="Descripción" placeholder="Add details (optional)" rows={3} />

        <WireDivider />

        {/* Assignee */}
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <span className="text-[11px] font-mono font-medium text-[#555] uppercase tracking-wider">
              Assigned to
            </span>
            <span className="text-[10px] font-mono text-[#888]">*requerido</span>
            <WireAnnotation label="member-selector" />
          </div>
          <div className="flex gap-3">
            {members.map((m, i) => (
              <button
                key={i}
                onClick={() => setSelectedAssignee(i)}
                className="flex flex-col items-center gap-1"
              >
                <div
                  className={`relative ${
                    selectedAssignee === i
                      ? "ring-2 ring-offset-1 ring-[#333] rounded-full"
                      : ""
                  }`}
                >
                  <WireAvatar size="md" label={m.label} />
                  {selectedAssignee === i && (
                    <div className="absolute -bottom-0.5 -right-0.5 w-4 h-4 bg-[#333] rounded-full flex items-center justify-center">
                      <svg width="8" height="6" viewBox="0 0 8 6" fill="none">
                        <path d="M1 3l2 2 4-4" stroke="white" strokeWidth="1.2" strokeLinecap="round" />
                      </svg>
                    </div>
                  )}
                </div>
                <span
                  className={`text-[9px] font-mono ${
                    selectedAssignee === i ? "text-[#222] font-medium" : "text-[#AAA]"
                  }`}
                >
                  {m.name}
                </span>
              </button>
            ))}
            <button className="flex flex-col items-center gap-1">
              <div className="w-10 h-10 rounded-full border-2 border-dashed border-[#C8C8C8] flex items-center justify-center text-[#CCC]">
                +
              </div>
              <span className="text-[9px] font-mono text-[#CCC]">Add</span>
            </button>
          </div>
        </div>

        <WireDivider />

        {/* Due date */}
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <span className="text-[11px] font-mono font-medium text-[#555] uppercase tracking-wider">
              Due date
            </span>
            <span className="text-[10px] font-mono text-[#888]">*requerido</span>
            <WireAnnotation label="date-picker" />
          </div>
          <div className="h-10 bg-white border border-[#C8C8C8] rounded flex items-center px-3 gap-2">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#ABABAB" strokeWidth="2" strokeLinecap="round">
              <rect x="3" y="4" width="18" height="18" rx="2" />
              <line x1="16" y1="2" x2="16" y2="6" />
              <line x1="8" y1="2" x2="8" y2="6" />
              <line x1="3" y1="10" x2="21" y2="10" />
            </svg>
            <span className="text-[12px] font-mono text-[#ABABAB]">Select date...</span>
          </div>
        </div>

        {/* Priority */}
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <span className="text-[11px] font-mono font-medium text-[#555] uppercase tracking-wider">
              Priority
            </span>
            <WireAnnotation label="segmented-control" />
          </div>
          <WireSegmented
            options={["High", "Medium", "Low"]}
            selected={priority}
            onSelect={setPriority}
          />
        </div>

        <WireDivider />

        {/* Recurrence toggle */}
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="text-[11px] font-mono font-medium text-[#555] uppercase tracking-wider">
                Recurrence
              </span>
              <WireAnnotation label="toggle" />
            </div>
            <WireToggle on={recurrence} />
          </div>

          {/* Toggle ON → reveal recurrence fields */}
          {recurrence && (
            <div className="ml-2 pl-3 border-l-2 border-[#DDD] space-y-3">
              <div className="space-y-1">
                <span className="text-[10px] font-mono text-[#888] uppercase">Frequency</span>
                <WireSegmented
                  options={["Daily", "Weekly", "Monthly"]}
                  selected={freqSel}
                  onSelect={setFreqSel}
                />
              </div>
              <WireInput label="Repeat on" placeholder="Mon, Wed, Fri" />
              <WireInput label="End date (optional)" placeholder="No end date" />
              <div className="text-[9px] font-mono text-[#ABABAB] bg-[#F5F5F5] border border-dashed border-[#DDD] p-2 rounded">
                ⚠ When editing a recurring task, a modal asks: "Edit only this instance or the entire series?"
              </div>
            </div>
          )}
        </div>

        <WireDivider />

        {/* Labels */}
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <span className="text-[11px] font-mono font-medium text-[#555] uppercase tracking-wider">
              Labels
            </span>
            <WireAnnotation label="multi-select chips" />
          </div>
          <div className="flex flex-wrap gap-1.5">
            {labelOptions.map((l) => (
              <WireChip
                key={l}
                label={l}
                active={activeLabels.includes(l)}
                onClick={() => toggleLabel(l)}
              />
            ))}
            <WireChip label="+ New label" />
          </div>
        </div>

        {/* Subtasks / Checklist */}
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <span className="text-[11px] font-mono font-medium text-[#555] uppercase tracking-wider">
              Checklist
            </span>
            <WireAnnotation label="subtasks" />
          </div>
          <div className="space-y-1.5">
            {["Sweep the floor", "Mop with cleaner"].map((sub, i) => (
              <div key={i} className="flex items-center gap-2 h-8 bg-white border border-[#E0E0E0] rounded px-2">
                <WireCheckbox />
                <span className="flex-1 text-[11px] font-mono text-[#888]">{sub}</span>
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#DDD" strokeWidth="2">
                  <line x1="5" y1="12" x2="19" y2="12" />
                </svg>
              </div>
            ))}
            <button className="flex items-center gap-1.5 text-[10px] font-mono text-[#888] py-1">
              + Add subtask
            </button>
          </div>
        </div>

        {/* Attachments */}
        <div className="space-y-1">
          <span className="text-[11px] font-mono font-medium text-[#555] uppercase tracking-wider">
            Attachments
          </span>
          <button className="flex items-center gap-2 h-10 px-3 bg-white border border-dashed border-[#C8C8C8] rounded w-full">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#ABABAB" strokeWidth="2" strokeLinecap="round">
              <path d="M21.44 11.05l-9.19 9.19a6 6 0 01-8.49-8.49l9.19-9.19a4 4 0 015.66 5.66l-9.2 9.19a2 2 0 01-2.83-2.83l8.49-8.48" />
            </svg>
            <span className="text-[11px] font-mono text-[#ABABAB]">Attach image or document</span>
          </button>
        </div>

        {/* Comments */}
        <WireTextarea label="Initial comment (optional)" placeholder="Leave a note for the assignee..." rows={2} />

        <WireDivider />

        {/* Actions */}
        <div className="space-y-2 pt-2">
          <WireButton variant="primary" fullWidth onClick={() => navigate("/tasks")}>
            SAVE TASK
          </WireButton>
          <WireButton variant="secondary" fullWidth onClick={() => navigate("/tasks")}>
            Cancel
          </WireButton>
        </div>
      </div>
    </div>
  );
}