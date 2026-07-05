// Cliente HTTP fino sobre o fetch. Todas as chamadas usam credentials:'include'
// para enviar/receber o cookie de sessao (JSESSIONID) do Spring Security.

export class ApiError extends Error {
  constructor(message, status, body) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.body = body;
  }
}

async function parse(response) {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

async function request(method, url, { body, isForm } = {}) {
  const options = {
    method,
    credentials: 'include',
    headers: {},
  };

  if (body !== undefined) {
    if (isForm) {
      options.body = body; // FormData: o browser define o Content-Type
    } else {
      options.headers['Content-Type'] = 'application/json';
      options.body = JSON.stringify(body);
    }
  }

  const response = await fetch(url, options);

  if (!response.ok) {
    const parsed = await parse(response);
    const message =
      (parsed && parsed.message) ||
      (typeof parsed === 'string' && parsed) ||
      `Erro ${response.status}`;
    throw new ApiError(message, response.status, parsed);
  }

  return parse(response);
}

export const api = {
  get: (url) => request('GET', url),
  post: (url, body) => request('POST', url, { body }),
  postForm: (url, formData) => request('POST', url, { body: formData, isForm: true }),
  put: (url, body) => request('PUT', url, { body }),
  del: (url) => request('DELETE', url),
};

// Faz o download de um arquivo (PDF) autenticado e abre em nova aba.
export async function openAuthenticatedFile(url) {
  const response = await fetch(url, { credentials: 'include' });
  if (!response.ok) {
    throw new ApiError(`Não foi possível abrir o arquivo (${response.status}).`, response.status);
  }
  const blob = await response.blob();
  const objectUrl = URL.createObjectURL(blob);
  window.open(objectUrl, '_blank');
  setTimeout(() => URL.revokeObjectURL(objectUrl), 60_000);
}
