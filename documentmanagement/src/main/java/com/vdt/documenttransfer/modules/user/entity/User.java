package com.vdt.documenttransfer.modules.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vdt.documenttransfer.modules.auditlog.entity.AuditLog;
import com.vdt.documenttransfer.modules.document.entity.Document;
import com.vdt.documenttransfer.modules.notification.entity.Notification;
import com.vdt.documenttransfer.modules.organization.entity.Organization;
import com.vdt.documenttransfer.modules.role.entity.Role;
import com.vdt.documenttransfer.modules.signature.entity.DocumentSignature;
import com.vdt.documenttransfer.modules.transfer.entity.DocumentTransfer;
import com.vdt.documenttransfer.modules.workflowstep.entity.WorkflowStep;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, unique = true, length = 100)
	private String username;

	@Column(nullable = false)
	private String password;

	@Column(name = "full_name", nullable = false)
	private String fullName;

	@Column(length = 20)
	private String phone;

	@Column(nullable = false, unique = true)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Status status;

	@JsonIgnore
	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@JsonIgnore
	@Builder.Default
	@OneToMany(mappedBy = "createdBy")
	private Set<Document> createdDocuments = new HashSet<>();

	@JsonIgnore
	@Builder.Default
	@OneToMany(mappedBy = "signer")
	private Set<DocumentSignature> signatures = new HashSet<>();

	@JsonIgnore
	@Builder.Default
	@OneToMany(mappedBy = "sender")
	private Set<DocumentTransfer> sentTransfers = new HashSet<>();

	@JsonIgnore
	@Builder.Default
	@OneToMany(mappedBy = "receiver")
	private Set<DocumentTransfer> receivedTransfers = new HashSet<>();

	@JsonIgnore
	@Builder.Default
	@OneToMany(mappedBy = "assignee")
	private Set<WorkflowStep> workflowSteps = new HashSet<>();

	@JsonIgnore
	@Builder.Default
	@OneToMany(mappedBy = "actor")
	private Set<AuditLog> auditLogs = new HashSet<>();

	@JsonIgnore
	@Builder.Default
	@OneToMany(mappedBy = "user")
	private Set<Notification> notifications = new HashSet<>();

	public enum Status {
		ACTIVE,
		INACTIVE,
		LOCKED,
		DELETED
	}
}
