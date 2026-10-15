import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";

// The dev server forwards /api to the Spring Boot backend, so the browser sees one origin.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": "http://localhost:8080",
    },
  },
  test: {
    environment: "node",
  },
});
