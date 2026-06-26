package com.vdt.documenttransfer.modules.organization.service;


import com.vdt.documenttransfer.common.response.PageResponse;
import com.vdt.documenttransfer.modules.organization.dto.NewOrgRequest;
import com.vdt.documenttransfer.modules.organization.dto.OrgResponse;
import com.vdt.documenttransfer.modules.organization.dto.UpdateOrgRequest;
import com.vdt.documenttransfer.modules.user.entity.User;

public interface OrganizationService {
    OrgResponse createNew(NewOrgRequest request, User user);

    OrgResponse accessNewOrInactiveOrg(Integer Id, User user);

    OrgResponse updateOrg(UpdateOrgRequest request, Integer id);

    PageResponse<OrgResponse> findAll(int page, int size);

    PageResponse<OrgResponse> findByStatus(int page, int size, String status);

    OrgResponse softDeleteOrg(Integer id);
}
