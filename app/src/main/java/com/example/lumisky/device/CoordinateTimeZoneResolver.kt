package com.example.lumisky.device

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import java.time.ZoneId

fun interface CoordinateTimeZoneResolver {
    fun resolve(latitude: Double, longitude: Double): String?
}

@Singleton
class SystemTimeZoneResolver @Inject constructor() : CoordinateTimeZoneResolver {
    override fun resolve(latitude: Double, longitude: Double): String = ZoneId.systemDefault().id
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CoordinateTimeZoneResolverModule {
    @Binds
    abstract fun bindCoordinateTimeZoneResolver(
        implementation: SystemTimeZoneResolver
    ): CoordinateTimeZoneResolver
}
