package com.vdt.documenttransfer.modules.document.entity;

import com.vdt.documenttransfer.modules.documentfile.entity.DocumentFile;
import com.vdt.documenttransfer.modules.organization.entity.Organization;
import com.vdt.documenttransfer.modules.signature.entity.DocumentSignature;
import com.vdt.documenttransfer.modules.transfer.entity.DocumentTransfer;
import com.vdt.documenttransfer.modules.user.entity.User;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
@Table(name = "documents")
public class Document {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "document_type", nullable = false, length = 100)
	private String documentType;

	@Column(name = "document_code", nullable = false, unique = true, length = 100)
	private String documentCode;

	@Column(columnDefinition = "TEXT")
	private String summary;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Status status;

	@ManyToOne(optional = false)
	@JoinColumn(name = "sender_org_id", nullable = false)
	private Organization senderOrganization;

	@ManyToOne(optional = false)
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Builder.Default
	@OneToMany(mappedBy = "document")
	private List<DocumentFile> files = new ArrayList<>();

	@OneToOne(mappedBy = "document")
	private DocumentSignature signature;

	@Builder.Default
	@OneToMany(mappedBy = "document")
	private List<DocumentTransfer> transfers = new ArrayList<>();

	public enum Status {
		CREATED,
		APPROVED,
		SIGNED,
		REJECTED
	}
}
