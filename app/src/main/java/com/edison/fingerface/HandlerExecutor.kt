package com.edison.fingerface

import android.os.Handler
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException

class HandlerExecutor(private val handler: Handler) : Executor {
    override fun execute(command: Runnable) {
        if (!handler.post(command)) {
            throw RejectedExecutionException("$handler is shutting down");
        }
    }
}
