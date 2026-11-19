package com.tiet.smartsocieties.repository;

import com.tiet.smartsocieties.entity.SocietyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SocietyMemberRepository extends JpaRepository<SocietyMember, Long> {
    List<SocietyMember> findByUserUserIdAndStatus(Long userId, SocietyMember.MembershipStatus status);
}
