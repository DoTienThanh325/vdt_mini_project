package com.vdt.documenttransfer.modules.organization.entity;

import com.vdt.documenttransfer.modules.document.entity.Document;
import com.vdt.documenttransfer.modules.interconnectedsystem.entity.InterconnectedSystem;
import com.vdt.documenttransfer.modules.transfer.entity.DocumentTransfer;
import com.vdt.documenttransfer.modules.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.*;
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
@Table(name = "organizations")
public class Organization {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "org_code", nullable = false, unique = true, length = 100)
	private String orgCode;

	@Column(name = "org_name", nullable = false)
	private String orgName;

	@Column(length = 500)
	private String address;

	@Column(nullable = false)
	private String email;

	@Column(length = 20)
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Status status;

	@ManyToOne(optional = false)
	@JoinColumn(name = "system_id", nullable = false)
	private InterconnectedSystem system;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Builder.Default
	@OneToMany(mappedBy = "organization")
	private List<User> users = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "senderOrganization")
	private List<Document> sentDocuments = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "receiverOrganization")
	private List<DocumentTransfer> documentTransfers = new ArrayList<>();

	public enum Status {
		ACTIVE,
		INACTIVE,
		DELETED
	}
}
