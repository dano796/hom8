import { createBrowserRouter } from "react-router";
import { WireframeLayout } from "./components/WireframeLayout";
import { DashboardScreen } from "./pages/DashboardScreen";
import { TasksListScreen } from "./pages/TasksListScreen";
import { CreateTaskScreen } from "./pages/CreateTaskScreen";
import { TaskDetailScreen } from "./pages/TaskDetailScreen";
import { CalendarScreen } from "./pages/CalendarScreen";
import { ExpensesScreen } from "./pages/ExpensesScreen";
import { CreateExpenseScreen } from "./pages/CreateExpenseScreen";
import { BalancesScreen } from "./pages/BalancesScreen";
import { ProfileScreen } from "./pages/ProfileScreen";
import { NotificationsScreen } from "./pages/NotificationsScreen";
import { OnboardingScreen } from "./pages/OnboardingScreen";

export const router = createBrowserRouter([
  {
    path: "/",
    Component: WireframeLayout,
    children: [
      { index: true, Component: DashboardScreen },
      { path: "onboarding", Component: OnboardingScreen },
      { path: "tasks", Component: TasksListScreen },
      { path: "tasks/create", Component: CreateTaskScreen },
      { path: "tasks/detail", Component: TaskDetailScreen },
      { path: "calendar", Component: CalendarScreen },
      { path: "expenses", Component: ExpensesScreen },
      { path: "expenses/create", Component: CreateExpenseScreen },
      { path: "expenses/balances", Component: BalancesScreen },
      { path: "profile", Component: ProfileScreen },
      { path: "notifications", Component: NotificationsScreen },
    ],
  },
]);
