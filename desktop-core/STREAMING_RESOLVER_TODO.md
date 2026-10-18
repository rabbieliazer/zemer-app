---
name: Desktop streaming resolver TODO
about: Guidance and checklist for implementing a full desktop streaming resolver that reuses :innertube and the cipher submodule.
---

## Implementing a Desktop Streaming Resolver

This repository includes a Noop resolver (`DesktopNoopResolver`) that logs when a developer hasn't provided `innertube_cookie.txt` or when the full resolver isn't present. To achieve streaming parity with the Android app, implement a resolver that:

- Reuses the `:innertube` JVM client (or a lightweight Ktor client) to call the `/player` endpoint with appropriate InnerTube context and headers.
- Uses the `cipher` submodule to perform sig/n deobfuscation + poToken generation.
- Produces a playable googlevideo URL with the `&pot=` parameter bound to the `videoId`.
- Optionally supports SABR (UMP) resolution using the existing `playback/sabr` pure-Kotlin components in the repo.

Checklist:
- [ ] Add `implementation(project(":innertube"))` and `implementation("com.zemer:cipher")` to `desktop-core/build.gradle.kts` when implementing the resolver.
- [ ] Read `innertube_cookie.txt` from repo root and attach `Cookie`/`VisitorData` to `/player` requests as the Android app does.
- [ ] Port or call the cipher code paths the Android app uses (look in `cipher/` submodule and `app/.../utils/YTPlayerUtils.kt`).
- [ ] Add unit tests that mirror the Node `tests/` harness `web-remix-stream.mjs` behavior using live credentials (marked as integration tests and skipped on CI unless credentials are provided).
- [ ] Ensure the resolver returns failover/non-null URLs only when correctly validated; otherwise log and return null so the desktop player falls back.

Why this file exists: developers or contributors who want to finish the full streaming resolver should follow the above checklist. This is intentionally a manual step because running live InnerTube calls requires credentials and careful testing.
