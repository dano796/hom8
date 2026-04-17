import { useNavigate } from "react-router";
import type { ReactNode } from "react";
import {
  WireAnnotation,
  WireTopBar,
} from "../components/WireComponents";

type NotifType = "task" | "due" | "overdue" | "comment" | "expense" | "member" | "digest";

interface Notification {
  type: NotifType;
  title: string;
  subtitle: string;
  time: string;
  unread: boolean;
  route?: string;
}

const NOTIF_ICON: Record<NotifType, ReactNode> = {
  task: (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2" strokeLinecap="round">
      <rect x="3" y="3" width="18" height="18" rx="2" />
      <path d="M9 12l2 2 4-4" />
    </svg>
  ),
  due: (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2" strokeLinecap="round">
      <circle cx="12" cy="12" r="10" />
      <polyline points="12,6 12,12 16,14" />
    </svg>
  ),
  overdue: (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2" strokeLinecap="round">
      <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" />
      <line x1="12" y1="9" x2="12" y2="13" />
      <line x1="12" y1="17" x2="12.01" y2="17" />
    </svg>
  ),
  comment: (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2" strokeLinecap="round">
      <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" />
    </svg>
  ),
  expense: (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2" strokeLinecap="round">
      <circle cx="12" cy="12" r="10" />
      <path d="M12 6v2m0 8v2M9 10c0-1.1.9-2 3-2s3 .9 3 2-3 3-3 3" />
    </svg>
  ),
  member: (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2" strokeLinecap="round">
      <path d="M16 21v-2a4 4 0 00-4-4H6a4 4 0 00-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <line x1="19" y1="8" x2="19" y2="14" />
      <line x1="22" y1="11" x2="16" y2="11" />
    </svg>
  ),
  digest: (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2" strokeLinecap="round">
      <line x1="8" y1="6" x2="21" y2="6" />
      <line x1="8" y1="12" x2="21" y2="12" />
      <line x1="8" y1="18" x2="21" y2="18" />
      <line x1="3" y1="6" x2="3.01" y2="6" />
      <line x1="3" y1="12" x2="3.01" y2="12" />
      <line x1="3" y1="18" x2="3.01" y2="18" />
    </svg>
  ),
};

const NOTIF_BG: Record<NotifType, string> = {
  task: "#444",
  due: "#666",
  overdue: "#333",
  comment: "#777",
  expense: "#555",
  member: "#888",
  digest: "#999",
};

const TODAY_NOTIFS: Notification[] = [
  {
    type: "task",
    title: "New task assigned to you",
    subtitle: '"Clean the kitchen" · Assigned by Carlos',
    time: "Just now",
    unread: true,
    route: "/tasks/detail",
  },
  {
    type: "due",
    title: "Task due in 2 hours",
    subtitle: '"Pay electric bill" · Due at 5:00pm',
    time: "30 min ago",
    unread: true,
    route: "/tasks/detail",
  },
  {
    type: "overdue",
    title: "Task is overdue",
    subtitle: '"Buy cleaning supplies" · Was due yesterday',
    time: "1h ago",
    unread: false,
    route: "/tasks/detail",
  },
];

const YESTERDAY_NOTIFS: Notification[] = [
  {
    type: "comment",
    title: "New comment in your task",
    subtitle: '"Buy groceries" · Carlos: "I\'ll take care of it!"',
    time: "1d ago",
    unread: false,
    route: "/tasks/detail",
  },
  {
    type: "expense",
    title: "New expense includes you",
    subtitle: '"Gas bill" · Ana registered $120.00',
    time: "1d ago",
    unread: false,
    route: "/expenses",
  },
];

const WEEK_NOTIFS: Notification[] = [
  {
    type: "member",
    title: "Luis García joined your home",
    subtitle: '"García-López Home" · Welcome message sent',
    time: "3d ago",
    unread: false,
  },
  {
    type: "digest",
    title: "Morning digest",
    subtitle: "3 tasks pending · 1 expense to settle · Good morning!",
    time: "5d ago",
    unread: false,
  },
  {
    type: "overdue",
    title: "2 tasks overdue",
    subtitle: "Review your task list for overdue items",
    time: "6d ago",
    unread: false,
    route: "/tasks",
  },
];

function NotifItem({
  notif,
  onClick,
}: {
  notif: Notification;
  onClick?: () => void;
}) {
  return (
    <button
      onClick={onClick}
      className={`w-full flex items-start gap-3 px-4 py-3 text-left ${
        notif.unread ? "bg-[#F8F8F8]" : "bg-white"
      } border-b border-[#F0F0F0]`}
    >
      {/* Icon */}
      <div
        className="w-8 h-8 rounded-full flex items-center justify-center shrink-0 mt-0.5"
        style={{ background: NOTIF_BG[notif.type] }}
      >
        {NOTIF_ICON[notif.type]}
      </div>

      {/* Content */}
      <div className="flex-1 min-w-0">
        <div className="flex items-start justify-between gap-2">
          <span
            className={`text-[12px] font-mono ${
              notif.unread ? "font-medium text-[#222]" : "text-[#555]"
            }`}
          >
            {notif.title}
          </span>
          {notif.unread && (
            <div className="w-2 h-2 rounded-full bg-[#333] shrink-0 mt-1.5" />
          )}
        </div>
        <div className="text-[10px] font-mono text-[#888] mt-0.5 leading-relaxed truncate">
          {notif.subtitle}
        </div>
        <div className="text-[9px] font-mono text-[#ABABAB] mt-0.5">{notif.time}</div>
      </div>
    </button>
  );
}

export function NotificationsScreen() {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col flex-1 min-h-0 bg-[#F2F2F2]">
      <WireTopBar
        title="Notificaciones"
        showBack
        onBack={() => navigate("/")}
        rightAction={
          <div className="flex items-center gap-2">
            <WireAnnotation label="mark-all-read" />
            <button className="text-[10px] font-mono text-[#888] underline">
              Mark all
            </button>
          </div>
        }
      />

      {/* Unread count summary */}
      <div className="bg-white border-b border-[#E8E8E8] px-4 py-2 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="w-5 h-5 bg-[#333] rounded-full flex items-center justify-center">
            <span className="text-[9px] text-white font-mono font-medium">3</span>
          </div>
          <span className="text-[11px] font-mono text-[#555]">unread notifications</span>
        </div>
        <WireAnnotation label="unread-badge" />
      </div>

      <div className="flex-1 overflow-y-auto bg-white">
        {/* TODAY */}
        <div className="px-4 py-2 bg-[#F5F5F5] border-b border-[#E8E8E8]">
          <span className="text-[9px] font-mono font-medium text-[#888] uppercase tracking-widest">
            Today
          </span>
        </div>
        {TODAY_NOTIFS.map((n, i) => (
          <NotifItem key={i} notif={n} onClick={() => n.route && navigate(n.route)} />
        ))}

        {/* YESTERDAY */}
        <div className="px-4 py-2 bg-[#F5F5F5] border-b border-[#E8E8E8] border-t border-t-[#E8E8E8]">
          <span className="text-[9px] font-mono font-medium text-[#888] uppercase tracking-widest">
            Yesterday
          </span>
        </div>
        {YESTERDAY_NOTIFS.map((n, i) => (
          <NotifItem key={i} notif={n} onClick={() => n.route && navigate(n.route)} />
        ))}

        {/* THIS WEEK */}
        <div className="px-4 py-2 bg-[#F5F5F5] border-b border-[#E8E8E8] border-t border-t-[#E8E8E8]">
          <span className="text-[9px] font-mono font-medium text-[#888] uppercase tracking-widest">
            This week
          </span>
        </div>
        {WEEK_NOTIFS.map((n, i) => (
          <NotifItem key={i} notif={n} onClick={() => n.route && navigate(n.route)} />
        ))}

        {/* Notification types legend */}
        <div className="px-4 py-4 border-t border-[#F0F0F0]">
          <div className="text-[9px] font-mono text-[#ABABAB] mb-2 uppercase tracking-wider">
            Notification types
          </div>
          <div className="grid grid-cols-2 gap-1.5">
            {[
              { type: "task" as NotifType, label: "Task assigned" },
              { type: "due" as NotifType, label: "Due soon (24h)" },
              { type: "overdue" as NotifType, label: "Task overdue" },
              { type: "comment" as NotifType, label: "New comment" },
              { type: "expense" as NotifType, label: "Expense added" },
              { type: "member" as NotifType, label: "Member joined" },
              { type: "digest" as NotifType, label: "Morning digest" },
            ].map((t) => (
              <div key={t.type} className="flex items-center gap-1.5">
                <div
                  className="w-4 h-4 rounded-full flex items-center justify-center shrink-0"
                  style={{ background: NOTIF_BG[t.type] }}
                >
                  <div className="scale-[0.6]">{NOTIF_ICON[t.type]}</div>
                </div>
                <span className="text-[9px] font-mono text-[#888]">{t.label}</span>
              </div>
            ))}
          </div>
          <WireAnnotation label="notification-types · 7 total" />
        </div>
      </div>
    </div>
  );
}