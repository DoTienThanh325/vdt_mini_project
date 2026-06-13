CREATE DATABASE IF NOT EXISTS document_management_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE document_management_db;

-- Xóa bảng theo đúng thứ tự phụ thuộc khóa ngoại
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS workflow_steps;
DROP TABLE IF EXISTS document_transfers;
DROP TABLE IF EXISTS document_signatures;
DROP TABLE IF EXISTS document_files;
DROP TABLE IF EXISTS document_receivers;
DROP TABLE IF EXISTS documents;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS organizations;
DROP TABLE IF EXISTS interconnected_systems;

-- =========================
-- 1. HỆ THỐNG LIÊN THÔNG
-- =========================
CREATE TABLE interconnected_systems (
    id INT AUTO_INCREMENT PRIMARY KEY,

    system_code VARCHAR(100) NOT NULL UNIQUE,
    system_name VARCHAR(255) NOT NULL,
    endpoint_url VARCHAR(500) NOT NULL UNIQUE,
    api_key VARCHAR(255) NOT NULL UNIQUE,

    status ENUM('ACTIVE', 'INACTIVE', 'DELETED') NOT NULL DEFAULT 'ACTIVE',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =========================
-- 2. ĐƠN VỊ / TỔ CHỨC
-- =========================
CREATE TABLE organizations (
    id INT AUTO_INCREMENT PRIMARY KEY,

    org_code VARCHAR(100) NOT NULL UNIQUE,
    org_name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NULL,

    status ENUM('ACTIVE', 'INACTIVE', 'DELETED') NOT NULL DEFAULT 'ACTIVE',

    system_id INT NOT NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_organizations_system
        FOREIGN KEY (system_id)
        REFERENCES interconnected_systems(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_organizations_email
        CHECK (email LIKE '%@%')
) ENGINE=InnoDB;

-- =========================
-- 3. QUYỀN / ROLE
-- =========================
CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,

    role_code VARCHAR(100) NOT NULL UNIQUE,
    role_name VARCHAR(255) NOT NULL,
    description TEXT NULL
) ENGINE=InnoDB;

-- =========================
-- 4. NGƯỜI DÙNG / NHÂN VIÊN
-- =========================
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,

    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NULL,
    email VARCHAR(255) NOT NULL UNIQUE,

    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED', 'DELETED') NOT NULL DEFAULT 'ACTIVE',

    role_id INT NOT NULL,
    organization_id INT NOT NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_users_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_users_email
        CHECK (email LIKE '%@%')
) ENGINE=InnoDB;

-- =========================
-- 5. VĂN BẢN
-- =========================
CREATE TABLE documents (
    id INT AUTO_INCREMENT PRIMARY KEY,

    document_type VARCHAR(100) NOT NULL,
    document_code VARCHAR(100) NOT NULL UNIQUE,
    summary TEXT NULL,

    status ENUM(
        'DRAFT',
        'PENDING',
        'SIGNED',
        'SENT',
        'RECEIVED',
        'RESPONDED',
        'REJECTED',
        'ARCHIVED',
        'DELETED'
    ) NOT NULL DEFAULT 'DRAFT',

    sender_org_id INT NOT NULL,
    created_by INT NOT NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_documents_sender_org
        FOREIGN KEY (sender_org_id)
        REFERENCES organizations(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_documents_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================
-- 6. BẢNG TRUNG GIAN: VĂN BẢN - ĐƠN VỊ NHẬN
-- =========================
CREATE TABLE document_receivers (
    document_id INT NOT NULL,
    receiver_org_id INT NOT NULL,

    PRIMARY KEY (document_id, receiver_org_id),

    CONSTRAINT fk_document_receivers_document
        FOREIGN KEY (document_id)
        REFERENCES documents(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_document_receivers_organization
        FOREIGN KEY (receiver_org_id)
        REFERENCES organizations(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================
-- 7. FILE TÀI LIỆU
-- =========================
CREATE TABLE document_files (
    id INT AUTO_INCREMENT PRIMARY KEY,

    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL UNIQUE,
    file_path VARCHAR(500) NOT NULL UNIQUE,
    file_type VARCHAR(100) NOT NULL,

    -- lưu đơn vị byte
    file_size BIGINT UNSIGNED NOT NULL,

    document_id INT NOT NULL,

    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_document_files_document
        FOREIGN KEY (document_id)
        REFERENCES documents(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT chk_document_files_file_size
        CHECK (file_size > 0)
) ENGINE=InnoDB;

-- =========================
-- 8. CHỮ KÝ VĂN BẢN
-- =========================
CREATE TABLE document_signatures (
    id INT AUTO_INCREMENT PRIMARY KEY,

    hash_value VARCHAR(255) NOT NULL UNIQUE,
    algorithm VARCHAR(100) NOT NULL,

    status ENUM('PENDING', 'VALID', 'INVALID', 'REVOKED') NOT NULL DEFAULT 'PENDING',

    document_id INT NOT NULL,
    signer_id INT NOT NULL,

    signed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_document_signatures_document
        FOREIGN KEY (document_id)
        REFERENCES documents(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_document_signatures_signer
        FOREIGN KEY (signer_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================
-- 9. GỬI / NHẬN VĂN BẢN
-- Giữ nguyên quan hệ sender_id, receiver_id trỏ tới users
-- =========================
CREATE TABLE document_transfers (
    id INT AUTO_INCREMENT PRIMARY KEY,

    response_content TEXT NULL,

    status ENUM('SENT', 'RECEIVED', 'RESPONDED', 'FAILED') NOT NULL DEFAULT 'SENT',

    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    document_id INT NOT NULL,

    sent_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    received_at DATETIME NULL DEFAULT NULL,
    responded_at DATETIME NULL DEFAULT NULL,

    CONSTRAINT fk_document_transfers_sender
        FOREIGN KEY (sender_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_document_transfers_receiver
        FOREIGN KEY (receiver_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_document_transfers_document
        FOREIGN KEY (document_id)
        REFERENCES documents(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT chk_document_transfers_received_at
        CHECK (received_at IS NULL OR received_at >= sent_at),

    CONSTRAINT chk_document_transfers_responded_at
        CHECK (responded_at IS NULL OR responded_at >= sent_at)
) ENGINE=InnoDB;

-- =========================
-- 10. CÁC BƯỚC XỬ LÝ VĂN BẢN
-- =========================
CREATE TABLE workflow_steps (
    id INT AUTO_INCREMENT PRIMARY KEY,

    step_name VARCHAR(255) NOT NULL,
    step_order INT NOT NULL,

    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'REJECTED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',

    assignee_id INT NOT NULL,
    document_id INT NOT NULL,

    processed_at DATETIME NULL DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_workflow_steps_assignee
        FOREIGN KEY (assignee_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_workflow_steps_document
        FOREIGN KEY (document_id)
        REFERENCES documents(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT uq_workflow_steps_document_order
        UNIQUE (document_id, step_order),

    CONSTRAINT chk_workflow_steps_step_order
        CHECK (step_order > 0)
) ENGINE=InnoDB;

-- =========================
-- 11. LOG HOẠT ĐỘNG
-- =========================
CREATE TABLE audit_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,

    action VARCHAR(100) NOT NULL,
    object_type VARCHAR(100) NOT NULL,

    object_value TEXT NULL,
    old_value TEXT NULL,
    new_value TEXT NULL,

    ip_address VARCHAR(100) NULL,

    actor_id INT NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_logs_actor
        FOREIGN KEY (actor_id)
        REFERENCES users(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================
-- 12. THÔNG BÁO
-- =========================
CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,

    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,

    is_read BOOLEAN NOT NULL DEFAULT FALSE,

    user_id INT NOT NULL,
    related_document_id INT NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_notifications_related_document
        FOREIGN KEY (related_document_id)
        REFERENCES documents(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB;