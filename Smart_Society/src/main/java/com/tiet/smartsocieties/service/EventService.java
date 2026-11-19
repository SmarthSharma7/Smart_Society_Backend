package com.tiet.smartsocieties.service;

import com.tiet.smartsocieties.dto.event.EventRequest;
import com.tiet.smartsocieties.entity.Category;
import com.tiet.smartsocieties.entity.Event;
import com.tiet.smartsocieties.entity.EventView;
import com.tiet.smartsocieties.entity.Society;
import com.tiet.smartsocieties.entity.User;
import com.tiet.smartsocieties.entity.SocietyMember;
import com.tiet.smartsocieties.entity.Enums;
import com.tiet.smartsocieties.repository.StudentInterestRepository;
import com.tiet.smartsocieties.repository.SocietyMemberRepository;
import com.tiet.smartsocieties.repository.CategoryRepository;
import com.tiet.smartsocieties.repository.EventRepository;
import com.tiet.smartsocieties.repository.EventViewRepository;
import com.tiet.smartsocieties.repository.SocietyRepository;
import com.tiet.smartsocieties.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final SocietyRepository societyRepository;
    private final CategoryRepository categoryRepository;
    private final EventViewRepository eventViewRepository;
    private final UserRepository userRepository;
    private final StudentInterestRepository studentInterestRepository;
    private final SocietyMemberRepository societyMemberRepository;

    public List<Event> getFeed(Long studentId) {
        List<Event> events = eventRepository
                .findByStatusAndStartDatetimeAfterOrderByStartDatetimeAsc(
                        Event.EventStatus.APPROVED, LocalDateTime.now());
        return personalize(events, studentId);
    }

    public List<Event> personalize(List<Event> events, Long studentId) {
        if (studentId == null) return events;
        var interests = studentInterestRepository.findByIdStudentId(studentId);
        if (interests.isEmpty()) return events;

        java.util.Map<Long, Double> categoryScores = new java.util.HashMap<>();
        for (var interest : interests) {
            var tag = interestTagName(interest.getId().getTagId());
            Long categoryId = categoryForTag(tag);
            if (categoryId != null) {
                double weight = interest.getWeight() == null ? 1.0 : interest.getWeight();
                categoryScores.merge(categoryId, weight, Double::sum);
            }
        }

        return events.stream().sorted((a, b) -> {
            double scoreA = categoryScores.getOrDefault(a.getCategory().getCategoryId(), 0.0);
            double scoreB = categoryScores.getOrDefault(b.getCategory().getCategoryId(), 0.0);
            int byScore = Double.compare(scoreB, scoreA);
            return byScore != 0 ? byScore : a.getStartDatetime().compareTo(b.getStartDatetime());
        }).toList();
    }

    private String interestTagName(Long tagId) {
        return studentInterestRepository.findTagNameByTagId(tagId).orElse("");
    }

    private Long categoryForTag(String tag) {
        String n = tag == null ? "" : tag.toLowerCase(java.util.Locale.ROOT);
        if (java.util.Set.of("artificial intelligence", "machine learning", "data science", "web development", "app development", "cloud computing", "cyber security", "competitive programming", "open source", "devops", "ui/ux design").contains(n)) return 1L;
        if (java.util.Set.of("robotics", "ros2", "embedded systems", "internet of things", "arduino", "raspberry pi", "electronics", "microcontrollers", "automation", "computer vision", "drones").contains(n)) return 2L;
        if (java.util.Set.of("automobile engineering", "electric vehicles", "cad/cam", "manufacturing", "industrial engineering").contains(n)) return 3L;
        if (java.util.Set.of("civil engineering", "structural design").contains(n)) return 4L;
        if (java.util.Set.of("chemical engineering", "biotechnology").contains(n)) return 5L;
        if (java.util.Set.of("physics", "mathematics", "astrophysics").contains(n)) return 6L;
        if (java.util.Set.of("startups", "product management", "business strategy", "finance", "economics").contains(n)) return 7L;
        if (java.util.Set.of("research", "innovation").contains(n)) return 8L;
        if (java.util.Set.of("dance", "music", "drama", "singing", "theatre", "creative writing", "poetry", "literature", "fine arts", "painting", "sketching").contains(n)) return 11L;
        if (java.util.Set.of("photography", "videography", "filmmaking").contains(n)) return 12L;
        if (java.util.Set.of("adventure sports", "trekking", "cycling").contains(n)) return 13L;
        if (java.util.Set.of("astronomy", "space technology").contains(n)) return 14L;
        if (java.util.Set.of("fitness", "yoga", "mental health").contains(n)) return 15L;
        if (java.util.Set.of("sustainability", "climate action", "energy conservation").contains(n)) return 16L;
        if (java.util.Set.of("community service", "teaching", "social impact").contains(n)) return 17L;
        if (java.util.Set.of("public speaking", "leadership", "debating", "communication skills", "networking").contains(n)) return 18L;
        if (n.equals("model united nations")) return 19L;
        return null;
    }

    public List<Event> getFeed() {
        return getFeed(null);
    }

    public List<Event> searchEvents(String title) {
        if (title == null || title.isBlank()) {
            return getFeed();
        }

        return eventRepository
                .findByStatusAndTitleContainingIgnoreCaseOrderByStartDatetimeAsc(
                        Event.EventStatus.APPROVED,
                        title
                );
    }

    public Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Event not found"));
    }

    public List<Event> getEventsBySociety(Long societyId) {
        return eventRepository
                .findBySocietySocietyIdOrderByStartDatetimeDesc(societyId);
    }

    @Transactional
    public Event createEvent(EventRequest request) {
        Society society = societyRepository.findById(request.societyId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Society not found"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Category not found"));

        validateDates(request);

        Event event = new Event();
        event.setSociety(society);
        event.setCategory(category);
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setVenue(request.venue());
        event.setStartDatetime(request.startDatetime());
        event.setEndDatetime(request.endDatetime());
        event.setRegistrationDeadline(request.registrationDeadline());
        event.setMaxCapacity(request.maxCapacity());

        if (request.status() != null && !request.status().isBlank()) {
            event.setStatus(
                    Event.EventStatus.valueOf(request.status().toUpperCase())
            );
        } else {
            event.setStatus(Event.EventStatus.DRAFT);
        }

        Event saved = eventRepository.save(event);
        initializeResponseRelations(saved);
        return saved;
    }

    @Transactional
    public Event createSocietyMemberEvent(Long userId, EventRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() != Enums.UserRole.SOCIETY_MEMBER) {
            throw new IllegalArgumentException("Only society members can create society events");
        }

        List<SocietyMember> memberships = societyMemberRepository
                .findByUserUserIdAndStatus(
                        userId,
                        SocietyMember.MembershipStatus.ACTIVE
                );

        if (memberships.isEmpty()) {
            throw new IllegalArgumentException("No active society membership found");
        }

        if (memberships.size() > 1) {
            throw new IllegalArgumentException(
                    "Your account has multiple active society memberships. Contact an administrator."
            );
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        validateDates(request);

        Event event = new Event();
        event.setSociety(memberships.get(0).getSociety());
        event.setCategory(category);
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setVenue(request.venue());
        event.setStartDatetime(request.startDatetime());
        event.setEndDatetime(request.endDatetime());
        event.setRegistrationDeadline(request.registrationDeadline());
        event.setMaxCapacity(request.maxCapacity());
        event.setStatus(Event.EventStatus.PENDING);

        Event saved = eventRepository.save(event);
        initializeResponseRelations(saved);
        return saved;
    }

    @Transactional
    public Event updateEvent(Long eventId, EventRequest request) {
        Event event = getEvent(eventId);

        Society society = societyRepository.findById(request.societyId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Society not found"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Category not found"));

        validateDates(request);

        event.setSociety(society);
        event.setCategory(category);
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setVenue(request.venue());
        event.setStartDatetime(request.startDatetime());
        event.setEndDatetime(request.endDatetime());
        event.setRegistrationDeadline(request.registrationDeadline());
        event.setMaxCapacity(request.maxCapacity());

        if (request.status() != null && !request.status().isBlank()) {
            event.setStatus(
                    Event.EventStatus.valueOf(request.status().toUpperCase())
            );
        }

        Event saved = eventRepository.save(event);
        initializeResponseRelations(saved);
        return saved;
    }

    @Transactional
    public Event approveEvent(Long eventId) {
        Event event = getEvent(eventId);

        if (event.getStatus() != Event.EventStatus.PENDING) {
            throw new IllegalArgumentException("Only pending events can be approved");
        }

        event.setStatus(Event.EventStatus.APPROVED);
        Event saved = eventRepository.save(event);
        initializeResponseRelations(saved);
        return saved;
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = getEvent(eventId);
        eventRepository.delete(event);
    }

    @Transactional(readOnly = true)
    public boolean canDeleteSocietyMemberEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        List<SocietyMember> activeMemberships = societyMemberRepository
                .findByUserUserIdAndStatus(
                        userId,
                        SocietyMember.MembershipStatus.ACTIVE
                );

        Long eventSocietyId = event.getSociety().getSocietyId();

        return activeMemberships.stream()
                .anyMatch(membership ->
                        membership.getSociety().getSocietyId().equals(eventSocietyId)
                );
    }

    @Transactional
    public void deleteSocietyMemberEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        List<SocietyMember> activeMemberships = societyMemberRepository
                .findByUserUserIdAndStatus(
                        userId,
                        SocietyMember.MembershipStatus.ACTIVE
                );

        Long eventSocietyId = event.getSociety().getSocietyId();

        boolean belongsToMemberSociety = activeMemberships.stream()
                .anyMatch(membership ->
                        membership.getSociety().getSocietyId().equals(eventSocietyId)
                );

        if (!belongsToMemberSociety) {
            throw new IllegalArgumentException(
                    "You can only delete events belonging to your active society"
            );
        }

        eventRepository.delete(event);
    }

    @Transactional
    public void logEventView(Long studentId, Long eventId, String source) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not found"));

        Event event = getEvent(eventId);

        EventView eventView = new EventView();
        eventView.setStudent(student);
        eventView.setEvent(event);
        eventView.setViewedAt(LocalDateTime.now());
        eventView.setSource(source);

        eventViewRepository.save(eventView);
    }

    private void validateDates(EventRequest request) {
        if (request.endDatetime().isBefore(request.startDatetime())) {
            throw new IllegalArgumentException(
                    "Event end time cannot be before start time");
        }

        if (request.registrationDeadline() != null &&
                request.registrationDeadline().isAfter(request.startDatetime())) {
            throw new IllegalArgumentException(
                    "Registration deadline cannot be after event start");
        }
    }

    private void initializeResponseRelations(Event event) {
        Hibernate.initialize(event.getSociety());
        Hibernate.initialize(event.getCategory());
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
}