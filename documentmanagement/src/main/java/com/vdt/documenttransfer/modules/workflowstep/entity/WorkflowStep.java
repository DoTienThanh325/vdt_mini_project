package com.vdt.documenttransfer.modules.workflowstep.entity;

import com.vdt.documenttransfer.modules.document.entity.Document;
import com.vdt.documenttransfer.modules.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
		name = "workflow_steps",
		uniqueConstraints = @UniqueConstraint(
				name = "uq_workflow_steps_document_order",
				columnNames = {"document_id", "step_order"}
		)
)
public class WorkflowStep {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "step_name", nullable = false)
	private String stepName;

	@Column(name = "step_order", nullable = false)
	private Integer stepOrder;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Status status;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "assignee_id", nullable = false)
	private User assignee;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "document_id", nullable = false)
	private Document document;

	@Column(name = "processed_at")
	private LocalDateTime processedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public enum Status {
		PENDING,
		PROCESSING,
		COMPLETED,
		REJECTED,
		CANCELLED
	}
}
