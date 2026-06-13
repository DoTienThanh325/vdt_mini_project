package com.vdt.documenttransfer.modules.auditlog.entity;

import com.vdt.documenttransfer.modules.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "audit_logs")
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, length = 100)
	private String action;

	@Column(name = "object_type", nullable = false, length = 100)
	private String objectType;

	@Column(name = "object_value", columnDefinition = "TEXT")
	private String objectValue;

	@Column(name = "old_value", columnDefinition = "TEXT")
	private String oldValue;

	@Column(name = "new_value", columnDefinition = "TEXT")
	private String newValue;

	@Column(name = "ip_address", length = 100)
	private String ipAddress;

	@ManyToOne
	@JoinColumn(name = "actor_id")
	private User actor;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
