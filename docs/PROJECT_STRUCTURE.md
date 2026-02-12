# Lumisky Project Structure

This repository now follows the shared-chat architecture skeleton.

## Modules

- `:app` -> UI, activity navigation, preview container
- `:core` -> shared helpers/utilities
- `:engine` -> OpenGL-oriented rendering core and domain packages
- `:snapshot` -> snapshot queue/cache/encoding/provider layers
- `:wallpaper` -> wallpaper service lifecycle and render scheduling

## Engine packages

- `com.example.engine.config`
- `com.example.engine.renderer`
- `com.example.engine.shader`
- `com.example.engine.sky`
- `com.example.engine.celestial`
- `com.example.engine.atmosphere`
- `com.example.engine.time`
- `com.example.engine.texture`

## Wallpaper packages

- `com.example.wallpaper.service`
- `com.example.wallpaper.engine`
- `com.example.wallpaper.render`

## Snapshot packages

- `com.example.snapshot.provider`
- `com.example.snapshot.worker`
- `com.example.snapshot.cache`
- `com.example.snapshot.encoder`

## App packages

- `com.example.lumisky.ui.home`
- `com.example.lumisky.ui.preview`
- `com.example.lumisky.ui.settings`
- `com.example.lumisky.adapter`
- `com.example.lumisky.viewmodel`
