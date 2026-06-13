package com.vdt.documenttransfer.modules.organization.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.vdt.documenttransfer.modules.interconnectedsystem.entity.InterconnectedSystem;
import com.vdt.documenttransfer.modules.interconnectedsystem.repository.InterconnectedSystemRepository;
import com.vdt.documenttransfer.modules.organization.dto.NewOrgRequest;
import com.vdt.documenttransfer.modules.organization.dto.OrgResponse;
import com.vdt.documenttransfer.modules.organization.dto.UpdateOrgRequest;
import com.vdt.documenttransfer.modules.organization.entity.Organization;
import com.vdt.documenttransfer.modules.organization.repository.OrganizationRepository;
import com.vdt.documenttransfer.modules.organization.service.OrganizationService;
import com.vdt.documenttransfer.modules.user.entity.User;
import com.vdt.documenttransfer.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final InterconnectedSystemRepository interconnectedSystemRepository;
    private final UserRepository userRepository;

    @Override
    public OrgResponse createNew(NewOrgRequest request, User user) {
        String orgCode = request.getOrgCode();
        String orgEmail = request.getEmail();
        String phone = request.getPhone();
        Integer systemId = request.getSystemId();

        if (organizationRepository.existsByOrgCode(orgCode)) {
            throw new RuntimeException("Mã đơn vị đã được sử dụng");
        }

        if (organizationRepository.existsByEmail(orgEmail)) {
            throw new RuntimeException("Email đơn vị đã được sử dụng");
        }

        if (organizationRepository.existsByPhone(phone)) {
            throw new RuntimeException("Số điện thoại đơn vị đã được sử dụng");
        }

        InterconnectedSystem interSystem = interconnectedSystemRepository.findById(systemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hệ thống liên thông"));

        LocalDateTime now = LocalDateTime.now();

        Organization organization = Organization.builder()
                .orgCode(orgCode)
                .orgName(request.getOrgName())
                .address(request.getAddress())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status(Organization.Status.INACTIVE)
                .system(interSystem)
                .createdAt(now)
                .updatedAt(null)
                .build();

        Organization savedOrganization = organizationRepository.save(organization);

        user.setOrganization(savedOrganization);
        user.setUpdatedAt(now);
        userRepository.save(user);

        return entityToResponse(savedOrganization, "Tạo org thành công");
    }

    @Override
    public OrgResponse accessNewOrg(Integer Id) {
        Organization organization = organizationRepository.findById(Id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tổ chức"));
        LocalDateTime now = LocalDateTime.now();
        organization.setStatus(Organization.Status.ACTIVE);
        organization.setUpdatedAt(now);

        Organization savedOrg = organizationRepository.save(organization);

        return entityToResponse(savedOrg, "Duyệt đơn vị liên thông thành công");
    }

    @Override
    public OrgResponse updateOrg(UpdateOrgRequest request, Integer id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tổ chức"));
        LocalDateTime now = LocalDateTime.now();
        organization.setUpdatedAt(now);
        String orgCode = request.getOrgCode();
        String orgAddress = request.getAddress();
        String email = request.getEmail();
        Integer systemId = request.getSystemId();

        if (orgCode != null && !orgCode.equals("")) {
            if (organizationRepository.existsByOrgCode(orgCode)) {
                throw new RuntimeException("Mã đơn vị đã được sử dụng");
            }
            organization.setOrgCode(orgCode);
        }

        if (orgAddress != null && !orgAddress.equals("")) {
            organization.setAddress(orgAddress);
        }

        if (email != null && !email.equals("")) {
            if (organizationRepository.existsByEmail(email)) {
                throw new RuntimeException("Email đơn vị đã được sử dụng");
            }
            organization.setEmail(email);
        }

        if (systemId != null) {
            InterconnectedSystem interSystem = interconnectedSystemRepository.findById(systemId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hệ thống liên thông"));
            organization.setSystem(interSystem);
        }

        Organization savedOrg = organizationRepository.save(organization);

        return entityToResponse(savedOrg, "Cập nhật thông tin đơn vị liên thông thành công");
    }

    @Override
    public List<OrgResponse> findAll() {
        List<Organization> orgs = organizationRepository.findAll();
        List<OrgResponse> orgResponses = orgs.stream().map(org -> entityToResponse(org, null)).toList();

        return orgResponses;
    }

    private OrgResponse entityToResponse(Organization entity, String message) {
        return OrgResponse.builder()
                .id(entity.getId())
                .orgCode(entity.getOrgCode())
                .orgName(entity.getOrgName())
                .address(entity.getAddress())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .status(entity.getStatus().name())
                .systemId(entity.getSystem().getId())
                .systemCode(entity.getSystem().getSystemCode())
                .systemName(entity.getSystem().getSystemName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .message(message)
                .build();
    }
}
