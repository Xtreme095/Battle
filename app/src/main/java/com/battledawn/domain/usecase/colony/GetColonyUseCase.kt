package com.battledawn.domain.usecase.colony

import com.battledawn.domain.model.Colony
import com.battledawn.domain.repository.ColonyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get a colony by ID
 */
class GetColonyUseCase @Inject constructor(
    private val colonyRepository: ColonyRepository
) {
    operator fun invoke(colonyId: String): Flow<Colony?> {
        return colonyRepository.getColony(colonyId)
    }
}
