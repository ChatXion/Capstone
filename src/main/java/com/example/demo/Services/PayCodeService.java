package com.example.demo.Services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.Entities.Paycode;
import com.example.demo.Repositories.PayCodeRepository;

import jakarta.transaction.Transactional;

@Service
public class PayCodeService {
    private final PayCodeRepository PayCodeRepository;

    public PayCodeService(PayCodeRepository PaycodeRepository) {
        this.PayCodeRepository = PaycodeRepository;
    }
    
    @Transactional
    public Optional<Paycode> getPaycode(Long PaycodeId) {
        return PayCodeRepository.findById(PaycodeId);
    }
    
    @Transactional
    public List<Paycode> getAllPaycodes() {
        return PayCodeRepository.findAll();
    }
    
    @Transactional
    public List<Paycode> getPaycodesByOrganization(Long organizationId) {
        return PayCodeRepository.findAll().stream()
            .filter(Paycode -> Paycode.getOrganization() != null 
                && Paycode.getOrganization().getId().equals(organizationId))
            .collect(Collectors.toList());
    }
}