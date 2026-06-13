package com.vdt.documenttransfer.modules.interconnectedsystem.entity;

import com.vdt.documenttransfer.modules.organization.entity.Organization;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "interconnected_systems")
public class InterconnectedSystem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "system_code", nullable = false, unique = true, length = 100)
	private String systemCode;

	@Column(name = "system_name", nullable = false)
	private String systemName;

	@Column(name = "endpoint_url", nullable = false, unique = true, length = 500)
	private String endpointUrl;

	@Column(name = "api_key", nullable = false, unique = true)
	private String apiKey;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Status status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Builder.Default
	@OneToMany(mappedBy = "system")
	private Set<Organization> organizations = new HashSet<>();

	public enum Status {
		ACTIVE,
		INACTIVE,
		DELETED
	}
}
