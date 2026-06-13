USE document_management_db;

-- Seed password for all users: 123456
-- BCrypt hash generated with Spring Security BCryptPasswordEncoder.
SET @seed_password = '$2a$10$vB26gEDPoVFGxvbsTTnmk.m/LwfYcbFqlFaa5nq0FD8UM3dT.7QcK';

INSERT INTO interconnected_systems (
    id,
    system_code,
    system_name,
    endpoint_url,
    api_key,
    status
) VALUES
    (1, 'DOC-HQ', 'Document Management HQ', 'https://hq.example.local/api', 'hq-seed-api-key', 'ACTIVE'),
    (2, 'DOC-BRANCH', 'Document Management Branch', 'https://branch.example.local/api', 'branch-seed-api-key', 'ACTIVE');

INSERT INTO organizations (
    id,
    org_code,
    org_name,
    address,
    email,
    phone,
    status,
    system_id
) VALUES
    (1, 'ORG-HQ', 'Head Office', '1 Main Street', 'hq@example.local', '0900000001', 'ACTIVE', 1),
    (2, 'ORG-HR', 'Human Resources Department', '2 Main Street', 'hr@example.local', '0900000002', 'ACTIVE', 1),
    (3, 'ORG-BRANCH', 'Branch Office', '3 Main Street', 'branch@example.local', '0900000003', 'ACTIVE', 2);

INSERT INTO roles (
    id,
    role_code,
    role_name,
    description
) VALUES
    (1, 'ADMIN', 'Administrator', 'Full system administration access'),
    (2, 'STAFF', 'Staff', 'Create, send, receive, and process documents'),
    (3, 'MANAGER', 'Manager', 'Review and approve document workflows');

INSERT INTO users (
    id,
    username,
    password,
    full_name,
    phone,
    email,
    status,
    role_id,
    organization_id
) VALUES
    (1, 'admin', @seed_password, 'System Administrator', '0911000001', 'admin@example.local', 'ACTIVE', 1, 1),
    (2, 'manager', @seed_password, 'Document Manager', '0911000002', 'manager@example.local', 'ACTIVE', 3, 1),
    (3, 'sender', @seed_password, 'Document Sender', '0911000003', 'sender@example.local', 'ACTIVE', 2, 2),
    (4, 'receiver', @seed_password, 'Document Receiver', '0911000004', 'receiver@example.local', 'ACTIVE', 2, 3);

INSERT INTO documents (
    id,
    document_type,
    document_code,
    summary,
    status,
    sender_org_id,
    created_by
) VALUES
    (1, 'OFFICIAL_LETTER', 'DOC-2026-0001', 'Seed outgoing official letter', 'SENT', 2, 3),
    (2, 'REPORT', 'DOC-2026-0002', 'Seed internal report awaiting review', 'PENDING', 1, 2);

INSERT INTO document_receivers (
    document_id,
    receiver_org_id
) VALUES
    (1, 3),
    (2, 2);

INSERT INTO document_files (
    id,
    original_file_name,
    stored_file_name,
    file_path,
    file_type,
    file_size,
    document_id
) VALUES
    (1, 'official-letter.pdf', 'DOC-2026-0001.pdf', '/uploads/documents/DOC-2026-0001.pdf', 'application/pdf', 102400, 1),
    (2, 'internal-report.pdf', 'DOC-2026-0002.pdf', '/uploads/documents/DOC-2026-0002.pdf', 'application/pdf', 204800, 2);

INSERT INTO document_signatures (
    id,
    hash_value,
    algorithm,
    status,
    document_id,
    signer_id
) VALUES
    (1, 'seed-signature-hash-doc-0001', 'SHA-256', 'VALID', 1, 3);

INSERT INTO document_transfers (
    id,
    response_content,
    status,
    sender_id,
    receiver_id,
    document_id,
    sent_at,
    received_at,
    responded_at
) VALUES
    (1, NULL, 'SENT', 3, 4, 1, CURRENT_TIMESTAMP, NULL, NULL);

INSERT INTO workflow_steps (
    id,
    step_name,
    step_order,
    status,
    assignee_id,
    document_id,
    processed_at
) VALUES
    (1, 'Create document', 1, 'COMPLETED', 3, 1, CURRENT_TIMESTAMP),
    (2, 'Manager review', 2, 'COMPLETED', 2, 1, CURRENT_TIMESTAMP),
    (3, 'Receive document', 3, 'PENDING', 4, 1, NULL),
    (4, 'Review report', 1, 'PROCESSING', 2, 2, NULL);

INSERT INTO audit_logs (
    id,
    action,
    object_type,
    object_value,
    old_value,
    new_value,
    ip_address,
    actor_id
) VALUES
    (1, 'LOGIN', 'USER', 'admin', NULL, 'Login seed event', '127.0.0.1', 1),
    (2, 'CREATE', 'DOCUMENT', 'DOC-2026-0001', NULL, 'Created seed document', '127.0.0.1', 3);

INSERT INTO notifications (
    id,
    title,
    content,
    is_read,
    user_id,
    related_document_id
) VALUES
    (1, 'New document received', 'Document DOC-2026-0001 is waiting for processing.', FALSE, 4, 1),
    (2, 'Report review', 'Document DOC-2026-0002 is waiting for review.', FALSE, 2, 2);
