package kr.hhplus.be.adapter.out.persistence.repository

import kr.hhplus.be.adapter.out.persistence.entity.BalanceChargeHistoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BalanceChargeHistoryRepository : JpaRepository<BalanceChargeHistoryEntity, Long>
