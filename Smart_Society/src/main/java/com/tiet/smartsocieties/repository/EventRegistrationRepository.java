package com.tiet.smartsocieties.repository;

import com.tiet.smartsocieties.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    Optional<EventRegistration> findByEventEventIdAndStudentUserId(Long eventId, Long studentId);

    @Query("select r from EventRegistration r join fetch r.event e join fetch e.society join fetch r.student where r.student.userId = :studentId order by r.registeredAt desc")
    List<EventRegistration> findAllForStudent(@Param("studentId") Long studentId);

    @Query("select r from EventRegistration r join fetch r.event e join fetch e.society join fetch r.student where e.society.societyId in :societyIds order by e.startDatetime desc, r.registeredAt desc")
    List<EventRegistration> findAllForSocieties(@Param("societyIds") List<Long> societyIds);

    long countByEventEventIdAndStatusIn(Long eventId, List<EventRegistration.RegistrationStatus> statuses);
}
