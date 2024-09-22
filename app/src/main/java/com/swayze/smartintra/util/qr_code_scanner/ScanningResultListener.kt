package com.swayze.smartintra.util.qr_code_scanner

interface ScanningResultListener {
    fun onScanned(result: String)
}