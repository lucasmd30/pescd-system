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

// `status` é o nome do enum (ex.: PLANO_ENVIADO) e define a cor; `label` é o texto.
export function StatusBadge({ label, status }) {
  const cls = status ? `badge st-${String(status).toLowerCase()}` : 'badge';
  return <span className={cls}>{label}</span>;
}
