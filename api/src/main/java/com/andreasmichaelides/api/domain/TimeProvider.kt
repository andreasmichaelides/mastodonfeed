package com.andreasmichaelides.api.domain

interface TimeProvider {

    fun getCurrentTimeInMillis(): Long

}