import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// A API Spring Boot roda em http://localhost:8082 (server.port no application.yaml).
// Para o navegador enxergar o front e a API na mesma origem (e assim manter o
// cookie de sessao do Spring Security sem precisar de CORS), o servidor de
// desenvolvimento do Vite faz proxy de tudo que comeca com /api para a API.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
    },
  },
});
