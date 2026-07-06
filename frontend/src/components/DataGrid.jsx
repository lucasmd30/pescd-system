import { formatDateTime } from '../utils/format.js';

// Grid de dados só-leitura (espelha o .info-grid do sistema antigo).
// items: [{ label, value, span2 }]
export function InfoGrid({ items }) {
  return (
    <div className="info-grid">
      {items.filter((it) => it && it.value != null && it.value !== '').map((it, i) => (
        <div key={i} className={it.span2 ? 'info-item span-2' : 'info-item'}>
          <label>{it.label}</label>
          <span>{it.value}</span>
        </div>
      ))}
    </div>
  );
}

// Linha do tempo do histórico de status.
export function StatusTimeline({ logs }) {
  if (!logs || logs.length === 0) {
    return <p className="muted">Nenhum histórico registrado.</p>;
  }
  return (
    <ul className="timeline">
      {logs.map((log) => (
        <li key={log.id}>
          <span className="dot" />
          <div>
            <strong>{log.newStatusLabel}</strong>
            {log.description ? ` — ${log.description}` : ''}
            <br />
            <span className="log-date">{formatDateTime(log.changedAt)}</span>
          </div>
        </li>
      ))}
    </ul>
  );
}

export function SectionTitle({ children }) {
  return <div className="section-title">{children}</div>;
}
