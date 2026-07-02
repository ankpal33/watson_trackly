package com.watson.trackly.usecase.base

/**
 * Created by dan on 20/01/2024
 *
 * Copyright © 2024 1010 Creative. All rights reserved.
 */

abstract class BaseUseCase<In,Out> {
    abstract suspend fun execute(input: In): Out
}