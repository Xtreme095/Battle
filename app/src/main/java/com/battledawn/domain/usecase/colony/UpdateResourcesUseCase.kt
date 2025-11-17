package com.battledawn.domain.usecase.colony

import com.battledawn.domain.model.Colony
import com.battledawn.domain.model.ResourceType
import com.battledawn.domain.repository.ColonyRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to update colony resources based on time elapsed
 */
class UpdateResourcesUseCase @Inject constructor(
    private val colonyRepository: ColonyRepository
) {
    suspend operator fun invoke(colonyId: String): Result<Colony> {
        return try {
            val colony = colonyRepository.getColony(colonyId).first()
                ?: return Result.failure(Exception("Colony not found"))

            // Update resources based on time elapsed
            val updatedColony = colony.updateResources()

            // Save updated colony
            colonyRepository.updateColony(updatedColony)

            Result.success(updatedColony)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
