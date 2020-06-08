package com.github.fj.springGrpc

import io.grpc.BindableService
import io.grpc.ServerInterceptor
import kotlin.reflect.KClass

internal typealias ServicesMap = Map<BindableService, List<ServerInterceptor>>

internal typealias InterceptorsMap = Map<KClass<*>, ServerInterceptor>
