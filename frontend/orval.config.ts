import { defineConfig } from 'orval'

export default defineConfig({
  api: {
    output: {
      mode: 'split',
      target: 'src/api/generated.ts',
      schemas: 'src/models',
      client: 'axios',
      mock: false,
      override: {
        mutator: {
          path: 'src/utils/api-mutator.ts',
          name: 'customFetch',
        },
        query: {
          useInfinite: false,
        },
      },
    },
    input: {
      target: 'http://localhost:8080/api/v3/api-docs',
    },
  },
})
