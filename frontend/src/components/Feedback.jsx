export function ErrorMessage({ children }) {
  if (!children) return null;
  return <div className="alert alert-error">{children}</div>;
}

export function SuccessMessage({ children }) {
  if (!children) return null;
  return <div className="alert alert-success">{children}</div>;
}

export function Loading({ children = 'Carregando…' }) {
  return <div className="loading">{children}</div>;
}

export function StatusBadge({ label }) {
  return <span className="badge">{label}</span>;
}
