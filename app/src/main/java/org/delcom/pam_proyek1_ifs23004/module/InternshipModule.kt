package org.delcom.pam_proyek1_ifs23004.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.delcom.pam_proyek1_ifs23004.network.internships.service.IInternshipAppContainer
import org.delcom.pam_proyek1_ifs23004.network.internships.service.IInternshipRepository
import org.delcom.pam_proyek1_ifs23004.network.internships.service.InternshipAppContainer

@Module
@InstallIn(SingletonComponent::class)
object InternshipModule {

    @Provides
    fun provideInternshipContainer(): IInternshipAppContainer {
        return InternshipAppContainer()
    }

    @Provides
    fun provideInternshipRepository(container: IInternshipAppContainer): IInternshipRepository {
        return container.repository
    }
}