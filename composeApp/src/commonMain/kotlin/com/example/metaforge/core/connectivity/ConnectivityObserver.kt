package com.example.metaforge.core.connectivity

import kotlinx.coroutines.flow.StateFlow

interface ConnectivityObserver {
    val isConnected: StateFlow<Boolean>
}
