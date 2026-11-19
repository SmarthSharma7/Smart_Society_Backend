package com.tiet.smartsocieties.repository;

import com.tiet.smartsocieties.entity.StudentInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface StudentInterestRepository extends JpaRepository<StudentInterest, StudentInterest.StudentInterestId> {
    List<StudentInterest> findByIdStudentId(Long studentId);

    void deleteByIdStudentId(Long studentId);

    @org.springframework.data.jpa.repository.Query("select t.name from InterestTag t where t.tagId = :tagId")
    java.util.Optional<String> findTagNameByTagId(@org.springframework.data.repository.query.Param("tagId") Long tagId);
}
