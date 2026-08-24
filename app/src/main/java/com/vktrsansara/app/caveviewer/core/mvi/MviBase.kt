package com.vktrsansara.app.caveviewer.core.mvi

/**
 * Marker interface for UI state representations in MVI architecture.
 */
interface UiState

/**
 * Marker interface for user actions / intents in MVI architecture.
 */
interface UiIntent

/**
 * Marker interface for one-time side effects (e.g. navigation, toasts, exit) in MVI architecture.
 */
interface UiEffect
