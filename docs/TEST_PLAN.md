# KachalChat – Test Plan

Owner: M4 (Mesh Engine & QA Lead)

## 1. Test levels

| Level | What | How | Status |
|---|---|---|---|
| Unit | Mesh routing rules (MeshEngine) | JUnit + fakes, `MeshEngineTest` (11 tests) | ✅ Passing |
| Unit | Room DAOs / repository | JUnit (M2) | ⬜ |
| Integration | Engine + real repository + real network | Debug build on real phones | ⬜ |
| System | Full app on 3+ phones | Scenarios in section 3 | ⬜ |
| Usability | UI flows vs Figma | Manual walkthrough | ⬜ |

## 2. Test environment

| Device | Model | Android version | Owner |
|---|---|---|---|
| A | | | |
| B | | | |
| C | | | |
| D (optional) | | | |

Rules: Wi-Fi and mobile data OFF on all phones (proves offline operation). Bluetooth and Location ON. Same app build on all phones.

## 3. Multi-device scenarios

| ID | Requirement | Setup | Steps | Expected result | Result |
|---|---|---|---|---|---|
| TC-01 | FR-01 Discovery | A, B within 2 m | Open app on both | Each sees the other on Discovery screen within 30 s | |
| TC-02 | FR-02 Connection | TC-01 done | Tap Connect | Both show "connected" | |
| TC-03 | FR-04 Public | A, B, C connected | A posts in public room | B and C show it once | |
| TC-04 | FR-03 Private | A, B connected | A sends private msg to B | Only B shows it | |
| TC-05 | FR-05 Multi-hop | A–B–C in a line, A and C out of range (different rooms/floors) | A posts public msg | C receives it via B | |
| TC-06 | FR-05 Private relay | Same as TC-05 | A sends private msg to C | C receives it; B does NOT display it | |
| TC-07 | FR-06 Dedup | A, B, C all connected (triangle) | A posts one msg | Every phone shows it exactly once | |
| TC-08 | FR-07 TTL | Chain of 5 phones if available | Send from first phone | Stops after TTL hops (default 3 relays) | |
| TC-09 | FR-08 Queue | A alone | A sends msg; then B comes in range | Msg shows QUEUED, then SENT; B receives it | |
| TC-10 | FR-09/11 History | Any | Send msgs, force-close and reopen app | History still shown | |
| TC-11 | FR-10 Encryption | Any | Inspect database file (adb / Device Explorer) | Message text not readable | |
| TC-12 | FR-12 Search | History exists | Search a word | Matching msgs listed | |
| TC-13 | FR-13 Reconnect | A, B connected | Move B out of range, then back | Reconnects automatically; queued msgs delivered | |
| TC-14 | Lifecycle | A, B connected | Minimise app on B, send from A | B still receives (foreground service) | |
| TC-15 | NFR-01 Offline | All | Airplane mode + Bluetooth on | All above still work | |
| TC-16 | Permissions | Fresh install on Android 12+ and 13+ | Open app | Permission prompts shown; app works after granting | |

Result values: ✅ Pass / ❌ Fail (link issue) / ⚠️ Partial.

## 4. Measurements for the report

| Metric | How to measure | Value |
|---|---|---|
| Discovery time | Stopwatch from app open to peer listed (avg of 5) | |
| 1-hop delivery time | Send → appear on receiver (avg of 10) | |
| 2-hop delivery time | A → C via B (avg of 10) | |
| Max reliable direct range | Walk apart until messages fail | |
| Delivery success rate | Messages received / sent (50 msgs) | |

## 5. Bug log

| ID | Found in | Description | Severity | Owner | Status |
|---|---|---|---|---|---|
| | | | | | |

## 6. Pull request review checklist

- [ ] Builds (Compile All Sources) with no errors
- [ ] All unit tests pass
- [ ] Follows the shared contracts (MeshPacket, MeshNetwork, MessageRepository) – no silent changes
- [ ] Only touches the author's own area (or explains why)
- [ ] No secrets, `local.properties` or build folders committed
- [ ] Meaningful commit messages
