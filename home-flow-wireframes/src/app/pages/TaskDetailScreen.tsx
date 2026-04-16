import { useNavigate } from "react-router";
import {
  WireAvatar,
  WireCard,
  WireCheckbox,
  WireButton,
  WirePriorityBadge,
  WireStatusChip,
  WireAnnotation,
  WireTopBar,
  WireDivider,
  WireChip,
} from "../components/WireComponents";

export function TaskDetailScreen() {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col flex-1 min-h-0 bg-[#F2F2F2]">
      {/* Top bar */}
      <WireTopBar
        title="Task Detail"
        showBack
        onBack={() => navigate("/tasks")}
        rightAction={
          <div className="flex items-center gap-2">
            <WireAnnotation label="context-menu" />
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

      <div className="flex-1 overflow-y-auto pb-28">
        {/* Title section */}
        <div className="bg-white px-4 py-3 border-b border-[#E8E8E8]">
          <div className="flex items-start justify-between gap-2">
            <h2 className="text-[15px] font-mono font-medium text-[#222] flex-1">
              Clean the bathroom thoroughly
            </h2>
            <WirePriorityBadge level="high" />
          </div>
          <div className="flex items-center gap-2 mt-2">
            <WireStatusChip status="pending" />
            <WireAnnotation label="status-chip" />
          </div>
        </div>

        <div className="p-4 space-y-4">
          {/* Metadata */}
          <WireCard>
            <div className="space-y-2.5">
              <WireSectionHeaderSimple label="Details" />
              <div className="flex items-center gap-2">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#999" strokeWidth="2" strokeLinecap="round">
                  <circle cx="12" cy="8" r="4" />
                  <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" />
                </svg>
                <span className="text-[10px] font-mono text-[#888]">Assigned to</span>
                <WireAvatar size="xs" label="AG" />
                <span className="text-[11px] font-mono text-[#444]">Ana García</span>
                <WireAnnotation label="assignee" />
              </div>
              <div className="flex items-center gap-2">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#999" strokeWidth="2" strokeLinecap="round">
                  <rect x="3" y="4" width="18" height="18" rx="2" />
                  <line x1="16" y1="2" x2="16" y2="6" />
                  <line x1="8" y1="2" x2="8" y2="6" />
                  <line x1="3" y1="10" x2="21" y2="10" />
                </svg>
                <span className="text-[10px] font-mono text-[#888]">Due date</span>
                <span className="text-[11px] font-mono text-[#555] font-medium">April 7, 2025</span>
                <span className="text-[9px] font-mono text-[#888] bg-[#F0F0F0] px-1.5 rounded border border-[#E0E0E0]">
                  Today
                </span>
              </div>
              <div className="flex items-center gap-2">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#999" strokeWidth="2" strokeLinecap="round">
                  <polygon points="12,2 15.09,8.26 22,9.27 17,14.14 18.18,21.02 12,17.77 5.82,21.02 7,14.14 2,9.27 8.91,8.26" />
                </svg>
                <span className="text-[10px] font-mono text-[#888]">Priority</span>
                <WirePriorityBadge level="high" />
              </div>
              <div className="flex items-start gap-2">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#999" strokeWidth="2" strokeLinecap="round" className="mt-0.5">
                  <path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z" />
                  <line x1="7" y1="7" x2="7.01" y2="7" />
                </svg>
                <span className="text-[10px] font-mono text-[#888] shrink-0">Labels</span>
                <div className="flex gap-1 flex-wrap">
                  <WireChip label="Cleaning" active />
                  <WireChip label="Kitchen" active />
                </div>
              </div>
              <div className="flex items-center gap-2">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#999" strokeWidth="2" strokeLinecap="round">
                  <path d="M17 1l4 4-14 14H3v-4L17 1z" />
                </svg>
                <span className="text-[10px] font-mono text-[#888]">Recurrence</span>
                <span className="text-[11px] font-mono text-[#555]">Every Monday</span>
              </div>
            </div>
          </WireCard>

          {/* Description */}
          <WireCard>
            <WireSectionHeaderSimple label="Description" />
            <p className="text-[11px] font-mono text-[#666] leading-relaxed mt-2">
              Please clean the bathroom completely: toilet, sink, shower, and floor.
              Use the cleaning supplies under the sink. Don't forget to replace the
              toilet paper roll.
            </p>
          </WireCard>

          {/* Checklist */}
          <WireCard>
            <div className="flex items-center justify-between mb-2">
              <WireSectionHeaderSimple label="Checklist (2/4)" />
              <WireAnnotation label="subtasks" />
            </div>
            <div className="space-y-2">
              {[
                { text: "Clean the toilet", done: true },
                { text: "Scrub the sink", done: true },
                { text: "Clean shower/tub", done: false },
                { text: "Mop the floor", done: false },
              ].map((sub, i) => (
                <div key={i} className="flex items-center gap-2">
                  <WireCheckbox checked={sub.done} />
                  <span
                    className={`text-[11px] font-mono ${
                      sub.done ? "line-through text-[#ABABAB]" : "text-[#444]"
                    }`}
                  >
                    {sub.text}
                  </span>
                </div>
              ))}
              {/* Progress */}
              <div className="h-1.5 bg-[#E8E8E8] rounded-full overflow-hidden mt-2">
                <div className="h-full bg-[#555] rounded-full" style={{ width: "50%" }} />
              </div>
            </div>
          </WireCard>

          {/* Attachments */}
          <WireCard>
            <WireSectionHeaderSimple label="Attachments (2)" />
            <div className="flex gap-2 mt-2">
              {["photo.jpg", "notes.pdf"].map((file, i) => (
                <div key={i} className="flex items-center gap-1.5 px-2 py-1.5 bg-[#F5F5F5] border border-[#E0E0E0] rounded">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#999" strokeWidth="2" strokeLinecap="round">
                    <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" />
                    <polyline points="14,2 14,8 20,8" />
                  </svg>
                  <span className="text-[9px] font-mono text-[#666]">{file}</span>
                </div>
              ))}
            </div>
          </WireCard>

          {/* Comments */}
          <WireCard>
            <div className="flex items-center justify-between mb-2">
              <WireSectionHeaderSimple label="Comments (2)" />
              <WireAnnotation label="comment-section" />
            </div>
            <div className="space-y-3">
              {[
                { who: "CG", name: "Carlos", text: "I'll start with the shower part.", time: "2h ago" },
                { who: "AG", name: "Ana", text: "Great, I'll handle the rest tonight.", time: "1h ago" },
              ].map((c, i) => (
                <div key={i} className="flex items-start gap-2">
                  <WireAvatar size="xs" label={c.who} />
                  <div className="flex-1 min-w-0">
                    <div className="flex items-baseline gap-1.5">
                      <span className="text-[10px] font-mono font-medium text-[#444]">{c.name}</span>
                      <span className="text-[9px] font-mono text-[#ABABAB]">{c.time}</span>
                    </div>
                    <div className="text-[11px] font-mono text-[#666] bg-[#F5F5F5] border border-[#E8E8E8] rounded px-2 py-1.5 mt-0.5">
                      {c.text}
                    </div>
                  </div>
                </div>
              ))}
            </div>
            {/* Comment input */}
            <div className="flex items-center gap-2 mt-3 pt-3 border-t border-[#E8E8E8]">
              <WireAvatar size="xs" label="AG" />
              <div className="flex-1 h-8 bg-[#F5F5F5] border border-[#E0E0E0] rounded-full px-3 flex items-center">
                <span className="text-[10px] font-mono text-[#ABABAB]">Write a comment...</span>
              </div>
              <button className="w-7 h-7 bg-[#333] rounded-full flex items-center justify-center shrink-0">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5" strokeLinecap="round">
                  <line x1="22" y1="2" x2="11" y2="13" />
                  <polygon points="22,2 15,22 11,13 2,9" />
                </svg>
              </button>
            </div>
          </WireCard>

          {/* Activity timeline */}
          <WireCard>
            <WireSectionHeaderSimple label="Activity" />
            <div className="mt-2 space-y-2.5">
              {[
                { text: "Assigned to Ana by Carlos", time: "Apr 3, 9:00am" },
                { text: "Priority changed to HIGH", time: "Apr 4, 2:15pm" },
                { text: "Checklist updated (2 items added)", time: "Apr 5, 11:00am" },
              ].map((evt, i) => (
                <div key={i} className="flex items-start gap-2">
                  <div className="flex flex-col items-center">
                    <div className="w-2 h-2 rounded-full bg-[#C8C8C8] border border-[#ABABAB] shrink-0 mt-0.5" />
                    {i < 2 && <div className="w-px h-4 bg-[#E0E0E0]" />}
                  </div>
                  <div className="flex-1">
                    <span className="text-[10px] font-mono text-[#666]">{evt.text}</span>
                    <div className="text-[9px] font-mono text-[#ABABAB]">{evt.time}</div>
                  </div>
                </div>
              ))}
            </div>
          </WireCard>
        </div>
      </div>

      {/* Sticky bottom CTA */}
      <div className="absolute bottom-16 left-0 right-0 p-4 bg-white border-t border-[#E8E8E8]">
        <WireButton variant="primary" fullWidth size="lg">
          ✓ MARK AS COMPLETE
        </WireButton>
        <WireAnnotation label="primary-cta" />
      </div>
    </div>
  );
}

function WireSectionHeaderSimple({ label }: { label: string }) {
  return (
    <span className="text-[10px] font-mono font-medium text-[#888] uppercase tracking-widest">
      {label}
    </span>
  );
}