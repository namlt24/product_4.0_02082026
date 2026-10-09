import { defineConfig, loadEnv, type Plugin } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { spawn, spawnSync, type ChildProcess } from 'node:child_process'
import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = fileURLToPath(new URL('.', import.meta.url))
// miniapps/<app> → team/platform workspace root → parent dir chứa sibling platform SDKs
const workspaceRoot = resolve(root, '../..')
const parentRoot = resolve(workspaceRoot, '..')

const PLATFORM_SDK_DIRS = ['mbccs-core', 'mbccs-design-system', 'mbccs-runtime'] as const
const TEAM_PACKAGE_DIRS = ['mbccs-service', 'mbccs-modules'] as const

type WatchRepo = { name: string; dir: string; dist: string }

function readPackageName(dir: string): string | null {
  try {
    const pkg = JSON.parse(readFileSync(resolve(dir, 'package.json'), 'utf8')) as { name?: string }
    return typeof pkg.name === 'string' ? pkg.name : null
  } catch {
    return null
  }
}

const platformSdkRepos: WatchRepo[] = PLATFORM_SDK_DIRS.map((dir) => ({
  name: dir,
  dir: resolve(parentRoot, dir),
  dist: resolve(parentRoot, dir, 'dist'),
}))

const teamPackageRepos: WatchRepo[] = TEAM_PACKAGE_DIRS.flatMap((dir) => {
  const pkgDir = resolve(workspaceRoot, 'packages', dir)
  if (!existsSync(resolve(pkgDir, 'package.json'))) return []
  return [{ name: dir, dir: pkgDir, dist: resolve(pkgDir, 'dist') }]
})

const watchRepos = [...platformSdkRepos, ...teamPackageRepos]
const siblingDistRoots = watchRepos.map((repo) => repo.dist)

const workspacePkgs = [
  ...PLATFORM_SDK_DIRS.map((dir) => readPackageName(resolve(parentRoot, dir))).filter(
    (name): name is string => Boolean(name),
  ),
  ...teamPackageRepos
    .map((repo) => readPackageName(repo.dir))
    .filter((name): name is string => Boolean(name)),
]

const isWindows = process.platform === 'win32'

/**
 * Dev-only orchestration cho SDK sibling repos + team packages:
 * - pnpm install / pnpm build khi thiếu node_modules / dist
 * - spawn `pnpm dev` (tsc --watch) trong từng repo
 * - full-reload (debounce) khi dist thay đổi
 */
function mbccsSdkDev(): Plugin {
  const children: ChildProcess[] = []
  let reloadTimer: ReturnType<typeof setTimeout> | undefined

  return {
    name: 'mbccs-sdk-dev',
    apply: 'serve',
    configureServer(server) {
      for (const repo of watchRepos) {
        if (!existsSync(resolve(repo.dir, 'package.json'))) {
          console.warn(`[mbccs-sdk-dev] Missing repo: ${repo.dir} — skipped`)
          continue
        }
        if (!existsSync(resolve(repo.dir, 'node_modules'))) {
          console.log(`[mbccs-sdk-dev] ${repo.name}: installing dependencies…`)
          spawnSync('pnpm', ['install'], { cwd: repo.dir, stdio: 'inherit', shell: isWindows })
        }
        if (!existsSync(resolve(repo.dist, 'index.js'))) {
          console.log(`[mbccs-sdk-dev] ${repo.name}: no dist — building…`)
          spawnSync('pnpm', ['build'], { cwd: repo.dir, stdio: 'inherit', shell: isWindows })
        }
        console.log(`[mbccs-sdk-dev] ${repo.name}: pnpm dev (watch)`)
        children.push(
          spawn('pnpm', ['dev'], { cwd: repo.dir, stdio: 'inherit', shell: isWindows }),
        )
      }
      server.watcher.add(siblingDistRoots)
      server.httpServer?.on('close', () => {
        for (const child of children) {
          if (!child.killed) child.kill('SIGTERM')
        }
      })
    },
    handleHotUpdate({ file, server }) {
      if (siblingDistRoots.some((dir) => file.startsWith(dir))) {
        clearTimeout(reloadTimer)
        reloadTimer = setTimeout(() => {
          server.config.logger.info('[mbccs-sdk-dev] SDK dist changed → full reload')
          server.ws.send({ type: 'full-reload' })
        }, 100)
        return []
      }
    },
  }
}

/**
 * Vite 8 base for BCCS miniapps.
 * - Dev: plugin mbccs-sdk-dev tự watch-build platform SDK + team packages + reload khi dist đổi
 * - Build: codeSplitting tách vendor chunks; Oxc minifier drop console trừ khi VITE_CONSOLE=open
 */
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const keepConsole = env.VITE_CONSOLE === 'open'

  return {
    server: {
      host: '0.0.0.0',
      watch: {
        ignored: (path) => {
          if (siblingDistRoots.some((dir) => path.startsWith(dir))) {
            return false
          }
          if (path.includes(`${'@mbccs'}/`) || path.includes('@mbccs\\')) {
            return false
          }
          return path.includes('node_modules')
        },
      },
    },
    plugins: [react(), tailwindcss(), mbccsSdkDev()],
    resolve: {
      alias: {
        '@': resolve(root, 'src'),
        '@pages': resolve(root, 'src/pages'),
        '@components': resolve(root, 'src/components'),
        '@i18n': resolve(root, 'src/i18n'),
        '@assets': resolve(root, 'src/assets'),
      },
      dedupe: ['react', 'react-dom'],
    },
    optimizeDeps: {
      exclude: workspacePkgs,
    },
    build: {
      rolldownOptions: {
        output: {
          codeSplitting: {
            groups: [
              {
                name: 'vendor-react',
                test: /node_modules\/(react|react-dom|react-router|react-router-dom|scheduler)\//,
              },
              {
                name: 'vendor-utils',
                test: /node_modules\/(i18next|react-i18next|zustand|axios)\//,
              },
              {
                name: 'vendor-mbccs',
                test: /node_modules\/@mbccs\//,
              },
            ],
          },
          minify: {
            compress: {
              dropConsole: !keepConsole,
              dropDebugger: true,
            },
          },
        },
      },
    },
  }
})
