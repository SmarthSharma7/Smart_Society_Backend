package com.tiet.smartsocieties.service;

import com.tiet.smartsocieties.dto.student.InterestUpdateRequest;
import com.tiet.smartsocieties.entity.InterestTag;
import com.tiet.smartsocieties.entity.StudentInterest;
import com.tiet.smartsocieties.entity.StudentProfile;
import com.tiet.smartsocieties.entity.User;
import com.tiet.smartsocieties.repository.InterestTagRepository;
import com.tiet.smartsocieties.repository.StudentInterestRepository;
import com.tiet.smartsocieties.repository.StudentProfileRepository;
import com.tiet.smartsocieties.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentProfileRepository studentProfileRepository;
    private final InterestTagRepository interestTagRepository;
    private final StudentInterestRepository studentInterestRepository;
    private final UserRepository userRepository;

    @Transactional
    public StudentProfile getProfile(Long userId) {
        return getOrCreateProfile(userId);
    }

    private StudentProfile getOrCreateProfile(Long userId) {
        return studentProfileRepository.findById(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found"));

                    StudentProfile profile = new StudentProfile();
                    profile.setUser(user);
                    return studentProfileRepository.save(profile);
                });
    }

    @Transactional
    public void updateInterests(Long userId, InterestUpdateRequest request) {
        // Society members may not have had a student_profiles row created at registration.
        // Ensure it exists before inserting student_interests because that table references it.
        getOrCreateProfile(userId);

        studentInterestRepository.deleteByIdStudentId(userId);

        List<Long> tagIds = request.tagIds();
        for (Long tagId : tagIds) {
            InterestTag tag = interestTagRepository.findById(tagId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Interest tag not found: " + tagId));

            StudentInterest studentInterest = new StudentInterest();
            StudentInterest.StudentInterestId id = new StudentInterest.StudentInterestId();
            id.setStudentId(userId);
            id.setTagId(tagId);
            studentInterest.setId(id);
            studentInterest.setWeight(1.0);
            studentInterestRepository.save(studentInterest);
        }
    }

    public List<StudentInterest> getInterests(Long userId) {
        return studentInterestRepository.findByIdStudentId(userId);
    }
}