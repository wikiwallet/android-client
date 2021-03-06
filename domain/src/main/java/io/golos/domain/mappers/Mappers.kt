package io.golos.domain.mappers

import androidx.annotation.WorkerThread
import io.golos.domain.Entity
import io.golos.domain.Model

interface CommunToEntityMapper<TC, TE : Entity> {
    @WorkerThread
    fun map(communObject: TC): TE
}

interface EntityToModelMapper<TE : Entity, TM : Model> {
    @WorkerThread
    fun map(entity: TE): TM
}

interface ModelToEntityMapper<TM : Model, TE : Entity> {
    @WorkerThread
    fun map(model: TM): TE
}