import { useState } from "react";
import { Outlet, useNavigate, useLocation } from "react-router";
import { BottomNav } from "./BottomNav";

// Status bar inside phone
function StatusBar() {
  return (
    <div className="flex items-center justify-between px-5 h-10 bg-white shrink-0 border-b border-[#F5F5F5]">
      <span className="text-[11px] font-mono font-medium text-[#222]">9:41</span>
      <div className="flex items-center gap-1.5">
        <div className="flex items-end gap-0.5">
          {[3, 5, 7, 9].map((h, i) => (
            <div key={i} className="w-1 bg-[#333] rounded-sm" style={{ height: h }} />
          ))}
        </div>
        <svg width="14" height="10" viewBox="0 0 14 10" fill="none">
          <path d="M1 3.5C3.5 1 10.5 1 13 3.5" stroke="#333" strokeWidth="1.2" strokeLinecap="round" />
          <path d="M3 6C4.6 4.4 9.4 4.4 11 6" stroke="#333" strokeWidth="1.2" strokeLinecap="round" />
          <circle cx="7" cy="9" r="1" fill="#333" />
        </svg>
        <div className="flex items-center gap-0.5">
          <div className="w-6 h-3 rounded-sm border border-[#333] relative flex items-center px-0.5">
            <div className="h-1.5 bg-[#333] rounded-sm" style={{ width: "70%" }} />
          </div>
          <div className="w-0.5 h-1.5 bg-[#333] rounded-sm" />
        </div>
      </div>
    </div>
  );
}

const SCREEN_GROUPS = [
  {
    group: "Inicio",
    items: [
      { label: "Welcome & Login", path: "/onboarding", badge: "OB-1" },
    ],
  },
  {
    group: "Main Flow",
    items: [
      { label: "Dashboard", path: "/", badge: "HM" },
      { label: "Lista de Tareas", path: "/tasks", badge: "TL" },
      { label: "Crear Tarea", path: "/tasks/create", badge: "CT", indent: true },
      { label: "Detalle de Tarea", path: "/tasks/detail", badge: "TD", indent: true },
      { label: "Calendario", path: "/calendar", badge: "CA" },
      { label: "Gastos", path: "/expenses", badge: "EX" },
      { label: "Crear Gasto", path: "/expenses/create", badge: "CE", indent: true },
      { label: "Balances", path: "/expenses/balances", badge: "BA", indent: true },
      { label: "Perfil", path: "/profile", badge: "PR" },
      { label: "Notificaciones", path: "/notifications", badge: "NT" },
    ],
  },
];

export function WireframeLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const currentScreen = SCREEN_GROUPS.flatMap((g) => g.items).find(
    (item) => {
      if (item.path === "/") return location.pathname === "/";
      return location.pathname.startsWith(item.path);
    }
  );

  return (
    <div className="min-h-screen bg-[#E8E6E1] flex items-stretch">
      {/* ─── Sidebar ─── */}
      <aside
        className={`fixed top-0 left-0 h-full z-30 bg-white border-r border-[#E0E0E0] flex flex-col transition-all duration-200 ${
          sidebarOpen ? "w-56" : "w-0 overflow-hidden"
        } lg:relative lg:w-56 lg:flex-shrink-0 lg:overflow-visible`}
      >
        {/* Header */}
        <div className="px-4 pt-6 pb-3 border-b border-[#EBEBEB]">
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 rounded bg-[#333] flex items-center justify-center">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5">
                <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z" />
              </svg>
            </div>
            <div>
              <div className="text-[12px] font-mono font-medium text-[#222]">HomeFlow</div>
              <div className="text-[9px] font-mono text-[#ABABAB]">Wireframes v1.1</div>
            </div>
          </div>
        </div>

        {/* Screen list */}
        <nav className="flex-1 overflow-y-auto py-2 px-2">
          {SCREEN_GROUPS.map((group) => (
            <div key={group.group} className="mb-3">
              <div className="px-2 py-1.5">
                <span className="text-[9px] font-mono font-medium text-[#ABABAB] uppercase tracking-widest">
                  {group.group}
                </span>
              </div>
              {group.items.map((item) => {
                const isActive =
                  item.path === "/"
                    ? location.pathname === "/"
                    : location.pathname.startsWith(item.path);
                return (
                  <button
                    key={item.path}
                    onClick={() => {
                      navigate(item.path);
                      setSidebarOpen(false);
                    }}
                    className={`w-full flex items-center gap-2 px-2 py-1.5 rounded-lg text-left mb-0.5 ${
                      item.indent ? "ml-3 w-[calc(100%-12px)]" : ""
                    } ${
                      isActive
                        ? "bg-[#F0F0F0] text-[#222]"
                        : "text-[#666] hover:bg-[#F8F8F8]"
                    }`}
                  >
                    <span
                      className={`text-[8px] font-mono px-1 rounded shrink-0 ${
                        isActive
                          ? "bg-[#333] text-white"
                          : "bg-[#EBEBEB] text-[#888]"
                      }`}
                    >
                      {item.badge}
                    </span>
                    <span className="text-[11px] font-mono">{item.label}</span>
                  </button>
                );
              })}
            </div>
          ))}
        </nav>

        {/* Footer */}
        <div className="px-4 py-3 border-t border-[#EBEBEB]">
          <div className="text-[8px] font-mono text-[#CCC] leading-relaxed">
            Low-fidelity · Grayscale<br />
            Base: 390×844pt · MVP
          </div>
        </div>
      </aside>

      {/* ─── Mobile overlay ─── */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black/30 z-20 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* ─── Main content ─── */}
      <main className="flex-1 flex flex-col items-center justify-center lg:justify-start lg:pt-8 lg:pb-8 relative">
        {/* Top toolbar (above phone) */}
        <div className="w-full flex items-center justify-between px-4 lg:px-8 py-3 lg:mb-4">
          {/* Hamburger */}
          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="lg:hidden flex flex-col gap-1 p-2 rounded-lg bg-white/80 border border-[#E0E0E0]"
          >
            <div className="w-4 h-0.5 bg-[#555]" />
            <div className="w-4 h-0.5 bg-[#555]" />
            <div className="w-4 h-0.5 bg-[#555]" />
          </button>

          {/* Current screen label */}
          <div className="flex items-center gap-2">
            <div className="hidden lg:flex items-center gap-1.5 text-[11px] font-mono text-[#888]">
              <span className="text-[9px] bg-[#333] text-white px-1.5 py-0.5 rounded">
                {currentScreen?.badge || "??"}
              </span>
              <span>{currentScreen?.label || "-"}</span>
            </div>
          </div>

          {/* Navigation arrows */}
          <div className="flex items-center gap-1.5">
            <button
              onClick={() => {
                const allItems = SCREEN_GROUPS.flatMap((g) => g.items);
                const idx = allItems.findIndex((item) =>
                  item.path === "/"
                    ? location.pathname === "/"
                    : location.pathname.startsWith(item.path)
                );
                if (idx > 0) navigate(allItems[idx - 1].path);
              }}
              className="w-8 h-8 rounded-lg bg-white/80 border border-[#E0E0E0] flex items-center justify-center text-[#666]"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                <path d="M15 18l-6-6 6-6" />
              </svg>
            </button>
            <button
              onClick={() => {
                const allItems = SCREEN_GROUPS.flatMap((g) => g.items);
                const idx = allItems.findIndex((item) =>
                  item.path === "/"
                    ? location.pathname === "/"
                    : location.pathname.startsWith(item.path)
                );
                if (idx < allItems.length - 1) navigate(allItems[idx + 1].path);
              }}
              className="w-8 h-8 rounded-lg bg-white/80 border border-[#E0E0E0] flex items-center justify-center text-[#666]"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                <path d="M9 18l6-6-6-6" />
              </svg>
            </button>
          </div>
        </div>

        {/* ─── Phone Frame ─── */}
        <div
          className="relative flex flex-col mx-auto flex-shrink-0"
          style={{
            width: 390,
            height: 844,
            background: "#1C1C1E",
            borderRadius: 50,
            padding: 12,
            boxShadow:
              "0 0 0 1.5px #3A3A3A, 0 40px 100px rgba(0,0,0,0.4), inset 0 0 0 1px #2A2A2A",
          }}
        >
          {/* Side buttons (decorative) */}
          <div className="absolute -left-[3px] top-24 w-[3px] h-8 bg-[#3A3A3A] rounded-l-sm" />
          <div className="absolute -left-[3px] top-36 w-[3px] h-12 bg-[#3A3A3A] rounded-l-sm" />
          <div className="absolute -left-[3px] top-52 w-[3px] h-12 bg-[#3A3A3A] rounded-l-sm" />
          <div className="absolute -right-[3px] top-32 w-[3px] h-16 bg-[#3A3A3A] rounded-r-sm" />

          {/* Screen */}
          <div
            className="flex flex-col overflow-hidden bg-[#F2F2F2]"
            style={{ borderRadius: 40, height: "100%" }}
          >
            {/* Dynamic Island / notch */}
            <div className="flex justify-center pt-2.5 pb-0 bg-[#F2F2F2]">
              <div className="w-28 h-8 bg-[#1C1C1E] rounded-full" />
            </div>

            <StatusBar />

            {/* Screen content */}
            <div className="flex-1 flex flex-col min-h-0">
              <Outlet />
            </div>

            <BottomNav />
          </div>

          {/* Home indicator */}
          <div className="flex justify-center pt-1.5">
            <div className="w-28 h-1 bg-[#3A3A3A] rounded-full" />
          </div>
        </div>

        {/* Below phone: screen counter */}
        <div className="mt-4 flex items-center gap-2">
          {SCREEN_GROUPS.flatMap((g) => g.items).map((item, i) => {
            const isActive =
              item.path === "/"
                ? location.pathname === "/"
                : location.pathname.startsWith(item.path);
            return (
              <button
                key={item.path}
                onClick={() => navigate(item.path)}
                className={`w-1.5 h-1.5 rounded-full transition-all ${
                  isActive ? "bg-[#555] w-4" : "bg-[#C8C8C8]"
                }`}
              />
            );
          })}
        </div>

        {/* Screen name below dots */}
        <div className="mt-2 text-[10px] font-mono text-[#AAA]">
          {currentScreen?.badge} · {currentScreen?.label}
        </div>
      </main>
    </div>
  );
}