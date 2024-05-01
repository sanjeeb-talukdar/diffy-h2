package ai.diffy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import ai.diffy.analysis.DifferenceResult;

@Component
@Repository
public interface DifferenceResultRepository extends JpaRepository<DifferenceResult, String>{
    
    @Query("select d from DifferenceResult d where d.timestampMsec >= :starTime and d.timestampMsec <= :endTime")
    List<DifferenceResult> findByTimestampMsecBetween(@Param("starTime") Long start,@Param("endTime") Long end);
}
