USE document_management_db;
-- BCrypt của mật khẩu: 123456
SET @password = '$2a$10$vB26gEDPoVFGxvbsTTnmk.m/LwfYcbFqlFaa5nq0FD8UM3dT.7QcK';
-- =====================================================
-- 1. INTERCONNECTED SYSTEMS
-- =====================================================
INSERT INTO interconnected_systems (
        system_code,
        system_name,
        endpoint_url,
        api_key,
        status,
        created_at,
        updated_at
    )
VALUES (
        'VT-HN',
        'Hệ thống Văn thư Hà Nội',
        'http://localhost:8081/api/interconnect/hn/receive',
        'vt-hn-api-key-2026',
        'ACTIVE',
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'VT-HP',
        'Hệ thống Văn thư Hải Phòng',
        'http://localhost:8081/api/interconnect/hp/receive',
        'vt-hp-api-key-2026',
        'ACTIVE',
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'VT-DN',
        'Hệ thống Văn thư Đà Nẵng',
        'http://localhost:8081/api/interconnect/dn/receive',
        'vt-dn-api-key-2026',
        'ACTIVE',
        CURRENT_TIMESTAMP,
        NULL
    ) ON DUPLICATE KEY
UPDATE system_name =
VALUES(system_name),
    endpoint_url =
VALUES(endpoint_url),
    api_key =
VALUES(api_key),
    status =
VALUES(status),
    updated_at = CURRENT_TIMESTAMP;
-- Lấy ID system
SET @system_hn = (
        SELECT id
        FROM interconnected_systems
        WHERE system_code = 'VT-HN'
    );
SET @system_hp = (
        SELECT id
        FROM interconnected_systems
        WHERE system_code = 'VT-HP'
    );
SET @system_dn = (
        SELECT id
        FROM interconnected_systems
        WHERE system_code = 'VT-DN'
    );
-- =====================================================
-- 2. ORGANIZATIONS
-- =====================================================
INSERT INTO organizations (
        org_code,
        org_name,
        address,
        email,
        phone,
        status,
        system_id,
        created_at,
        updated_at
    )
VALUES (
        'ORG_HN',
        'Viettel Hà Nội',
        'Hà Nội',
        'vanthu.hn@example.com',
        '0240000001',
        'ACTIVE',
        @system_hn,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'ORG_HP',
        'Viettel Hải Phòng',
        'Hải Phòng',
        'vanthu.hp@example.com',
        '0225000001',
        'ACTIVE',
        @system_hp,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'ORG_DN',
        'Viettel Đà Nẵng',
        'Đà Nẵng',
        'vanthu.dn@example.com',
        '0236000001',
        'ACTIVE',
        @system_dn,
        CURRENT_TIMESTAMP,
        NULL
    ) ON DUPLICATE KEY
UPDATE org_name =
VALUES(org_name),
    address =
VALUES(address),
    email =
VALUES(email),
    phone =
VALUES(phone),
    status =
VALUES(status),
    system_id =
VALUES(system_id),
    updated_at = CURRENT_TIMESTAMP;
-- Lấy ID organization
SET @org_hn = (
        SELECT id
        FROM organizations
        WHERE org_code = 'ORG_HN'
    );
SET @org_hp = (
        SELECT id
        FROM organizations
        WHERE org_code = 'ORG_HP'
    );
SET @org_dn = (
        SELECT id
        FROM organizations
        WHERE org_code = 'ORG_DN'
    );
-- =====================================================
-- 3. ROLES
-- Danh sách yêu cầu thực tế có 6 role
-- =====================================================
INSERT INTO roles (role_code, role_name, description)
VALUES (
        'ADMIN',
        'Quản trị hệ thống',
        'Quản trị toàn bộ hệ thống'
    ),
    (
        'ORGADMIN',
        'Quản trị đơn vị',
        'Quản trị người dùng và thông tin của đơn vị'
    ),
    (
        'LEADER',
        'Lãnh đạo',
        'Ký và quản lý văn bản'
    ),
    (
        'MANAGER',
        'Quản lý',
        'Phê duyệt hoặc từ chối văn bản'
    ),
    (
        'STAFF',
        'Chuyên viên',
        'Tạo và xử lý văn bản'
    ),
    (
        'CLERK',
        'Văn thư',
        'Gửi và tiếp nhận văn bản'
    ) ON DUPLICATE KEY
UPDATE role_name =
VALUES(role_name),
    description =
VALUES(description);
-- Lấy ID role
SET @role_admin = (
        SELECT id
        FROM roles
        WHERE role_code = 'ADMIN'
    );
SET @role_orgadmin = (
        SELECT id
        FROM roles
        WHERE role_code = 'ORGADMIN'
    );
SET @role_leader = (
        SELECT id
        FROM roles
        WHERE role_code = 'LEADER'
    );
SET @role_manager = (
        SELECT id
        FROM roles
        WHERE role_code = 'MANAGER'
    );
SET @role_staff = (
        SELECT id
        FROM roles
        WHERE role_code = 'STAFF'
    );
SET @role_clerk = (
        SELECT id
        FROM roles
        WHERE role_code = 'CLERK'
    );
-- =====================================================
-- 4. ADMIN
-- Admin không thuộc organization cụ thể
-- =====================================================
INSERT INTO users (
        username,
        password,
        full_name,
        phone,
        email,
        status,
        role_id,
        organization_id,
        created_at,
        updated_at
    )
VALUES (
        'admin',
        @password,
        'Quản trị hệ thống',
        '0900000000',
        'admin@vdt.vn',
        'ACTIVE',
        @role_admin,
        NULL,
        CURRENT_TIMESTAMP,
        NULL
    ) ON DUPLICATE KEY
UPDATE password =
VALUES(password),
    full_name =
VALUES(full_name),
    phone =
VALUES(phone),
    status =
VALUES(status),
    role_id =
VALUES(role_id),
    organization_id =
VALUES(organization_id),
    updated_at = CURRENT_TIMESTAMP;
-- =====================================================
-- 5. USERS VĂN THƯ HÀ NỘI
-- =====================================================
INSERT INTO users (
        username,
        password,
        full_name,
        phone,
        email,
        status,
        role_id,
        organization_id,
        created_at,
        updated_at
    )
VALUES (
        'orgadmin_hn',
        @password,
        'Quản trị đơn vị Hà Nội',
        '0901000001',
        'orgadmin.hn@vdt.vn',
        'ACTIVE',
        @role_orgadmin,
        @org_hn,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'staff_hn',
        @password,
        'Chuyên viên Hà Nội',
        '0901000002',
        'staff.hn@vdt.vn',
        'ACTIVE',
        @role_staff,
        @org_hn,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'manager_hn',
        @password,
        'Quản lý Hà Nội',
        '0901000003',
        'manager.hn@vdt.vn',
        'ACTIVE',
        @role_manager,
        @org_hn,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'leader_hn',
        @password,
        'Lãnh đạo Hà Nội',
        '0901000004',
        'leader.hn@vdt.vn',
        'ACTIVE',
        @role_leader,
        @org_hn,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'clerk_hn',
        @password,
        'Văn thư Hà Nội',
        '0901000005',
        'clerk.hn@vdt.vn',
        'ACTIVE',
        @role_clerk,
        @org_hn,
        CURRENT_TIMESTAMP,
        NULL
    ) ON DUPLICATE KEY
UPDATE password =
VALUES(password),
    full_name =
VALUES(full_name),
    phone =
VALUES(phone),
    status =
VALUES(status),
    role_id =
VALUES(role_id),
    organization_id =
VALUES(organization_id),
    updated_at = CURRENT_TIMESTAMP;
-- =====================================================
-- 6. USERS VĂN THƯ HẢI PHÒNG
-- =====================================================
INSERT INTO users (
        username,
        password,
        full_name,
        phone,
        email,
        status,
        role_id,
        organization_id,
        created_at,
        updated_at
    )
VALUES (
        'orgadmin_hp',
        @password,
        'Quản trị đơn vị Hải Phòng',
        '0902000001',
        'orgadmin.hp@vdt.vn',
        'ACTIVE',
        @role_orgadmin,
        @org_hp,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'staff_hp',
        @password,
        'Chuyên viên Hải Phòng',
        '0902000002',
        'staff.hp@vdt.vn',
        'ACTIVE',
        @role_staff,
        @org_hp,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'manager_hp',
        @password,
        'Quản lý Hải Phòng',
        '0902000003',
        'manager.hp@vdt.vn',
        'ACTIVE',
        @role_manager,
        @org_hp,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'leader_hp',
        @password,
        'Lãnh đạo Hải Phòng',
        '0902000004',
        'leader.hp@vdt.vn',
        'ACTIVE',
        @role_leader,
        @org_hp,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'clerk_hp',
        @password,
        'Văn thư Hải Phòng',
        '0902000005',
        'clerk.hp@vdt.vn',
        'ACTIVE',
        @role_clerk,
        @org_hp,
        CURRENT_TIMESTAMP,
        NULL
    ) ON DUPLICATE KEY
UPDATE password =
VALUES(password),
    full_name =
VALUES(full_name),
    phone =
VALUES(phone),
    status =
VALUES(status),
    role_id =
VALUES(role_id),
    organization_id =
VALUES(organization_id),
    updated_at = CURRENT_TIMESTAMP;
-- =====================================================
-- 7. USERS VĂN THƯ ĐÀ NẴNG
-- =====================================================
INSERT INTO users (
        username,
        password,
        full_name,
        phone,
        email,
        status,
        role_id,
        organization_id,
        created_at,
        updated_at
    )
VALUES (
        'orgadmin_dn',
        @password,
        'Quản trị đơn vị Đà Nẵng',
        '0903000001',
        'orgadmin.dn@vdt.vn',
        'ACTIVE',
        @role_orgadmin,
        @org_dn,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'staff_dn',
        @password,
        'Chuyên viên Đà Nẵng',
        '0903000002',
        'staff.dn@vdt.vn',
        'ACTIVE',
        @role_staff,
        @org_dn,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'manager_dn',
        @password,
        'Quản lý Đà Nẵng',
        '0903000003',
        'manager.dn@vdt.vn',
        'ACTIVE',
        @role_manager,
        @org_dn,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'leader_dn',
        @password,
        'Lãnh đạo Đà Nẵng',
        '0903000004',
        'leader.dn@vdt.vn',
        'ACTIVE',
        @role_leader,
        @org_dn,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'clerk_dn',
        @password,
        'Văn thư Đà Nẵng',
        '0903000005',
        'clerk.dn@vdt.vn',
        'ACTIVE',
        @role_clerk,
        @org_dn,
        CURRENT_TIMESTAMP,
        NULL
    ) ON DUPLICATE KEY
UPDATE password =
VALUES(password),
    full_name =
VALUES(full_name),
    phone =
VALUES(phone),
    status =
VALUES(status),
    role_id =
VALUES(role_id),
    organization_id =
VALUES(organization_id),
    updated_at = CURRENT_TIMESTAMP;