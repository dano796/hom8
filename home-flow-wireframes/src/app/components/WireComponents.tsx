import React from "react";

// ─────────────────────────────────────────
// Wireframe Primitive Components
// Low-fidelity · Grayscale · Annotated
// ─────────────────────────────────────────

// Avatar placeholder
export function WireAvatar({
  size = "md",
  label = "A",
  active = false,
}: {
  size?: "xs" | "sm" | "md" | "lg";
  label?: string;
  active?: boolean;
}) {
  const sizeMap = {
    xs: "w-5 h-5 text-[7px]",
    sm: "w-7 h-7 text-[9px]",
    md: "w-10 h-10 text-xs",
    lg: "w-14 h-14 text-sm",
  };
  return (
    <div
      className={`${sizeMap[size]} rounded-full bg-[#C8C8C8] border-2 border-[#A8A8A8] flex items-center justify-center text-[#555] font-mono shrink-0 select-none ${
        active ? "ring-2 ring-offset-1 ring-[#333]" : ""
      }`}
    >
      {label}
    </div>
  );
}

// Avatar group (overlapping)
export function WireAvatarGroup({ labels = ["A", "B", "C"] }: { labels?: string[] }) {
  return (
    <div className="flex -space-x-2">
      {labels.map((l, i) => (
        <div
          key={i}
          className="w-7 h-7 rounded-full bg-[#C8C8C8] border-2 border-white flex items-center justify-center text-[9px] text-[#555] font-mono"
        >
          {l}
        </div>
      ))}
    </div>
  );
}

// Image placeholder (X-through box)
export function WireBox({
  width,
  height,
  label,
  className = "",
}: {
  width?: number | string;
  height?: number | string;
  label?: string;
  className?: string;
}) {
  return (
    <div
      className={`bg-[#E4E4E4] border border-[#C0C0C0] flex items-center justify-center relative overflow-hidden ${className}`}
      style={{ width, height }}
    >
      <svg
        className="absolute inset-0 w-full h-full"
        preserveAspectRatio="none"
        viewBox="0 0 100 100"
      >
        <line x1="0" y1="0" x2="100" y2="100" stroke="#C0C0C0" strokeWidth="1" vectorEffect="non-scaling-stroke" />
        <line x1="100" y1="0" x2="0" y2="100" stroke="#C0C0C0" strokeWidth="1" vectorEffect="non-scaling-stroke" />
      </svg>
      {label && (
        <span className="relative z-10 text-[10px] text-[#888] bg-white/70 px-1 rounded">
          {label}
        </span>
      )}
    </div>
  );
}

// Horizontal placeholder text lines
export function WirePlaceholderLines({
  lines = 2,
  lastShort = true,
}: {
  lines?: number;
  lastShort?: boolean;
}) {
  return (
    <div className="space-y-1.5">
      {Array.from({ length: lines }).map((_, i) => (
        <div
          key={i}
          className="h-2.5 bg-[#DCDCDC] rounded-full"
          style={{
            width: i === lines - 1 && lastShort ? "60%" : "100%",
          }}
        />
      ))}
    </div>
  );
}

// Button
export function WireButton({
  variant = "primary",
  children,
  className = "",
  fullWidth = false,
  size = "md",
  onClick,
}: {
  variant?: "primary" | "secondary" | "destructive" | "ghost" | "text";
  children: React.ReactNode;
  className?: string;
  fullWidth?: boolean;
  size?: "sm" | "md" | "lg";
  onClick?: () => void;
}) {
  const variants = {
    primary: "bg-[#222] text-white border border-[#222]",
    secondary: "bg-white text-[#333] border border-[#444]",
    destructive: "bg-white text-[#888] border border-[#AAA] line-through-none",
    ghost: "bg-[#F0F0F0] text-[#444] border border-[#DDD]",
    text: "text-[#555] underline border border-transparent",
  };
  const sizes = {
    sm: "py-1.5 px-3 text-[11px] rounded",
    md: "py-2.5 px-4 text-[12px] rounded-md",
    lg: "py-3.5 px-6 text-[13px] rounded-md",
  };
  return (
    <button
      onClick={onClick}
      className={`${variants[variant]} ${sizes[size]} ${
        fullWidth ? "w-full" : ""
      } font-mono font-medium tracking-wide ${className}`}
    >
      {children}
    </button>
  );
}

// Input field
export function WireInput({
  label,
  placeholder = "Type here...",
  required = false,
  note,
  className = "",
}: {
  label?: string;
  placeholder?: string;
  required?: boolean;
  note?: string;
  className?: string;
}) {
  return (
    <div className={`space-y-1 ${className}`}>
      {label && (
        <div className="flex items-center gap-1">
          <span className="text-[11px] text-[#555] font-mono font-medium uppercase tracking-wider">
            {label}
          </span>
          {required && <span className="text-[10px] text-[#888] font-mono">*required</span>}
        </div>
      )}
      <div className="h-10 bg-white border border-[#C8C8C8] rounded px-3 flex items-center text-[#ABABAB] text-[12px] font-mono">
        {placeholder}
      </div>
      {note && <div className="text-[10px] text-[#999] font-mono">{note}</div>}
    </div>
  );
}

// Textarea
export function WireTextarea({
  label,
  placeholder = "Type here...",
  rows = 3,
  className = "",
}: {
  label?: string;
  placeholder?: string;
  rows?: number;
  className?: string;
}) {
  const heightMap: Record<number, string> = {
    2: "h-16",
    3: "h-24",
    4: "h-32",
  };
  return (
    <div className={`space-y-1 ${className}`}>
      {label && (
        <span className="text-[11px] text-[#555] font-mono font-medium uppercase tracking-wider">
          {label}
        </span>
      )}
      <div
        className={`${heightMap[rows] || "h-24"} bg-white border border-[#C8C8C8] rounded px-3 py-2 text-[#ABABAB] text-[12px] font-mono`}
      >
        {placeholder}
      </div>
    </div>
  );
}

// Chip / Tag
export function WireChip({
  label,
  active = false,
  dot,
  className = "",
  onClick,
}: {
  label: string;
  active?: boolean;
  dot?: string;
  className?: string;
  onClick?: () => void;
}) {
  return (
    <button
      onClick={onClick}
      className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-mono border shrink-0 ${
        active
          ? "bg-[#333] text-white border-[#333]"
          : "bg-white text-[#555] border-[#C8C8C8]"
      } ${className}`}
    >
      {dot && (
        <span
          className="w-1.5 h-1.5 rounded-full shrink-0"
          style={{ background: dot }}
        />
      )}
      {label}
    </button>
  );
}

// Priority badge
export function WirePriorityBadge({ level }: { level: "high" | "medium" | "low" }) {
  const map = {
    high: { label: "HIGH", cls: "bg-[#333] text-white" },
    medium: { cls: "bg-[#888] text-white", label: "MED" },
    low: { cls: "bg-[#DDD] text-[#666]", label: "LOW" },
  };
  const { label, cls } = map[level];
  return (
    <span className={`inline-block px-1.5 py-0.5 rounded text-[8px] font-mono font-medium ${cls}`}>
      {label}
    </span>
  );
}

// Status chip
export function WireStatusChip({ status }: { status: "pending" | "in-progress" | "done" | "overdue" }) {
  const map = {
    pending: { label: "PENDING", cls: "bg-[#F0F0F0] text-[#666] border-[#CCC]" },
    "in-progress": { label: "IN PROGRESS", cls: "bg-[#E8E8E8] text-[#444] border-[#BBB]" },
    done: { label: "DONE ✓", cls: "bg-[#DCDCDC] text-[#333] border-[#BBB]" },
    overdue: { label: "⚠ OVERDUE", cls: "bg-[#EEE] text-[#555] border-[#AAA]" },
  };
  const { label, cls } = map[status];
  return (
    <span className={`inline-block px-2 py-0.5 rounded-full text-[9px] font-mono font-medium border ${cls}`}>
      {label}
    </span>
  );
}

// Checkbox (wireframe style)
export function WireCheckbox({ checked = false }: { checked?: boolean }) {
  return (
    <div
      className={`w-5 h-5 rounded border-2 shrink-0 flex items-center justify-center ${
        checked ? "bg-[#333] border-[#333]" : "bg-white border-[#ABABAB]"
      }`}
    >
      {checked && (
        <svg width="10" height="8" viewBox="0 0 10 8" fill="none">
          <path d="M1 4l3 3 5-6" stroke="white" strokeWidth="1.5" strokeLinecap="round" />
        </svg>
      )}
    </div>
  );
}

// Toggle switch (wireframe)
export function WireToggle({ on = false, label }: { on?: boolean; label?: string }) {
  return (
    <div className="flex items-center justify-between">
      {label && <span className="text-[12px] text-[#444] font-mono">{label}</span>}
      <div
        className={`w-10 h-5 rounded-full relative border ${
          on ? "bg-[#444] border-[#333]" : "bg-[#E0E0E0] border-[#C8C8C8]"
        }`}
      >
        <div
          className={`absolute top-0.5 w-4 h-4 rounded-full bg-white border border-[#C8C8C8] transition-all ${
            on ? "right-0.5" : "left-0.5"
          }`}
        />
      </div>
    </div>
  );
}

// Segmented control
export function WireSegmented({
  options,
  selected,
  onSelect,
}: {
  options: string[];
  selected: string;
  onSelect?: (v: string) => void;
}) {
  return (
    <div className="flex rounded-lg border border-[#C8C8C8] overflow-hidden bg-white">
      {options.map((opt) => (
        <button
          key={opt}
          onClick={() => onSelect?.(opt)}
          className={`flex-1 py-2 text-[11px] font-mono font-medium border-r last:border-r-0 border-[#C8C8C8] ${
            selected === opt ? "bg-[#222] text-white" : "text-[#666] bg-white"
          }`}
        >
          {opt}
        </button>
      ))}
    </div>
  );
}

// Section header
export function WireSectionHeader({
  title,
  action,
  onAction,
}: {
  title: string;
  action?: string;
  onAction?: () => void;
}) {
  return (
    <div className="flex items-center justify-between mb-2">
      <span className="text-[11px] font-mono font-medium text-[#555] uppercase tracking-widest">
        {title}
      </span>
      {action && (
        <button
          onClick={onAction}
          className="text-[10px] font-mono text-[#888] underline"
        >
          {action}
        </button>
      )}
    </div>
  );
}

// Divider
export function WireDivider({ label }: { label?: string }) {
  return (
    <div className="flex items-center gap-2 my-2">
      <div className="flex-1 h-px bg-[#E0E0E0]" />
      {label && (
        <span className="text-[9px] font-mono text-[#ABABAB] uppercase tracking-wider">
          {label}
        </span>
      )}
      <div className="flex-1 h-px bg-[#E0E0E0]" />
    </div>
  );
}

// Card wrapper
export function WireCard({
  children,
  className = "",
  onClick,
}: {
  children: React.ReactNode;
  className?: string;
  onClick?: () => void;
}) {
  return (
    <div
      onClick={onClick}
      className={`bg-white border border-[#E0E0E0] rounded-xl p-3 ${
        onClick ? "cursor-pointer active:bg-[#F8F8F8]" : ""
      } ${className}`}
    >
      {children}
    </div>
  );
}

// Empty state
export function WireEmptyState({
  icon,
  title,
  subtitle,
  cta,
}: {
  icon?: React.ReactNode;
  title: string;
  subtitle?: string;
  cta?: string;
}) {
  return (
    <div className="flex flex-col items-center justify-center py-12 px-6 gap-3">
      <div className="w-20 h-20 rounded-full bg-[#EBEBEB] border-2 border-dashed border-[#C8C8C8] flex items-center justify-center">
        {icon || (
          <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
            <rect x="4" y="8" width="24" height="18" rx="2" stroke="#C8C8C8" strokeWidth="2" />
            <path d="M10 16h12M10 20h8" stroke="#C8C8C8" strokeWidth="2" strokeLinecap="round" />
          </svg>
        )}
      </div>
      <div className="text-center">
        <div className="text-[13px] font-mono font-medium text-[#555]">{title}</div>
        {subtitle && (
          <div className="text-[11px] font-mono text-[#999] mt-1">{subtitle}</div>
        )}
      </div>
      {cta && (
        <WireButton variant="secondary" size="sm">
          {cta}
        </WireButton>
      )}
    </div>
  );
}

// Annotation label (small overlay label for wireframe documentation)
export function WireAnnotation({ label }: { label: string }) {
  return (
    <span className="inline-block text-[8px] font-mono text-[#ABABAB] bg-[#F5F5F5] border border-[#DCDCDC] px-1 py-0.5 rounded uppercase tracking-wider">
      {label}
    </span>
  );
}

// Skeleton block (loading state)
export function WireSkeleton({ height = 40, className = "" }: { height?: number; className?: string }) {
  return (
    <div
      className={`bg-[#E8E8E8] rounded animate-pulse ${className}`}
      style={{ height }}
    />
  );
}

// FAB button
export function WireFAB({ onClick }: { onClick?: () => void }) {
  return (
    <button
      onClick={onClick}
      className="absolute bottom-20 right-4 w-14 h-14 rounded-full bg-[#222] text-white shadow-lg flex items-center justify-center text-2xl border-2 border-[#333] z-10"
    >
      +
    </button>
  );
}

// Progress bar
export function WireProgressBar({
  value,
  label,
  sublabel,
}: {
  value: number;
  label?: string;
  sublabel?: string;
}) {
  return (
    <div className="space-y-1">
      <div className="flex justify-between">
        {label && <span className="text-[10px] font-mono text-[#666]">{label}</span>}
        {sublabel && <span className="text-[10px] font-mono text-[#999]">{sublabel}</span>}
      </div>
      <div className="h-3 bg-[#E8E8E8] rounded-full overflow-hidden border border-[#D0D0D0]">
        <div
          className="h-full bg-[#555] rounded-full"
          style={{ width: `${value}%` }}
        />
      </div>
    </div>
  );
}

// Screen title bar (inside phone)
export function WireTopBar({
  title,
  showBack = false,
  rightAction,
  onBack,
}: {
  title: string;
  showBack?: boolean;
  rightAction?: React.ReactNode;
  onBack?: () => void;
}) {
  return (
    <div className="flex items-center h-12 px-4 bg-white border-b border-[#E8E8E8] shrink-0 gap-3">
      {showBack && (
        <button onClick={onBack} className="text-[#555] mr-1">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
            <path d="M15 18l-6-6 6-6" />
          </svg>
        </button>
      )}
      <span className="flex-1 text-[14px] font-mono font-medium text-[#222] truncate">{title}</span>
      {rightAction}
    </div>
  );
}

// Category icon box
export function WireCategoryBox({
  label,
  emoji,
  active = false,
  onClick,
}: {
  label: string;
  emoji: string;
  active?: boolean;
  onClick?: () => void;
}) {
  return (
    <button
      onClick={onClick}
      className={`flex flex-col items-center gap-1 p-2 rounded-xl border ${
        active
          ? "bg-[#333] border-[#333] text-white"
          : "bg-white border-[#DCDCDC] text-[#555]"
      }`}
    >
      <span className="text-lg">{emoji}</span>
      <span className="text-[9px] font-mono">{label}</span>
    </button>
  );
}
