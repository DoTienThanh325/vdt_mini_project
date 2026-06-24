package com.vdt.documenttransfer.modules.document.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vdt.documenttransfer.common.logging.AppLogger;
import com.vdt.documenttransfer.common.response.PageResponse;
import com.vdt.documenttransfer.modules.document.dto.DocumentResponse;
import com.vdt.documenttransfer.modules.document.dto.NewDocumentRequest;
import com.vdt.documenttransfer.modules.document.entity.Document;
import com.vdt.documenttransfer.modules.document.mapper.EntityToDTO;
import com.vdt.documenttransfer.modules.document.repository.DocumentRepository;
import com.vdt.documenttransfer.modules.document.service.DocumentService;
import com.vdt.documenttransfer.modules.notification.service.NotificationService;
import com.vdt.documenttransfer.modules.organization.entity.Organization;
import com.vdt.documenttransfer.modules.organization.repository.OrganizationRepository;
import com.vdt.documenttransfer.modules.user.entity.User;
import com.vdt.documenttransfer.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
        private final UserRepository userRepository;
        private final OrganizationRepository organizationRepository;
        private final DocumentRepository documentRepository;
        private final NotificationService notificationService;
        private final AppLogger appLogger;
        private final EntityToDTO entityToDTO;

        @Override
        @Transactional
        public DocumentResponse createNewDocument(Integer userId, Integer orgId,
                        NewDocumentRequest request) {
                String documentCode = null;
                try {
                        User user = userRepository.findById(userId)
                                        .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
                        Organization org = organizationRepository.findById(orgId)
                                        .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị liên thông"));
                        String code = getOrganizationCodeSuffix(org.getOrgCode());
                        LocalDateTime now = LocalDateTime.now();

                        Document document = Document
                                        .builder()
                                        .documentType(request.getDocumentType())
                                        .documentCode(generateDocumentCode(now.getYear(), code))
                                        .summary(request.getSummary())
                                        .status(Document.Status.CREATED)
                                        .senderOrganization(org)
                                        .createdBy(user)
                                        .createdAt(now)
                                        .build();

                        Document savedDocument = documentRepository.save(document);
                        documentCode = savedDocument.getDocumentCode();

                        notificationService.createNotification(userId, "Tài liệu, văn bản mới được tạo",
                                        "Tài liệu, văn bản "
                                                        + savedDocument.getDocumentCode() + " - "
                                                        + savedDocument.getSummary()
                                                        + " đã được tạo và đang chờ xử lý");

                        appLogger.infoDocument("CREATE_DOCUMENT", userId, savedDocument.getDocumentCode(),
                                        "Create document successfully");

                        return entityToDTO.entityToResponseNotSelectFilesAndTransfers(savedDocument,
                                        "Tạo văn bản thành công");
                } catch (RuntimeException e) {
                        appLogger.errorDocument("CREATE_DOCUMENT", userId,
                                        documentCode != null ? documentCode : String.valueOf(orgId),
                                        "Create document failed: " + e.getMessage(), e);
                        throw e;
                }
        }

        private String generateDocumentCode(int year, String code) {
                int nextSequence = documentRepository.findMaxDocumentCodeSequenceByYear(year, code) + 1;
                return String.format("DOC-%s-%d-%04d", code, year, nextSequence);
        }

        private String getOrganizationCodeSuffix(String orgCode) {
                if (orgCode == null || orgCode.isBlank()) {
                        throw new RuntimeException("Mã đơn vị không hợp lệ");
                }

                if (!orgCode.startsWith("ORG_")) {
                        throw new RuntimeException("Mã đơn vị phải có dạng ORG_XXX");
                }

                return orgCode.substring(4);
        }

        @Override
        public DocumentResponse approveNewDocument(Integer documentId, User user) {
                String documentCode = String.valueOf(documentId);
                try {
                        Document document = documentRepository.findById(documentId)
                                        .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu"));
                        documentCode = document.getDocumentCode();

                        if (!user.getOrganization().getId().equals(document.getSenderOrganization().getId())) {
                                throw new RuntimeException("Văn bản không thuộc đơn vị liên thông của bạn");
                        }

                        if (!document.getStatus().name().equals("CREATED")) {
                                throw new RuntimeException("Văn bản này đã được duyệt hoặc bị từ chối");
                        }
                        document.setStatus(Document.Status.APPROVED);

                        Document savedDocument = documentRepository.save(document);

                        notificationService.createNotification(document.getCreatedBy().getId(),
                                        "Tài liệu, văn bản được phê duyệt",
                                        "Tài liệu, văn bản " + savedDocument.getDocumentCode() + " - "
                                                        + savedDocument.getSummary()
                                                        + " đã được trưởng phòng phê duyệt");

                        appLogger.infoDocument("APPROVE_DOCUMENT", user.getId(), savedDocument.getDocumentCode(),
                                        "Approve document successfully");

                        return entityToDTO.entityToResponseNotSelectFilesAndTransfers(savedDocument,
                                        "Duyệt văn bản thành công");
                } catch (RuntimeException e) {
                        appLogger.errorDocument("APPROVE_DOCUMENT", user != null ? user.getId() : null, documentCode,
                                        "Approve document failed: " + e.getMessage(), e);
                        throw e;
                }
        }

        @Override
        public DocumentResponse rejectNewDocument(Integer documentId, User user) {
                String documentCode = String.valueOf(documentId);
                try {
                        Document document = documentRepository.findById(documentId)
                                        .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu"));
                        documentCode = document.getDocumentCode();

                        if (!user.getOrganization().getId().equals(document.getSenderOrganization().getId())) {
                                throw new RuntimeException("Văn bản không thuộc đơn vị liên thông của bạn");
                        }

                        if (!document.getStatus().name().equals("CREATED")) {
                                throw new RuntimeException("Văn bản này đã được duyệt hoặc bị từ chối");
                        }
                        document.setStatus(Document.Status.REJECTED);

                        Document savedDocument = documentRepository.save(document);

                        notificationService.createNotification(document.getCreatedBy().getId(),
                                        "Tài liệu, văn bản bị từ chối",
                                        "Tài liệu, văn bản " + savedDocument.getSummary()
                                                        + " đã bị trưởng phòng từ chối");

                        appLogger.infoDocument("REJECT_DOCUMENT", user.getId(), savedDocument.getDocumentCode(),
                                        "Reject document successfully");

                        return entityToDTO.entityToResponseNotSelectFilesAndTransfers(savedDocument,
                                        "Từ chối văn bản thành công");
                } catch (RuntimeException e) {
                        appLogger.errorDocument("REJECT_DOCUMENT", user != null ? user.getId() : null, documentCode,
                                        "Reject document failed: " + e.getMessage(), e);
                        throw e;
                }
        }

        @Override
        public PageResponse<DocumentResponse> findByStatusAndSenderOrg(String status, int orgId, int page, int size) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                Document.Status documentStatus = Document.Status.valueOf(status);

                Page<Document> documentPage = documentRepository.findByStatusAndSenderOrganization_Id(documentStatus,
                                orgId, pageable);

                List<DocumentResponse> content = documentPage.getContent().stream()
                                .map(document -> entityToDTO.entityToResponseNotSelectFilesAndTransfers(document, null))
                                .toList();

                return PageResponse.<DocumentResponse>builder()
                                .content(content)
                                .page(documentPage.getNumber())
                                .size(documentPage.getSize())
                                .totalElements(documentPage.getTotalElements())
                                .totalPages(documentPage.getTotalPages())
                                .first(documentPage.isFirst())
                                .last(documentPage.isLast())
                                .build();
        }

        @Override
        public DocumentResponse findById(Integer documentId) {
                Document document = documentRepository.findById(documentId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tài liệu, văn bản"));

                DocumentResponse response = entityToDTO.entityToResponseSelectFilesAndTransfers(document,
                                "Lấy thông tin tài liệu, văn bản thành công");

                return response;
        }

        @Override
        public PageResponse<DocumentResponse> findAllByOrg(int page, int size, Integer senderOrgId) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

                Page<Document> documentPage = documentRepository.findBySenderOrganization_Id(senderOrgId, pageable);

                List<DocumentResponse> content = documentPage.getContent().stream()
                                .map(document -> entityToDTO.entityToResponseNotSelectFilesAndTransfers(document, null))
                                .toList();

                return PageResponse.<DocumentResponse>builder()
                                .content(content)
                                .page(documentPage.getNumber())
                                .size(documentPage.getSize())
                                .totalElements(documentPage.getTotalElements())
                                .totalPages(documentPage.getTotalPages())
                                .first(documentPage.isFirst())
                                .last(documentPage.isLast())
                                .build();
        }

        @Override
        public PageResponse<DocumentResponse> findAllByStaff(int staffId, int page, int size) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

                Page<Document> documentPage = documentRepository.findByCreatedBy_Id(staffId, pageable);

                List<DocumentResponse> content = documentPage.getContent().stream()
                                .map(document -> entityToDTO.entityToResponseNotSelectFilesAndTransfers(document, null))
                                .toList();

                return PageResponse.<DocumentResponse>builder()
                                .content(content)
                                .page(documentPage.getNumber())
                                .size(documentPage.getSize())
                                .totalElements(documentPage.getTotalElements())
                                .totalPages(documentPage.getTotalPages())
                                .first(documentPage.isFirst())
                                .last(documentPage.isLast())
                                .build();
        }
}
