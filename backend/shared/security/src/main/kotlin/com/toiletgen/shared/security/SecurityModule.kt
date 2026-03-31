package com.toiletgen.shared.security

import org.koin.dsl.module

fun securityModule(config: JwtConfig) = module {
    single { config }
    single { JwtService(config) }
}
