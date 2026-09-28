#pragma once

#include "inttypes.hpp"
#include <SDL3/SDL.h>

namespace Touch
{
constexpr i32 DEATHBOMB_TOLERANCE = 5;

void FingerDown(const SDL_TouchFingerEvent &f);
void FingerUp(const SDL_TouchFingerEvent &f);
void FingerMotion(const SDL_TouchFingerEvent &f);

u16 GetButtonBits();

bool IsFocus();

// Toggle-style mobile controls. These latches are only emitted while gameplay
// touch mode is active, so an enabled Z/S toggle cannot auto-select menu items.
void SetShotLatched(bool enabled);
void SetFocusLatched(bool enabled);
bool IsShotLatched();
bool IsFocusLatched();

// Android mobile control mode. FREE applies the full touch delta each frame;
// LIMIT preserves the original movement-speed cap.
bool IsFreeMove();
void SetFreeMove(bool enabled);


// Practice+ mobile helpers. JNI only posts requests; the game thread consumes
// them from EnemyManager::OnUpdate so boss/ECL state is never mutated from the
// Android UI thread.
void SetPracticeInvincible(bool enabled);
bool IsPracticeInvincible();

void RequestPracticeNextPhase();
void RequestPracticeRetryPhase();
bool ConsumePracticeNextPhaseRequest();
bool ConsumePracticeRetryPhaseRequest();

bool GetPlayerDelta(f32 *dx, f32 *dy);
void SetPlayerDelta(f32 dx, f32 dy);
void ConsumePlayerDelta(f32 dx, f32 dy);

bool WasUsedThisRun();
bool UsedTouchToBomb();
void ResetRunUsage();
void CancelTouches();
} // namespace Touch
