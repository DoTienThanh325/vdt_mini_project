package com.vdt.documenttransfer.modules.transfer.entity;

import com.vdt.documenttransfer.modules.document.entity.Document;
import com.vdt.documenttransfer.modules.organization.entity.Organization;
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
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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
@Table(name = "document_transfers")
public class DocumentTransfer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "response_content", columnDefinition = "TEXT")
	private String responseContent;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Status status;

	@ManyToOne(optional = false)
	@JoinColumn(name = "sender_id", nullable = false)
	private User sender;

	@ManyToOne
	@JoinColumn(name = "receiver_id")
	private User receiver;

	@ManyToOne
	@JoinColumn(name = "receiver_org_id")
	private Organization receiverOrganization;

	@ManyToOne(optional = false)
	@JoinColumn(name = "document_id", nullable = false)
	private Document document;

	@Column(name = "sent_at", nullable = false, updatable = false)
	private LocalDateTime sentAt;

	@Column(name = "received_at")
	private LocalDateTime receivedAt;

	@Column(name = "responded_at")
	private LocalDateTime respondedAt;

	public enum Status {
		SENT,
		RECEIVED,
		RESPONDED,
		FAILED,
		PENDING
	}
}
