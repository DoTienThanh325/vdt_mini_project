package com.vdt.documenttransfer.modules.organization.service;

import java.util.List;

import com.vdt.documenttransfer.modules.organization.dto.NewOrgRequest;
import com.vdt.documenttransfer.modules.organization.dto.OrgResponse;
import com.vdt.documenttransfer.modules.organization.dto.UpdateOrgRequest;
import com.vdt.documenttransfer.modules.user.entity.User;

public interface OrganizationService {
    OrgResponse createNew(NewOrgRequest request, User user);

    OrgResponse accessNewOrg(Integer Id, User user);

    OrgResponse updateOrg(UpdateOrgRequest request, Integer id);

    List<OrgResponse> findAll();
}
