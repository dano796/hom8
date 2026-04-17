import { useState } from "react";
import { useNavigate } from "react-router";
import {
  WireInput,
  WireButton,
  WireAnnotation,
  WireDivider,
} from "../components/WireComponents";

// Persistent home membership key (simulates Firebase Auth state)
const HF_HAS_HOME_KEY = "hf_has_home";

export function hasExistingHome(): boolean {
  return localStorage.getItem(HF_HAS_HOME_KEY) === "true";
}

export function setHasHome(value: boolean) {
  if (value) {
    localStorage.setItem(HF_HAS_HOME_KEY, "true");
  } else {
    localStorage.removeItem(HF_HAS_HOME_KEY);
  }
}

// Sub-screens: "welcome" | "login" | "setup"
type Step = "welcome" | "login" | "setup";

export function OnboardingScreen() {
  const navigate = useNavigate();
  const [step, setStep] = useState<Step>("welcome");
  const [setupMode, setSetupMode] = useState<"create" | "join" | null>(null);

  // After login: if user already has a home, skip setup entirely
  function handleLoginContinue() {
    if (hasExistingHome()) {
      navigate("/");
    } else {
      setStep("setup");
    }
  }

  // When user completes setup (joins or creates a home)
  function handleSetupComplete() {
    setHasHome(true);
    navigate("/");
  }

  return (
    <div className="flex flex-col flex-1 min-h-0 bg-[#F5F5F5] overflow-hidden">
      {/* Step indicator */}
      <div className="flex justify-center gap-1.5 pt-4 pb-0">
        {(["welcome", "login", "setup"] as Step[]).map((s) => (
          <button
            key={s}
            onClick={() => setStep(s)}
            className={`h-1 rounded-full transition-all ${
              step === s ? "w-6 bg-[#333]" : "w-2 bg-[#D0D0D0]"
            }`}
          />
        ))}
      </div>

      {/* Screen content */}
      <div className="flex-1 overflow-y-auto flex flex-col">
        {step === "welcome" && (
          <WelcomeStep onLogin={() => setStep("login")} onCreate={() => setStep("login")} />
        )}
        {step === "login" && (
          <LoginStep onContinue={handleLoginContinue} onBack={() => setStep("welcome")} />
        )}
        {step === "setup" && (
          <SetupStep
            mode={setupMode}
            onSelect={setSetupMode}
            onContinue={handleSetupComplete}
            onBack={() => setStep("login")}
          />
        )}
      </div>
    </div>
  );
}

// ─── Welcome ───
function WelcomeStep({
  onLogin,
  onCreate,
}: {
  onLogin: () => void;
  onCreate: () => void;
}) {
  return (
    <div className="flex-1 flex flex-col items-center justify-center px-8 gap-6">
      {/* Logo placeholder */}
      <div className="flex flex-col items-center gap-3">
        <div className="w-24 h-24 rounded-3xl bg-[#E8E8E8] border-2 border-[#D0D0D0] flex items-center justify-center relative overflow-hidden">
          <svg className="absolute inset-0 w-full h-full" viewBox="0 0 100 100" preserveAspectRatio="none">
            <line x1="0" y1="0" x2="100" y2="100" stroke="#C8C8C8" strokeWidth="1" vectorEffect="non-scaling-stroke" />
            <line x1="100" y1="0" x2="0" y2="100" stroke="#C8C8C8" strokeWidth="1" vectorEffect="non-scaling-stroke" />
          </svg>
          <span className="relative z-10 text-[10px] font-mono text-[#ABABAB]">LOGO</span>
        </div>
        <div className="text-center">
          <div className="text-[20px] font-mono font-medium text-[#222]">HomeFlow</div>
          <div className="text-[12px] font-mono text-[#888] mt-1">
            Your home, organized.
          </div>
          <div className="text-[9px] font-mono text-[#ABABAB] mt-2 max-w-[220px] text-center leading-relaxed">
            "No more forgotten tasks, unfair splits, or missed reminders"
          </div>
        </div>
      </div>

      <WireAnnotation label="splash-screen" />

      {/* Feature highlights */}
      <div className="w-full space-y-2">
        {[
          { icon: "✅", label: "Manage tasks together" },
          { icon: "💰", label: "Split expenses fairly" },
          { icon: "📅", label: "Stay in sync as a home" },
        ].map((f) => (
          <div key={f.label} className="flex items-center gap-3 px-3 py-2 bg-white border border-[#E8E8E8] rounded-xl">
            <span className="text-base">{f.icon}</span>
            <span className="text-[11px] font-mono text-[#555]">{f.label}</span>
          </div>
        ))}
      </div>

      <div className="w-full space-y-2">
        <WireButton variant="primary" fullWidth size="lg" onClick={onCreate}>
          CREATE ACCOUNT
        </WireButton>
        <WireButton variant="secondary" fullWidth size="lg" onClick={onLogin}>
          I ALREADY HAVE AN ACCOUNT
        </WireButton>
      </div>
    </div>
  );
}

// ─── Login / Register ───
function LoginStep({
  onContinue,
  onBack,
}: {
  onContinue: () => void;
  onBack: () => void;
}) {
  return (
    <div className="flex-1 flex flex-col px-6 pt-8 gap-4">
      <button onClick={onBack} className="flex items-center gap-1 text-[#888] self-start">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
          <path d="M15 18l-6-6 6-6" />
        </svg>
        <span className="text-[11px] font-mono">Back</span>
      </button>

      <div>
        <div className="text-[16px] font-mono font-medium text-[#222]">Sign in</div>
        <div className="text-[11px] font-mono text-[#888] mt-0.5">Enter your credentials to continue</div>
      </div>

      <WireAnnotation label="login-screen" />

      {/* Google SSO */}
      <button className="h-11 bg-white border border-[#D0D0D0] rounded-xl flex items-center justify-center gap-2.5">
        <div className="w-5 h-5 rounded bg-[#E8E8E8] border border-[#D0D0D0] flex items-center justify-center">
          <span className="text-[8px] font-mono text-[#888]">G</span>
        </div>
        <span className="text-[12px] font-mono text-[#444]">Continuar con Google</span>
        <WireAnnotation label="google-sso" />
      </button>

      <WireDivider label="or sign in with email" />

      {/* Email/password */}
      <div className="space-y-3">
        <WireInput label="Email" placeholder="ana@email.com" required />
        <div className="space-y-1">
          <WireInput label="Password" placeholder="••••••••" required />
          <button className="text-[10px] font-mono text-[#888] underline self-end block ml-auto">
            Forgot my password
          </button>
        </div>
      </div>

      <div className="space-y-2 mt-2">
        <WireButton variant="primary" fullWidth size="lg" onClick={onContinue}>
          SIGN IN
        </WireButton>

        <div className="text-center">
          <span className="text-[10px] font-mono text-[#ABABAB]">Don't have an account? </span>
          <button className="text-[10px] font-mono text-[#555] underline">Create one</button>
        </div>
      </div>

      {/* Error state hint */}
      <div className="bg-[#F5F5F5] border border-dashed border-[#DDD] rounded-xl p-3">
        <div className="text-[9px] font-mono text-[#ABABAB] mb-1 uppercase tracking-wider">Error state</div>
        <div className="h-9 bg-white border border-[#ABABAB] rounded px-3 flex items-center gap-2">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#888" strokeWidth="2" strokeLinecap="round">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          <span className="text-[10px] font-mono text-[#888]">Invalid email or password</span>
        </div>
      </div>
    </div>
  );
}

// ─── Setup Home ───
function SetupStep({
  mode,
  onSelect,
  onContinue,
  onBack,
}: {
  mode: "create" | "join" | null;
  onSelect: (m: "create" | "join") => void;
  onContinue: () => void;
  onBack: () => void;
}) {
  return (
    <div className="flex-1 flex flex-col px-6 pt-8 gap-5">
      <button onClick={onBack} className="flex items-center gap-1 text-[#888] self-start">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
          <path d="M15 18l-6-6 6-6" />
        </svg>
        <span className="text-[11px] font-mono">Back</span>
      </button>

      <div>
        <div className="text-[16px] font-mono font-medium text-[#222]">Set up your home</div>
        <div className="text-[11px] font-mono text-[#888] mt-0.5">Choose how you want to get started</div>
      </div>

      <WireAnnotation label="setup-home-screen" />

      {/* Options */}
      <div className="space-y-3">
        {/* Create new home */}
        <button
          onClick={() => onSelect("create")}
          className={`w-full text-left p-4 rounded-2xl border-2 flex items-start gap-3 ${
            mode === "create"
              ? "border-[#333] bg-[#F5F5F5]"
              : "border-[#E0E0E0] bg-white"
          }`}
        >
          <div className="w-12 h-12 rounded-xl bg-[#E8E8E8] border border-[#D0D0D0] flex items-center justify-center text-xl shrink-0">
            🏠
          </div>
          <div>
            <div className="text-[12px] font-mono font-medium text-[#333]">Create a new home</div>
            <div className="text-[10px] font-mono text-[#888] mt-0.5 leading-relaxed">
              Start fresh and invite your household members
            </div>
          </div>
          {mode === "create" && (
            <div className="ml-auto w-5 h-5 rounded-full bg-[#333] flex items-center justify-center shrink-0">
              <svg width="10" height="8" viewBox="0 0 10 8" fill="none">
                <path d="M1 4l3 3 5-5" stroke="white" strokeWidth="1.2" strokeLinecap="round" />
              </svg>
            </div>
          )}
        </button>

        {/* Expanded: create home form */}
        {mode === "create" && (
          <div className="ml-4 pl-3 border-l-2 border-[#D0D0D0] space-y-2">
            <WireInput label="Home name" placeholder="e.g. García Family" required />
            <div className="text-[9px] font-mono text-[#ABABAB]">
              An invite code will be generated after creation
            </div>
          </div>
        )}

        {/* Join existing home */}
        <button
          onClick={() => onSelect("join")}
          className={`w-full text-left p-4 rounded-2xl border-2 flex items-start gap-3 ${
            mode === "join"
              ? "border-[#333] bg-[#F5F5F5]"
              : "border-[#E0E0E0] bg-white"
          }`}
        >
          <div className="w-12 h-12 rounded-xl bg-[#E8E8E8] border border-[#D0D0D0] flex items-center justify-center text-xl shrink-0">
            🔗
          </div>
          <div>
            <div className="text-[12px] font-mono font-medium text-[#333]">Join an existing home</div>
            <div className="text-[10px] font-mono text-[#888] mt-0.5 leading-relaxed">
              Enter the invite code or link from your household
            </div>
          </div>
          {mode === "join" && (
            <div className="ml-auto w-5 h-5 rounded-full bg-[#333] flex items-center justify-center shrink-0">
              <svg width="10" height="8" viewBox="0 0 10 8" fill="none">
                <path d="M1 4l3 3 5-5" stroke="white" strokeWidth="1.2" strokeLinecap="round" />
              </svg>
            </div>
          )}
        </button>

        {/* Expanded: join form */}
        {mode === "join" && (
          <div className="ml-4 pl-3 border-l-2 border-[#D0D0D0] space-y-2">
            <WireInput label="Invite code or link" placeholder="e.g. HF-2847" required />
            {/* Preview card after code validation */}
            <div className="bg-white border border-[#E0E0E0] rounded-xl p-3">
              <div className="text-[9px] font-mono text-[#ABABAB] mb-1">Home preview (after validation)</div>
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-lg bg-[#E8E8E8] flex items-center justify-center text-sm">🏠</div>
                <div>
                  <div className="text-[11px] font-mono text-[#555]">García-López Home</div>
                  <div className="text-[9px] font-mono text-[#ABABAB]">3 members · Created Jan 2025</div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      <div className="mt-auto pb-4 space-y-2">
        <WireButton
          variant="primary"
          fullWidth
          size="lg"
          onClick={mode ? onContinue : undefined}
          className={!mode ? "opacity-40 cursor-not-allowed" : ""}
        >
          CONTINUE
        </WireButton>
        {!mode && (
          <div className="text-[9px] font-mono text-center text-[#ABABAB]">
            Select an option to continue
          </div>
        )}
      </div>
    </div>
  );
}