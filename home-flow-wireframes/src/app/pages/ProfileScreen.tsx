import { useNavigate } from "react-router";
import {
  WireAvatar,
  WireCard,
  WireButton,
  WireToggle,
  WireSegmented,
  WireAnnotation,
  WireTopBar,
  WireDivider,
} from "../components/WireComponents";
import { setHasHome } from "./OnboardingScreen";

const MEMBERS = [
  { label: "AG", name: "Ana García", role: "Admin", isYou: true },
  { label: "CG", name: "Carlos García", role: "Member", isYou: false },
  { label: "LG", name: "Luis García", role: "Member", isYou: false },
];

const NOTIFICATION_SETTINGS = [
  { label: "New task assigned to me", on: true },
  { label: "Task due reminders (24h)", on: true },
  { label: "Overdue task alerts", on: false },
  { label: "New comment in my tasks", on: true },
  { label: "Expense that includes me", on: true },
  { label: "Daily morning digest", on: false },
];

export function ProfileScreen() {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col flex-1 min-h-0 bg-[#F2F2F2]">
      <WireTopBar
        title="Perfil"
        rightAction={
          <div className="flex items-center gap-2">
            <WireAnnotation label="settings" />
            <button className="text-[#666]">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                <circle cx="12" cy="12" r="3" />
                <path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83 0 2 2 0 010-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 010-2.83 2 2 0 012.83 0l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 0 2 2 0 010 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z" />
              </svg>
            </button>
          </div>
        }
      />

      <div className="flex-1 overflow-y-auto p-4 space-y-4 pb-6">
        {/* Personal info */}
        <WireCard>
          <div className="flex items-center gap-3">
            <div className="relative">
              <WireAvatar size="lg" label="AG" />
              <button className="absolute -bottom-0.5 -right-0.5 w-5 h-5 bg-[#333] rounded-full flex items-center justify-center border-2 border-white">
                <svg width="8" height="8" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2" strokeLinecap="round">
                  <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" />
                  <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z" />
                </svg>
              </button>
              <WireAnnotation label="avatar (edit)" />
            </div>
            <div className="flex-1">
              <div className="text-[14px] font-mono font-medium text-[#222]">Ana García</div>
              <div className="text-[11px] font-mono text-[#888]">ana.garcia@email.com</div>
              <div className="text-[9px] font-mono text-[#ABABAB] mt-0.5">Member since Jan 2025</div>
            </div>
            <button className="text-[9px] font-mono text-[#888] underline">Edit</button>
          </div>
        </WireCard>

        {/* Personal stats */}
        <div>
          <div className="flex items-center gap-2 mb-2">
            <span className="text-[11px] font-mono font-medium text-[#555] uppercase tracking-wider">
              My stats
            </span>
            <WireAnnotation label="statistics" />
          </div>
          <div className="grid grid-cols-3 gap-2">
            {[
              { label: "Tareas completadas", value: "23" },
              { label: "Racha", value: "7 days 🔥" },
              { label: "Puntos", value: "450 pts" },
            ].map((stat) => (
              <WireCard key={stat.label} className="text-center">
                <div className="text-[15px] font-mono font-medium text-[#333]">{stat.value}</div>
                <div className="text-[8px] font-mono text-[#AAA] mt-0.5">{stat.label}</div>
              </WireCard>
            ))}
          </div>
        </div>

        <WireDivider />

        {/* My home */}
        <WireCard>
          <div className="flex items-center justify-between mb-2">
            <span className="text-[10px] font-mono text-[#888] uppercase tracking-wider">My Home</span>
            <WireAnnotation label="home-info" />
          </div>
          <div className="flex items-center gap-2 mb-3">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#888" strokeWidth="2" strokeLinecap="round">
              <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z" />
            </svg>
            <span className="text-[13px] font-mono font-medium text-[#333]">García-López Home</span>
          </div>

          {/* Invite code */}
          <div className="bg-[#F5F5F5] border border-[#E8E8E8] rounded-lg p-2 mb-3">
            <div className="text-[9px] font-mono text-[#ABABAB] mb-0.5">Invite code (expires in 48h)</div>
            <div className="flex items-center justify-between">
              <span className="text-[14px] font-mono font-medium text-[#333] tracking-widest">HF-2847</span>
              <button className="flex items-center gap-1 text-[10px] font-mono text-[#666] bg-white border border-[#DDD] px-2 py-1 rounded">
                <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                  <rect x="9" y="9" width="13" height="13" rx="2" />
                  <path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1" />
                </svg>
                Copy
              </button>
            </div>
          </div>

          {/* Members list */}
          <div className="space-y-2">
            {MEMBERS.map((m, i) => (
              <div key={i} className="flex items-center gap-2">
                <WireAvatar size="sm" label={m.label} />
                <div className="flex-1">
                  <span className="text-[11px] font-mono text-[#444]">{m.name}</span>
                  {m.isYou && (
                    <span className="text-[8px] font-mono text-[#ABABAB] ml-1">(you)</span>
                  )}
                </div>
                <span
                  className={`text-[8px] font-mono px-2 py-0.5 rounded-full border ${
                    m.role === "Admin"
                      ? "bg-[#333] text-white border-[#333]"
                      : "bg-[#F0F0F0] text-[#888] border-[#E0E0E0]"
                  }`}
                >
                  {m.role}
                </span>
                {!m.isYou && (
                  <button className="text-[#CCC]">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <circle cx="12" cy="5" r="1" fill="currentColor" />
                      <circle cx="12" cy="12" r="1" fill="currentColor" />
                      <circle cx="12" cy="19" r="1" fill="currentColor" />
                    </svg>
                  </button>
                )}
              </div>
            ))}
          </div>

          <div className="text-[8px] font-mono text-[#ABABAB] mt-2 border-t border-[#F0F0F0] pt-2">
            Admin can: change roles, remove members, rename home
          </div>
        </WireCard>

        <WireDivider />

        {/* Notifications */}
        <div>
          <div className="flex items-center gap-2 mb-2">
            <span className="text-[11px] font-mono font-medium text-[#555] uppercase tracking-wider">
              Notifications
            </span>
            <WireAnnotation label="toggles" />
          </div>
          <WireCard>
            <div className="space-y-3">
              {NOTIFICATION_SETTINGS.map((n, i) => (
                <WireToggle key={i} on={n.on} label={n.label} />
              ))}
            </div>
          </WireCard>
        </div>

        <WireDivider />

        {/* Settings */}
        <div>
          <div className="flex items-center gap-2 mb-2">
            <span className="text-[11px] font-mono font-medium text-[#555] uppercase tracking-wider">
              Settings
            </span>
            <WireAnnotation label="app-settings" />
          </div>
          <WireCard>
            <div className="space-y-3">
              {/* Theme */}
              <div className="space-y-1">
                <span className="text-[10px] font-mono text-[#888]">Theme</span>
                <WireSegmented options={["Light", "Dark"]} selected="Light" />
              </div>
              {/* Language */}
              <div className="flex items-center justify-between">
                <span className="text-[12px] font-mono text-[#444]">Language</span>
                <div className="flex items-center gap-1 text-[11px] font-mono text-[#666] bg-[#F5F5F5] border border-[#E0E0E0] px-2 py-1 rounded">
                  English (EN) ▼
                </div>
              </div>
              {/* Currency */}
              <div className="flex items-center justify-between">
                <span className="text-[12px] font-mono text-[#444]">Currency</span>
                <div className="flex items-center gap-1 text-[11px] font-mono text-[#666] bg-[#F5F5F5] border border-[#E0E0E0] px-2 py-1 rounded">
                  USD ($) ▼
                </div>
              </div>
            </div>
          </WireCard>
        </div>

        <WireDivider />

        {/* Log out */}
        <div className="space-y-2">
          <WireAnnotation label="destructive-action" />
          <WireButton
            variant="destructive"
            fullWidth
            size="lg"
            onClick={() => {
              setHasHome(false);
              navigate("/onboarding");
            }}
          >
            LOG OUT
          </WireButton>
          <p className="text-[9px] font-mono text-[#ABABAB] text-center">
            You will be redirected to the login screen
          </p>
        </div>
      </div>
    </div>
  );
}