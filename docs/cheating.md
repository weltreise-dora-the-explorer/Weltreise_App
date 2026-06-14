# Cheating Feature — App Side

Developer documentation for the "shake cheat" and the reporting mechanic in
the Android app. The player-facing explanation lives in the in-app rulebook
(`GameRules.kt` → `rule_cheat_*` strings). The wire protocol is documented
on the server side in `Weltreise_Server/docs/network-protocol.md`
(`USE_SHAKE_CHEAT`, `REPORT_CHEAT`).

## Overview

Two related mechanics:

1. **Shake cheat** — when a player has **exactly 1** movement point left,
   shaking the phone secretly bumps it to 2. Usable once per dice roll.
2. **Reporting** — any player can report a suspected cheater. A **correct**
   report makes the cheater skip their next turn; a **false** report
   penalises the reporter.

The app only *triggers* these commands and renders the outcome — all rules
(when a cheat is allowed, whether a report hits, the penalties) are enforced
**server-side**. The app intentionally trusts the server.

## Shake cheat flow

```
Accelerometer ──► ShakeDetector.onShake()
              ──► AppViewModel.onShakeCheat()
              ──► MyStomp.useShakeCheat(lobbyId, playerId)
              ──► STOMP USE_SHAKE_CHEAT  ─────────────►  Server
```

- **`sensor/ShakeDetector.kt`** — listens to `TYPE_ACCELEROMETER`. Fires
  `onShake` when the g-force exceeds `SHAKE_THRESHOLD_G = 2.7` and at least
  `DEBOUNCE_MS = 800` ms passed since the last trigger.
- **`MainActivity.kt`** — owns the detector; `start(this)` in `onResume`,
  `stop()` in `onPause`. `onShake` calls `viewModel.onShakeCheat()`.
- **`AppViewModel.onShakeCheat()`** — sends the command via `MyStomp`.
- **Hidden failure:** if the server rejects the cheat (e.g. not exactly 1
  step left, or already used this roll → `SHAKE_CHEAT_NOT_ALLOWED`), the
  error is **not** shown in the UI — see the `COMMAND_USE_SHAKE_CHEAT`
  branch in `AppViewModel`, which only logs it. This keeps the cheat covert.

The server, on success, sets `remainingSteps` from 1 to 2 and marks
`shakeCheatUsedThisRoll = true` (reset on the next roll).

## Reporting flow

```
GameScreen report button ──► confirm dialog ──► AppViewModel.reportCheat(target)
                          ──► MyStomp.reportCheat(lobbyId, playerId, reportedPlayerId)
                          ──► STOMP REPORT_CHEAT  ──────────►  Server
Server broadcast ──► AppViewModel derives ReportFeedback (HIT / MISS) ──► toast
```

- **Report window** — `AppViewModel.reportablePlayerId` holds the player who
  may currently be reported (cleared once another player rolls, matching the
  server's report window).
- **`GameScreen.kt`** — shows a report affordance on other players when
  `canBeReported` (`isOtherPlayer && playerName == reportablePlayerId`),
  opens a confirmation dialog, then calls `viewModel.reportCheat(target)`.
- **`AppViewModel.reportCheat(reportedPlayerId)`** — guards (not blank, not
  self, target in lobby), stores `pendingReportTarget`, sends `REPORT_CHEAT`.
- **Feedback** — when the report response/broadcast arrives, the VM compares
  the target against the set of players now marked "skip next turn" and
  emits `ReportFeedback.HIT` or `ReportFeedback.MISS`; `GameScreen` shows a
  toast ("Caught the cheater!" / "False accusation — you lose a turn.").
  A `MISS` while reporting outside your own turn means *you* skip next turn.

> Note: only the **sender** (`playerId`) is identity-checked by the server's
> session authorization. `reportedPlayerId` is data, so reporting another
> player works normally — you just cannot forge a report as someone else.

## Files involved

| File | Role |
|---|---|
| `sensor/ShakeDetector.kt` | Accelerometer-based shake detection |
| `MainActivity.kt` | Detector lifecycle, wires `onShake` to the VM |
| `AppViewModel.kt` | `onShakeCheat()`, `reportCheat()`, report window, `ReportFeedback` |
| `MyStomp.kt` | Sends `USE_SHAKE_CHEAT` / `REPORT_CHEAT` over STOMP |
| `GameConstants.kt` | `COMMAND_USE_SHAKE_CHEAT`, `COMMAND_REPORT_CHEAT` |
| `ui/theme/GameScreen.kt` | Report button, confirm dialog, feedback toast |
| `ui/theme/GameRules.kt` | Player-facing rulebook entry (`rule_cheat_*`) |

## Related

- Server protocol: `Weltreise_Server/docs/network-protocol.md`
- Server error codes: `SHAKE_CHEAT_NOT_ALLOWED`, `REPORT_NOT_ALLOWED`
