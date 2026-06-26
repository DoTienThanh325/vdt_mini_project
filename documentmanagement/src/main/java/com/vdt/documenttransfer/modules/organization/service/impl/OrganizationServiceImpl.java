package com.vdt.documenttransfer.modules.organization.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import com.vdt.documenttransfer.common.response.PageResponse;
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
    public OrgResponse accessNewOrInactiveOrg(Integer Id, User user) {
        Organization organization = organizationRepository.findById(Id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tổ chức"));
        String message = "";

        if (organization.getStatus().name().equals("ACTIVE")) {
            organization.setStatus(Organization.Status.INACTIVE);
            message = "Vô hiệu hóa đơn vị liên thông thành công";
        } else {
            organization.setStatus(Organization.Status.ACTIVE);
            message = "Duyệt đơn vị liên thông thành công";
        }
        LocalDateTime now = LocalDateTime.now();
        organization.setUpdatedAt(now);

        Organization savedOrg = organizationRepository.save(organization);

        return entityToResponse(savedOrg, message);
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
        String phone = request.getPhone();
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

        if (phone != null && !phone.equals("")) {
            if (organizationRepository.existsByPhone(phone)) {
                throw new RuntimeException("Số điện thoại đơn vị đã được sử dụng");
            }
            organization.setPhone(phone);
        }

        if (systemId != null) {
            InterconnectedSystem interSystem = interconnectedSystemRepository.findById(systemId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hệ thống liên thông"));
            organization.setSystem(interSystem);
        }

        Organization savedOrg = organizationRepository.save(organization);

        return entityToResponse(savedOrg, "Cập nhật thông tin đơn vị liên thông thành công");
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

    @Override
    public PageResponse<OrgResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Organization> orgPage = organizationRepository.findAll(pageable);

        List<OrgResponse> content = orgPage.getContent().stream()
                .map(org -> entityToResponse(org, null))
                .toList();

        return PageResponse.<OrgResponse>builder()
                .content(content)
                .page(orgPage.getNumber())
                .size(orgPage.getSize())
                .totalElements(orgPage.getTotalElements())
                .totalPages(orgPage.getTotalPages())
                .first(orgPage.isFirst())
                .last(orgPage.isLast())
                .build();
    }

    @Override
    public PageResponse<OrgResponse> findByStatus(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Organization.Status orgStatus = Organization.Status.valueOf(status);

        Page<Organization> orgPage = organizationRepository.findByStatus(orgStatus, pageable);

        List<OrgResponse> content = orgPage.getContent().stream()
                .map(org -> entityToResponse(org, null))
                .toList();

        return PageResponse.<OrgResponse>builder()
                .content(content)
                .page(orgPage.getNumber())
                .size(orgPage.getSize())
                .totalElements(orgPage.getTotalElements())
                .totalPages(orgPage.getTotalPages())
                .first(orgPage.isFirst())
                .last(orgPage.isLast())
                .build();
    }

    @Override
    public OrgResponse softDeleteOrg(Integer id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tổ chức"));

        LocalDateTime now = LocalDateTime.now();
        organization.setStatus(Organization.Status.DELETED);
        organization.setUpdatedAt(now);

        Organization savedOrg = organizationRepository.save(organization);

        return entityToResponse(savedOrg, "Xoá đơn vị liên thông thành công");
    }
}
