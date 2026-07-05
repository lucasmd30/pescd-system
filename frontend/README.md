# PESCD — Front-end React

Front-end em React (Vite) que consome a **REST API** do PESCD. Substitui as
telas Thymeleaf, mantendo os mesmos fluxos por perfil.

## Pré-requisitos

A API precisa estar no ar em `http://localhost:8080` (veja o README da raiz:
`docker compose up -d` + `./mvnw spring-boot:run`).

## Executar em desenvolvimento

```bash
cd frontend
npm install
npm run dev      # http://localhost:5173
```

O Vite faz **proxy de `/api` para `http://localhost:8080`** (ver `vite.config.js`),
então o navegador enxerga front e API na mesma origem — o cookie de sessão do
Spring Security funciona sem CORS.

## Autenticação

Baseada em sessão (cookie `JSESSIONID`). O `fetch` usa `credentials: 'include'`.

- `POST /api/auth/login` — entra e cria a sessão
- `GET  /api/auth/me` — usuário atual (usado para restaurar a sessão)
- `POST /api/auth/logout` — encerra a sessão

Usuários de exemplo (seed): `admin/admin123`, `secretario/123456`,
`lferreira/123456` (aluno), professores como `alevada/123456`.

## Organização

- `src/api/client.js` — cliente HTTP fino sobre o fetch + download autenticado
- `src/auth/` — contexto de autenticação
- `src/components/` — layout, rota protegida por perfil, feedback
- `src/pages/` — telas por perfil:
  - `secretary/` — ofertas (S.01) e alunos manual/CSV (S.02)
  - `aluno/` — ofertas e envios de plano/documentação/relatório (AL.01–AL.04)
  - `admin/` — CRUD de usuários (AD.01)
  - `professor/` — supervisor (PS.*) e responsável (PR.*)

## Build de produção

```bash
npm run build    # gera dist/
```
