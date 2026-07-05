CREATE DATABASE IF NOT EXISTS document_management_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE document_management_db;
-- 1. interconnected_systems
CREATE TABLE IF NOT EXISTS interconnected_systems (
    id INT NOT NULL AUTO_INCREMENT,
    system_code VARCHAR(100) NOT NULL,
    system_name VARCHAR(255) NOT NULL,
    endpoint_url VARCHAR(500) NOT NULL,
    api_key VARCHAR(255) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'DELETED') NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_interconnected_systems_system_code UNIQUE (system_code),
    CONSTRAINT uk_interconnected_systems_endpoint_url UNIQUE (endpoint_url),
    CONSTRAINT uk_interconnected_systems_api_key UNIQUE (api_key)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- 2. organizations
CREATE TABLE IF NOT EXISTS organizations (
    id INT NOT NULL AUTO_INCREMENT,
    org_code VARCHAR(100) NOT NULL,
    org_name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'DELETED') NOT NULL,
    system_id INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_organizations_org_code UNIQUE (org_code),
    CONSTRAINT fk_organizations_system FOREIGN KEY (system_id) REFERENCES interconnected_systems (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- 3. roles
CREATE TABLE IF NOT EXISTS roles (
    id INT NOT NULL AUTO_INCREMENT,
    role_code VARCHAR(100) NOT NULL,
    role_name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_roles_role_code UNIQUE (role_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- 4. users
CREATE TABLE IF NOT EXISTS users (
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NULL,
    email VARCHAR(255) NOT NULL,
    status ENUM(
        'ACTIVE',
        'INACTIVE',
        'LOCKED',
        'DELETED'
    ) NOT NULL,
    role_id INT NOT NULL,
    organization_id INT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_users_organization FOREIGN KEY (organization_id) REFERENCES organizations (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- 5. documents
CREATE TABLE IF NOT EXISTS documents (
    id INT NOT NULL AUTO_INCREMENT,
    document_type VARCHAR(100) NOT NULL,
    document_code VARCHAR(100) NOT NULL,
    summary TEXT NULL,
    status ENUM(
        'CREATED',
        'APPROVED',
        'SIGNED',
        'REJECTED'
    ) NOT NULL,
    sender_org_id INT NOT NULL,
    created_by INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_documents_document_code UNIQUE (document_code),
    CONSTRAINT fk_documents_sender_organization FOREIGN KEY (sender_org_id) REFERENCES organizations (id),
    CONSTRAINT fk_documents_created_by FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- 6. document_files
CREATE TABLE IF NOT EXISTS document_files (
    id INT NOT NULL AUTO_INCREMENT,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    document_id INT NOT NULL,
    uploaded_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_document_files_stored_file_name UNIQUE (stored_file_name),
    CONSTRAINT uk_document_files_file_path UNIQUE (file_path),
    CONSTRAINT fk_document_files_document FOREIGN KEY (document_id) REFERENCES documents (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- 7. document_signatures
CREATE TABLE IF NOT EXISTS document_signatures (
    id INT NOT NULL AUTO_INCREMENT,
    hash_value LONGTEXT NOT NULL,
    algorithm VARCHAR(100) NOT NULL,
    status ENUM(
        'PENDING',
        'VALID',
        'INVALID',
        'REVOKED'
    ) NOT NULL,
    document_id INT NOT NULL,
    signer_id INT NOT NULL,
    signed_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_document_signatures_hash_value UNIQUE (hash_value),
    -- Quan hệ @OneToOne: mỗi document chỉ có một signature
    CONSTRAINT uk_document_signatures_document UNIQUE (document_id),
    CONSTRAINT fk_document_signatures_document FOREIGN KEY (document_id) REFERENCES documents (id),
    CONSTRAINT fk_document_signatures_signer FOREIGN KEY (signer_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
-- 8. document_transfers
CREATE TABLE IF NOT EXISTS document_transfers (
    id INT NOT NULL AUTO_INCREMENT,
    response_content TEXT NULL,
    status ENUM(
        'SENT',
        'RECEIVED',
        'RESPONDED',
        'FAILED',
        'PENDING'
    ) NOT NULL,
    sender_id INT NOT NULL,
    receiver_id INT NULL,
    receiver_org_id INT NULL,
    document_id INT NOT NULL,
    sent_at DATETIME(6) NOT NULL,
    received_at DATETIME(6) NULL,
    responded_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_document_transfers_sender FOREIGN KEY (sender_id) REFERENCES users (id),
    CONSTRAINT fk_document_transfers_receiver FOREIGN KEY (receiver_id) REFERENCES users (id),
    CONSTRAINT fk_document_transfers_receiver_organization FOREIGN KEY (receiver_org_id) REFERENCES organizations (id),
    CONSTRAINT fk_document_transfers_document FOREIGN KEY (document_id) REFERENCES documents (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;