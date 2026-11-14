# Wear OS sync protocol

How the Aqua phone app and the Wear OS companion exchange data over the Wear Data Layer (`DataClient`).
The wire types live in [`core/model`](../core/model/src/main/java/com/arekb/aqua/sync) so both apps share one definition.
Current protocol version: **1** (`WearSyncContract.PROTOCOL_VERSION`).

## Principles

- **The phone is the source of truth** for history, goals, presets and units.
- **Watch → phone is append-only events**, each with a client-generated UUID, so delivery can be repeated safely.
- **Phone → watch is a single snapshot**; only the phone writes it, so last-write-wins is safe.
- **`DataClient`, not `MessageClient`**: DataItems are persisted and delivered once the devices reconnect.

## Messages

All payloads are UTF-8 JSON. Unknown keys are ignored, so newer senders stay readable by older receivers.

### Watch → phone: `IntakeEvent` at `/aqua/intake/{eventId}`

```json
{ "v": 1, "eventId": "7b0c5b0e-0f4e-4f0c-9f43-2f0c3a9c1e10", "amountMl": 350,
  "timestampMillis": 1793400000000, "localDate": "2026-10-31" }
```

`localDate` is computed on the watch, in the time zone where the drink happened.

The phone (`RecordWatchIntakeUseCase`) stores the drink **once** and then deletes the DataItem. It rejects an event when:

| Check | Rule |
|---|---|
| `eventId` | non-blank, at most 64 characters, no `/` or whitespace (it is part of the path) |
| `amountMl` | 1 to 10 000 |
| `localDate` | valid ISO date |
| `timestampMillis` | positive and not more than 1 hour in the future |
| date vs. timestamp | `localDate` within one day of the timestamp's UTC date |

Duplicates are detected through `intake_entries.externalId`, which has a unique index (Room v5). Entries logged on the phone keep `externalId = NULL`.

### Phone → watch: `WatchState` at `/aqua/state`

```json
{ "v": 1, "localDate": "2026-10-31", "totalMl": 1250, "goalMl": 2000,
  "presetsMl": [200, 350, 500], "liquidUnit": "ML",
  "acknowledgedEventIds": ["7b0c5b0e-0f4e-4f0c-9f43-2f0c3a9c1e10"] }
```

- Published by `WatchStatePublisher` (started in `AquaApplication`) whenever today's total, settings or the acknowledged ids change, and again at local midnight.
- `acknowledgedEventIds` lists watch events stored with a local date in the last `ACK_WINDOW_DAYS` (7) days. The watch removes these from its pending queue.
- Unchanged payloads are not re-sent by the Data Layer.

## What the watch displays

```
shown total = (snapshot.localDate == watch today ? snapshot.totalMl : 0)
              + sum of pending events for today that are not in acknowledgedEventIds
```

## Components on the phone

| Component | Role |
|---|---|
| `WearSyncService` | `WearableListenerService` for `/aqua/intake/*`; stores events, deletes handled DataItems |
| `RecordWatchIntakeUseCase` | Validation and de-duplicated insert |
| `ObserveWatchStateUseCase` | Builds the `WatchState` flow (handles midnight roll-over) |
| `WatchStatePublisher` | Collects the flow and hands snapshots to a `WatchStateSink` |
| `WatchStateSink` / `DataClientWatchStateSink` | Transport abstraction; the only code that touches Google Play services |
| `WatchIntakeRepository` | Storage operations for the sync, implemented by `RoomHydrationRepository` |

## Limitations and notes

- Sync needs Google Play services (the Wearable Data Layer). Without it the phone app works unchanged; the sink logs a warning and carries on.
- Rejected events are logged and deleted; they are never acknowledged because they were never stored.
- Backups include `externalId`, so restoring a backup does not allow already-synced watch events to be stored again.
