package com.example.demo.Services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.Entities.PayCode;
import com.example.demo.Repositories.PayCodeRepository;

import jakarta.transaction.Transactional;

@Service
public class PayCodeService {
    private final PayCodeRepository payCodeRepository;

    public PayCodeService(PayCodeRepository payCodeRepository) {
        this.payCodeRepository = payCodeRepository;
    }
    
    @Transactional
    public Optional<PayCode> getPayCode(Long payCodeId) {
        return payCodeRepository.findById(payCodeId);
    }
    
    @Transactional
    public List<PayCode> getAllPayCodes() {
        return payCodeRepository.findAll();
    }
    
    @Transactional
    public List<PayCode> getPayCodesByOrganization(Long organizationId) {
        return payCodeRepository.findAll().stream()
            .filter(payCode -> payCode.getOrganization() != null 
                && payCode.getOrganization().getId().equals(organizationId))
            .collect(Collectors.toList());
    }
}