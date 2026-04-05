package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.FinancialRecordRequest;
import com.finance.dashboard.dto.response.FinancialRecordResponse;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.finance.dashboard.model.RecordType;

@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;

    public FinancialRecordResponse createRecord(FinancialRecordRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .date(request.getDate())
                .notes(request.getNotes())
                .user(user)
                .build();

        FinancialRecord savedRecord = recordRepository.save(record);
        return mapToResponse(savedRecord);
    }

    public Page<FinancialRecordResponse> getAllRecords(
            RecordType type, String category, LocalDate startDate, LocalDate endDate, String search, Pageable pageable) {

        Specification<FinancialRecord> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            if (category != null && !category.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), endDate));
            }
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate notesMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("notes")), searchPattern);
                Predicate categoryMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("category")), searchPattern);
                predicates.add(criteriaBuilder.or(notesMatch, categoryMatch));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return recordRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    public FinancialRecordResponse getRecordById(Long id) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record not found with id: " + id));
        return mapToResponse(record);
    }

    public FinancialRecordResponse updateRecord(Long id, FinancialRecordRequest request) {
        FinancialRecord existingRecord = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record not found with id: " + id));

        existingRecord.setAmount(request.getAmount());
        existingRecord.setType(request.getType());
        existingRecord.setCategory(request.getCategory());
        existingRecord.setDate(request.getDate());
        existingRecord.setNotes(request.getNotes());

        FinancialRecord updatedRecord = recordRepository.save(existingRecord);
        return mapToResponse(updatedRecord);
    }

    public void deleteRecord(Long id) {
        if (!recordRepository.existsById(id)) {
            throw new ResourceNotFoundException("Financial record not found with id: " + id);
        }
        recordRepository.deleteById(id);
    }

    private FinancialRecordResponse mapToResponse(FinancialRecord record) {
        return FinancialRecordResponse.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .category(record.getCategory())
                .date(record.getDate())
                .notes(record.getNotes())
                .createdBy(record.getUser().getName())
                .build();
    }
}