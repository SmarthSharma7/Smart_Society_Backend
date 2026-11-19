package com.tiet.smartsocieties.repository;

import com.tiet.smartsocieties.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Optional<Feedback> findByEventEventIdAndStudentUserId(Long eventId, Long studentId);

    boolean existsByEventEventIdAndStudentUserId(Long eventId, Long studentId);

    @Query("select f from Feedback f join fetch f.event e join fetch e.society join fetch f.student " +
            "where e.society.societyId in :societyIds order by f.createdAt desc")
    List<Feedback> findAllForSocieties(@Param("societyIds") List<Long> societyIds);
}