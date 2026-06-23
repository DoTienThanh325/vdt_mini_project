package com.vdt.documenttransfer.modules.interconnectedsystem.service;

import java.util.List;

import com.vdt.documenttransfer.modules.interconnectedsystem.dto.InterconnectedSystemResponse;
import com.vdt.documenttransfer.modules.interconnectedsystem.dto.NewInterconnectedSystemRequest;
import com.vdt.documenttransfer.modules.interconnectedsystem.dto.UpdateInterconnectedSystemRequest;
import com.vdt.documenttransfer.modules.user.entity.User;

public interface InterconnectedSystemService {
    List<InterconnectedSystemResponse> findAll();

    InterconnectedSystemResponse createNew(NewInterconnectedSystemRequest request, User user);

    InterconnectedSystemResponse update(Integer id, UpdateInterconnectedSystemRequest request, User user);
}
